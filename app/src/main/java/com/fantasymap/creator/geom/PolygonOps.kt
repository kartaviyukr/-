package com.fantasymap.creator.geom

import android.graphics.Path
import android.graphics.PathMeasure
import com.fantasymap.creator.model.BiomeRegion
import com.fantasymap.creator.model.Geometry
import com.fantasymap.creator.model.Vec

/** Итог выравнивания границ. */
data class AlignResult(
    val regions: List<BiomeRegion>,
    val trimmed: Int,
    val removed: Int
) {
    val changed: Boolean get() = trimmed > 0 || removed > 0
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
     * Убрать наложения природных зон: у каждой области вырезается всё, что
     * закрыто нарисованными позже. Общая граница двух зон становится одной линией.
     *
     * Порядок в списке — порядок рисования: последняя нарисованная область главнее.
     */
    fun resolveOverlaps(regions: List<BiomeRegion>, minArea: Float): AlignResult {
        if (regions.size < 2) return AlignResult(regions, 0, 0)

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

        return AlignResult(result.filterNotNull(), trimmed, removed)
    }

    private const val SAMPLE_STEP = 3f
    private const val MIN_CONTOUR_LENGTH = 6f
    private const val SIMPLIFY_TOLERANCE = 1.2f
}
