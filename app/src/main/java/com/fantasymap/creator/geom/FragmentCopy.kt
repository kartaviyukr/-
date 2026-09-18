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
import kotlin.math.max

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
                    extraContours = pieces.drop(1)
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
