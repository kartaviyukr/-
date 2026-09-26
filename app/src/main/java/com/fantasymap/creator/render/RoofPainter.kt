package com.fantasymap.creator.render

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import com.fantasymap.creator.model.BuildingLook
import com.fantasymap.creator.model.BuildingType
import com.fantasymap.creator.model.RoofDetail
import com.fantasymap.creator.model.RoofForm
import com.fantasymap.creator.model.Vec
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Крыши построек сверху: фото-текстура материала, подкрашенная цветом постройки,
 * форма крыши (скаты, купол, шатёр, двор…) и деталь (трубы, шпиль, фонарь…).
 * Экземпляр не потокобезопасен.
 */
class RoofPainter(private val texture: (String) -> Bitmap?) {

    private val shaders = HashMap<Bitmap, BitmapShader>()
    private val matrix = Matrix()
    private val tex = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val path = Path()

    /** Каркас крыши: середина, длинная ось a, короткая ось b и полуразмеры. */
    private class Frame(val c: Vec, val a: Vec, val b: Vec, val l: Float, val w: Float, val corners: List<Vec>)

    private fun frameOf(points: List<Vec>): Frame {
        val cx = points.map { it.x }.average().toFloat()
        val cy = points.map { it.y }.average().toFloat()
        val c = Vec(cx, cy)
        if (points.size == 4) {
            val e1 = Vec(points[1].x - points[0].x, points[1].y - points[0].y)
            val e2 = Vec(points[2].x - points[1].x, points[2].y - points[1].y)
            val l1 = max(0.001f, sqrt(e1.x * e1.x + e1.y * e1.y))
            val l2 = max(0.001f, sqrt(e2.x * e2.x + e2.y * e2.y))
            return if (l1 >= l2) {
                Frame(c, Vec(e1.x / l1, e1.y / l1), Vec(e2.x / l2, e2.y / l2), l1 / 2f, l2 / 2f, points)
            } else {
                Frame(c, Vec(e2.x / l2, e2.y / l2), Vec(e1.x / l1, e1.y / l1), l2 / 2f, l1 / 2f, points)
            }
        }
        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }
        val hx = (maxX - minX) / 2f
        val hy = (maxY - minY) / 2f
        return if (hx >= hy) Frame(c, Vec(1f, 0f), Vec(0f, 1f), hx, hy, points)
        else Frame(c, Vec(0f, 1f), Vec(1f, 0f), hy, hx, points)
    }

    private fun Frame.at(along: Float, across: Float) =
        Vec(c.x + a.x * along + b.x * across, c.y + a.y * along + b.y * across)

    private fun polygon(points: List<Vec>): Path {
        path.reset()
        path.moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) path.lineTo(points[i].x, points[i].y)
        path.close()
        return path
    }

    /**
     * Нарисовать крышу. [area] — контур постройки на экране, [screen] — его вершины.
     */
    fun draw(canvas: Canvas, area: Path, screen: List<Vec>, type: BuildingType, ink: Int, u: Float) {
        val look = BuildingLook.of(type)
        val f = frameOf(screen)
        val size = min(f.l, f.w)

        // 1. Материал: фото-текстура, подкрашенная цветом постройки.
        val bitmap = if (size > 2.5f) texture(look.material.texture) else null
        if (bitmap != null && !bitmap.isRecycled) {
            val shader = shaders.getOrPut(bitmap) {
                BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            }
            val side = max(18f, f.w * 4.4f)
            val k = side / max(1, bitmap.width)
            matrix.reset()
            matrix.setScale(k, k)
            matrix.postRotate(Math.toDegrees(atan2(f.a.y, f.a.x).toDouble()).toFloat())
            matrix.postTranslate(f.c.x, f.c.y)
            shader.setLocalMatrix(matrix)
            tex.shader = shader
            canvas.drawPath(area, tex)
            tex.shader = null
            fill.color = MapRenderer.withAlpha(type.color, 105)
            canvas.drawPath(area, fill)
        } else {
            fill.color = type.color
            canvas.drawPath(area, fill)
        }
        if (size < 2f) return

        val dark = MapRenderer.darken(type.color, 0.45f)
        line.color = dark
        line.strokeWidth = max(0.6f, min(1.1f * u, size * 0.18f))
        line.pathEffect = null
        val shade = 0x33000000
        val light = 0x3DFFFFFF

        // 2. Форма крыши.
        when (look.form) {
            RoofForm.GABLE -> {
                fill.color = shade
                canvas.drawPath(polygon(listOf(f.at(-f.l, 0f), f.at(f.l, 0f), f.at(f.l, f.w), f.at(-f.l, f.w))), fill)
                ridge(canvas, f.at(-f.l, 0f), f.at(f.l, 0f))
            }
            RoofForm.HIP -> {
                val r = max(0f, f.l - f.w)
                val r1 = f.at(-r, 0f)
                val r2 = f.at(r, 0f)
                fill.color = shade
                canvas.drawPath(polygon(listOf(r1, r2, f.at(f.l, f.w), f.at(-f.l, f.w))), fill)
                fill.color = light
                canvas.drawPath(polygon(listOf(r1, f.at(-f.l, -f.w), f.at(-f.l, f.w))), fill)
                ridge(canvas, r1, r2)
                ridge(canvas, r1, f.at(-f.l, -f.w)); ridge(canvas, r1, f.at(-f.l, f.w))
                ridge(canvas, r2, f.at(f.l, -f.w)); ridge(canvas, r2, f.at(f.l, f.w))
            }
            RoofForm.PYRAMID -> {
                fill.color = shade
                canvas.drawPath(polygon(listOf(f.c, f.at(f.l, f.w), f.at(-f.l, f.w))), fill)
                fill.color = light
                canvas.drawPath(polygon(listOf(f.c, f.at(-f.l, -f.w), f.at(f.l, -f.w))), fill)
                for (sx in intArrayOf(-1, 1)) for (sy in intArrayOf(-1, 1)) ridge(canvas, f.c, f.at(sx * f.l, sy * f.w))
            }
            RoofForm.DOME -> {
                val r = size * 0.86f
                fill.color = light
                canvas.drawCircle(f.c.x, f.c.y, r, fill)
                canvas.drawCircle(f.c.x, f.c.y, r, line)
                canvas.drawCircle(f.c.x, f.c.y, r * 0.42f, line)
                fill.color = 0x55FFFFFF
                canvas.drawCircle(f.c.x - r * 0.35f, f.c.y - r * 0.35f, r * 0.18f, fill)
            }
            RoofForm.CONE -> {
                val r = size * 0.95f
                fill.color = shade
                path.reset()
                path.addArc(f.c.x - r, f.c.y - r, f.c.x + r, f.c.y + r, 0f, 180f)
                path.lineTo(f.c.x, f.c.y)
                path.close()
                canvas.drawPath(path, fill)
                canvas.drawCircle(f.c.x, f.c.y, r, line)
                for (i in 0 until 8) {
                    val t = i * Math.PI.toFloat() / 4f
                    canvas.drawLine(f.c.x, f.c.y, f.c.x + kotlin.math.cos(t) * r, f.c.y + kotlin.math.sin(t) * r, line)
                }
            }
            RoofForm.FLAT -> {
                val inner = f.corners.map { lerp(it, f.c, 0.2f) }
                fill.color = light
                canvas.drawPath(polygon(inner), fill)
                canvas.drawPath(polygon(inner), line)
            }
            RoofForm.COURTYARD -> {
                val inner = f.corners.map { lerp(it, f.c, 0.44f) }
                fill.color = 0xFFCDBE9C.toInt()
                canvas.drawPath(polygon(inner), fill)
                canvas.drawPath(polygon(inner), line)
                for (i in f.corners.indices) ridge(canvas, lerp(f.corners[i], f.c, 0.22f), lerp(f.corners[(i + 1) % f.corners.size], f.c, 0.22f))
            }
            RoofForm.SAWTOOTH -> {
                val count = (f.l / max(0.5f, f.w) * 1.6f).toInt().coerceIn(2, 6)
                val step = f.l * 2f / count
                for (i in 0 until count) {
                    val x0 = -f.l + i * step
                    fill.color = shade
                    canvas.drawPath(polygon(listOf(f.at(x0 + step * 0.55f, -f.w), f.at(x0 + step, -f.w), f.at(x0 + step, f.w), f.at(x0 + step * 0.55f, f.w))), fill)
                    ridge(canvas, f.at(x0 + step * 0.55f, -f.w), f.at(x0 + step * 0.55f, f.w))
                }
            }
            RoofForm.CROSS -> {
                fill.color = shade
                canvas.drawPath(polygon(listOf(f.at(-f.l, 0f), f.at(f.l, 0f), f.at(f.l, f.w), f.at(-f.l, f.w))), fill)
                val t = f.l * 0.35f
                val half = min(f.w * 0.55f, f.l * 0.25f)
                fill.color = MapRenderer.darken(type.color, 0.12f)
                canvas.drawPath(polygon(listOf(f.at(t - half, -f.w), f.at(t + half, -f.w), f.at(t + half, f.w), f.at(t - half, f.w))), fill)
                ridge(canvas, f.at(-f.l, 0f), f.at(f.l, 0f))
                ridge(canvas, f.at(t, -f.w), f.at(t, f.w))
            }
            RoofForm.TENT -> {
                val ring = Geometryish.ringPoints(f.corners, 16)
                for (i in ring.indices) {
                    if (i % 2 == 0) continue
                    fill.color = shade
                    canvas.drawPath(polygon(listOf(f.c, ring[i], ring[(i + 1) % ring.size])), fill)
                }
                for (p in ring) ridge(canvas, f.c, p)
                fill.color = dark
                canvas.drawCircle(f.c.x, f.c.y, size * 0.14f, fill)
            }
            RoofForm.RUIN -> {
                fill.color = 0x99201810.toInt()
                canvas.drawPath(polygon(listOf(f.at(-f.l * 0.6f, -f.w * 0.5f), f.at(-f.l * 0.1f, -f.w * 0.7f), f.at(0f, f.w * 0.1f), f.at(-f.l * 0.5f, f.w * 0.3f))), fill)
                canvas.drawPath(polygon(listOf(f.at(f.l * 0.3f, f.w * 0.1f), f.at(f.l * 0.75f, -f.w * 0.2f), f.at(f.l * 0.6f, f.w * 0.6f))), fill)
                fill.color = MapRenderer.darken(type.color, 0.25f)
                canvas.drawCircle(f.at(f.l * 0.1f, f.w * 0.55f).x, f.at(f.l * 0.1f, f.w * 0.55f).y, size * 0.12f, fill)
                canvas.drawCircle(f.at(-f.l * 0.8f, f.w * 0.6f).x, f.at(-f.l * 0.8f, f.w * 0.6f).y, size * 0.09f, fill)
            }
        }

        // 3. Деталь.
        if (size < 3.5f) return
        val d = size * 0.3f
        when (look.detail) {
            RoofDetail.NONE -> Unit
            RoofDetail.CHIMNEY -> chimney(canvas, f.at(f.l * 0.55f, -f.w * 0.4f), d, dark)
            RoofDetail.TWIN_CHIMNEYS -> {
                chimney(canvas, f.at(f.l * 0.6f, -f.w * 0.4f), d, dark)
                chimney(canvas, f.at(-f.l * 0.6f, -f.w * 0.4f), d, dark)
            }
            RoofDetail.DORMERS -> {
                fill.color = MapRenderer.lighten(type.color, 0.25f)
                for (x in floatArrayOf(-0.45f, 0f, 0.45f)) {
                    val tip = f.at(f.l * x, -f.w * 0.75f)
                    val left = f.at(f.l * x - d, -f.w * 0.15f)
                    val right = f.at(f.l * x + d, -f.w * 0.15f)
                    canvas.drawPath(polygon(listOf(left, tip, right)), fill)
                    canvas.drawPath(polygon(listOf(left, tip, right)), line)
                }
            }
            RoofDetail.SKYLIGHT -> {
                fill.color = 0xFFBFE0EA.toInt()
                canvas.drawPath(polygon(listOf(f.at(-f.l * 0.3f - d, -f.w * 0.7f), f.at(-f.l * 0.3f + d, -f.w * 0.7f), f.at(-f.l * 0.3f + d, -f.w * 0.2f), f.at(-f.l * 0.3f - d, -f.w * 0.2f))), fill)
                canvas.drawPath(path, line)
            }
            RoofDetail.VENTS -> {
                fill.color = dark
                for (x in floatArrayOf(-0.5f, 0f, 0.5f)) {
                    val p = f.at(f.l * x, 0f)
                    canvas.drawCircle(p.x, p.y, d * 0.45f, fill)
                }
            }
            RoofDetail.LANTERN -> {
                fill.color = 0x66FFE08A
                canvas.drawCircle(f.c.x, f.c.y, d * 1.6f, fill)
                fill.color = 0xFFFFD35A.toInt()
                canvas.drawCircle(f.c.x, f.c.y, d * 0.7f, fill)
                canvas.drawCircle(f.c.x, f.c.y, d * 0.7f, line)
            }
            RoofDetail.SPIRE -> {
                val p = f.at(f.l * 0.68f, 0f)
                fill.color = dark
                canvas.drawCircle(p.x, p.y, d * 0.9f, fill)
                fill.color = 0xFFE8D8A0.toInt()
                canvas.drawCircle(p.x, p.y, d * 0.35f, fill)
            }
            RoofDetail.BANNER -> {
                val p = f.at(-f.l * 0.75f, -f.w * 0.6f)
                fill.color = 0xFFB0302A.toInt()
                canvas.drawPath(polygon(listOf(p, Vec(p.x + d * 2.2f, p.y + d * 0.6f), Vec(p.x, p.y + d * 1.2f))), fill)
                canvas.drawLine(p.x, p.y - d * 0.4f, p.x, p.y + d * 1.8f, line)
            }
            RoofDetail.GARDEN -> {
                val p = f.at(-f.l * 0.45f, f.w * 0.3f)
                fill.color = 0xFF6E9A52.toInt()
                canvas.drawCircle(p.x, p.y, d * 1.1f, fill)
                canvas.drawCircle(p.x, p.y, d * 1.1f, line)
            }
            RoofDetail.WEATHERVANE -> {
                val p = f.at(f.l * 0.4f, 0f)
                canvas.drawLine(p.x - d, p.y, p.x + d, p.y, line)
                canvas.drawLine(p.x, p.y - d, p.x, p.y + d, line)
                fill.color = dark
                canvas.drawCircle(p.x, p.y, d * 0.3f, fill)
            }
            RoofDetail.BELL -> {
                fill.color = 0xFFD2A843.toInt()
                canvas.drawCircle(f.c.x, f.c.y, d * 0.8f, fill)
                canvas.drawCircle(f.c.x, f.c.y, d * 0.8f, line)
            }
        }
    }

    private fun ridge(canvas: Canvas, p: Vec, q: Vec) {
        canvas.drawLine(p.x, p.y, q.x, q.y, line)
    }

    private fun chimney(canvas: Canvas, p: Vec, d: Float, color: Int) {
        fill.color = color
        canvas.drawRect(p.x - d * 0.5f, p.y - d * 0.5f, p.x + d * 0.5f, p.y + d * 0.5f, fill)
        fill.color = 0x55FFFFFF
        canvas.drawRect(p.x - d * 0.25f, p.y - d * 0.25f, p.x + d * 0.25f, p.y + d * 0.25f, fill)
    }

    private fun lerp(p: Vec, q: Vec, t: Float) = Vec(p.x + (q.x - p.x) * t, p.y + (q.y - p.y) * t)

    /** Точки по контуру через равные доли периметра. */
    private object Geometryish {
        fun ringPoints(corners: List<Vec>, count: Int): List<Vec> {
            val lengths = corners.indices.map { corners[it].distanceTo(corners[(it + 1) % corners.size]) }
            val total = lengths.sum()
            if (total <= 0f) return corners
            return List(count) { i ->
                var target = total * i / count
                var k = 0
                while (k < lengths.size - 1 && target > lengths[k]) {
                    target -= lengths[k]
                    k++
                }
                val a = corners[k]
                val b = corners[(k + 1) % corners.size]
                val t = if (lengths[k] > 0f) (target / lengths[k]).coerceIn(0f, 1f) else 0f
                Vec(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
            }
        }
    }
}
