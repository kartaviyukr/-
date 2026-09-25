package com.fantasymap.creator.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.fantasymap.creator.model.MarkerIcons
import com.fantasymap.creator.model.MarkerType

/**
 * Рисует собственные значки объектов из [MarkerIcons].
 * Фигуры один раз собираются в Path в единичных координатах и затем
 * рисуются с масштабом холста. Экземпляр не потокобезопасен.
 */
class MarkerIconPainter {

    private class Shape(val kind: Char, val path: Path?, val oval: RectF?, val start: Float = 0f, val sweep: Float = 0f)

    private val cache = HashMap<MarkerType, List<Shape>>()
    private val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    fun has(type: MarkerType): Boolean = MarkerIcons.source(type) != null

    /** Рисует значок; false — у объекта своего значка нет. */
    fun draw(canvas: Canvas, type: MarkerType, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint): Boolean {
        val shapes = cache[type] ?: run {
            val src = MarkerIcons.source(type) ?: return false
            build(MarkerIcons.parse(src)).also { cache[type] = it }
        }
        if (s <= 0f) return true
        val width = stroke.strokeWidth
        val join = stroke.strokeJoin
        val cap = stroke.strokeCap
        ink.color = stroke.color
        ink.alpha = stroke.alpha
        canvas.save()
        canvas.translate(cx, cy)
        canvas.scale(s, s)
        stroke.strokeWidth = width / s
        stroke.strokeJoin = Paint.Join.ROUND
        stroke.strokeCap = Paint.Cap.ROUND
        for (sh in shapes) {
            when (sh.kind) {
                'P', 'B' -> { canvas.drawPath(sh.path!!, fill); canvas.drawPath(sh.path, stroke) }
                'S' -> canvas.drawPath(sh.path!!, ink)
                'L', 'Z' -> canvas.drawPath(sh.path!!, stroke)
                'E' -> { canvas.drawOval(sh.oval!!, fill); canvas.drawOval(sh.oval, stroke) }
                'O' -> canvas.drawOval(sh.oval!!, stroke)
                'K' -> canvas.drawOval(sh.oval!!, ink)
                'A' -> canvas.drawArc(sh.oval!!, sh.start, sh.sweep, false, stroke)
            }
        }
        canvas.restore()
        stroke.strokeWidth = width
        stroke.strokeJoin = join
        stroke.strokeCap = cap
        return true
    }

    private fun build(ops: List<MarkerIcons.Op>): List<Shape> = ops.map { op ->
        val v = op.v
        when (op.kind) {
            'P', 'S', 'L' -> {
                val p = Path()
                p.moveTo(v[0], v[1])
                var i = 2
                while (i < v.size) { p.lineTo(v[i], v[i + 1]); i += 2 }
                if (op.kind != 'L') p.close()
                Shape(op.kind, p, null)
            }
            'Z' -> {
                val p = Path()
                val n = v.size / 2
                p.moveTo(v[0], v[1])
                for (i in 1 until n - 1) {
                    p.quadTo(v[2 * i], v[2 * i + 1], (v[2 * i] + v[2 * i + 2]) / 2f, (v[2 * i + 1] + v[2 * i + 3]) / 2f)
                }
                p.lineTo(v[2 * n - 2], v[2 * n - 1])
                Shape('Z', p, null)
            }
            'B' -> {
                val p = Path()
                val n = v.size / 2
                p.moveTo((v[2 * n - 2] + v[0]) / 2f, (v[2 * n - 1] + v[1]) / 2f)
                for (i in 0 until n) {
                    val j = (i + 1) % n
                    p.quadTo(v[2 * i], v[2 * i + 1], (v[2 * i] + v[2 * j]) / 2f, (v[2 * i + 1] + v[2 * j + 1]) / 2f)
                }
                p.close()
                Shape('B', p, null)
            }
            'A' -> Shape('A', null, RectF(v[0] - v[2], v[1] - v[3], v[0] + v[2], v[1] + v[3]), v[4], v[5])
            else -> Shape(op.kind, null, RectF(v[0] - v[2], v[1] - v[3], v[0] + v[2], v[1] + v[3]))
        }
    }
}
