package com.fantasymap.creator.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.fantasymap.creator.model.BiomePattern
import kotlin.math.cos
import kotlin.math.sin

/**
 * Узоры особых зон: у каждой фэнтезийной и городской зоны свой рисунок.
 * Центр — (x, y), s — размер знака, variant — случайное число клетки 0..1.
 */
class PatternArt {

    private val path = Path()
    private val rect = RectF()

    private inline fun accent(paint: Paint, color: Int, widthScale: Float = 1f, block: () -> Unit) {
        val oldColor = paint.color
        val oldWidth = paint.strokeWidth
        paint.color = color
        paint.strokeWidth = oldWidth * widthScale
        block()
        paint.color = oldColor
        paint.strokeWidth = oldWidth
    }

    private fun tree(c: Canvas, x: Float, y: Float, s: Float, p: Paint) {
        c.drawLine(x, y + s * 0.6f, x, y, p)
        rect.set(x - s * 0.5f, y - s * 0.75f, x + s * 0.5f, y + s * 0.15f)
        c.drawOval(rect, p)
    }

    private fun conifer(c: Canvas, x: Float, y: Float, s: Float, p: Paint) {
        c.drawLine(x, y + s * 0.65f, x, y - s * 0.75f, p)
        c.drawLine(x, y - s * 0.75f, x - s * 0.5f, y + s * 0.15f, p)
        c.drawLine(x, y - s * 0.75f, x + s * 0.5f, y + s * 0.15f, p)
        c.drawLine(x - s * 0.5f, y + s * 0.15f, x + s * 0.5f, y + s * 0.15f, p)
    }

    private fun tuft(c: Canvas, x: Float, y: Float, s: Float, p: Paint) {
        c.drawLine(x, y + s * 0.3f, x - s * 0.25f, y - s * 0.3f, p)
        c.drawLine(x, y + s * 0.3f, x, y - s * 0.45f, p)
        c.drawLine(x, y + s * 0.3f, x + s * 0.25f, y - s * 0.3f, p)
    }

    private fun star(c: Canvas, x: Float, y: Float, r: Float, p: Paint) {
        c.drawLine(x - r, y, x + r, y, p)
        c.drawLine(x, y - r, x, y + r, p)
        c.drawLine(x - r * 0.5f, y - r * 0.5f, x + r * 0.5f, y + r * 0.5f, p)
        c.drawLine(x + r * 0.5f, y - r * 0.5f, x - r * 0.5f, y + r * 0.5f, p)
    }

    private fun peak(c: Canvas, x: Float, y: Float, w: Float, h: Float, p: Paint) {
        c.drawLine(x - w, y, x, y - h, p)
        c.drawLine(x, y - h, x + w, y, p)
    }

    private fun wave(c: Canvas, x: Float, y: Float, w: Float, a: Float, p: Paint) {
        path.reset()
        path.moveTo(x - w, y)
        path.quadTo(x - w * 0.5f, y - a, x, y)
        path.quadTo(x + w * 0.5f, y + a, x + w, y)
        c.drawPath(path, p)
    }

    private fun dune(c: Canvas, x: Float, y: Float, s: Float, p: Paint) {
        rect.set(x - s * 0.8f, y - s * 0.5f, x + s * 0.8f, y + s * 0.5f)
        c.drawArc(rect, 200f, 140f, false, p)
    }

    private fun hill(c: Canvas, x: Float, y: Float, s: Float, p: Paint) {
        rect.set(x - s * 0.8f, y - s * 0.4f, x + s * 0.8f, y + s * 0.6f)
        c.drawArc(rect, 190f, 160f, false, p)
    }

    private fun swampLines(c: Canvas, x: Float, y: Float, s: Float, p: Paint) {
        c.drawLine(x - s * 0.6f, y + s * 0.2f, x + s * 0.6f, y + s * 0.2f, p)
        c.drawLine(x - s * 0.3f, y + s * 0.45f, x + s * 0.3f, y + s * 0.45f, p)
    }

    fun draw(c: Canvas, pattern: BiomePattern, x: Float, y: Float, s: Float, p: Paint, v: Float) {
        when (pattern) {
            // ------------------------------------------------ реальные зоны
            BiomePattern.BAMBOO -> {
                for (i in -1..1) {
                    val bx = x + i * s * 0.3f
                    val top = y - s * (0.8f - (i + 1) * 0.1f)
                    c.drawLine(bx, y + s * 0.7f, bx, top, p)
                    var k = y + s * 0.4f
                    while (k > top) {
                        c.drawLine(bx - s * 0.07f, k, bx + s * 0.07f, k, p)
                        k -= s * 0.3f
                    }
                    c.drawLine(bx, top + s * 0.2f, bx + s * 0.25f, top, p)
                }
            }
            BiomePattern.FROST_POLYGONS -> {
                path.reset()
                for (i in 0..6) {
                    val a = i * 1.0472f + 0.3f
                    val px = x + cos(a) * s * 0.7f
                    val py = y + sin(a) * s * 0.55f
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                c.drawPath(path, p)
                c.drawLine(x + s * 0.7f, y, x + s * 1.1f, y - s * 0.2f, p)
            }
            BiomePattern.CHAPARRAL_SHRUBS -> {
                rect.set(x - s * 0.7f, y - s * 0.3f, x - s * 0.1f, y + s * 0.3f)
                c.drawOval(rect, p)
                rect.set(x - s * 0.25f, y - s * 0.45f, x + s * 0.45f, y + s * 0.2f)
                c.drawOval(rect, p)
                rect.set(x + s * 0.2f, y - s * 0.1f, x + s * 0.75f, y + s * 0.4f)
                c.drawOval(rect, p)
            }
            BiomePattern.PAMPAS -> {
                for (i in -1..1) {
                    val bx = x + i * s * 0.3f
                    c.drawLine(bx, y + s * 0.6f, bx + i * s * 0.1f, y - s * 0.2f, p)
                    rect.set(bx + i * s * 0.1f - s * 0.12f, y - s * 0.85f, bx + i * s * 0.1f + s * 0.12f, y - s * 0.15f)
                    c.drawOval(rect, p)
                }
            }
            BiomePattern.SNOW_PEAKS -> {
                peak(c, x, y + s * 0.6f, s * 0.8f, s * 1.5f, p)
                c.drawLine(x - s * 0.3f, y - s * 0.35f, x - s * 0.05f, y - s * 0.2f, p)
                c.drawLine(x - s * 0.05f, y - s * 0.2f, x + s * 0.12f, y - s * 0.4f, p)
                c.drawLine(x + s * 0.12f, y - s * 0.4f, x + s * 0.3f, y - s * 0.3f, p)
                c.drawLine(x, y - s * 0.9f, x + s * 0.35f, y + s * 0.6f, p)
            }
            BiomePattern.CANYON -> {
                path.reset()
                path.moveTo(x - s * 0.9f, y - s * 0.5f)
                path.cubicTo(x - s * 0.2f, y - s * 0.6f, x + s * 0.1f, y + s * 0.4f, x + s * 0.9f, y + s * 0.3f)
                c.drawPath(path, p)
                path.reset()
                path.moveTo(x - s * 0.9f, y - s * 0.15f)
                path.cubicTo(x - s * 0.3f, y - s * 0.25f, x, y + s * 0.75f, x + s * 0.9f, y + s * 0.65f)
                c.drawPath(path, p)
                for (i in 0 until 4) {
                    val t = -0.6f + i * 0.4f
                    c.drawLine(x + t * s, y - s * 0.4f + i * s * 0.25f, x + t * s + s * 0.1f, y - s * 0.2f + i * s * 0.25f, p)
                }
            }
            BiomePattern.LAKES -> accent(p, 0xFF4E86AE.toInt()) {
                rect.set(x - s * 0.6f, y - s * 0.3f, x + s * 0.5f, y + s * 0.3f)
                c.drawOval(rect, p)
                c.drawLine(x - s * 0.3f, y, x + s * 0.1f, y, p)
                if (v < 0.5f) {
                    rect.set(x + s * 0.5f, y + s * 0.3f, x + s * 0.9f, y + s * 0.55f)
                    c.drawOval(rect, p)
                }
            }
            BiomePattern.RICE -> {
                for (i in 0 until 3) {
                    rect.set(x - s * 1.1f, y - s * 0.6f + i * s * 0.35f, x + s * 1.1f, y + s * 0.6f + i * s * 0.35f)
                    c.drawArc(rect, 200f, 140f, false, p)
                }
                accent(p, 0xFF5E9A3E.toInt()) {
                    for (i in -1..1) c.drawLine(x + i * s * 0.35f, y - s * 0.15f, x + i * s * 0.35f, y - s * 0.4f, p)
                }
            }

            // ------------------------------------------------ фэнтезийные земли
            BiomePattern.ENCHANTED -> {
                tree(c, x, y, s, p)
                accent(p, 0xFFB8F0FF.toInt()) { star(c, x + s * 0.55f, y - s * 0.7f, s * 0.22f, p) }
            }
            BiomePattern.DARK_TREES -> {
                c.drawLine(x, y + s * 0.65f, x, y - s * 0.8f, p)
                for (i in 0 until 3) {
                    val yy = y - s * 0.55f + i * s * 0.35f
                    c.drawLine(x, yy, x - s * (0.3f + i * 0.12f), yy + s * 0.3f, p)
                    c.drawLine(x, yy, x + s * (0.3f + i * 0.12f), yy + s * 0.3f, p)
                }
                if (v < 0.3f) accent(p, 0xFFE8D040.toInt()) {
                    c.drawPoint(x - s * 0.55f, y + s * 0.2f, p)
                    c.drawPoint(x - s * 0.4f, y + s * 0.2f, p)
                }
            }
            BiomePattern.CURSED -> {
                c.drawCircle(x, y, s * 0.45f, p)
                c.drawLine(x - s * 0.3f, y + s * 0.3f, x + s * 0.3f, y - s * 0.3f, p)
                c.drawLine(x - s * 0.3f, y - s * 0.3f, x, y + s * 0.1f, p)
                c.drawLine(x, y - s * 0.45f, x, y - s * 0.75f, p)
            }
            BiomePattern.ASH_DRIFTS -> {
                c.drawLine(x - s * 0.1f, y + s * 0.4f, x - s * 0.1f, y - s * 0.1f, p)
                c.drawLine(x + s * 0.15f, y + s * 0.4f, x + s * 0.15f, y, p)
                wave(c, x + s * 0.1f, y - s * 0.4f, s * 0.35f, s * 0.2f, p)
                wave(c, x + s * 0.2f, y - s * 0.75f, s * 0.3f, s * 0.2f, p)
            }
            BiomePattern.BLIGHT -> {
                for (i in -1..1) {
                    val bx = x + i * s * 0.35f
                    path.reset()
                    path.moveTo(bx, y + s * 0.5f)
                    path.quadTo(bx, y - s * 0.4f, bx + s * 0.3f, y - s * 0.1f)
                    c.drawPath(path, p)
                }
            }
            BiomePattern.FAIRY_RING -> {
                for (i in 0 until 7) {
                    val a = i * 0.8976f
                    val mx = x + cos(a) * s * 0.7f
                    val my = y + sin(a) * s * 0.5f
                    rect.set(mx - s * 0.13f, my - s * 0.12f, mx + s * 0.13f, my + s * 0.02f)
                    c.drawArc(rect, 180f, 180f, true, p)
                }
                accent(p, 0xFFE8B0F0.toInt()) { c.drawPoint(x, y, p) }
            }
            BiomePattern.WISPS -> {
                swampLines(c, x, y, s, p)
                if (v < 0.45f) accent(p, 0xFF9FF0C8.toInt(), 1.5f) { c.drawCircle(x + s * 0.2f, y - s * 0.5f, s * 0.12f, p) }
            }
            BiomePattern.MAGMA -> accent(p, 0xFFE0602A.toInt(), 1.5f) {
                path.reset()
                path.moveTo(x - s * 0.9f, y - s * 0.2f)
                path.quadTo(x - s * 0.3f, y + s * 0.3f, x + s * 0.2f, y - s * 0.1f)
                path.quadTo(x + s * 0.6f, y - s * 0.4f, x + s * 0.9f, y + s * 0.2f)
                c.drawPath(path, p)
                c.drawLine(x + s * 0.2f, y - s * 0.1f, x + s * 0.3f, y + s * 0.5f, p)
            }
            BiomePattern.FROST_SPIKES -> {
                peak(c, x - s * 0.3f, y + s * 0.5f, s * 0.15f, s * 1.1f, p)
                peak(c, x + s * 0.1f, y + s * 0.5f, s * 0.12f, s * 0.8f, p)
                peak(c, x + s * 0.45f, y + s * 0.5f, s * 0.14f, s * 1.3f, p)
            }
            BiomePattern.HOLY -> accent(p, 0xFFD9B640.toInt()) {
                c.drawLine(x, y - s * 0.6f, x, y + s * 0.5f, p)
                c.drawLine(x - s * 0.3f, y - s * 0.25f, x + s * 0.3f, y - s * 0.25f, p)
                for (i in 0 until 8) {
                    val a = i * 0.785f
                    c.drawLine(x + cos(a) * s * 0.55f, y - s * 0.25f + sin(a) * s * 0.55f, x + cos(a) * s * 0.75f, y - s * 0.25f + sin(a) * s * 0.75f, p)
                }
            }
            BiomePattern.WAR -> {
                c.drawLine(x - s * 0.6f, y + s * 0.6f, x + s * 0.5f, y - s * 0.5f, p)
                c.drawLine(x + s * 0.6f, y + s * 0.6f, x - s * 0.5f, y - s * 0.5f, p)
                c.drawLine(x - s * 0.45f, y - s * 0.2f, x - s * 0.2f, y - s * 0.45f, p)
                c.drawLine(x + s * 0.45f, y - s * 0.2f, x + s * 0.2f, y - s * 0.45f, p)
            }
            BiomePattern.SPIRITS -> {
                tree(c, x - s * 0.4f, y, s * 0.8f, p)
                accent(p, 0xFFD8F0F8.toInt()) {
                    path.reset()
                    path.moveTo(x + s * 0.4f, y + s * 0.4f)
                    path.cubicTo(x + s * 0.1f, y, x + s * 0.8f, y - s * 0.3f, x + s * 0.45f, y - s * 0.7f)
                    c.drawPath(path, p)
                    c.drawCircle(x + s * 0.45f, y - s * 0.8f, s * 0.12f, p)
                }
            }
            BiomePattern.TWILIGHT -> {
                conifer(c, x - s * 0.2f, y, s, p)
                accent(p, 0xFFE0D8A0.toInt()) {
                    rect.set(x + s * 0.3f, y - s * 1.0f, x + s * 0.7f, y - s * 0.6f)
                    c.drawArc(rect, 90f, 180f, false, p)
                }
            }
            BiomePattern.AMBER -> {
                tree(c, x, y, s, p)
                accent(p, 0xFFE0A030.toInt(), 1.4f) { c.drawCircle(x + s * 0.2f, y + s * 0.25f, s * 0.12f, p) }
            }
            BiomePattern.SILVER -> {
                c.drawLine(x, y + s * 0.7f, x, y - s * 0.8f, p)
                for (i in 0 until 4) c.drawLine(x - s * 0.08f, y - s * 0.5f + i * s * 0.35f, x + s * 0.08f, y - s * 0.5f + i * s * 0.35f, p)
                rect.set(x - s * 0.35f, y - s * 0.9f, x + s * 0.35f, y - s * 0.2f)
                c.drawOval(rect, p)
                accent(p, 0xFFE8EEF4.toInt()) { star(c, x + s * 0.45f, y - s * 0.8f, s * 0.15f, p) }
            }
            BiomePattern.GIANT_MUSHROOMS -> if (v < 0.4f) {
                c.drawLine(x - s * 0.15f, y + s * 0.9f, x - s * 0.1f, y - s * 0.3f, p)
                c.drawLine(x + s * 0.15f, y + s * 0.9f, x + s * 0.1f, y - s * 0.3f, p)
                rect.set(x - s * 1.0f, y - s * 1.1f, x + s * 1.0f, y + s * 0.1f)
                c.drawArc(rect, 180f, 180f, true, p)
                c.drawCircle(x - s * 0.4f, y - s * 0.6f, s * 0.12f, p)
                c.drawCircle(x + s * 0.35f, y - s * 0.75f, s * 0.1f, p)
            } else {
                c.drawPoint(x, y, p)
            }
            BiomePattern.SINGING -> {
                dune(c, x, y + s * 0.2f, s, p)
                accent(p, 0xFF8A5E3A.toInt()) {
                    c.drawCircle(x + s * 0.2f, y - s * 0.35f, s * 0.1f, p)
                    c.drawLine(x + s * 0.3f, y - s * 0.35f, x + s * 0.3f, y - s * 0.85f, p)
                    c.drawLine(x + s * 0.3f, y - s * 0.85f, x + s * 0.5f, y - s * 0.7f, p)
                }
            }
            BiomePattern.GLASS -> {
                path.reset()
                path.moveTo(x - s * 0.5f, y + s * 0.3f)
                path.lineTo(x - s * 0.1f, y - s * 0.6f)
                path.lineTo(x + s * 0.2f, y + s * 0.3f)
                path.close()
                c.drawPath(path, p)
                accent(p, 0xFFFFFFFF.toInt()) { star(c, x + s * 0.5f, y - s * 0.4f, s * 0.2f, p) }
            }
            BiomePattern.MIRROR -> {
                path.reset()
                path.moveTo(x, y - s * 0.5f)
                path.lineTo(x + s * 0.7f, y)
                path.lineTo(x, y + s * 0.5f)
                path.lineTo(x - s * 0.7f, y)
                path.close()
                c.drawPath(path, p)
                accent(p, 0xFFFFFFFF.toInt()) { c.drawLine(x - s * 0.25f, y - s * 0.05f, x + s * 0.05f, y - s * 0.3f, p) }
            }
            BiomePattern.WHISPERS -> {
                tuft(c, x - s * 0.3f, y + s * 0.2f, s * 0.8f, p)
                path.reset()
                path.moveTo(x - s * 0.1f, y - s * 0.3f)
                path.quadTo(x + s * 0.4f, y - s * 0.6f, x + s * 0.6f, y - s * 0.3f)
                path.quadTo(x + s * 0.7f, y - s * 0.05f, x + s * 0.45f, y - s * 0.1f)
                c.drawPath(path, p)
            }
            BiomePattern.BLOOD -> {
                swampLines(c, x, y, s, p)
                accent(p, 0xFF9E1E24.toInt(), 1.3f) {
                    path.reset()
                    path.moveTo(x, y - s * 0.7f)
                    path.quadTo(x + s * 0.25f, y - s * 0.3f, x, y - s * 0.2f)
                    path.quadTo(x - s * 0.25f, y - s * 0.3f, x, y - s * 0.7f)
                    c.drawPath(path, p)
                }
            }
            BiomePattern.STARFALL -> if (v < 0.25f) {
                accent(p, 0xFFF0E0A0.toInt(), 1.3f) {
                    star(c, x + s * 0.5f, y - s * 0.5f, s * 0.3f, p)
                    c.drawLine(x + s * 0.4f, y - s * 0.4f, x - s * 0.8f, y + s * 0.6f, p)
                }
                rect.set(x - s * 1.0f, y + s * 0.45f, x - s * 0.4f, y + s * 0.8f)
                c.drawOval(rect, p)
            } else {
                star(c, x, y, s * 0.15f, p)
            }
            BiomePattern.MIST -> {
                wave(c, x, y - s * 0.3f, s * 0.8f, s * 0.15f, p)
                wave(c, x + s * 0.2f, y + s * 0.1f, s * 0.7f, s * 0.15f, p)
                wave(c, x - s * 0.1f, y + s * 0.5f, s * 0.6f, s * 0.15f, p)
            }
            BiomePattern.EMBERS -> {
                hill(c, x, y + s * 0.2f, s, p)
                accent(p, 0xFFE87830.toInt(), 1.5f) {
                    c.drawPoint(x - s * 0.2f, y - s * 0.3f, p)
                    c.drawPoint(x + s * 0.15f, y - s * 0.5f, p)
                    c.drawPoint(x + s * 0.35f, y - s * 0.2f, p)
                }
            }
            BiomePattern.STONE_FACES -> {
                rect.set(x - s * 0.55f, y - s * 0.6f, x + s * 0.55f, y + s * 0.5f)
                c.drawRoundRect(rect, s * 0.3f, s * 0.3f, p)
                c.drawLine(x - s * 0.3f, y - s * 0.15f, x - s * 0.1f, y - s * 0.15f, p)
                c.drawLine(x + s * 0.1f, y - s * 0.15f, x + s * 0.3f, y - s * 0.15f, p)
                c.drawLine(x - s * 0.2f, y + s * 0.25f, x + s * 0.2f, y + s * 0.25f, p)
            }
            BiomePattern.VOID -> {
                path.reset()
                path.moveTo(x, y)
                for (i in 1..24) {
                    val a = i * 0.45f
                    val r = s * 0.04f * i
                    path.lineTo(x + cos(a) * r, y + sin(a) * r * 0.7f)
                }
                c.drawPath(path, p)
            }
            BiomePattern.SUNKEN -> {
                wave(c, x, y + s * 0.3f, s * 0.8f, s * 0.15f, p)
                c.drawLine(x - s * 0.2f, y + s * 0.2f, x - s * 0.2f, y - s * 0.6f, p)
                c.drawLine(x + s * 0.15f, y + s * 0.2f, x + s * 0.15f, y - s * 0.6f, p)
                c.drawLine(x - s * 0.3f, y - s * 0.6f, x + s * 0.25f, y - s * 0.6f, p)
            }
            BiomePattern.CORAL -> {
                c.drawLine(x, y + s * 0.7f, x, y - s * 0.1f, p)
                c.drawLine(x, y + s * 0.1f, x - s * 0.45f, y - s * 0.4f, p)
                c.drawLine(x, y - s * 0.1f, x + s * 0.4f, y - s * 0.6f, p)
                c.drawLine(x - s * 0.3f, y - s * 0.25f, x - s * 0.3f, y - s * 0.7f, p)
                c.drawLine(x + s * 0.25f, y - s * 0.4f, x + s * 0.55f, y - s * 0.35f, p)
            }
            BiomePattern.MOONS -> accent(p, 0xFFD8D4F0.toInt()) {
                rect.set(x - s * 0.4f, y - s * 0.4f, x + s * 0.4f, y + s * 0.4f)
                c.drawArc(rect, 60f, 240f, false, p)
                star(c, x + s * 0.6f, y - s * 0.5f, s * 0.12f, p)
            }
            BiomePattern.DREAMS -> {
                path.reset()
                path.moveTo(x, y)
                for (i in 1..16) {
                    val a = i * 0.5f
                    val r = s * 0.045f * i
                    path.lineTo(x + cos(a) * r, y + sin(a) * r)
                }
                c.drawPath(path, p)
                accent(p, 0xFFE0C8F0.toInt()) { c.drawCircle(x + s * 0.6f, y - s * 0.5f, s * 0.12f, p) }
            }
            BiomePattern.OBSIDIAN -> {
                path.reset()
                path.moveTo(x - s * 0.6f, y + s * 0.4f)
                path.lineTo(x - s * 0.2f, y - s * 0.6f)
                path.lineTo(x + s * 0.1f, y + s * 0.1f)
                path.lineTo(x + s * 0.5f, y - s * 0.3f)
                path.lineTo(x + s * 0.6f, y + s * 0.4f)
                path.close()
                c.drawPath(path, p)
                c.drawLine(x - s * 0.2f, y - s * 0.6f, x - s * 0.1f, y + s * 0.4f, p)
            }
            BiomePattern.PETRIFIED -> {
                c.drawLine(x - s * 0.25f, y + s * 0.5f, x - s * 0.2f, y - s * 0.3f, p)
                c.drawLine(x + s * 0.25f, y + s * 0.5f, x + s * 0.2f, y - s * 0.2f, p)
                c.drawLine(x - s * 0.2f, y - s * 0.3f, x, y - s * 0.15f, p)
                c.drawLine(x, y - s * 0.15f, x + s * 0.2f, y - s * 0.2f, p)
                c.drawLine(x - s * 0.1f, y + s * 0.1f, x + s * 0.1f, y + s * 0.25f, p)
            }
            BiomePattern.AURORA -> accent(p, 0xFF60D8A0.toInt()) {
                for (i in 0 until 3) {
                    val xx = x - s * 0.5f + i * s * 0.5f
                    path.reset()
                    path.moveTo(xx, y + s * 0.4f)
                    path.quadTo(xx + s * 0.25f, y - s * 0.1f, xx, y - s * 0.7f)
                    c.drawPath(path, p)
                }
            }
            BiomePattern.ACID -> accent(p, 0xFF8CC43A.toInt()) {
                c.drawCircle(x - s * 0.3f, y, s * 0.2f, p)
                c.drawCircle(x + s * 0.15f, y - s * 0.25f, s * 0.13f, p)
                c.drawCircle(x + s * 0.35f, y + s * 0.2f, s * 0.1f, p)
                swampLines(c, x, y + s * 0.2f, s, p)
            }
            BiomePattern.CLOUD_PEAKS -> {
                peak(c, x, y + s * 0.6f, s * 0.8f, s * 1.4f, p)
                rect.set(x - s * 0.9f, y - s * 0.3f, x - s * 0.1f, y + s * 0.1f)
                c.drawArc(rect, 180f, 180f, false, p)
                rect.set(x - s * 0.3f, y - s * 0.35f, x + s * 0.6f, y + s * 0.1f)
                c.drawArc(rect, 180f, 180f, false, p)
            }
            BiomePattern.CRIMSON -> {
                dune(c, x, y + s * 0.3f, s, p)
                rect.set(x - s * 0.5f, y - s * 0.7f, x + s * 0.5f, y + s * 0.3f)
                c.drawArc(rect, 180f, 180f, false, p)
                rect.set(x - s * 0.25f, y - s * 0.35f, x + s * 0.25f, y + s * 0.3f)
                c.drawArc(rect, 180f, 180f, false, p)
            }
            BiomePattern.GOLDEN -> accent(p, 0xFFB48A20.toInt()) {
                c.drawLine(x, y + s * 0.6f, x, y - s * 0.6f, p)
                for (i in 0 until 3) {
                    val yy = y - s * 0.5f + i * s * 0.25f
                    c.drawLine(x, yy + s * 0.1f, x - s * 0.18f, yy, p)
                    c.drawLine(x, yy + s * 0.1f, x + s * 0.18f, yy, p)
                }
            }
            BiomePattern.FIREFLIES -> {
                tree(c, x, y, s, p)
                accent(p, 0xFFF0E060.toInt(), 1.6f) {
                    c.drawPoint(x - s * 0.7f, y - s * 0.2f, p)
                    c.drawPoint(x + s * 0.6f, y - s * 0.6f, p)
                    c.drawPoint(x + s * 0.7f, y + s * 0.2f, p)
                }
            }
            BiomePattern.DEADWOOD -> {
                c.drawLine(x, y + s * 0.7f, x, y - s * 0.6f, p)
                c.drawLine(x, y - s * 0.1f, x - s * 0.45f, y - s * 0.5f, p)
                c.drawLine(x, y - s * 0.3f, x + s * 0.4f, y - s * 0.75f, p)
                c.drawLine(x - s * 0.3f, y - s * 0.35f, x - s * 0.35f, y - s * 0.65f, p)
            }
            BiomePattern.HONEY -> {
                accent(p, 0xFFC89A30.toInt()) {
                    for (k in 0 until 2) {
                        val hx = x - s * 0.3f + k * s * 0.6f
                        path.reset()
                        for (i in 0..6) {
                            val a = i * 1.0472f
                            val px = hx + cos(a) * s * 0.3f
                            val py = y + sin(a) * s * 0.3f
                            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                        }
                        c.drawPath(path, p)
                    }
                }
                if (v < 0.3f) c.drawCircle(x, y - s * 0.6f, s * 0.08f, p)
            }
            BiomePattern.FLESH -> accent(p, 0xFF8E3E44.toInt()) {
                path.reset()
                path.moveTo(x - s * 0.8f, y)
                path.cubicTo(x - s * 0.3f, y - s * 0.5f, x + s * 0.2f, y + s * 0.5f, x + s * 0.8f, y - s * 0.1f)
                c.drawPath(path, p)
                c.drawLine(x - s * 0.1f, y + s * 0.05f, x, y + s * 0.5f, p)
                c.drawCircle(x + s * 0.3f, y - s * 0.4f, s * 0.14f, p)
            }

            // ------------------------------------------------ городские зоны
            BiomePattern.PARK -> {
                tree(c, x - s * 0.3f, y, s * 0.8f, p)
                path.reset()
                path.moveTo(x - s * 0.9f, y + s * 0.8f)
                path.quadTo(x + s * 0.2f, y + s * 0.3f, x + s * 0.9f, y + s * 0.7f)
                c.drawPath(path, p)
                c.drawLine(x + s * 0.3f, y + s * 0.1f, x + s * 0.7f, y + s * 0.1f, p)
            }
            BiomePattern.VEGETABLES -> {
                c.drawCircle(x - s * 0.4f, y, s * 0.18f, p)
                c.drawCircle(x, y, s * 0.18f, p)
                c.drawCircle(x + s * 0.4f, y, s * 0.18f, p)
                c.drawLine(x - s * 0.7f, y + s * 0.35f, x + s * 0.7f, y + s * 0.35f, p)
            }
            BiomePattern.PAVING -> {
                rect.set(x - s * 0.8f, y - s * 0.5f, x, y + s * 0.1f)
                c.drawRect(rect, p)
                rect.set(x + s * 0.05f, y - s * 0.5f, x + s * 0.8f, y - s * 0.05f)
                c.drawRect(rect, p)
                rect.set(x - s * 0.5f, y + s * 0.15f, x + s * 0.4f, y + s * 0.6f)
                c.drawRect(rect, p)
            }
            BiomePattern.MARKET -> {
                rect.set(x - s * 0.5f, y - s * 0.1f, x + s * 0.5f, y + s * 0.4f)
                c.drawRect(rect, p)
                accent(p, 0xFFB5402E.toInt()) {
                    path.reset()
                    path.moveTo(x - s * 0.65f, y - s * 0.1f)
                    path.lineTo(x - s * 0.4f, y - s * 0.45f)
                    path.lineTo(x + s * 0.4f, y - s * 0.45f)
                    path.lineTo(x + s * 0.65f, y - s * 0.1f)
                    c.drawPath(path, p)
                }
            }
            BiomePattern.YARD -> {
                rect.set(x - s * 0.6f, y - s * 0.2f, x - s * 0.2f, y + s * 0.2f)
                c.drawRect(rect, p)
                c.drawCircle(x + s * 0.3f, y, s * 0.2f, p)
                c.drawLine(x - s * 0.6f, y - s * 0.2f, x - s * 0.2f, y + s * 0.2f, p)
            }
            BiomePattern.PUDDLES -> {
                rect.set(x - s * 0.5f, y - s * 0.15f, x + s * 0.3f, y + s * 0.2f)
                c.drawOval(rect, p)
                c.drawPoint(x + s * 0.5f, y - s * 0.3f, p)
            }
            BiomePattern.RUBBLE -> {
                rect.set(x - s * 0.5f, y - s * 0.1f, x - s * 0.1f, y + s * 0.15f)
                c.drawRect(rect, p)
                rect.set(x, y - s * 0.3f, x + s * 0.35f, y - s * 0.05f)
                c.drawRect(rect, p)
                rect.set(x - s * 0.2f, y + s * 0.2f, x + s * 0.2f, y + s * 0.45f)
                c.drawRect(rect, p)
            }
            BiomePattern.LILIES -> accent(p, 0xFF5E9A5E.toInt()) {
                rect.set(x - s * 0.35f, y - s * 0.35f, x + s * 0.35f, y + s * 0.35f)
                c.drawArc(rect, 20f, 320f, true, p)
                if (v < 0.4f) accent(p, 0xFFE8A0C0.toInt()) { c.drawCircle(x + s * 0.5f, y - s * 0.4f, s * 0.1f, p) }
            }
            BiomePattern.DRILL -> {
                c.drawLine(x - s * 0.15f, y - s * 0.15f, x + s * 0.15f, y + s * 0.15f, p)
                c.drawLine(x + s * 0.15f, y - s * 0.15f, x - s * 0.15f, y + s * 0.15f, p)
            }
            BiomePattern.FAIR -> {
                path.reset()
                path.moveTo(x - s * 0.5f, y + s * 0.4f)
                path.lineTo(x, y - s * 0.4f)
                path.lineTo(x + s * 0.5f, y + s * 0.4f)
                path.close()
                c.drawPath(path, p)
                c.drawLine(x, y - s * 0.4f, x, y - s * 0.75f, p)
                accent(p, 0xFFC0402E.toInt()) { c.drawLine(x, y - s * 0.75f, x + s * 0.3f, y - s * 0.65f, p) }
            }
            BiomePattern.GROVE -> {
                tree(c, x - s * 0.35f, y + s * 0.1f, s * 0.75f, p)
                tree(c, x + s * 0.35f, y - s * 0.05f, s * 0.8f, p)
            }
            BiomePattern.RAKED_SAND -> {
                for (i in 1..3) {
                    rect.set(x - s * 0.3f * i, y - s * 0.2f * i, x + s * 0.3f * i, y + s * 0.2f * i)
                    c.drawArc(rect, 200f, 140f, false, p)
                }
            }
            BiomePattern.CANAL -> {
                c.drawLine(x - s, y - s * 0.2f, x + s, y - s * 0.2f, p)
                c.drawLine(x - s, y + s * 0.2f, x + s, y + s * 0.2f, p)
                if (v < 0.3f) {
                    path.reset()
                    path.moveTo(x - s * 0.3f, y)
                    path.lineTo(x + s * 0.3f, y)
                    path.lineTo(x + s * 0.2f, y + s * 0.1f)
                    path.lineTo(x - s * 0.2f, y + s * 0.1f)
                    path.close()
                    c.drawPath(path, p)
                }
            }
            BiomePattern.SLUM -> {
                rect.set(x - s * 0.4f, y - s * 0.1f, x, y + s * 0.3f)
                c.drawRect(rect, p)
                c.drawLine(x - s * 0.45f, y - s * 0.1f, x + s * 0.05f, y - s * 0.3f, p)
                c.drawPoint(x + s * 0.3f, y + s * 0.2f, p)
                c.drawPoint(x + s * 0.45f, y + s * 0.05f, p)
            }
            BiomePattern.TOPIARY -> {
                peak(c, x - s * 0.4f, y + s * 0.4f, s * 0.2f, s * 0.8f, p)
                c.drawLine(x - s * 0.6f, y + s * 0.4f, x - s * 0.2f, y + s * 0.4f, p)
                c.drawCircle(x + s * 0.35f, y, s * 0.28f, p)
                c.drawLine(x + s * 0.35f, y + s * 0.28f, x + s * 0.35f, y + s * 0.5f, p)
            }
            BiomePattern.TOURNEY -> {
                c.drawLine(x - s, y, x + s, y, p)
                c.drawLine(x - s * 0.6f, y, x - s * 0.6f, y - s * 0.6f, p)
                accent(p, 0xFFB5402E.toInt()) {
                    path.reset()
                    path.moveTo(x - s * 0.6f, y - s * 0.6f)
                    path.lineTo(x - s * 0.2f, y - s * 0.5f)
                    path.lineTo(x - s * 0.6f, y - s * 0.4f)
                    c.drawPath(path, p)
                }
            }
            BiomePattern.OLD_GRAVES -> {
                c.drawLine(x - s * 0.1f, y + s * 0.5f, x + s * 0.1f, y - s * 0.5f, p)
                c.drawLine(x - s * 0.3f, y - s * 0.15f, x + s * 0.3f, y - s * 0.3f, p)
                tuft(c, x + s * 0.5f, y + s * 0.4f, s * 0.5f, p)
            }
            BiomePattern.HERB_BEDS -> {
                rect.set(x - s * 0.7f, y - s * 0.25f, x + s * 0.7f, y + s * 0.3f)
                c.drawRect(rect, p)
                accent(p, 0xFF5E8E3E.toInt()) {
                    for (i in -1..1) tuft(c, x + i * s * 0.4f, y, s * 0.35f, p)
                }
            }
            BiomePattern.LOGS -> {
                for (i in 0 until 3) {
                    c.drawCircle(x - s * 0.3f + i * s * 0.3f, y + s * 0.2f, s * 0.15f, p)
                }
                c.drawCircle(x - s * 0.15f, y - s * 0.07f, s * 0.15f, p)
                c.drawCircle(x + s * 0.15f, y - s * 0.07f, s * 0.15f, p)
            }
            BiomePattern.QUARRY -> {
                c.drawLine(x - s * 0.8f, y + s * 0.4f, x - s * 0.4f, y + s * 0.4f, p)
                c.drawLine(x - s * 0.4f, y + s * 0.4f, x - s * 0.4f, y, p)
                c.drawLine(x - s * 0.4f, y, x, y, p)
                c.drawLine(x, y, x, y - s * 0.4f, p)
                c.drawLine(x, y - s * 0.4f, x + s * 0.5f, y - s * 0.4f, p)
            }
            BiomePattern.MAGIC_FLOWERS -> {
                c.drawLine(x, y + s * 0.5f, x, y - s * 0.1f, p)
                accent(p, 0xFF9E7ED8.toInt()) {
                    for (i in 0 until 5) {
                        val a = i * 1.2566f
                        c.drawCircle(x + cos(a) * s * 0.2f, y - s * 0.3f + sin(a) * s * 0.2f, s * 0.09f, p)
                    }
                }
                accent(p, 0xFFE8FFFF.toInt()) { star(c, x + s * 0.45f, y - s * 0.6f, s * 0.14f, p) }
            }
            BiomePattern.CHARRED -> {
                c.drawLine(x - s * 0.6f, y + s * 0.3f, x + s * 0.4f, y - s * 0.2f, p)
                c.drawLine(x - s * 0.3f, y - s * 0.3f, x + s * 0.5f, y + s * 0.3f, p)
                c.drawPoint(x + s * 0.1f, y + s * 0.5f, p)
            }
            else -> Unit
        }
    }
}
