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

    private const val GUARD_LIMIT = 40000

    /**
     * Нрав квартала: как в нём стоят дома.
     *
     * @param size размер обычного дома
     * @param gap промежуток между соседями, в долях размера дома
     * @param rowGap промежуток между рядами, в долях размера дома
     * @param jitter разброс дома внутри места
     * @param tilt разброс поворота дома, радианы
     * @param skip доля пустых мест: дворы, огороды, проулки
     * @param bigShare доля крупных построек среди прочих
     * @param landmark главное здание в середине квартала
     */
    data class DistrictPlan(
        val size: Float,
        val gap: Float,
        val rowGap: Float,
        val jitter: Float,
        val tilt: Float,
        val skip: Float,
        val bigShare: Float,
        val landmark: BuildingType? = null
    )

    /** Нрав застройки для каждого рода квартала. */
    fun planFor(type: DistrictType): DistrictPlan = when (type) {
        // Трущобы: лачуги лепятся друг к другу как попало, просветов почти нет.
        DistrictType.SLUMS -> DistrictPlan(
            size = 0.6f, gap = 0.1f, rowGap = 0.18f, jitter = 0.5f,
            tilt = 0.5f, skip = 0.05f, bigShare = 0f
        )
        DistrictType.POOR_QUARTER -> DistrictPlan(
            size = 0.78f, gap = 0.18f, rowGap = 0.3f, jitter = 0.38f,
            tilt = 0.3f, skip = 0.08f, bigShare = 0.04f
        )
        // Знатный квартал: несколько больших особняков в глубине садов.
        DistrictType.NOBLE_QUARTER -> DistrictPlan(
            size = 1.75f, gap = 0.9f, rowGap = 1.1f, jitter = 0.18f,
            tilt = 0.04f, skip = 0.3f, bigShare = 0.55f, landmark = BuildingType.PALACE
        )
        // Храмовый квартал: собор в середине, вокруг строгие ряды.
        DistrictType.TEMPLE_QUARTER -> DistrictPlan(
            size = 1.25f, gap = 0.6f, rowGap = 0.8f, jitter = 0.1f,
            tilt = 0.02f, skip = 0.22f, bigShare = 0.35f, landmark = BuildingType.CATHEDRAL_HOUSE
        )
        // Торговый квартал: сплошные лавки вдоль улицы, торговые ряды в середине.
        DistrictType.MARKET_QUARTER -> DistrictPlan(
            size = 1f, gap = 0.14f, rowGap = 0.45f, jitter = 0.14f,
            tilt = 0.05f, skip = 0.1f, bigShare = 0.14f, landmark = BuildingType.MARKET_HALL
        )
        // Ремесленный квартал: мастерские с дворами позади.
        DistrictType.CRAFT_QUARTER -> DistrictPlan(
            size = 0.95f, gap = 0.28f, rowGap = 0.6f, jitter = 0.3f,
            tilt = 0.14f, skip = 0.16f, bigShare = 0.1f
        )
        // Порт: длинные склады, развёрнутые к воде.
        DistrictType.HARBOUR_QUARTER -> DistrictPlan(
            size = 1.2f, gap = 0.3f, rowGap = 0.55f, jitter = 0.16f,
            tilt = 0.05f, skip = 0.14f, bigShare = 0.3f
        )
        // Военный квартал: одинаковые казармы по линейке.
        DistrictType.GARRISON_QUARTER -> DistrictPlan(
            size = 1.15f, gap = 0.45f, rowGap = 0.7f, jitter = 0.02f,
            tilt = 0f, skip = 0.08f, bigShare = 0.25f, landmark = BuildingType.BARRACKS_HOUSE
        )
        DistrictType.SCHOLAR_QUARTER -> DistrictPlan(
            size = 1.2f, gap = 0.55f, rowGap = 0.8f, jitter = 0.12f,
            tilt = 0.03f, skip = 0.24f, bigShare = 0.35f, landmark = BuildingType.UNIVERSITY_HOUSE
        )
        // Магический квартал: кривые дома, башни, много пустого места.
        DistrictType.MAGIC_QUARTER -> DistrictPlan(
            size = 1.1f, gap = 0.6f, rowGap = 0.85f, jitter = 0.45f,
            tilt = 0.35f, skip = 0.3f, bigShare = 0.3f, landmark = BuildingType.MAGE_SCHOOL
        )
        DistrictType.FOREIGN_QUARTER -> DistrictPlan(
            size = 1f, gap = 0.3f, rowGap = 0.55f, jitter = 0.35f,
            tilt = 0.22f, skip = 0.18f, bigShare = 0.16f, landmark = BuildingType.CARAVAN_YARD
        )
        // Старый город: тесная путаница домов.
        DistrictType.OLD_TOWN -> DistrictPlan(
            size = 0.88f, gap = 0.12f, rowGap = 0.25f, jitter = 0.4f,
            tilt = 0.26f, skip = 0.08f, bigShare = 0.08f
        )
        // Новый город: ровная застройка по плану.
        DistrictType.NEW_TOWN -> DistrictPlan(
            size = 1.05f, gap = 0.35f, rowGap = 0.6f, jitter = 0.08f,
            tilt = 0.02f, skip = 0.14f, bigShare = 0.14f
        )
        // Квартал зрелищ: большие залы и балаганы вокруг театра.
        DistrictType.FUN_QUARTER -> DistrictPlan(
            size = 1.35f, gap = 0.5f, rowGap = 0.8f, jitter = 0.2f,
            tilt = 0.08f, skip = 0.22f, bigShare = 0.45f, landmark = BuildingType.THEATRE_HOUSE
        )
        // Огороды: редкие усадьбы среди грядок.
        DistrictType.FARM_QUARTER -> DistrictPlan(
            size = 1.15f, gap = 1.1f, rowGap = 1.4f, jitter = 0.3f,
            tilt = 0.18f, skip = 0.45f, bigShare = 0.2f
        )
        // Кладбище: редкие склепы и часовня.
        DistrictType.GRAVE_QUARTER -> DistrictPlan(
            size = 0.7f, gap = 0.7f, rowGap = 0.9f, jitter = 0.16f,
            tilt = 0.06f, skip = 0.5f, bigShare = 0.1f, landmark = BuildingType.CHAPEL
        )
        // Парк: почти пусто, изредка беседка или купальня.
        DistrictType.PARK_QUARTER -> DistrictPlan(
            size = 1.25f, gap = 1.6f, rowGap = 2f, jitter = 0.3f,
            tilt = 0.1f, skip = 0.6f, bigShare = 0.3f
        )
    }

    /**
     * Заполнить квартал домами по нраву квартала.
     *
     * Дома встают рядами вдоль ближайшей улицы: в трущобах — вплотную и вкривь,
     * в знатном квартале — редкие большие особняки, в храмовом — собор посередине.
     * Дома не залезают на улицы, воду, друг на друга и за границу квартала.
     */
    fun fillDistrict(
        project: MapProject,
        district: District,
        density: Float,
        seed: Int
    ): List<Building> {
        val outline = district.points
        if (outline.size < 3) return emptyList()
        val plan = planFor(district.type)
        val bounds = Geometry.bounds(outline)
        val base = min(project.worldWidth, project.worldHeight)

        // Ползунок густоты: правее — дома мельче и теснее.
        val sizeByDensity = 1.3f - density * 0.55f
        val gapByDensity = (1.5f - density * 1.1f).coerceAtLeast(0.2f)
        val unit = base * 0.0165f * plan.size * sizeByDensity
        if (unit <= 0.01f) return emptyList()
        val gap = unit * plan.gap * gapByDensity + unit * 0.04f
        val rowStep = unit * 1.15f + unit * plan.rowGap * gapByDensity

        val center = Geometry.centroid(outline)
        val angle = streetAngle(project, center, bounds)
        val cosA = cos(angle)
        val sinA = sin(angle)

        fun toWorld(u: Float, v: Float) = Vec(
            center.x + u * cosA - v * sinA,
            center.y + u * sinA + v * cosA
        )

        fun toLocal(point: Vec): Vec {
            val dx = point.x - center.x
            val dy = point.y - center.y
            return Vec(dx * cosA + dy * sinA, -dx * sinA + dy * cosA)
        }

        // Границы квартала в повёрнутой системе координат.
        var uMin = Float.MAX_VALUE
        var uMax = -Float.MAX_VALUE
        var vMin = Float.MAX_VALUE
        var vMax = -Float.MAX_VALUE
        for (point in outline) {
            val local = toLocal(point)
            uMin = min(uMin, local.x)
            uMax = max(uMax, local.x)
            vMin = min(vMin, local.y)
            vMax = max(vMax, local.y)
        }
        if (uMax - uMin < unit * 0.6f || vMax - vMin < unit * 0.6f) return emptyList()

        // Занятые места: уже стоящие здания квартала и то, что поставим сейчас.
        val taken = ArrayList<FloatArray>()
        for (existing in project.buildings) {
            if (existing.points.size < 3) continue
            val box = Geometry.bounds(existing.points)
            if (!box.intersects(bounds.expand(unit))) continue
            var eu0 = Float.MAX_VALUE
            var eu1 = -Float.MAX_VALUE
            var ev0 = Float.MAX_VALUE
            var ev1 = -Float.MAX_VALUE
            for (point in existing.points) {
                val local = toLocal(point)
                eu0 = min(eu0, local.x)
                eu1 = max(eu1, local.x)
                ev0 = min(ev0, local.y)
                ev1 = max(ev1, local.y)
            }
            taken.add(floatArrayOf(eu0, eu1, ev0, ev1))
        }

        fun free(u0: Float, u1: Float, v0: Float, v1: Float): Boolean {
            val inset = unit * 0.06f
            for (box in taken) {
                if (u0 + inset < box[1] && box[0] + inset < u1 &&
                    v0 + inset < box[3] && box[2] + inset < v1
                ) return false
            }
            return true
        }

        val kinds = housesFor(district.type)
        val small = kinds.filter { !it.big }.ifEmpty { kinds }
        val large = kinds.filter { it.big }
        val placed = ArrayList<Building>()

        /** Поставить дом, если место свободно и он целиком внутри квартала. */
        fun place(type: BuildingType, u: Float, v: Float, width: Float, depth: Float, tilt: Float): Boolean {
            val half = max(width, depth) * 0.5f
            val u0 = u - width * 0.5f
            val u1 = u + width * 0.5f
            val v0 = v - depth * 0.5f
            val v1 = v + depth * 0.5f
            if (!free(u0, u1, v0, v1)) return false
            val spot = toWorld(u, v)
            if (!Geometry.pointInPolygon(spot, outline)) return false
            val footprint = rect(spot, width, depth, angle + tilt)
            if (footprint.any { !Geometry.pointInPolygon(it, outline) }) return false
            if (tooCloseToStreet(project, spot, half)) return false
            taken.add(floatArrayOf(u0, u1, v0, v1))
            placed.add(Building(type = type, points = footprint))
            return true
        }

        // Главное здание квартала — в середине, всё прочее строится вокруг него.
        val landmark = plan.landmark
        if (landmark != null && uMax - uMin > unit * 5f && vMax - vMin > unit * 4f) {
            val width = unit * 2.6f * landmark.shape.widthScale
            val depth = unit * 2.6f * landmark.shape.depthScale
            place(landmark, 0f, 0f, width, depth, 0f)
        }

        var guard = 0
        var rowIndex = 0
        var v = vMin + rowStep * 0.5f
        while (v < vMax && guard < GUARD_LIMIT) {
            var u = uMin
            var slot = 0
            while (u < uMax && guard < GUARD_LIMIT) {
                guard++
                slot++
                val roll = Geometry.hashNoise(rowIndex * 131 + slot * 7, rowIndex * 17 + 3, seed)
                if (roll < plan.skip) {
                    // Двор, огород или проулок вместо дома.
                    u += unit * (0.7f + roll * 2f)
                    continue
                }
                val pickBig = Geometry.hashNoise(slot * 29, rowIndex * 41 + 11, seed) < plan.bigShare
                val pool = if (pickBig && large.isNotEmpty()) large else small
                val pick = Geometry.hashNoise(slot * 53 + rowIndex, rowIndex * 59 + 7, seed)
                val type = pool[(pick * pool.size).toInt().coerceIn(0, pool.size - 1)]
                val sizeNoise = 0.85f + Geometry.hashNoise(slot * 71, rowIndex * 19, seed) * 0.35f
                val bigScale = if (type.big) 1.7f else 1f
                val width = unit * type.shape.widthScale * sizeNoise * bigScale
                val depth = unit * type.shape.depthScale * sizeNoise * bigScale
                val jitterU = (Geometry.hashNoise(slot * 13, rowIndex * 37, seed) - 0.5f) * unit * plan.jitter
                val jitterV = (Geometry.hashNoise(slot * 23, rowIndex * 43, seed) - 0.5f) * unit * plan.jitter
                val tilt = (Geometry.hashNoise(slot * 83, rowIndex * 11, seed) - 0.5f) * 2f * plan.tilt
                place(
                    type,
                    u + width * 0.5f + jitterU,
                    v + jitterV,
                    width,
                    depth,
                    tilt
                )
                val step = gap * (0.7f + Geometry.hashNoise(slot * 97, rowIndex * 31, seed) * 0.8f)
                u += width + step
            }
            v += rowStep
            rowIndex++
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
            BuildingType.SHOP, BuildingType.SHOP, BuildingType.SHOP, BuildingType.HOUSE,
            BuildingType.TAVERN, BuildingType.WAREHOUSE, BuildingType.BAKERY,
            BuildingType.BUTCHER, BuildingType.SPICE_SHOP, BuildingType.BANK_HOUSE,
            BuildingType.TALL_HOUSE, BuildingType.MARKET_HALL, BuildingType.INN_HOUSE,
            BuildingType.CUSTOMS, BuildingType.GUILD_HOUSE
        )
        DistrictType.CRAFT_QUARTER -> listOf(
            BuildingType.SMITHY, BuildingType.POTTERY, BuildingType.WEAVER, BuildingType.HOUSE,
            BuildingType.CARPENTER, BuildingType.TANNERY, BuildingType.DYER,
            BuildingType.ARMOURER, BuildingType.STONECUTTER, BuildingType.GLASSBLOWER,
            BuildingType.BREWERY, BuildingType.HOUSE, BuildingType.GUILD_HOUSE
        )
        DistrictType.TEMPLE_QUARTER -> listOf(
            BuildingType.CHAPEL, BuildingType.SHRINE_HOUSE, BuildingType.CHURCH,
            BuildingType.MONASTERY_HOUSE, BuildingType.HEALER, BuildingType.OLD_TEMPLE,
            BuildingType.BELL_TOWER, BuildingType.HOUSE, BuildingType.CRYPT_HOUSE,
            BuildingType.HOSPITAL
        )
        DistrictType.NOBLE_QUARTER -> listOf(
            BuildingType.MANOR, BuildingType.RICH_HOUSE, BuildingType.MANOR,
            BuildingType.RICH_HOUSE, BuildingType.JEWELLER, BuildingType.STABLE,
            BuildingType.TOWER_HOUSE, BuildingType.BATH_HOUSE_CITY, BuildingType.CART_YARD,
            BuildingType.OPERA_HOUSE
        )
        DistrictType.POOR_QUARTER -> listOf(
            BuildingType.HUT, BuildingType.HOUSE, BuildingType.TENEMENT,
            BuildingType.SHACK_ROW, BuildingType.HUT, BuildingType.TAVERN,
            BuildingType.ABANDONED_HOUSE
        )
        DistrictType.SLUMS -> listOf(
            BuildingType.HUT, BuildingType.HUT, BuildingType.SHACK_ROW,
            BuildingType.ABANDONED_HOUSE, BuildingType.HUT, BuildingType.BURNT_HOUSE,
            BuildingType.RUINED_HOUSE, BuildingType.PLAGUE_HOUSE
        )
        DistrictType.HARBOUR_QUARTER -> listOf(
            BuildingType.WAREHOUSE, BuildingType.DOCK_HOUSE, BuildingType.TAVERN,
            BuildingType.HOUSE, BuildingType.FISH_MARKET, BuildingType.SHIPYARD_HOUSE,
            BuildingType.WAREHOUSE, BuildingType.INN_HOUSE, BuildingType.CUSTOMS,
            BuildingType.LIGHTHOUSE_HOUSE
        )
        DistrictType.GARRISON_QUARTER -> listOf(
            BuildingType.BARRACKS_HOUSE, BuildingType.GUARD_HOUSE, BuildingType.STABLE,
            BuildingType.SMITHY, BuildingType.ARMOURER, BuildingType.GRANARY,
            BuildingType.WATCH_TOWER_HOUSE, BuildingType.PRISON_HOUSE, BuildingType.HOUSE
        )
        DistrictType.SCHOLAR_QUARTER -> listOf(
            BuildingType.SCHOOL, BuildingType.LIBRARY_HOUSE, BuildingType.HOUSE,
            BuildingType.APOTHECARY, BuildingType.RICH_HOUSE, BuildingType.OBSERVATORY_HOUSE,
            BuildingType.HEALER, BuildingType.TALL_HOUSE
        )
        DistrictType.MAGIC_QUARTER -> listOf(
            BuildingType.ALCHEMIST_HOUSE, BuildingType.ENCHANTER, BuildingType.HOUSE,
            BuildingType.SEER_HOUSE, BuildingType.WIZARD_HOUSE, BuildingType.ASTROLOGER_TOWER,
            BuildingType.TOWER_HOUSE, BuildingType.APOTHECARY
        )
        DistrictType.FOREIGN_QUARTER -> listOf(
            BuildingType.HOUSE, BuildingType.SHOP, BuildingType.CARAVAN_YARD,
            BuildingType.TAVERN, BuildingType.SPICE_SHOP, BuildingType.SHRINE_HOUSE,
            BuildingType.STABLE, BuildingType.INN_HOUSE
        )
        DistrictType.OLD_TOWN -> listOf(
            BuildingType.HOUSE, BuildingType.TALL_HOUSE, BuildingType.SHOP,
            BuildingType.TAVERN, BuildingType.HOUSE, BuildingType.OLD_TEMPLE,
            BuildingType.TENEMENT, BuildingType.TOWER_HOUSE, BuildingType.HOUSE
        )
        DistrictType.NEW_TOWN -> listOf(
            BuildingType.HOUSE, BuildingType.RICH_HOUSE, BuildingType.SHOP,
            BuildingType.TALL_HOUSE, BuildingType.HOUSE, BuildingType.BAKERY,
            BuildingType.SCHOOL, BuildingType.WATER_HOUSE
        )
        DistrictType.FUN_QUARTER -> listOf(
            BuildingType.THEATRE_HOUSE, BuildingType.PUPPET_THEATRE, BuildingType.DANCE_HOUSE,
            BuildingType.MUSIC_HALL, BuildingType.GAMBLING_DEN, BuildingType.TAVERN,
            BuildingType.BATH_HOUSE_CITY, BuildingType.STORYTELLER_STAGE,
            BuildingType.AMPHITHEATRE, BuildingType.CONCERT_HALL, BuildingType.CIRCUS_TENT,
            BuildingType.PLEASURE_HOUSE, BuildingType.COCKPIT, BuildingType.INN_HOUSE
        )
        DistrictType.FARM_QUARTER -> listOf(
            BuildingType.FARMSTEAD_HOUSE, BuildingType.GRANARY, BuildingType.HUT,
            BuildingType.STABLE, BuildingType.MILL_HOUSE, BuildingType.DOVECOTE,
            BuildingType.HOUSE
        )
        DistrictType.GRAVE_QUARTER -> listOf(
            BuildingType.CRYPT_HOUSE, BuildingType.CHAPEL, BuildingType.CRYPT_HOUSE,
            BuildingType.SHRINE_HOUSE, BuildingType.PLAGUE_HOUSE
        )
        DistrictType.PARK_QUARTER -> listOf(
            BuildingType.BATH_HOUSE_CITY, BuildingType.RICH_HOUSE, BuildingType.DOVECOTE,
            BuildingType.STORYTELLER_STAGE, BuildingType.SHRINE_HOUSE
        )
    }
}
