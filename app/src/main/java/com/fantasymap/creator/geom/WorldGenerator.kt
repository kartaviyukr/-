package com.fantasymap.creator.geom

import android.graphics.Paint
import android.graphics.Path
import com.fantasymap.creator.model.BBox
import com.fantasymap.creator.model.BiomeRegion
import com.fantasymap.creator.model.BiomeType
import com.fantasymap.creator.model.Geometry
import com.fantasymap.creator.model.LandKind
import com.fantasymap.creator.model.Landmass
import com.fantasymap.creator.model.LineFeature
import com.fantasymap.creator.model.LineFeatureType
import com.fantasymap.creator.model.MapProject
import com.fantasymap.creator.model.MarkerType
import com.fantasymap.creator.model.Vec
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Помощники, которые заполняют выделенную область карты:
 * рваное побережье, реки от гор к морю и природные зоны по широте.
 * Всё делается только внутри выделения — остальная карта не трогается.
 */
object WorldGenerator {

    // ------------------------------------------------------------ побережье

    /**
     * Рваный контур материка, вписанный в область.
     * roughness 0 — почти ровный овал, 1 — сильно изрезанный берег.
     */
    fun coastline(rect: BBox, roughness: Float, seed: Int): List<Vec> {
        val centerX = rect.centerX
        val centerY = rect.centerY
        val radiusX = rect.width * 0.44f
        val radiusY = rect.height * 0.44f
        val rough = roughness.coerceIn(0f, 1f)

        // Начальный контур: овал, «помятый» несколькими гармониками — он всегда замкнут.
        val start = 28
        var points = ArrayList<Vec>(start)
        for (i in 0 until start) {
            val angle = (2.0 * Math.PI * i / start).toFloat()
            var wave = 0f
            for (harmonic in 1..4) {
                val phase = Geometry.hashNoise(harmonic, 7, seed) * 6.283f
                wave += sin(angle * harmonic + phase) / harmonic
            }
            val factor = 1f - rough * 0.32f + wave * rough * 0.26f
            points.add(
                Vec(
                    centerX + cos(angle) * radiusX * factor,
                    centerY + sin(angle) * radiusY * factor
                )
            )
        }

        // Дробление рёбер со смещением середины — отсюда берутся заливы и мысы.
        repeat(3) { pass ->
            points = displace(points, rough * (0.34f / (pass + 1)), seed + pass * 31)
        }

        val smooth = Geometry.smoothClosed(points, 1)
        return smooth.map {
            Vec(
                it.x.coerceIn(rect.minX, rect.maxX),
                it.y.coerceIn(rect.minY, rect.maxY)
            )
        }
    }

    /** Несколько островов, разбросанных по области. */
    fun islands(rect: BBox, count: Int, roughness: Float, seed: Int): List<List<Vec>> {
        val result = ArrayList<List<Vec>>()
        for (index in 0 until count) {
            val nx = Geometry.hashNoise(index * 13 + 1, 5, seed)
            val ny = Geometry.hashNoise(index * 17 + 3, 9, seed)
            val ns = Geometry.hashNoise(index * 23 + 7, 11, seed)
            val width = rect.width * (0.16f + ns * 0.22f)
            val height = rect.height * (0.16f + (1f - ns) * 0.22f)
            val cx = rect.minX + width / 2f + nx * (rect.width - width)
            val cy = rect.minY + height / 2f + ny * (rect.height - height)
            val area = BBox(cx - width / 2f, cy - height / 2f, cx + width / 2f, cy + height / 2f)
            result.add(coastline(area, roughness, seed + index * 101))
        }
        return result
    }

    private fun displace(points: List<Vec>, amount: Float, seed: Int): ArrayList<Vec> {
        val result = ArrayList<Vec>(points.size * 2)
        for (i in points.indices) {
            val a = points[i]
            val b = points[(i + 1) % points.size]
            result.add(a)
            val dx = b.x - a.x
            val dy = b.y - a.y
            val length = sqrt(dx * dx + dy * dy)
            if (length < 1e-3f) continue
            // Середину ребра сдвигаем по нормали на долю его длины — так растут мысы и заливы.
            val shift = (Geometry.hashNoise(i * 37, (a.x + a.y).toInt(), seed) - 0.5f) * 2f * amount
            result.add(
                Vec(
                    (a.x + b.x) / 2f - dy * shift,
                    (a.y + b.y) / 2f + dx * shift
                )
            )
        }
        return result
    }

    // ------------------------------------------------------------ реки

    /**
     * Реки, стекающие от гор к ближайшей воде.
     * Исток берётся у горных хребтов, вершин и горных зон внутри области.
     */
    fun rivers(project: MapProject, rect: BBox, count: Int, seed: Int): List<LineFeature> {
        val sources = ArrayList<Vec>()
        for (feature in project.lines) {
            if (feature.type != LineFeatureType.MOUNTAIN_RANGE &&
                feature.type != LineFeatureType.HILL_RANGE &&
                feature.type != LineFeatureType.ICE_RIDGE
            ) {
                continue
            }
            for (point in Geometry.resample(feature.points, max(30f, rect.width * 0.08f))) {
                if (rect.contains(point)) sources.add(point)
            }
        }
        for (marker in project.markers) {
            val mountainous = marker.type == MarkerType.MOUNTAIN_PEAK ||
                marker.type == MarkerType.VOLCANO ||
                marker.type == MarkerType.MOUNTAIN_PASS ||
                marker.type == MarkerType.GLACIER_TONGUE
            if (mountainous && rect.contains(marker.pos)) sources.add(marker.pos)
        }
        for (region in project.biomes) {
            val mountainous = region.biome == BiomeType.HIGH_MOUNTAINS ||
                region.biome == BiomeType.MOUNTAINS ||
                region.biome == BiomeType.GLACIER
            if (!mountainous) continue
            val center = Geometry.centroid(region.points)
            if (rect.contains(center)) sources.add(center)
        }
        if (sources.isEmpty()) return emptyList()

        val step = max(6f, min(rect.width, rect.height) * 0.02f)
        val chosen = pickSpread(sources, count, seed)
        val result = ArrayList<LineFeature>()

        for ((index, source) in chosen.withIndex()) {
            val path = ArrayList<Vec>()
            var point = source
            var direction = downhill(project, point, seed + index * 53) ?: continue
            var guard = 0
            while (guard++ < 400) {
                path.add(point)
                if (!onLand(project, point)) break
                val wobble = (Geometry.hashNoise(guard * 7, index * 11, seed) - 0.5f) * 0.9f
                val angle = kotlin.math.atan2(direction.y, direction.x) + wobble * 0.35f
                point = Vec(point.x + cos(angle) * step, point.y + sin(angle) * step)
                if (point.x < 0f || point.y < 0f ||
                    point.x > project.worldWidth || point.y > project.worldHeight
                ) {
                    break
                }
                if (guard % 6 == 0) {
                    direction = downhill(project, point, seed + index * 53) ?: direction
                }
            }
            if (path.size < 4) continue
            val simplified = Geometry.simplify(path, step * 0.35f)
            val smooth = Geometry.smoothOpen(simplified, 2)
            result.add(LineFeature(type = LineFeatureType.RIVER, points = smooth))
        }
        return result
    }

    /** Направление к ближайшей воде — берегу или водоёму. */
    private fun downhill(project: MapProject, from: Vec, seed: Int): Vec? {
        var best: Vec? = null
        var bestDistance = Float.MAX_VALUE
        for (land in project.landmasses) {
            for (point in land.points) {
                val distance = from.distanceTo(point)
                if (distance in 1f..bestDistance) {
                    bestDistance = distance
                    best = point
                }
            }
        }
        for (water in project.waters) {
            val center = Geometry.centroid(water.points)
            val distance = from.distanceTo(center)
            if (distance in 1f..bestDistance) {
                bestDistance = distance
                best = center
            }
        }
        val target = best ?: return null
        val dx = target.x - from.x
        val dy = target.y - from.y
        val length = sqrt(dx * dx + dy * dy)
        if (length < 1e-3f) {
            val angle = Geometry.hashNoise(seed, 3, seed) * 6.283f
            return Vec(cos(angle), sin(angle))
        }
        return Vec(dx / length, dy / length)
    }

    private fun onLand(project: MapProject, point: Vec): Boolean {
        if (project.waters.any { Geometry.pointInPolygon(point, it.points) }) return false
        return project.landmasses.any { Geometry.pointInPolygon(point, it.points) }
    }

    /** Выбрать истоки подальше друг от друга, чтобы реки не слипались. */
    private fun pickSpread(points: List<Vec>, count: Int, seed: Int): List<Vec> {
        if (points.size <= count) return points
        val result = ArrayList<Vec>()
        val start = (Geometry.hashNoise(1, 2, seed) * points.size).toInt().coerceIn(0, points.size - 1)
        result.add(points[start])
        while (result.size < count) {
            var best: Vec? = null
            var bestDistance = -1f
            for (candidate in points) {
                val distance = result.minOf { it.distanceTo(candidate) }
                if (distance > bestDistance) {
                    bestDistance = distance
                    best = candidate
                }
            }
            result.add(best ?: break)
        }
        return result
    }

    // ------------------------------------------------------------ природные зоны

    /** Пояс природных зон: от экватора к полюсу, как на настоящей планете. */
    private val BANDS: List<Pair<Float, BiomeType>> = listOf(
        0.03f to BiomeType.RAINFOREST,
        0.07f to BiomeType.SAVANNA,
        0.13f to BiomeType.SAND_DESERT,
        0.19f to BiomeType.STEPPE,
        0.25f to BiomeType.BROADLEAF_FOREST,
        0.33f to BiomeType.MIXED_FOREST,
        0.40f to BiomeType.TAIGA,
        0.45f to BiomeType.TUNDRA,
        1f to BiomeType.ICE_SHEET
    )

    /**
     * Разложить природные зоны внутри области: широтные пояса, обрезанные по суше,
     * плюс горные зоны вдоль хребтов.
     */
    fun biomeBands(project: MapProject, rect: BBox, seed: Int): List<BiomeRegion> {
        val land = Path()
        for (mass in project.landmasses) {
            if (mass.points.size < 3) continue
            land.op(PolygonOps.buildPath(listOf(mass.points)), Path.Op.UNION)
        }
        if (land.isEmpty) return emptyList()
        val area = Path()
        area.addRect(rect.minX, rect.minY, rect.maxX, rect.maxY, Path.Direction.CW)
        land.op(area, Path.Op.INTERSECT)
        if (land.isEmpty) return emptyList()

        val minArea = max(40f, rect.width * rect.height * 0.0004f)
        val result = ArrayList<BiomeRegion>()
        val height = max(project.worldHeight, 1f)

        var previous = 0f
        for ((edge, biome) in BANDS) {
            val bandTop = previous
            previous = edge
            // Пояс идёт двумя полосами: к северу и к югу от экватора.
            for (side in intArrayOf(-1, 1)) {
                val fromLat = 0.5f + side * bandTop
                val toLat = 0.5f + side * min(edge, 0.5f)
                val y1 = min(fromLat, toLat) * height
                val y2 = max(fromLat, toLat) * height
                if (y2 < rect.minY || y1 > rect.maxY) continue
                val band = Path()
                band.addRect(rect.minX - 1f, y1, rect.maxX + 1f, y2, Path.Direction.CW)
                if (!band.op(land, Path.Op.INTERSECT)) continue
                if (band.isEmpty) continue
                val pieces = PolygonOps.contoursOf(band, minArea)
                if (pieces.isEmpty()) continue
                result.add(
                    BiomeRegion(
                        biome = biome,
                        points = pieces.first(),
                        extraContours = pieces.drop(1)
                    )
                )
            }
        }

        // Горы поверх поясов — по нарисованным хребтам.
        for (feature in project.lines) {
            val biome = when (feature.type) {
                LineFeatureType.MOUNTAIN_RANGE -> BiomeType.MOUNTAINS
                LineFeatureType.HILL_RANGE -> BiomeType.HILLS
                LineFeatureType.ICE_RIDGE -> BiomeType.GLACIER
                else -> null
            } ?: continue
            if (!Geometry.bounds(feature.points).intersects(rect)) continue
            val line = Path()
            val screen = feature.points
            line.moveTo(screen[0].x, screen[0].y)
            for (i in 1 until screen.size) line.lineTo(screen[i].x, screen[i].y)
            val band = Path()
            val paint = Paint()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = feature.effectiveWidth * 3.2f
            paint.strokeJoin = Paint.Join.ROUND
            paint.strokeCap = Paint.Cap.ROUND
            paint.getFillPath(line, band)
            if (!band.op(land, Path.Op.INTERSECT)) continue
            if (band.isEmpty) continue
            val pieces = PolygonOps.contoursOf(band, minArea)
            if (pieces.isEmpty()) continue
            result.add(
                BiomeRegion(
                    biome = biome,
                    points = pieces.first(),
                    extraContours = pieces.drop(1)
                )
            )
        }
        return result
    }

    /** Область целиком внутри выделения? Нужно, чтобы заменять только свои зоны. */
    fun insideRect(points: List<Vec>, rect: BBox): Boolean {
        if (points.isEmpty()) return false
        val bounds = Geometry.bounds(points)
        return bounds.minX >= rect.minX && bounds.maxX <= rect.maxX &&
            bounds.minY >= rect.minY && bounds.maxY <= rect.maxY
    }

    /** Материк или остров — по тому, велика ли получившаяся суша. */
    fun kindFor(points: List<Vec>, project: MapProject): LandKind {
        val share = Geometry.area(points) / max(1f, project.worldWidth * project.worldHeight)
        return if (share > 0.06f) LandKind.CONTINENT else LandKind.ISLAND
    }
}
