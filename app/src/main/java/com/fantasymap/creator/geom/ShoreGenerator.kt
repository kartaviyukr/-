package com.fantasymap.creator.geom

import android.graphics.Paint
import android.graphics.Path
import com.fantasymap.creator.model.BiomeGroup
import com.fantasymap.creator.model.BiomeRegion
import com.fantasymap.creator.model.BiomeType
import com.fantasymap.creator.model.Geometry
import com.fantasymap.creator.model.LineFeatureType
import com.fantasymap.creator.model.MapKind
import com.fantasymap.creator.model.MapProject
import com.fantasymap.creator.model.Vec
import kotlin.math.max
import kotlin.math.min

/**
 * Естественные берега: узкая полоса суши вдоль всей воды на карте —
 * пляж, галька, скалы, камыши. Внутренняя кромка неровная, как у настоящего берега.
 */
object ShoreGenerator {

    /** Зоны, которые считаются водой. */
    private val WATER_ZONES = setOf(
        BiomeType.SHALLOW_SEA, BiomeType.DEEP_SEA, BiomeType.ABYSS, BiomeType.CORAL_REEF,
        BiomeType.KELP_FOREST, BiomeType.ICE_SEA, BiomeType.LAGOON, BiomeType.LAKELAND,
        BiomeType.CITY_POND, BiomeType.CITY_CANALS, BiomeType.SHALLOW_WATER, BiomeType.DEEP_WATER,
        BiomeType.SUNKEN_LANDS
    )

    /** Линии, вдоль которых тоже нужен берег (на карте города и боя). */
    private val WATER_LINES = setOf(
        LineFeatureType.RIVER, LineFeatureType.BIG_RIVER, LineFeatureType.STREAM,
        LineFeatureType.CANAL, LineFeatureType.MOAT, LineFeatureType.DITCH_WATER
    )

    /** Обычная ширина берега в единицах карты. */
    fun baseWidth(project: MapProject): Float {
        val side = min(project.worldWidth, project.worldHeight)
        return when (project.kind) {
            MapKind.WORLD -> side * 0.007f
            MapKind.CITY -> side * 0.012f
            MapKind.BATTLE -> project.gridCell * 0.9f
        }
    }

    fun generate(project: MapProject, shore: BiomeType, widthScale: Float, seed: Int): List<BiomeRegion> {
        val width = baseWidth(project) * widthScale.coerceIn(0.3f, 4f)
        val minArea = width * width * 0.25f

        val world = Path().apply {
            addRect(0f, 0f, project.worldWidth, project.worldHeight, Path.Direction.CW)
        }

        // Суша: материки или весь лист, если карта целиком суша или материков нет.
        val land = if (project.landBase || project.kind == MapKind.BATTLE || project.landmasses.isEmpty()) {
            Path(world)
        } else {
            val union = Path().apply { fillType = Path.FillType.WINDING }
            for (mass in project.landmasses) {
                if (mass.points.size >= 3) union.op(PolygonOps.buildPath(listOf(mass.points)), Path.Op.UNION)
            }
            union
        }

        // Вода: океан вокруг материков, озёра и моря, водные зоны, реки.
        val water = Path()
        if (!project.landBase && project.kind != MapKind.BATTLE && project.landmasses.isNotEmpty()) {
            val ocean = Path(world)
            ocean.op(land, Path.Op.DIFFERENCE)
            water.op(ocean, Path.Op.UNION)
        }
        for (body in project.waters) {
            if (body.points.size >= 3) water.op(PolygonOps.buildPath(listOf(body.points)), Path.Op.UNION)
        }
        for (region in project.biomes) {
            if (region.biome in WATER_ZONES) water.op(PolygonOps.buildPath(region.contours()), Path.Op.UNION)
        }
        if (project.kind != MapKind.WORLD) {
            val paint = Paint().apply {
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            for (line in project.lines) {
                if (line.type !in WATER_LINES || line.points.size < 2) continue
                val centre = Path()
                centre.moveTo(line.points[0].x, line.points[0].y)
                for (i in 1 until line.points.size) centre.lineTo(line.points[i].x, line.points[i].y)
                paint.strokeWidth = line.effectiveWidth * 1.2f
                val river = Path()
                paint.getFillPath(centre, river)
                water.op(river, Path.Op.UNION)
            }
        }
        if (water.isEmpty) return emptyList()

        // Неровная полоса: кружки разного размера вдоль кромки воды.
        val outlines = PolygonOps.contoursOf(water, width * width * 0.05f, width * 0.35f, 20000)
        val total = outlines.sumOf { ring -> ringLength(ring).toDouble() }.toFloat()
        val step = max(width * 0.45f, total / 5000f)
        val grown = Path().apply { fillType = Path.FillType.WINDING }
        for ((ringIndex, ring) in outlines.withIndex()) {
            val closed = ring + ring.first()
            val samples = Geometry.resample(closed, step)
            for ((i, p) in samples.withIndex()) {
                val radius = width * (0.5f + 0.9f * smoothNoise(i, ringIndex, seed))
                grown.addCircle(p.x, p.y, radius, Path.Direction.CW)
            }
        }

        val band = Path(grown)
        band.op(land, Path.Op.INTERSECT)
        band.op(water, Path.Op.DIFFERENCE)
        val contours = PolygonOps.contoursOf(band, minArea, max(0.5f, width * 0.2f), 30000)
        if (contours.isEmpty()) return emptyList()
        return listOf(
            BiomeRegion(
                biome = shore,
                points = contours.first(),
                extraContours = contours.drop(1)
            )
        )
    }

    /** Берега, которые рисует эта кнопка, — их можно заменить при повторе. */
    fun isShore(type: BiomeType): Boolean = type.group == BiomeGroup.SHORE

    private fun ringLength(ring: List<Vec>): Float {
        var length = 0f
        for (i in ring.indices) length += ring[i].distanceTo(ring[(i + 1) % ring.size])
        return length
    }

    /** Плавный шум вдоль кромки: берег то шире, то уже. */
    private fun smoothNoise(i: Int, ring: Int, seed: Int): Float {
        val period = 5
        val cell = i / period
        val t = (i % period).toFloat() / period
        val a = Geometry.hashNoise(cell, ring * 31, seed)
        val b = Geometry.hashNoise(cell + 1, ring * 31, seed)
        val smooth = t * t * (3f - 2f * t)
        return a + (b - a) * smooth
    }
}
