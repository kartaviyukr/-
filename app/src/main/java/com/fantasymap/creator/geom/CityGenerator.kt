package com.fantasymap.creator.geom

import com.fantasymap.creator.model.BBox
import com.fantasymap.creator.model.Building
import com.fantasymap.creator.model.BuildingType
import com.fantasymap.creator.model.District
import com.fantasymap.creator.model.DistrictType
import com.fantasymap.creator.model.Geometry
import com.fantasymap.creator.model.MapProject
import com.fantasymap.creator.model.Vec
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Застройка квартала домами: вручную рисовать сотни домов никто не станет. */
object CityGenerator {

    /**
     * Заполнить квартал рядами домов.
     * Дома ставятся вдоль ближайшей улицы, не залезают на улицы,
     * не накладываются друг на друга и не выходят за границу квартала.
     */
    fun fillDistrict(
        project: MapProject,
        district: District,
        density: Float,
        seed: Int
    ): List<Building> {
        val outline = district.points
        if (outline.size < 3) return emptyList()
        val bounds = Geometry.bounds(outline)
        val base = min(project.worldWidth, project.worldHeight)

        val houseWidth = base * 0.016f * (1.25f - density * 0.4f)
        val houseDepth = base * 0.012f * (1.25f - density * 0.4f)
        val gap = base * 0.006f * (1.3f - density * 0.5f)
        val stepX = houseWidth + gap
        val stepY = houseDepth + gap * 1.6f
        if (stepX <= 0.01f || stepY <= 0.01f) return emptyList()

        val angle = streetAngle(project, Geometry.centroid(outline), bounds)
        val cosA = cos(angle)
        val sinA = sin(angle)
        val radius = (bounds.width + bounds.height) * 0.6f

        val kinds = housesFor(district.type)
        val placed = ArrayList<Building>()
        val takenBounds = ArrayList<BBox>()
        for (existing in project.buildings) {
            if (existing.points.size >= 3) {
                val box = Geometry.bounds(existing.points)
                if (box.intersects(bounds.expand(stepX))) takenBounds.add(box)
            }
        }

        val center = Geometry.centroid(outline)
        var row = -(radius / stepY).toInt()
        val lastRow = (radius / stepY).toInt()
        var guard = 0
        while (row <= lastRow && guard < 20000) {
            var column = -(radius / stepX).toInt()
            val lastColumn = (radius / stepX).toInt()
            while (column <= lastColumn && guard < 20000) {
                guard++
                val localX = column * stepX
                val localY = row * stepY
                val jitterX = (Geometry.hashNoise(column * 7, row * 13, seed) - 0.5f) * gap * 0.8f
                val jitterY = (Geometry.hashNoise(column * 17, row * 3, seed) - 0.5f) * gap * 0.8f
                val spot = Vec(
                    center.x + (localX + jitterX) * cosA - (localY + jitterY) * sinA,
                    center.y + (localX + jitterX) * sinA + (localY + jitterY) * cosA
                )
                column++
                if (!bounds.contains(spot)) continue

                val pick = Geometry.hashNoise(column * 31, row * 29, seed)
                val type = kinds[(pick * kinds.size).toInt().coerceIn(0, kinds.size - 1)]
                val sizeNoise = Geometry.hashNoise(column * 11, row * 19, seed)
                val scale = (if (type.big) 1.6f else 1f) * (0.85f + sizeNoise * 0.4f)
                val width = houseWidth * scale
                val depth = houseDepth * scale
                val tilt = angle + (Geometry.hashNoise(column * 5, row * 23, seed) - 0.5f) * 0.12f

                val footprint = rect(spot, width, depth, tilt)
                if (footprint.any { !Geometry.pointInPolygon(it, outline) }) continue
                val box = Geometry.bounds(footprint)
                if (takenBounds.any { it.intersects(box) }) continue
                if (tooCloseToStreet(project, spot, max(width, depth) * 0.5f)) continue

                takenBounds.add(box)
                placed.add(Building(type = type, points = footprint))
            }
            row++
        }
        return placed
    }

    /** Направление ближайшей улицы — дома встают вдоль неё. */
    private fun streetAngle(project: MapProject, center: Vec, bounds: BBox): Float {
        var best: Pair<Vec, Vec>? = null
        var bestDistance = Float.MAX_VALUE
        val reach = (bounds.width + bounds.height)
        for (road in project.roads) {
            val points = road.points
            for (i in 0 until points.size - 1) {
                val distance = Geometry.distanceToSegment(center, points[i], points[i + 1])
                if (distance < bestDistance && distance < reach) {
                    bestDistance = distance
                    best = points[i] to points[i + 1]
                }
            }
        }
        val segment = best ?: return 0f
        return atan2(segment.second.y - segment.first.y, segment.second.x - segment.first.x)
    }

    private fun tooCloseToStreet(project: MapProject, point: Vec, halfSize: Float): Boolean {
        for (road in project.roads) {
            val distance = Geometry.distanceToPolyline(point, road.points)
            if (distance < road.type.width * 0.8f + halfSize) return true
        }
        for (feature in project.lines) {
            val distance = Geometry.distanceToPolyline(point, feature.points)
            if (distance < feature.effectiveWidth * 0.6f + halfSize) return true
        }
        for (water in project.waters) {
            if (Geometry.pointInPolygon(point, water.points)) return true
        }
        return false
    }

    private fun rect(center: Vec, width: Float, depth: Float, angle: Float): List<Vec> {
        val halfWidth = width / 2f
        val halfDepth = depth / 2f
        val cosA = cos(angle)
        val sinA = sin(angle)
        fun corner(dx: Float, dy: Float) = Vec(
            center.x + dx * cosA - dy * sinA,
            center.y + dx * sinA + dy * cosA
        )
        return listOf(
            corner(-halfWidth, -halfDepth),
            corner(halfWidth, -halfDepth),
            corner(halfWidth, halfDepth),
            corner(-halfWidth, halfDepth)
        )
    }

    /** Какие дома уместны в квартале такого рода. */
    fun housesFor(type: DistrictType): List<BuildingType> = when (type) {
        DistrictType.MARKET_QUARTER -> listOf(
            BuildingType.SHOP, BuildingType.SHOP, BuildingType.HOUSE, BuildingType.TAVERN,
            BuildingType.WAREHOUSE, BuildingType.BAKERY, BuildingType.SPICE_SHOP,
            BuildingType.BANK_HOUSE, BuildingType.MARKET_HALL, BuildingType.INN_HOUSE
        )
        DistrictType.CRAFT_QUARTER -> listOf(
            BuildingType.SMITHY, BuildingType.POTTERY, BuildingType.WEAVER, BuildingType.HOUSE,
            BuildingType.CARPENTER, BuildingType.TANNERY, BuildingType.DYER,
            BuildingType.ARMOURER, BuildingType.STONECUTTER, BuildingType.HOUSE
        )
        DistrictType.TEMPLE_QUARTER -> listOf(
            BuildingType.CHAPEL, BuildingType.HOUSE, BuildingType.SHRINE_HOUSE,
            BuildingType.MONASTERY_HOUSE, BuildingType.HEALER, BuildingType.HOUSE
        )
        DistrictType.NOBLE_QUARTER -> listOf(
            BuildingType.RICH_HOUSE, BuildingType.MANOR, BuildingType.RICH_HOUSE,
            BuildingType.JEWELLER, BuildingType.HOUSE, BuildingType.STABLE
        )
        DistrictType.POOR_QUARTER -> listOf(
            BuildingType.HUT, BuildingType.HOUSE, BuildingType.TENEMENT,
            BuildingType.SHACK_ROW, BuildingType.HUT
        )
        DistrictType.SLUMS -> listOf(
            BuildingType.HUT, BuildingType.SHACK_ROW, BuildingType.ABANDONED_HOUSE,
            BuildingType.HUT, BuildingType.BURNT_HOUSE
        )
        DistrictType.HARBOUR_QUARTER -> listOf(
            BuildingType.WAREHOUSE, BuildingType.DOCK_HOUSE, BuildingType.TAVERN,
            BuildingType.HOUSE, BuildingType.FISH_MARKET, BuildingType.SHIPYARD_HOUSE
        )
        DistrictType.GARRISON_QUARTER -> listOf(
            BuildingType.BARRACKS_HOUSE, BuildingType.GUARD_HOUSE, BuildingType.STABLE,
            BuildingType.SMITHY, BuildingType.HOUSE
        )
        DistrictType.SCHOLAR_QUARTER -> listOf(
            BuildingType.SCHOOL, BuildingType.LIBRARY_HOUSE, BuildingType.HOUSE,
            BuildingType.APOTHECARY, BuildingType.RICH_HOUSE
        )
        DistrictType.MAGIC_QUARTER -> listOf(
            BuildingType.ALCHEMIST_HOUSE, BuildingType.ENCHANTER, BuildingType.HOUSE,
            BuildingType.SEER_HOUSE, BuildingType.WIZARD_HOUSE
        )
        DistrictType.FOREIGN_QUARTER -> listOf(
            BuildingType.HOUSE, BuildingType.SHOP, BuildingType.CARAVAN_YARD,
            BuildingType.TAVERN, BuildingType.SPICE_SHOP
        )
        DistrictType.OLD_TOWN -> listOf(
            BuildingType.HOUSE, BuildingType.TALL_HOUSE, BuildingType.SHOP,
            BuildingType.TAVERN, BuildingType.HOUSE, BuildingType.OLD_TEMPLE
        )
        DistrictType.NEW_TOWN -> listOf(
            BuildingType.HOUSE, BuildingType.RICH_HOUSE, BuildingType.SHOP,
            BuildingType.TALL_HOUSE, BuildingType.HOUSE
        )
        DistrictType.FARM_QUARTER -> listOf(
            BuildingType.FARMSTEAD_HOUSE, BuildingType.GRANARY, BuildingType.HUT,
            BuildingType.STABLE, BuildingType.MILL_HOUSE
        )
        DistrictType.GRAVE_QUARTER -> listOf(
            BuildingType.CRYPT_HOUSE, BuildingType.CHAPEL, BuildingType.CRYPT_HOUSE
        )
        DistrictType.PARK_QUARTER -> listOf(
            BuildingType.HOUSE, BuildingType.RICH_HOUSE, BuildingType.BATH_HOUSE_CITY
        )
    }
}
