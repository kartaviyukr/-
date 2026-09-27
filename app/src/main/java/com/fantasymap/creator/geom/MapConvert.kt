package com.fantasymap.creator.geom

import com.fantasymap.creator.model.BiomeGroup
import com.fantasymap.creator.model.BiomeRegion
import com.fantasymap.creator.model.BiomeType
import com.fantasymap.creator.model.District
import com.fantasymap.creator.model.DistrictType
import com.fantasymap.creator.model.Geometry
import com.fantasymap.creator.model.LineFeature
import com.fantasymap.creator.model.LineFeatureType
import com.fantasymap.creator.model.MapKind
import com.fantasymap.creator.model.MapProject
import com.fantasymap.creator.model.MapStyle
import com.fantasymap.creator.model.Marker
import com.fantasymap.creator.model.MarkerType
import com.fantasymap.creator.model.Road
import com.fantasymap.creator.model.RoadType
import com.fantasymap.creator.model.Vec
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Переделка карты в карту другого вида: кусок мира — в карту города или
 * боевую локацию, кусок города — в обычную карту и так далее.
 * Что уместно на новой карте, переносится как есть; остальное превращается
 * в ближайшее по смыслу (город на карте мира → квартал, дом → пол и стены,
 * улица → мостовая) или отбрасывается.
 */
object MapConvert {

    fun convert(source: MapProject, to: MapKind): MapProject {
        if (source.kind == to) return source
        val w = source.worldWidth
        val h = source.worldHeight
        val base = min(w, h)

        // ------------------------------------------------------------ объекты
        val markers = ArrayList<Marker>()
        val districts = ArrayList<District>()
        for (marker in source.markers) {
            when {
                marker.type.scope.fits(to) -> markers.add(marker)
                // Город или деревня с карты мира на карте города — квартал вокруг точки.
                to == MapKind.CITY && marker.type.isSettlement -> {
                    val radius = base * if (marker.type == MarkerType.CAPITAL) 0.16f else 0.1f
                    districts.add(
                        District(
                            type = if (marker.type == MarkerType.CAPITAL) DistrictType.PALACE_QUARTER else DistrictType.OLD_TOWN,
                            name = marker.name,
                            points = circle(marker.pos, radius, 20)
                        )
                    )
                }
            }
        }

        // Город с карты города на обычной карте — один значок города.
        if (to == MapKind.WORLD && (source.districts.isNotEmpty() || source.buildings.isNotEmpty())) {
            val all = source.districts.flatMap { it.points } + source.buildings.flatMap { it.points }
            val box = Geometry.bounds(all)
            markers.add(
                Marker(
                    type = if (source.buildings.size > 200) MarkerType.CITY else MarkerType.TOWN,
                    name = source.name.substringBefore(" — "),
                    pos = Vec((box.minX + box.maxX) / 2f, (box.minY + box.maxY) / 2f)
                )
            )
        }

        // ------------------------------------------------------------ зоны
        val biomes = ArrayList<BiomeRegion>()
        for (region in source.biomes) {
            val kind = convertBiome(region.biome, to) ?: continue
            biomes.add(region.copy(biome = kind, assetId = if (kind == region.biome) region.assetId else null))
        }

        // ------------------------------------------------------------ линии и дороги
        val lines = source.lines.filter { it.type.fits(to) }.toMutableList()
        val roads = ArrayList<Road>()
        for (road in source.roads) {
            if (to == MapKind.BATTLE) {
                // Улица на боевой карте — мостовая или тропа под ногами.
                val floor = if (road.type.dashed) BiomeType.DIRT_GROUND else BiomeType.COBBLE_FLOOR
                val width = max(base * 0.02f, road.type.width * (if (source.kind == MapKind.CITY) 1f else base * 0.004f))
                strip(road.points, width)?.let { biomes.add(BiomeRegion(biome = floor, points = it)) }
            } else {
                roads.add(road.copy(type = convertRoad(road.type, to)))
            }
        }

        // ------------------------------------------------------------ постройки
        if (to == MapKind.BATTLE) {
            for (building in source.buildings) {
                if (building.points.size < 3) continue
                biomes.add(BiomeRegion(biome = BiomeType.WOOD_FLOOR, points = building.points))
                lines.add(LineFeature(type = LineFeatureType.BRICK_WALL, points = building.points + building.points.first()))
            }
        }
        if (to == MapKind.CITY) districts.addAll(0, source.districts)

        val battle = to == MapKind.BATTLE
        val style = when {
            battle -> source.style.copy(
                oceanColor = 0xFF24211E.toInt(),
                deskColor = 0xFF161412.toInt(),
                showCompass = false,
                showFrame = false,
                photoTextures = true
            )
            source.kind == MapKind.BATTLE -> MapStyle(seed = source.style.seed)
            else -> source.style
        }

        return source.copy(
            kind = to,
            stage = 1,
            markers = markers,
            districts = if (to == MapKind.CITY) districts else emptyList(),
            buildings = if (to == MapKind.CITY) source.buildings else emptyList(),
            biomes = biomes,
            lines = lines,
            roads = roads,
            countries = if (to == MapKind.WORLD) source.countries else emptyList(),
            tokens = if (battle) source.tokens else emptyList(),
            fog = if (battle) source.fog else emptyList(),
            groundBiome = if (battle) (source.groundBiome ?: BiomeType.GRASS_GROUND) else null,
            gridCell = if (battle && source.kind != MapKind.BATTLE) max(w, h) / 30f else source.gridCell,
            landBase = if (battle) false else source.landBase,
            style = style
        )
    }

    /** Зона на карте другого вида или null, если ей там не место. */
    fun convertBiome(biome: BiomeType, to: MapKind): BiomeType? {
        val battle = biome.group.battle
        return when (to) {
            MapKind.BATTLE -> if (battle) biome else when (biome.group) {
                BiomeGroup.FOREST -> BiomeType.FOREST_GROUND
                BiomeGroup.GRASS -> BiomeType.GRASS_GROUND
                BiomeGroup.WATER -> BiomeType.SHALLOW_WATER
                BiomeGroup.DRY -> BiomeType.SAND_GROUND
                BiomeGroup.COLD -> BiomeType.SNOW_GROUND
                BiomeGroup.HIGH -> BiomeType.ROCK_GROUND
                BiomeGroup.WET -> BiomeType.MUD_GROUND
                BiomeGroup.SHORE -> BiomeType.SAND_GROUND
                BiomeGroup.CITY -> BiomeType.COBBLE_FLOOR
                BiomeGroup.FANTASY -> BiomeType.MOSS_GROUND
                else -> null
            }
            MapKind.CITY -> if (battle) null else biome
            MapKind.WORLD -> if (battle || biome.group == BiomeGroup.CITY) null else biome
        }
    }

    fun convertRoad(type: RoadType, to: MapKind): RoadType = when (to) {
        MapKind.CITY -> when (type) {
            RoadType.HIGHWAY, RoadType.ROYAL_ROAD -> RoadType.MAIN_STREET
            RoadType.ROAD, RoadType.CARAVAN_ROUTE -> RoadType.STREET
            RoadType.TRAIL, RoadType.PILGRIM_ROAD -> RoadType.LANE
            RoadType.SECRET_PATH -> RoadType.ALLEY
            else -> type
        }
        MapKind.WORLD -> when (type) {
            RoadType.MAIN_STREET -> RoadType.HIGHWAY
            RoadType.STREET, RoadType.WATERFRONT -> RoadType.ROAD
            RoadType.LANE, RoadType.ALLEY, RoadType.STAIRS_WAY -> RoadType.TRAIL
            RoadType.CITY_CANAL_WAY -> RoadType.RIVER_ROUTE
            else -> type
        }
        MapKind.BATTLE -> type
    }

    private fun circle(center: Vec, r: Float, sides: Int) = List(sides) { i ->
        val a = i * 2f * PI.toFloat() / sides
        Vec(center.x + cos(a) * r, center.y + sin(a) * r)
    }

    /** Полоса шириной width вдоль ломаной — многоугольник. */
    fun strip(points: List<Vec>, width: Float): List<Vec>? {
        if (points.size < 2) return null
        val half = width / 2f
        val left = ArrayList<Vec>()
        val right = ArrayList<Vec>()
        for (i in points.indices) {
            val a = points[max(0, i - 1)]
            val b = points[min(points.size - 1, i + 1)]
            val dx = b.x - a.x
            val dy = b.y - a.y
            val len = sqrt(dx * dx + dy * dy)
            if (len < 0.0001f) continue
            val nx = -dy / len * half
            val ny = dx / len * half
            left.add(Vec(points[i].x + nx, points[i].y + ny))
            right.add(Vec(points[i].x - nx, points[i].y - ny))
        }
        if (left.size < 2) return null
        return left + right.reversed()
    }
}
