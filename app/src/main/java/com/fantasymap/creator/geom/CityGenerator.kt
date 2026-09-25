package com.fantasymap.creator.geom

import com.fantasymap.creator.model.BBox
import com.fantasymap.creator.model.BiomeRegion
import com.fantasymap.creator.model.BiomeType
import com.fantasymap.creator.model.Building
import com.fantasymap.creator.model.BuildingType
import com.fantasymap.creator.model.District
import com.fantasymap.creator.model.DistrictType
import com.fantasymap.creator.model.Geometry
import com.fantasymap.creator.model.MapProject
import com.fantasymap.creator.model.Road
import com.fantasymap.creator.model.RoadType
import com.fantasymap.creator.model.Vec
import java.util.Random
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/** Как нарезаны улицы внутри квартала. */
enum class StreetPattern {
    /** Ровная сетка улиц. */
    GRID,

    /** Кривые переулки со сдвигами и тупиками. */
    CROOKED,

    /** Длинные параллельные улочки, дома стоят сплошными рядами спина к спине. */
    TERRACES,

    /** Парадная аллея посередине и симметричные улицы. */
    AVENUE,

    /** Улицы лучами от главного здания и кольцо вокруг него. */
    RADIAL,

    /** Несколько извилистых тропинок. */
    PATHS,
    NONE
}

/** Что получилось при застройке квартала: дома, улицы, сады и площади. */
data class DistrictFill(
    val buildings: List<Building>,
    val roads: List<Road>,
    val gardens: List<BiomeRegion>
) {
    val isEmpty: Boolean get() = buildings.isEmpty() && roads.isEmpty() && gardens.isEmpty()
}

/** Застройка квартала домами и улицами: вручную рисовать сотни домов никто не станет. */
object CityGenerator {

    private const val GUARD_LIMIT = 40000

    /**
     * Нрав квартала: как в нём стоят дома и идут улицы.
     *
     * @param size размер обычного дома
     * @param gap промежуток между соседями, в долях размера дома
     * @param rowGap промежуток между рядами, в долях размера дома
     * @param jitter разброс дома внутри места
     * @param tilt разброс поворота дома, радианы
     * @param skip доля пустых мест: дворы, огороды, проулки
     * @param bigShare доля крупных построек среди прочих
     * @param landmark главное здание в середине квартала
     * @param streets рисунок улиц
     * @param blockDepth расстояние между продольными улицами, в размерах дома
     * @param blockLength расстояние между поперечными улицами, в размерах дома
     * @param wiggle кривизна улиц, 0 — по линейке
     * @param deadEnds доля оборванных переулков
     * @param aspect разброс пропорций домов
     * @param clusters лепить дома кучками, а не рядами
     * @param gardens сад позади крупного дома
     * @param plaza площадь вокруг главного здания
     * @param mirror застраивать зеркально относительно главной аллеи
     * @param setback отступ домов от улицы, в размерах дома
     */
    data class DistrictPlan(
        val size: Float,
        val gap: Float,
        val rowGap: Float,
        val jitter: Float,
        val tilt: Float,
        val skip: Float,
        val bigShare: Float,
        val landmark: BuildingType? = null,
        val streets: StreetPattern = StreetPattern.GRID,
        val blockDepth: Float = 3.2f,
        val blockLength: Float = 6f,
        val mainRoad: RoadType = RoadType.STREET,
        val laneRoad: RoadType = RoadType.LANE,
        val wiggle: Float = 0.1f,
        val deadEnds: Float = 0f,
        val aspect: Float = 0.15f,
        val clusters: Boolean = false,
        val gardens: BiomeType? = null,
        val plaza: BiomeType? = null,
        val mirror: Boolean = false,
        val landmarkScale: Float = 2.6f,
        val setback: Float = 0.12f
    )

    /** Нрав застройки для каждого рода квартала. */
    fun planFor(type: DistrictType): DistrictPlan = when (type) {
        // Трущобы: кучки лачуг вкривь и вкось, кривые закоулки, тупики, пожарища.
        DistrictType.SLUMS -> DistrictPlan(
            size = 0.55f, gap = 0.08f, rowGap = 0.15f, jitter = 0.6f, tilt = 0.8f,
            skip = 0.08f, bigShare = 0f, streets = StreetPattern.CROOKED,
            blockDepth = 4.5f, blockLength = 4f, mainRoad = RoadType.ALLEY, laneRoad = RoadType.ALLEY,
            wiggle = 0.9f, deadEnds = 0.45f, aspect = 0.6f, clusters = true
        )
        // Бедный квартал: одинаковые домики сплошными рядами спина к спине вдоль узких улочек.
        DistrictType.POOR_QUARTER -> DistrictPlan(
            size = 0.8f, gap = 0.02f, rowGap = 0.05f, jitter = 0.04f, tilt = 0.02f,
            skip = 0.04f, bigShare = 0.03f, streets = StreetPattern.TERRACES,
            blockDepth = 2.6f, blockLength = 14f, mainRoad = RoadType.LANE, laneRoad = RoadType.LANE,
            wiggle = 0.05f, aspect = 0.05f, setback = 0.02f
        )
        // Знатный квартал: широкие улицы, особняки в глубине участков, сад за каждым.
        DistrictType.NOBLE_QUARTER -> DistrictPlan(
            size = 1.5f, gap = 0.9f, rowGap = 0.6f, jitter = 0.1f, tilt = 0.03f,
            skip = 0.2f, bigShare = 0.5f, streets = StreetPattern.GRID,
            blockDepth = 4.6f, blockLength = 7f, mainRoad = RoadType.STREET, laneRoad = RoadType.STREET,
            wiggle = 0.12f, aspect = 0.2f, gardens = BiomeType.CITY_GARDEN, setback = 0.5f
        )
        // Дворцовый квартал: дворец посередине, аллея, всё строго зеркально, парадные сады.
        DistrictType.PALACE_QUARTER -> DistrictPlan(
            size = 1.6f, gap = 1.1f, rowGap = 0.9f, jitter = 0f, tilt = 0f,
            skip = 0.15f, bigShare = 0.45f, landmark = BuildingType.PALACE,
            streets = StreetPattern.AVENUE, blockDepth = 5f, blockLength = 6f,
            mainRoad = RoadType.MAIN_STREET, laneRoad = RoadType.STREET, wiggle = 0f,
            aspect = 0f, gardens = BiomeType.CITY_ROYAL_GARDEN, plaza = BiomeType.CITY_TILED_PLAZA,
            mirror = true, landmarkScale = 4f, setback = 0.6f
        )
        // Храмовый квартал: собор на площади, улицы лучами, строгие ряды.
        DistrictType.TEMPLE_QUARTER -> DistrictPlan(
            size = 1.15f, gap = 0.4f, rowGap = 0.6f, jitter = 0.08f, tilt = 0.02f,
            skip = 0.18f, bigShare = 0.3f, landmark = BuildingType.CATHEDRAL_HOUSE,
            streets = StreetPattern.RADIAL, mainRoad = RoadType.STREET, laneRoad = RoadType.LANE,
            plaza = BiomeType.CITY_TILED_PLAZA, landmarkScale = 3f
        )
        // Торговый квартал: частая сетка, сплошные лавки, торговые ряды на рыночной площади.
        DistrictType.MARKET_QUARTER -> DistrictPlan(
            size = 0.95f, gap = 0.1f, rowGap = 0.25f, jitter = 0.1f, tilt = 0.04f,
            skip = 0.06f, bigShare = 0.12f, landmark = BuildingType.MARKET_HALL,
            streets = StreetPattern.GRID, blockDepth = 3f, blockLength = 4.5f,
            mainRoad = RoadType.MAIN_STREET, laneRoad = RoadType.STREET, wiggle = 0.15f,
            plaza = BiomeType.CITY_MARKET_SQUARE, setback = 0.05f
        )
        // Ремесленный квартал: мастерские вдоль переулков, дворы позади.
        DistrictType.CRAFT_QUARTER -> DistrictPlan(
            size = 0.95f, gap = 0.25f, rowGap = 0.5f, jitter = 0.25f, tilt = 0.12f,
            skip = 0.14f, bigShare = 0.1f, streets = StreetPattern.GRID,
            blockDepth = 3.4f, blockLength = 5f, laneRoad = RoadType.LANE, wiggle = 0.3f,
            deadEnds = 0.1f, aspect = 0.25f, gardens = BiomeType.CITY_YARD
        )
        // Порт: длинные склады, прямые улицы к воде.
        DistrictType.HARBOUR_QUARTER -> DistrictPlan(
            size = 1.2f, gap = 0.25f, rowGap = 0.5f, jitter = 0.1f, tilt = 0.03f,
            skip = 0.12f, bigShare = 0.3f, streets = StreetPattern.GRID,
            blockDepth = 3.2f, blockLength = 5f, mainRoad = RoadType.WATERFRONT, laneRoad = RoadType.STREET,
            wiggle = 0.05f
        )
        // Военный квартал: казармы по линейке вокруг плаца.
        DistrictType.GARRISON_QUARTER -> DistrictPlan(
            size = 1.1f, gap = 0.45f, rowGap = 0.6f, jitter = 0f, tilt = 0f,
            skip = 0.06f, bigShare = 0.3f, landmark = BuildingType.BARRACKS_HOUSE,
            streets = StreetPattern.GRID, blockDepth = 3.6f, blockLength = 6f, wiggle = 0f,
            aspect = 0f, plaza = BiomeType.CITY_DRILL_YARD
        )
        DistrictType.SCHOLAR_QUARTER -> DistrictPlan(
            size = 1.15f, gap = 0.5f, rowGap = 0.7f, jitter = 0.1f, tilt = 0.03f,
            skip = 0.2f, bigShare = 0.35f, landmark = BuildingType.UNIVERSITY_HOUSE,
            streets = StreetPattern.AVENUE, blockDepth = 4f, blockLength = 6f,
            wiggle = 0.05f, gardens = BiomeType.CITY_GARDEN, plaza = BiomeType.CITY_SQUARE
        )
        // Магический квартал: извилистые тропы, башни вразброс, волшебные сады.
        DistrictType.MAGIC_QUARTER -> DistrictPlan(
            size = 1.1f, gap = 0.6f, rowGap = 0.85f, jitter = 0.5f, tilt = 0.4f,
            skip = 0.3f, bigShare = 0.3f, landmark = BuildingType.MAGE_SCHOOL,
            streets = StreetPattern.PATHS, laneRoad = RoadType.ALLEY, wiggle = 0.8f,
            aspect = 0.3f, gardens = BiomeType.CITY_MAGIC_GARDEN
        )
        DistrictType.FOREIGN_QUARTER -> DistrictPlan(
            size = 0.95f, gap = 0.2f, rowGap = 0.4f, jitter = 0.3f, tilt = 0.2f,
            skip = 0.14f, bigShare = 0.15f, landmark = BuildingType.CARAVAN_YARD,
            streets = StreetPattern.CROOKED, blockDepth = 3.4f, blockLength = 4.5f,
            laneRoad = RoadType.LANE, wiggle = 0.5f, deadEnds = 0.2f, aspect = 0.3f,
            plaza = BiomeType.CITY_SQUARE
        )
        // Старый город: тесная путаница кривых улочек.
        DistrictType.OLD_TOWN -> DistrictPlan(
            size = 0.85f, gap = 0.08f, rowGap = 0.2f, jitter = 0.25f, tilt = 0.25f,
            skip = 0.06f, bigShare = 0.08f, streets = StreetPattern.CROOKED,
            blockDepth = 3f, blockLength = 3.8f, mainRoad = RoadType.STREET, laneRoad = RoadType.LANE,
            wiggle = 0.6f, deadEnds = 0.25f, aspect = 0.3f
        )
        // Новый город: ровная сетка по плану.
        DistrictType.NEW_TOWN -> DistrictPlan(
            size = 1.05f, gap = 0.3f, rowGap = 0.5f, jitter = 0.04f, tilt = 0.01f,
            skip = 0.1f, bigShare = 0.14f, streets = StreetPattern.GRID,
            blockDepth = 3.4f, blockLength = 6f, wiggle = 0f, aspect = 0.1f
        )
        // Квартал зрелищ: театр на площади, улицы лучами.
        DistrictType.FUN_QUARTER -> DistrictPlan(
            size = 1.2f, gap = 0.45f, rowGap = 0.7f, jitter = 0.15f, tilt = 0.08f,
            skip = 0.18f, bigShare = 0.45f, landmark = BuildingType.THEATRE_HOUSE,
            streets = StreetPattern.RADIAL, plaza = BiomeType.CITY_SQUARE, landmarkScale = 2.8f
        )
        DistrictType.GUILD_QUARTER -> DistrictPlan(
            size = 1.15f, gap = 0.2f, rowGap = 0.5f, jitter = 0.08f, tilt = 0.03f,
            skip = 0.1f, bigShare = 0.35f, landmark = BuildingType.ADVENTURERS_GUILD,
            streets = StreetPattern.GRID, blockDepth = 3.4f, blockLength = 5f,
            mainRoad = RoadType.MAIN_STREET, plaza = BiomeType.CITY_SQUARE
        )
        // Огороды: редкие усадьбы, огороды вокруг, тропинки.
        DistrictType.FARM_QUARTER -> DistrictPlan(
            size = 1.1f, gap = 1.1f, rowGap = 1.4f, jitter = 0.3f, tilt = 0.18f,
            skip = 0.4f, bigShare = 0.25f, streets = StreetPattern.PATHS,
            laneRoad = RoadType.ALLEY, wiggle = 0.6f, gardens = BiomeType.CITY_VEGETABLE
        )
        DistrictType.GRAVE_QUARTER -> DistrictPlan(
            size = 0.7f, gap = 0.7f, rowGap = 0.9f, jitter = 0.1f, tilt = 0.04f,
            skip = 0.5f, bigShare = 0.1f, landmark = BuildingType.CHAPEL,
            streets = StreetPattern.PATHS, laneRoad = RoadType.ALLEY, wiggle = 0.4f
        )
        DistrictType.PARK_QUARTER -> DistrictPlan(
            size = 1.2f, gap = 1.6f, rowGap = 2f, jitter = 0.3f, tilt = 0.1f,
            skip = 0.65f, bigShare = 0.3f, streets = StreetPattern.PATHS,
            laneRoad = RoadType.ALLEY, wiggle = 0.9f
        )
    }

    /**
     * Застроить квартал по его нраву: сначала улицы, потом площадь и главное
     * здание, потом дома рядами вдоль улиц (или кучками — в трущобах).
     * Дома не залезают на улицы, воду, друг на друга и за границу квартала.
     */
    fun fillDistrict(
        project: MapProject,
        district: District,
        density: Float,
        seed: Int,
        withStreets: Boolean = true
    ): DistrictFill {
        val empty = DistrictFill(emptyList(), emptyList(), emptyList())
        val contours = district.contours().filter { it.size >= 3 }
        if (contours.isEmpty()) return empty
        val plan = planFor(district.type)
        val all = contours.flatten()
        val bounds = Geometry.bounds(all)
        val base = min(project.worldWidth, project.worldHeight)
        val random = Random(seed.toLong() * 7919L + district.id.hashCode())

        // Ползунок густоты: правее — дома мельче и теснее.
        val sizeByDensity = 1.3f - density * 0.55f
        val gapByDensity = (1.5f - density * 1.1f).coerceAtLeast(0.2f)
        val unit = base * 0.0165f * plan.size * sizeByDensity
        if (unit <= 0.01f) return empty
        val gap = unit * plan.gap * gapByDensity + unit * 0.02f
        val rowStep = unit * 0.95f + unit * plan.rowGap * gapByDensity
        // Густота управляет и пустырями: правее — меньше дворов и проулков.
        val skipChance = plan.skip * (1.4f - density * 0.8f)

        fun inside(p: Vec) = Geometry.pointInContours(p, contours)

        val center = Geometry.centroid(contours[0])
        val angle = streetAngle(project, center, bounds)
        val cosA = cos(angle)
        val sinA = sin(angle)

        fun toWorld(u: Float, v: Float) = Vec(center.x + u * cosA - v * sinA, center.y + u * sinA + v * cosA)
        fun toLocal(point: Vec): Vec {
            val dx = point.x - center.x
            val dy = point.y - center.y
            return Vec(dx * cosA + dy * sinA, -dx * sinA + dy * cosA)
        }

        var uMin = Float.MAX_VALUE
        var uMax = -Float.MAX_VALUE
        var vMin = Float.MAX_VALUE
        var vMax = -Float.MAX_VALUE
        for (point in all) {
            val local = toLocal(point)
            uMin = min(uMin, local.x)
            uMax = max(uMax, local.x)
            vMin = min(vMin, local.y)
            vMax = max(vMax, local.y)
        }
        if (uMax - uMin < unit * 0.6f || vMax - vMin < unit * 0.6f) return empty

        // ------------------------------------------------------------ занятые места
        var allRoads: List<Road> = project.roads
        val taken = ArrayList<FloatArray>()
        for (existing in project.buildings) {
            if (existing.points.size < 3) continue
            if (!Geometry.bounds(existing.points).intersects(bounds.expand(unit))) continue
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

        fun free(u0: Float, u1: Float, v0: Float, v1: Float, inset: Float = unit * 0.05f): Boolean {
            for (box in taken) {
                if (u0 + inset < box[1] && box[0] + inset < u1 && v0 + inset < box[3] && box[2] + inset < v1) return false
            }
            return true
        }

        val kinds = housesFor(district.type)
        val small = kinds.filter { !it.big }.ifEmpty { kinds }
        val large = kinds.filter { it.big }
        val placed = ArrayList<Building>()
        val gardens = ArrayList<BiomeRegion>()

        fun rectLocal(u0: Float, u1: Float, v0: Float, v1: Float): List<Vec> =
            listOf(toWorld(u0, v0), toWorld(u1, v0), toWorld(u1, v1), toWorld(u0, v1))

        /** Сад или двор позади дома. */
        fun addGarden(u: Float, v: Float, width: Float, depth: Float, behind: Float) {
            val kind = plan.gardens ?: return
            val gardenDepth = depth * (0.8f + random.nextFloat() * 0.5f)
            val near = v + behind * (depth * 0.5f + unit * 0.08f)
            val far = near + behind * gardenDepth
            val v0 = min(near, far)
            val v1 = max(near, far)
            val u0 = u - width * 0.55f
            val u1 = u + width * 0.55f
            if (!free(u0, u1, v0, v1, 0f)) return
            val corners = rectLocal(u0, u1, v0, v1)
            if (corners.any { !inside(it) }) return
            if (blocked(allRoads, project, corners, unit * 0.04f)) return
            taken.add(floatArrayOf(u0, u1, v0, v1))
            gardens.add(BiomeRegion(biome = kind, points = corners))
        }

        /** Поставить дом, если место свободно и он целиком внутри квартала. */
        fun place(type: BuildingType, u: Float, v: Float, width: Float, depth: Float, tilt: Float, behind: Float): Boolean {
            val u0 = u - width * 0.5f
            val u1 = u + width * 0.5f
            val v0 = v - depth * 0.5f
            val v1 = v + depth * 0.5f
            if (!free(u0, u1, v0, v1)) return false
            val spot = toWorld(u, v)
            if (!inside(spot)) return false
            val footprint = rect(spot, width, depth, angle + tilt)
            if (footprint.any { !inside(it) }) return false
            if (blocked(allRoads, project, footprint, unit * 0.04f)) return false
            taken.add(floatArrayOf(u0, u1, v0, v1))
            placed.add(Building(type = type, points = footprint))
            if (type.big || (plan.gardens != null && random.nextFloat() < 0.35f)) {
                addGarden(u, v, width, depth, behind)
            }
            return true
        }

        /** Зеркальная застройка: дом ставится и по другую сторону аллеи. */
        fun placeMaybeMirrored(type: BuildingType, u: Float, v: Float, width: Float, depth: Float, tilt: Float, behind: Float) {
            val ok = place(type, u, v, width, depth, tilt, behind)
            if (ok && plan.mirror && abs(v) > unit * 0.3f) place(type, u, -v, width, depth, -tilt, -behind)
        }

        fun pickType(): BuildingType {
            val pool = if (random.nextFloat() < plan.bigShare && large.isNotEmpty()) large else small
            return pool[random.nextInt(pool.size)]
        }

        fun houseSize(type: BuildingType): Pair<Float, Float> {
            val noise = 0.85f + random.nextFloat() * 0.35f
            val big = if (type.big) 1.7f else 1f
            val stretch = 1f + (random.nextFloat() - 0.5f) * 2f * plan.aspect
            val width = unit * type.shape.widthScale * noise * big * stretch
            val depth = unit * type.shape.depthScale * noise * big / stretch
            return width to depth
        }

        // ------------------------------------------------------------ главное здание и площадь
        val landmark = plan.landmark
        var plazaRadius = 0f
        if (landmark != null && uMax - uMin > unit * 5f && vMax - vMin > unit * 4f) {
            val width = unit * plan.landmarkScale * landmark.shape.widthScale
            val depth = unit * plan.landmarkScale * landmark.shape.depthScale
            if (place(landmark, 0f, 0f, width, depth, 0f, 1f)) plazaRadius = max(width, depth) * 0.5f + unit * 1.4f
        }
        val plazaKind = plan.plaza
        if (plazaKind != null) {
            var radius = if (plazaRadius > 0f) plazaRadius else unit * 2.6f
            for (attempt in 0 until 3) {
                val ring = List(24) { i ->
                    val a = i * 2f * PI.toFloat() / 24
                    val r = radius * (0.92f + random.nextFloat() * 0.12f)
                    toWorld(cos(a) * r * 1.15f, sin(a) * r)
                }
                if (ring.all { inside(it) }) {
                    gardens.add(BiomeRegion(biome = plazaKind, points = ring))
                    taken.add(floatArrayOf(-radius * 1.1f, radius * 1.1f, -radius * 0.95f, radius * 0.95f))
                    plazaRadius = max(plazaRadius, radius)
                    break
                }
                radius *= 0.72f
            }
        }

        // ------------------------------------------------------------ улицы
        val roads = ArrayList<Road>()
        val streetRows = ArrayList<Float>()

        /** Волнистая линия: чем больше wiggle, тем кривее улица. */
        fun wavy(from: Vec, to: Vec, amount: Float): List<Vec> {
            val dx = to.x - from.x
            val dy = to.y - from.y
            val length = sqrt(dx * dx + dy * dy)
            if (length < 0.01f) return listOf(from, to)
            val nx = -dy / length
            val ny = dx / length
            val steps = max(2, (length / (unit * 0.6f)).toInt())
            val phase1 = random.nextFloat() * 6.28f
            val phase2 = random.nextFloat() * 6.28f
            val amp = amount * unit * 1.2f
            return List(steps + 1) { i ->
                val t = i.toFloat() / steps
                val along = t * length
                val offset = amp * (sin(along / (unit * 3.1f) + phase1) * 0.7f + sin(along / (unit * 1.3f) + phase2) * 0.3f)
                Vec(from.x + dx * t + nx * offset, from.y + dy * t + ny * offset)
            }
        }

        /** Уложить улицу: только те куски, что внутри квартала. */
        fun addStreet(local: List<Vec>, type: RoadType) {
            val world = local.map { toWorld(it.x, it.y) }
            val sampled = Geometry.resample(world, unit * 0.4f)
            var run = ArrayList<Vec>()
            fun flush() {
                if (run.size >= 2 && run.size * unit * 0.4f >= unit * 1.6f) {
                    val simplified = Geometry.simplify(run, unit * 0.06f)
                    if (simplified.size >= 2) roads.add(Road(type = type, points = simplified))
                }
                run = ArrayList()
            }
            for (point in sampled) {
                val local = toLocal(point)
                val nearCenter = plazaRadius > 0f && sqrt(local.x * local.x + local.y * local.y) < plazaRadius * 0.8f
                if (inside(point) && !nearCenter) run.add(point) else flush()
            }
            flush()
        }

        if (withStreets) {
            val spacing = unit * plan.blockDepth
            val cross = unit * plan.blockLength
            val reach = unit * 1.5f
            when (plan.streets) {
                StreetPattern.GRID, StreetPattern.TERRACES -> {
                    var v = vMin + spacing * (0.55f + random.nextFloat() * 0.3f)
                    var index = 0
                    while (v < vMax - spacing * 0.35f) {
                        streetRows.add(v)
                        val type = if (index % 3 == 1) plan.mainRoad else plan.laneRoad
                        addStreet(wavy(Vec(uMin - reach, v), Vec(uMax + reach, v), plan.wiggle), type)
                        v += spacing
                        index++
                    }
                    var u = uMin + cross * (0.5f + random.nextFloat() * 0.4f)
                    while (u < uMax - cross * 0.3f) {
                        addStreet(wavy(Vec(u, vMin - reach), Vec(u, vMax + reach), plan.wiggle), plan.laneRoad)
                        u += cross
                    }
                }
                StreetPattern.CROOKED -> {
                    var v = vMin + spacing * (0.5f + random.nextFloat() * 0.4f)
                    while (v < vMax - spacing * 0.3f) {
                        streetRows.add(v)
                        v += spacing * (0.75f + random.nextFloat() * 0.5f)
                    }
                    val edges = listOf(vMin - reach) + streetRows + listOf(vMax + reach)
                    for (row in streetRows) {
                        // Продольный переулок местами обрывается тупиком.
                        var u = uMin - reach
                        while (u < uMax + reach) {
                            val piece = cross * (1.2f + random.nextFloat() * 2f)
                            if (random.nextFloat() >= plan.deadEnds * 0.5f) {
                                addStreet(wavy(Vec(u, row), Vec(u + piece, row), plan.wiggle), plan.mainRoad)
                            }
                            u += piece
                        }
                    }
                    // Поперечные проходы в каждом ряду кварталов свои — сдвинуты, часть тупиковые.
                    for (i in 0 until edges.size - 1) {
                        var u = uMin + cross * random.nextFloat()
                        while (u < uMax) {
                            if (random.nextFloat() >= plan.deadEnds) {
                                val top = edges[i]
                                val bottom = edges[i + 1]
                                val end = if (random.nextFloat() < plan.deadEnds) top + (bottom - top) * 0.55f else bottom
                                addStreet(wavy(Vec(u, top), Vec(u + (random.nextFloat() - 0.5f) * unit, end), plan.wiggle), plan.laneRoad)
                            }
                            u += cross * (0.6f + random.nextFloat() * 0.8f)
                        }
                    }
                }
                StreetPattern.AVENUE -> {
                    streetRows.add(0f)
                    addStreet(listOf(Vec(uMin - reach, 0f), Vec(uMax + reach, 0f)), plan.mainRoad)
                    var k = 1
                    while (k * spacing < max(-vMin, vMax)) {
                        for (sign in listOf(-1f, 1f)) {
                            val v = sign * k * spacing
                            if (v > vMin && v < vMax) {
                                streetRows.add(v)
                                addStreet(listOf(Vec(uMin - reach, v), Vec(uMax + reach, v)), plan.laneRoad)
                            }
                        }
                        k++
                    }
                    var j = 1
                    while (j * cross < max(-uMin, uMax)) {
                        for (sign in listOf(-1f, 1f)) {
                            val u = sign * j * cross
                            addStreet(listOf(Vec(u, vMin - reach), Vec(u, vMax + reach)), plan.laneRoad)
                        }
                        j++
                    }
                    streetRows.sort()
                }
                StreetPattern.RADIAL -> {
                    val radius = max(max(-uMin, uMax), max(-vMin, vMax)) + reach
                    val start = unit * plan.landmarkScale * 1.1f
                    val turn = random.nextFloat() * 6.28f
                    val spokes = 6 + random.nextInt(3)
                    for (i in 0 until spokes) {
                        val a = turn + i * 2f * PI.toFloat() / spokes
                        val from = Vec(cos(a) * start, sin(a) * start)
                        val to = Vec(cos(a) * radius, sin(a) * radius)
                        addStreet(wavy(from, to, plan.wiggle), if (i % 2 == 0) plan.mainRoad else plan.laneRoad)
                    }
                    val ring = min(min(-uMin, uMax), min(-vMin, vMax)) * 0.55f
                    if (ring > start * 1.4f) {
                        val points = List(33) { i ->
                            val a = i * 2f * PI.toFloat() / 32
                            Vec(cos(a) * ring, sin(a) * ring)
                        }
                        addStreet(points, plan.laneRoad)
                    }
                }
                StreetPattern.PATHS -> {
                    val radius = max(max(-uMin, uMax), max(-vMin, vMax)) + reach
                    val count = 2 + random.nextInt(2)
                    for (i in 0 until count) {
                        val a = random.nextFloat() * PI.toFloat()
                        val shift = (random.nextFloat() - 0.5f) * unit * 4f
                        val from = Vec(-cos(a) * radius - sin(a) * shift, -sin(a) * radius + cos(a) * shift)
                        val to = Vec(cos(a) * radius - sin(a) * shift, sin(a) * radius + cos(a) * shift)
                        addStreet(wavy(from, to, plan.wiggle), plan.laneRoad)
                    }
                }
                StreetPattern.NONE -> Unit
            }
        }
        allRoads = project.roads + roads

        var guard = 0

        /** Один ряд домов вдоль улицы. */
        fun fillRow(v: Float, behind: Float) {
            if (plan.mirror && v > 0f) return
            var u = uMin + random.nextFloat() * gap
            while (u < uMax && guard < GUARD_LIMIT) {
                guard++
                if (random.nextFloat() < skipChance) {
                    u += unit * (0.7f + random.nextFloat() * 1.8f)
                    continue
                }
                val type = pickType()
                val (width, depth) = houseSize(type)
                val jitterU = (random.nextFloat() - 0.5f) * unit * plan.jitter
                val jitterV = (random.nextFloat() - 0.5f) * unit * plan.jitter
                val tilt = (random.nextFloat() - 0.5f) * 2f * plan.tilt
                // v — линия фасадов: дома стоят лицом к улице, вглубь квартала.
                placeMaybeMirrored(type, u + width * 0.5f + jitterU, v + behind * depth * 0.5f + jitterV, width, depth, tilt, behind)
                u += width + gap * (0.7f + random.nextFloat() * 0.8f)
            }
        }

        when {
            plan.clusters -> {
                // Кучки лачуг: вокруг случайных середин, вкривь и вкось.
                val area = (uMax - uMin) * (vMax - vMin)
                val count = (area / (unit * unit * 18f) * (0.6f + density * 0.8f)).toInt().coerceIn(3, 500)
                repeat(count) {
                    val cu = uMin + random.nextFloat() * (uMax - uMin)
                    val cv = vMin + random.nextFloat() * (vMax - vMin)
                    val huts = 4 + random.nextInt(9)
                    repeat(huts) {
                        if (guard++ > GUARD_LIMIT) return@repeat
                        val type = pickType()
                        val (width, depth) = houseSize(type)
                        val du = (random.nextGaussian().toFloat()) * unit * 1.3f
                        val dv = (random.nextGaussian().toFloat()) * unit * 1.3f
                        val tilt = (random.nextFloat() - 0.5f) * 2f * plan.tilt
                        place(type, cu + du, cv + dv, width, depth, tilt, 1f)
                    }
                }
            }
            streetRows.isNotEmpty() -> {
                // Ряды домов с двух сторон каждой улицы, между ними — задние дворы.
                val lines = streetRows.sorted()
                // Отступ ряда от оси улицы: полширины улицы, запас проверки,
                // кривизна улицы и разброс дома — иначе ряд целиком отбраковывается.
                val road = max(plan.laneRoad.width, plan.mainRoad.width) * 0.5f + unit * 0.06f +
                    plan.wiggle * unit * 1.25f + plan.jitter * unit * 0.5f
                val edges = listOf(vMin) + lines + listOf(vMax)
                for (i in 0 until edges.size - 1) {
                    val top = edges[i]
                    val bottom = edges[i + 1]
                    val topRoad = if (i > 0) road + unit * plan.setback else unit * 0.1f
                    val bottomRoad = if (i < edges.size - 2) road + unit * plan.setback else unit * 0.1f
                    val first = top + topRoad
                    val last = bottom - bottomRoad
                    if (last - first < unit * 0.5f) continue
                    fillRow(first, 1f)
                    if (last - first > unit * 1.9f) fillRow(last, -1f)
                    var v = first + rowStep
                    while (v + unit * 1.1f < last - rowStep * 0.5f && guard < GUARD_LIMIT) {
                        fillRow(v, 1f)
                        v += rowStep
                    }
                }
            }
            else -> {
                var v = vMin + rowStep * 0.5f
                while (v < vMax && guard < GUARD_LIMIT) {
                    fillRow(v, 1f)
                    v += rowStep
                }
            }
        }
        return DistrictFill(placed, roads, gardens)
    }

    /** Итог соединения улиц: новые улицы, снесённые на пути дома, число связок. */
    data class StreetLinks(
        val roads: List<Road>,
        val removedBuildings: Set<String>,
        val links: Int
    )

    private class StreetEnd(val road: Int, val atStart: Boolean, val point: Vec, val dirX: Float, val dirY: Float)

    /**
     * Соединить улицы соседних кварталов на их границе.
     *
     * Трогаются только оборванные концы улиц рядом с границей квартала:
     * конец дотягивается до конца соседней улицы или упирается в ближайшую
     * улицу по ту сторону. Длина связки ограничена, так что дальние улицы
     * не перекраиваются; сносятся только дома, стоящие прямо на связке.
     */
    fun connectStreets(project: MapProject): StreetLinks {
        val roads = project.roads.toMutableList()
        val unit = min(project.worldWidth, project.worldHeight) * 0.0165f
        val maxGap = unit * 5f
        val borders = project.districts.flatMap { it.contours() }.filter { it.size >= 3 }

        fun nearBorder(p: Vec): Boolean =
            borders.isEmpty() || borders.any { Geometry.distanceToPolygonOutline(p, it) <= maxGap }

        // Оборванные концы: рядом нет другой улицы.
        val ends = ArrayList<StreetEnd>()
        for ((index, road) in roads.withIndex()) {
            val pts = road.points
            if (pts.size < 2) continue
            for (atStart in listOf(true, false)) {
                val p = if (atStart) pts.first() else pts.last()
                val q = if (atStart) pts[1] else pts[pts.size - 2]
                val touching = roads.withIndex().any { (other, candidate) ->
                    other != index && candidate.points.size >= 2 &&
                        Geometry.distanceToPolyline(p, candidate.points) <= candidate.type.width * 0.5f + road.type.width
                }
                if (touching || !nearBorder(p)) continue
                val dx = p.x - q.x
                val dy = p.y - q.y
                val len = max(0.0001f, sqrt(dx * dx + dy * dy))
                ends.add(StreetEnd(index, atStart, p, dx / len, dy / len))
            }
        }

        fun facing(end: StreetEnd, target: Vec): Float {
            val dx = target.x - end.point.x
            val dy = target.y - end.point.y
            val len = max(0.0001f, sqrt(dx * dx + dy * dy))
            return (dx * end.dirX + dy * end.dirY) / len
        }

        val links = ArrayList<Pair<StreetEnd, Vec>>()
        val used = HashSet<StreetEnd>()

        // 1. Конец к концу: две улицы смотрят друг на друга через границу.
        data class Candidate(val a: StreetEnd, val b: StreetEnd, val score: Float)
        val pairs = ArrayList<Candidate>()
        for (i in ends.indices) {
            for (j in i + 1 until ends.size) {
                val a = ends[i]
                val b = ends[j]
                if (a.road == b.road) continue
                val distance = a.point.distanceTo(b.point)
                if (distance > maxGap) continue
                val fa = facing(a, b.point)
                val fb = facing(b, a.point)
                // Совсем рядом — соединяем и вбок, издалека — только если смотрят друг на друга.
                val close = distance < unit * 2.5f
                if (close && (fa < -0.3f || fb < -0.3f)) continue
                if (!close && (fa < 0.1f || fb < 0.1f)) continue
                pairs.add(Candidate(a, b, distance * (2.2f - fa - fb)))
            }
        }
        for (candidate in pairs.sortedBy { it.score }) {
            if (candidate.a in used || candidate.b in used) continue
            used.add(candidate.a)
            used.add(candidate.b)
            links.add(candidate.a to candidate.b.point)
        }

        // 2. Конец упирается в ближайшую улицу: перекрёсток буквой Т.
        for (end in ends) {
            if (end in used) continue
            var best: Vec? = null
            var bestDistance = maxGap * 0.8f
            for ((index, road) in roads.withIndex()) {
                if (index == end.road || road.points.size < 2) continue
                for (k in 0 until road.points.size - 1) {
                    val q = closestOnSegment(end.point, road.points[k], road.points[k + 1])
                    val distance = end.point.distanceTo(q)
                    if (distance < bestDistance && facing(end, q) > 0.35f) {
                        bestDistance = distance
                        best = q
                    }
                }
            }
            val target = best ?: continue
            used.add(end)
            links.add(end to target)
        }

        // Удлинить улицы и снести дома, стоящие прямо на связке.
        val removed = HashSet<String>()
        for ((end, target) in links) {
            val road = roads[end.road]
            val points = road.points.toMutableList()
            if (end.atStart) points.add(0, target) else points.add(target)
            roads[end.road] = road.copy(points = points)
            for (building in project.buildings) {
                if (building.id in removed || building.points.size < 3) continue
                if (segmentHitsPolygon(end.point, target, building.points)) removed.add(building.id)
            }
        }
        return StreetLinks(roads, removed, links.size)
    }

    private fun closestOnSegment(p: Vec, a: Vec, b: Vec): Vec {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val lengthSq = dx * dx + dy * dy
        if (lengthSq < 0.0001f) return a
        val t = (((p.x - a.x) * dx + (p.y - a.y) * dy) / lengthSq).coerceIn(0f, 1f)
        return Vec(a.x + dx * t, a.y + dy * t)
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

    private fun segmentsCross(a: Vec, b: Vec, c: Vec, d: Vec): Boolean {
        fun side(o: Vec, p: Vec, q: Vec) = (p.x - o.x) * (q.y - o.y) - (p.y - o.y) * (q.x - o.x)
        val d1 = side(c, d, a)
        val d2 = side(c, d, b)
        val d3 = side(a, b, c)
        val d4 = side(a, b, d)
        return (d1 > 0f) != (d2 > 0f) && (d3 > 0f) != (d4 > 0f)
    }

    /** Отрезок пересекает многоугольник или лежит внутри него. */
    private fun segmentHitsPolygon(a: Vec, b: Vec, polygon: List<Vec>): Boolean {
        if (Geometry.pointInPolygon(a, polygon) || Geometry.pointInPolygon(b, polygon)) return true
        for (j in polygon.indices) {
            if (segmentsCross(a, b, polygon[j], polygon[(j + 1) % polygon.size])) return true
        }
        return false
    }

    /**
     * Мешает ли что-то дому: улица проходит сквозь него или ближе половины
     * своей ширины, стена с башнями, вода. Проверяется сам след дома,
     * а не круг вокруг него, — длинные дома встают вплотную к улице.
     */
    private fun blocked(roads: List<Road>, project: MapProject, footprint: List<Vec>, margin: Float): Boolean {
        val box = Geometry.bounds(footprint)
        fun near(a: Vec, b: Vec, clearance: Float): Boolean =
            !(max(a.x, b.x) < box.minX - clearance || min(a.x, b.x) > box.maxX + clearance ||
                max(a.y, b.y) < box.minY - clearance || min(a.y, b.y) > box.maxY + clearance)

        fun hits(points: List<Vec>, clearance: Float): Boolean {
            for (i in 0 until points.size - 1) {
                val a = points[i]
                val b = points[i + 1]
                if (!near(a, b, clearance)) continue
                if (segmentHitsPolygon(a, b, footprint)) return true
                for (corner in footprint) {
                    if (Geometry.distanceToSegment(corner, a, b) < clearance) return true
                }
            }
            return false
        }

        for (road in roads) {
            if (hits(road.points, road.type.width * 0.5f + margin)) return true
        }
        for (feature in project.lines) {
            // У стен по бокам башни — держимся подальше.
            if (hits(feature.points, feature.effectiveWidth * 1.6f + margin)) return true
        }
        for (water in project.waters) {
            if (footprint.any { Geometry.pointInPolygon(it, water.points) }) return true
            if (water.points.any { Geometry.pointInPolygon(it, footprint) }) return true
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
            BuildingType.AUCTION_HOUSE, BuildingType.BLACK_MARKET,
            BuildingType.SHOP, BuildingType.SHOP, BuildingType.SHOP, BuildingType.HOUSE,
            BuildingType.TAVERN, BuildingType.WAREHOUSE, BuildingType.BAKERY,
            BuildingType.BUTCHER, BuildingType.SPICE_SHOP, BuildingType.BANK_HOUSE,
            BuildingType.TALL_HOUSE, BuildingType.MARKET_HALL, BuildingType.INN_HOUSE,
            BuildingType.CUSTOMS, BuildingType.GUILD_HOUSE
        )
        DistrictType.CRAFT_QUARTER -> listOf(
            BuildingType.DWARVEN_FORGE, BuildingType.LOCKSMITH, BuildingType.BOWYER, BuildingType.TAILOR,
            BuildingType.SMITHY, BuildingType.POTTERY, BuildingType.WEAVER, BuildingType.HOUSE,
            BuildingType.CARPENTER, BuildingType.TANNERY, BuildingType.DYER,
            BuildingType.ARMOURER, BuildingType.STONECUTTER, BuildingType.GLASSBLOWER,
            BuildingType.BREWERY, BuildingType.HOUSE, BuildingType.GUILD_HOUSE
        )
        DistrictType.TEMPLE_QUARTER -> listOf(
            BuildingType.SUN_TEMPLE, BuildingType.ALMSHOUSE,
            BuildingType.CHAPEL, BuildingType.SHRINE_HOUSE, BuildingType.CHURCH,
            BuildingType.MONASTERY_HOUSE, BuildingType.HEALER, BuildingType.OLD_TEMPLE,
            BuildingType.BELL_TOWER, BuildingType.HOUSE, BuildingType.CRYPT_HOUSE,
            BuildingType.HOSPITAL
        )
        DistrictType.NOBLE_QUARTER -> listOf(
            BuildingType.MANOR, BuildingType.RICH_HOUSE, BuildingType.RICH_HOUSE,
            BuildingType.MANOR, BuildingType.RICH_HOUSE, BuildingType.TOWER_HOUSE,
            BuildingType.JEWELLER, BuildingType.STABLE, BuildingType.BATH_HOUSE_CITY,
            BuildingType.OPERA_HOUSE, BuildingType.EMBASSY, BuildingType.VAMPIRE_MANOR
        )
        DistrictType.POOR_QUARTER -> listOf(
            BuildingType.HOUSE, BuildingType.TENEMENT, BuildingType.HOUSE,
            BuildingType.TENEMENT, BuildingType.HOUSE, BuildingType.TENEMENT,
            BuildingType.TAVERN, BuildingType.BAKERY, BuildingType.SHOP,
            BuildingType.ORPHANAGE, BuildingType.ALMSHOUSE
        )
        DistrictType.SLUMS -> listOf(
            BuildingType.HUT, BuildingType.HUT, BuildingType.HUT, BuildingType.HUT,
            BuildingType.SHACK_ROW, BuildingType.BURNT_HOUSE, BuildingType.RUINED_HOUSE,
            BuildingType.ABANDONED_HOUSE, BuildingType.PLAGUE_HOUSE, BuildingType.HUT,
            BuildingType.SMUGGLERS_DEN, BuildingType.HAUNTED_HOUSE
        )
        DistrictType.HARBOUR_QUARTER -> listOf(
            BuildingType.SMUGGLERS_DEN, BuildingType.AIRSHIP_DOCK,
            BuildingType.WAREHOUSE, BuildingType.DOCK_HOUSE, BuildingType.TAVERN,
            BuildingType.HOUSE, BuildingType.FISH_MARKET, BuildingType.SHIPYARD_HOUSE,
            BuildingType.WAREHOUSE, BuildingType.INN_HOUSE, BuildingType.CUSTOMS,
            BuildingType.LIGHTHOUSE_HOUSE
        )
        DistrictType.GARRISON_QUARTER -> listOf(
            BuildingType.ARSENAL, BuildingType.GRIFFON_AERIE,
            BuildingType.BARRACKS_HOUSE, BuildingType.GUARD_HOUSE, BuildingType.STABLE,
            BuildingType.SMITHY, BuildingType.ARMOURER, BuildingType.GRANARY,
            BuildingType.WATCH_TOWER_HOUSE, BuildingType.PRISON_HOUSE, BuildingType.HOUSE
        )
        DistrictType.SCHOLAR_QUARTER -> listOf(
            BuildingType.STAR_DOME, BuildingType.GREENHOUSE, BuildingType.CARTOGRAPHER, BuildingType.ASYLUM,
            BuildingType.SCHOOL, BuildingType.LIBRARY_HOUSE, BuildingType.HOUSE,
            BuildingType.APOTHECARY, BuildingType.RICH_HOUSE, BuildingType.OBSERVATORY_HOUSE,
            BuildingType.HEALER, BuildingType.TALL_HOUSE
        )
        DistrictType.MAGIC_QUARTER -> listOf(
            BuildingType.WITCH_HOUSE, BuildingType.GOLEM_WORKSHOP, BuildingType.ALCHEMY_LAB, BuildingType.PORTAL_HALL,
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
            BuildingType.MENAGERIE, BuildingType.FIGHTING_PIT,
            BuildingType.THEATRE_HOUSE, BuildingType.PUPPET_THEATRE, BuildingType.DANCE_HOUSE,
            BuildingType.MUSIC_HALL, BuildingType.GAMBLING_DEN, BuildingType.TAVERN,
            BuildingType.BATH_HOUSE_CITY, BuildingType.STORYTELLER_STAGE,
            BuildingType.AMPHITHEATRE, BuildingType.CONCERT_HALL, BuildingType.CIRCUS_TENT,
            BuildingType.PLEASURE_HOUSE, BuildingType.COCKPIT, BuildingType.INN_HOUSE
        )
        DistrictType.PALACE_QUARTER -> listOf(
            BuildingType.GUARD_HOUSE, BuildingType.TREASURY, BuildingType.HALL_OF_FAME,
            BuildingType.ARSENAL, BuildingType.BARRACKS_HOUSE, BuildingType.GUARD_HOUSE,
            BuildingType.STABLE, BuildingType.CART_YARD, BuildingType.GREENHOUSE,
            BuildingType.CHAPEL, BuildingType.EMBASSY
        )
        DistrictType.GUILD_QUARTER -> listOf(
            BuildingType.GUILD_HOUSE, BuildingType.MAGES_GUILD, BuildingType.MERCENARY_HALL,
            BuildingType.ADVENTURERS_GUILD, BuildingType.AUCTION_HOUSE, BuildingType.CARTOGRAPHER,
            BuildingType.LOCKSMITH, BuildingType.BOWYER, BuildingType.TAILOR,
            BuildingType.CANDLE_MAKER, BuildingType.MONSTER_HUNTERS, BuildingType.TAVERN,
            BuildingType.BARD_COLLEGE, BuildingType.HOUSE
        )
        DistrictType.FARM_QUARTER -> listOf(
            BuildingType.FARMSTEAD_HOUSE, BuildingType.GRANARY, BuildingType.HUT,
            BuildingType.STABLE, BuildingType.MILL_HOUSE, BuildingType.DOVECOTE,
            BuildingType.HOUSE
        )
        DistrictType.GRAVE_QUARTER -> listOf(
            BuildingType.DEATH_TEMPLE,
            BuildingType.CRYPT_HOUSE, BuildingType.CHAPEL, BuildingType.CRYPT_HOUSE,
            BuildingType.SHRINE_HOUSE, BuildingType.PLAGUE_HOUSE
        )
        DistrictType.PARK_QUARTER -> listOf(
            BuildingType.TREEHOUSE, BuildingType.GREENHOUSE,
            BuildingType.BATH_HOUSE_CITY, BuildingType.RICH_HOUSE, BuildingType.DOVECOTE,
            BuildingType.STORYTELLER_STAGE, BuildingType.SHRINE_HOUSE
        )
    }
}
