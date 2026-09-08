package com.fantasymap.creator.geom

import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import com.fantasymap.creator.model.BiomeRegion
import com.fantasymap.creator.model.Geometry
import com.fantasymap.creator.model.Vec

/** Итог выравнивания границ. */
data class AlignResult(
    val regions: List<BiomeRegion>,
    val merged: Int,
    val trimmed: Int,
    val removed: Int
) {
    val changed: Boolean get() = merged > 0 || trimmed > 0 || removed > 0
}

/**
 * Булевы операции над областями карты.
 * Используется системный клиппер Android (Path.op), результат снова
 * превращается в наборы точек.
 */
object PolygonOps {

    /** Собрать путь из контуров. Правило чётности: вложенный контур — дыра. */
    fun buildPath(contours: List<List<Vec>>, out: Path = Path()): Path {
        out.reset()
        out.fillType = Path.FillType.EVEN_ODD
        for (contour in contours) {
            if (contour.size < 3) continue
            out.moveTo(contour[0].x, contour[0].y)
            for (i in 1 until contour.size) out.lineTo(contour[i].x, contour[i].y)
            out.close()
        }
        return out
    }

    /** Разобрать путь обратно на контуры, отбросив слишком мелкие обрезки. */
    fun contoursOf(path: Path, minArea: Float): List<List<Vec>> {
        val result = ArrayList<List<Vec>>()
        val measure = PathMeasure(path, true)
        val position = FloatArray(2)
        do {
            val length = measure.length
            if (length > MIN_CONTOUR_LENGTH) {
                val count = (length / SAMPLE_STEP).toInt().coerceIn(12, 600)
                val points = ArrayList<Vec>(count)
                for (i in 0 until count) {
                    if (measure.getPosTan(length * i / count, position, null)) {
                        points.add(Vec(position[0], position[1]))
                    }
                }
                val simplified = Geometry.simplify(points, SIMPLIFY_TOLERANCE)
                if (simplified.size >= 3 && Geometry.area(simplified) >= minArea) {
                    result.add(simplified)
                }
            }
        } while (measure.nextContour())
        return result
    }

    /**
     * Выровнять границы зон целиком:
     * сначала одинаковые соседние зоны сливаются в одну,
     * затем у разных зон убираются наложения.
     */
    fun alignZones(regions: List<BiomeRegion>, minArea: Float, touchTolerance: Float): AlignResult {
        val merge = mergeSameBiome(regions, minArea, touchTolerance)
        val cut = resolveOverlaps(merge.first, minArea)
        return AlignResult(
            regions = cut.regions,
            merged = merge.second,
            trimmed = cut.trimmed,
            removed = cut.removed
        )
    }

    /**
     * Слить соседние области одного и того же ландшафта в одну.
     * Предгорья рядом с предгорьями станут одной зоной, а предгорья
     * рядом с горами останутся двумя — граница между ними сохранится.
     *
     * Возвращает новый список и число слияний.
     */
    fun mergeSameBiome(
        regions: List<BiomeRegion>,
        minArea: Float,
        touchTolerance: Float
    ): Pair<List<BiomeRegion>, Int> {
        val count = regions.size
        if (count < 2) return regions to 0

        val contours = regions.map { it.contours().filter { contour -> contour.size >= 3 } }
        val bounds = contours.map { Geometry.bounds(it.flatten()) }
        val paths = contours.map { buildPath(it) }
        val grown = arrayOfNulls<Path>(count)
        val weld = touchTolerance * 0.55f

        fun grownPath(index: Int): Path {
            grown[index]?.let { return it }
            val created = inflated(paths[index], weld)
            grown[index] = created
            return created
        }

        val parent = IntArray(count) { it }
        fun root(start: Int): Int {
            var node = start
            while (parent[node] != node) {
                parent[node] = parent[parent[node]]
                node = parent[node]
            }
            return node
        }

        var merges = 0
        for (i in 0 until count) {
            if (contours[i].isEmpty()) continue
            for (j in i + 1 until count) {
                if (contours[j].isEmpty()) continue
                if (regions[i].biome != regions[j].biome) continue
                if (namedApart(regions[i].name, regions[j].name)) continue
                val rootI = root(i)
                val rootJ = root(j)
                if (rootI == rootJ) continue
                if (!bounds[i].expand(touchTolerance).intersects(bounds[j])) continue
                if (!touching(paths[i], grownPath(i), paths[j], touchTolerance)) continue
                parent[rootJ] = rootI
                merges++
            }
        }
        if (merges == 0) return regions to 0

        val groups = LinkedHashMap<Int, MutableList<Int>>()
        for (index in 0 until count) {
            if (contours[index].isEmpty()) continue
            groups.getOrPut(root(index)) { ArrayList() }.add(index)
        }

        val result = ArrayList<BiomeRegion>(groups.size)
        for ((_, members) in groups) {
            if (members.size == 1) {
                result.add(regions[members[0]])
                continue
            }
            // Сначала пробуем обычное объединение: если области налегают друг на
            // друга, шва не будет. Если остался зазор, объединяем чуть раздутые.
            val union = Path(paths[members[0]])
            for (k in 1 until members.size) union.op(paths[members[k]], Path.Op.UNION)
            var pieces = contoursOf(union, minArea)
            if (pieces.size > 1) {
                val welded = Path(grownPath(members[0]))
                for (k in 1 until members.size) welded.op(grownPath(members[k]), Path.Op.UNION)
                val weldedPieces = contoursOf(welded, minArea)
                if (weldedPieces.isNotEmpty() && weldedPieces.size < pieces.size) pieces = weldedPieces
            }
            val base = regions[members[0]]
            if (pieces.isEmpty()) {
                result.add(base)
                continue
            }
            val name = members.map { regions[it].name }.firstOrNull { it.isNotBlank() }.orEmpty()
            result.add(base.copy(name = name, points = pieces.first(), extraContours = pieces.drop(1)))
        }
        return result to merges
    }

    /** Области с разными собственными названиями не сливаем — их назвали по отдельности. */
    private fun namedApart(first: String, second: String): Boolean =
        first.isNotBlank() && second.isNotBlank() && !first.equals(second, ignoreCase = true)

    /** Области налегают друг на друга или их границы сходятся ближе tolerance. */
    private fun touching(a: Path, grownA: Path, b: Path, tolerance: Float): Boolean {
        val direct = Path(a)
        if (direct.op(b, Path.Op.INTERSECT) && !direct.isEmpty) return true
        if (tolerance <= 0f) return false
        val near = Path(grownA)
        return near.op(b, Path.Op.INTERSECT) && !near.isEmpty
    }

    /** Раздуть область наружу на amount — чтобы «сварить» тонкие щели между соседями. */
    private fun inflated(path: Path, amount: Float): Path {
        val result = Path(path)
        if (amount <= 0f) return result
        val band = Path()
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = amount * 2f
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.getFillPath(path, band)
        result.op(band, Path.Op.UNION)
        return result
    }

    /**
     * Убрать наложения природных зон: у каждой области вырезается всё, что
     * закрыто нарисованными позже. Общая граница двух зон становится одной линией.
     *
     * Порядок в списке — порядок рисования: последняя нарисованная область главнее.
     */
    fun resolveOverlaps(regions: List<BiomeRegion>, minArea: Float): AlignResult {
        if (regions.size < 2) return AlignResult(regions, 0, 0, 0)

        val result = arrayOfNulls<BiomeRegion>(regions.size)
        val covered = Path()
        var trimmed = 0
        var removed = 0

        for (index in regions.indices.reversed()) {
            val region = regions[index]
            val contours = region.contours().filter { it.size >= 3 }
            if (contours.isEmpty()) {
                removed++
                continue
            }
            val regionPath = buildPath(contours)

            if (index == regions.lastIndex) {
                result[index] = region
            } else {
                val probe = Path(regionPath)
                val overlaps = probe.op(covered, Path.Op.INTERSECT) && !probe.isEmpty
                if (!overlaps) {
                    result[index] = region
                } else {
                    val cut = Path(regionPath)
                    if (!cut.op(covered, Path.Op.DIFFERENCE)) {
                        result[index] = region
                    } else {
                        val pieces = contoursOf(cut, minArea)
                        if (pieces.isEmpty()) {
                            removed++
                        } else {
                            result[index] = region.copy(
                                points = pieces.first(),
                                extraContours = pieces.drop(1)
                            )
                            trimmed++
                        }
                    }
                }
            }
            covered.op(regionPath, Path.Op.UNION)
        }

        return AlignResult(result.filterNotNull(), 0, trimmed, removed)
    }

    private const val SAMPLE_STEP = 3f
    private const val MIN_CONTOUR_LENGTH = 6f
    private const val SIMPLIFY_TOLERANCE = 1.2f
}
