package com.fantasymap.creator.geom

import android.graphics.Path
import com.fantasymap.creator.model.BBox
import com.fantasymap.creator.model.BiomeRegion
import com.fantasymap.creator.model.Country
import com.fantasymap.creator.model.Geometry
import com.fantasymap.creator.model.Landmass
import com.fantasymap.creator.model.LineFeature
import com.fantasymap.creator.model.MapLabel
import com.fantasymap.creator.model.MapProject
import com.fantasymap.creator.model.Marker
import com.fantasymap.creator.model.Road
import com.fantasymap.creator.model.Vec
import com.fantasymap.creator.model.WaterBody
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

/**
 * Копирование куска карты в отдельную карту.
 *
 * Исходная карта не меняется: выделенный прямоугольник переносится в новый
 * проект и растягивается на весь его размер, чтобы материк или область можно
 * было заполнять подробнее.
 */
object FragmentCopy {

    /** Наименьшая площадь обрезка, который стоит переносить (в единицах исходной карты). */
    private const val MIN_PIECE_AREA = 6f

    /**
     * Вставить одну карту в прямоугольник другой.
     * Содержимое вписывается в выделение целиком, с сохранением пропорций;
     * страны переносятся с новыми идентификаторами, чтобы не спорить со своими.
     */
    fun insertInto(target: MapProject, source: MapProject, rect: BBox): MapProject {
        val scale = min(
            rect.width / max(source.worldWidth, 1f),
            rect.height / max(source.worldHeight, 1f)
        )
        val offsetX = rect.minX + (rect.width - source.worldWidth * scale) / 2f
        val offsetY = rect.minY + (rect.height - source.worldHeight * scale) / 2f

        fun place(point: Vec) = Vec(offsetX + point.x * scale, offsetY + point.y * scale)
        fun place(points: List<Vec>) = points.map { place(it) }
        fun fresh() = UUID.randomUUID().toString()

        val countryIds = source.countries.associate { it.id to fresh() }

        val countries = source.countries.map { country ->
            country.copy(
                id = countryIds[country.id] ?: fresh(),
                areas = country.areas.map { place(it) }
            )
        }
        val markers = source.markers.map { marker ->
            marker.copy(
                id = fresh(),
                pos = place(marker.pos),
                countryId = marker.countryId?.let { countryIds[it] }
            )
        }

        return target.copy(
            landmasses = target.landmasses +
                source.landmasses.map { it.copy(id = fresh(), points = place(it.points)) },
            waters = target.waters +
                source.waters.map { it.copy(id = fresh(), points = place(it.points)) },
            biomes = target.biomes + source.biomes.map {
                it.copy(
                    id = fresh(),
                    points = place(it.points),
                    extraContours = it.extraContours.map { contour -> place(contour) }
                )
            },
            lines = target.lines +
                source.lines.map { it.copy(id = fresh(), points = place(it.points)) },
            roads = target.roads +
                source.roads.map { it.copy(id = fresh(), points = place(it.points)) },
            markers = target.markers + markers,
            labels = target.labels + source.labels.map {
                it.copy(id = fresh(), pos = place(it.pos), path = place(it.path))
            },
            countries = target.countries + countries
        )
    }

    fun create(
        source: MapProject,
        rect: BBox,
        name: String,
        targetLongSide: Float
    ): MapProject {
        val width = max(rect.width, 1f)
        val height = max(rect.height, 1f)
        val scale = targetLongSide / max(width, height)

        fun move(point: Vec) = Vec((point.x - rect.minX) * scale, (point.y - rect.minY) * scale)
        fun move(points: List<Vec>) = points.map { move(it) }

        val clip = Path().apply {
            addRect(rect.minX, rect.minY, rect.maxX, rect.maxY, Path.Direction.CW)
        }

        /** Обрезать область прямоугольником и перенести в новые координаты. */
        fun cutArea(contours: List<List<Vec>>): List<List<Vec>> {
            val usable = contours.filter { it.size >= 3 }
            if (usable.isEmpty()) return emptyList()
            if (!Geometry.bounds(usable.flatten()).intersects(rect)) return emptyList()
            val path = PolygonOps.buildPath(usable)
            if (!path.op(clip, Path.Op.INTERSECT)) return emptyList()
            if (path.isEmpty) return emptyList()
            return PolygonOps.contoursOf(path, MIN_PIECE_AREA).map { move(it) }
        }

        /** Разрезать линию на куски, попавшие внутрь прямоугольника. */
        fun cutLine(points: List<Vec>): List<List<Vec>> {
            if (points.size < 2) return emptyList()
            val pieces = ArrayList<List<Vec>>()
            var current = ArrayList<Vec>()
            for (index in points.indices) {
                val point = points[index]
                if (rect.contains(point)) {
                    if (current.isEmpty() && index > 0) current.add(points[index - 1])
                    current.add(point)
                } else if (current.isNotEmpty()) {
                    current.add(point)
                    pieces.add(current)
                    current = ArrayList()
                }
            }
            if (current.isNotEmpty()) pieces.add(current)
            return pieces.filter { it.size >= 2 }.map { move(it) }
        }

        val landmasses = ArrayList<Landmass>()
        for (land in source.landmasses) {
            for (piece in cutArea(listOf(land.points))) {
                landmasses.add(Landmass(name = land.name, kind = land.kind, points = piece))
            }
        }

        val waters = ArrayList<WaterBody>()
        for (water in source.waters) {
            for (piece in cutArea(listOf(water.points))) {
                waters.add(WaterBody(name = water.name, kind = water.kind, points = piece))
            }
        }

        val biomes = ArrayList<BiomeRegion>()
        for (region in source.biomes) {
            val pieces = cutArea(region.contours())
            if (pieces.isEmpty()) continue
            biomes.add(
                BiomeRegion(
                    biome = region.biome,
                    name = region.name,
                    points = pieces.first(),
                    extraContours = pieces.drop(1),
                    assetId = region.assetId
                )
            )
        }

        val lines = ArrayList<LineFeature>()
        for (feature in source.lines) {
            for (piece in cutLine(feature.points)) {
                lines.add(
                    LineFeature(
                        type = feature.type,
                        name = feature.name,
                        points = piece,
                        width = feature.width
                    )
                )
            }
        }

        val roads = ArrayList<Road>()
        for (road in source.roads) {
            for (piece in cutLine(road.points)) {
                roads.add(Road(type = road.type, name = road.name, points = piece))
            }
        }

        val markers = source.markers
            .filter { rect.contains(it.pos) }
            .map { marker: Marker -> marker.copy(pos = move(marker.pos)) }

        val labels = source.labels
            .filter { rect.contains(it.pos) }
            .map { label: MapLabel -> label.copy(pos = move(label.pos)) }

        val countries = ArrayList<Country>()
        for (country in source.countries) {
            val areas = ArrayList<List<Vec>>()
            for (area in country.areas) {
                areas.addAll(cutArea(listOf(area)))
            }
            val hasMarkers = markers.any { it.countryId == country.id }
            if (areas.isEmpty() && !hasMarkers) continue
            countries.add(country.copy(areas = areas))
        }

        return MapProject(
            name = name.ifBlank { source.name + " — фрагмент" },
            worldWidth = width * scale,
            worldHeight = height * scale,
            stage = source.stage,
            landmasses = landmasses,
            waters = waters,
            biomes = biomes,
            lines = lines,
            roads = roads,
            markers = markers,
            countries = countries,
            labels = labels,
            style = source.style
        )
    }
}
