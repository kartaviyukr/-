package com.fantasymap.creator.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.fantasymap.creator.model.BiomePattern
import com.fantasymap.creator.model.Glyph
import kotlin.math.cos
import kotlin.math.sin

/**
 * Рисование значков объектов и текстур ландшафта.
 * Экземпляр не потокобезопасен: один экземпляр — один поток отрисовки.
 */
class Glyphs {

    private val path = Path()
    private val rect = RectF()

    /**
     * Значок объекта. Центр — (cx, cy), s — характерный радиус в пикселях экрана.
     * fill — заливка, stroke — контур (толщина задаётся вызывающей стороной).
     */
    fun drawGlyph(canvas: Canvas, glyph: Glyph, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        when (glyph) {
            Glyph.CAPITAL -> capital(canvas, cx, cy, s, fill, stroke)
            Glyph.CITY -> city(canvas, cx, cy, s, fill, stroke)
            Glyph.TOWN -> town(canvas, cx, cy, s, fill, stroke)
            Glyph.VILLAGE -> house(canvas, cx, cy, s, fill, stroke)
            Glyph.CASTLE -> castle(canvas, cx, cy, s, fill, stroke)
            Glyph.TOWER -> tower(canvas, cx, cy, s, fill, stroke)
            Glyph.GATE -> gate(canvas, cx, cy, s, fill, stroke)
            Glyph.WALL -> wall(canvas, cx, cy, s, fill, stroke)
            Glyph.CAMP -> camp(canvas, cx, cy, s, fill, stroke)
            Glyph.BATTLE -> battle(canvas, cx, cy, s, stroke)
            Glyph.ARENA -> arena(canvas, cx, cy, s, fill, stroke)
            Glyph.ANCHOR -> anchor(canvas, cx, cy, s, stroke)
            Glyph.SHIP -> ship(canvas, cx, cy, s, fill, stroke)
            Glyph.LIGHTHOUSE -> lighthouse(canvas, cx, cy, s, fill, stroke)
            Glyph.BRIDGE -> bridge(canvas, cx, cy, s, stroke)
            Glyph.MARKET -> market(canvas, cx, cy, s, fill, stroke)
            Glyph.TEMPLE -> temple(canvas, cx, cy, s, fill, stroke)
            Glyph.OBELISK -> obelisk(canvas, cx, cy, s, fill, stroke)
            Glyph.GRAVE -> grave(canvas, cx, cy, s, fill, stroke)
            Glyph.PORTAL -> portal(canvas, cx, cy, s, fill, stroke)
            Glyph.STONE_CIRCLE -> stoneCircle(canvas, cx, cy, s, fill, stroke)
            Glyph.RUINS -> ruins(canvas, cx, cy, s, fill, stroke)
            Glyph.DUNGEON -> dungeon(canvas, cx, cy, s, fill, stroke)
            Glyph.CAVE -> cave(canvas, cx, cy, s, fill, stroke)
            Glyph.MOUNTAIN -> mountainGlyph(canvas, cx, cy, s, fill, stroke)
            Glyph.VOLCANO -> volcano(canvas, cx, cy, s, fill, stroke)
            Glyph.WATERFALL -> waterfall(canvas, cx, cy, s, stroke)
            Glyph.GEYSER -> geyser(canvas, cx, cy, s, stroke)
            Glyph.WHIRLPOOL -> whirlpool(canvas, cx, cy, s, stroke)
            Glyph.ROCK -> rock(canvas, cx, cy, s, fill, stroke)
            Glyph.TREE -> treeGlyph(canvas, cx, cy, s, fill, stroke)
            Glyph.MINE -> mine(canvas, cx, cy, s, fill, stroke)
            Glyph.FARM -> farm(canvas, cx, cy, s, fill, stroke)
            Glyph.MILL -> mill(canvas, cx, cy, s, fill, stroke)
            Glyph.FORGE -> forge(canvas, cx, cy, s, fill, stroke)
            Glyph.MONSTER -> monster(canvas, cx, cy, s, fill, stroke)
            Glyph.TREASURE -> treasure(canvas, cx, cy, s, fill, stroke)
            Glyph.ANOMALY -> anomaly(canvas, cx, cy, s, fill, stroke)
        }
    }

    // ---------- поселения ----------

    private fun capital(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        canvas.drawCircle(cx, cy, s * 1.05f, fill)
        canvas.drawCircle(cx, cy, s * 1.05f, stroke)
        star(path, cx, cy, s * 0.78f, s * 0.34f, 5)
        val old = fill.color
        fill.color = 0xFFFFF6DC.toInt()
        canvas.drawPath(path, fill)
        fill.color = old
        canvas.drawPath(path, stroke)
    }

    private fun city(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        canvas.drawCircle(cx, cy, s, fill)
        canvas.drawCircle(cx, cy, s, stroke)
        canvas.drawCircle(cx, cy, s * 0.42f, stroke)
        // зубцы стены
        val n = 8
        for (i in 0 until n) {
            val a = (i * 2.0 * Math.PI / n).toFloat()
            val r1 = s
            val r2 = s * 1.3f
            canvas.drawLine(
                cx + cos(a) * r1, cy + sin(a) * r1,
                cx + cos(a) * r2, cy + sin(a) * r2, stroke
            )
        }
    }

    private fun town(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        canvas.drawCircle(cx, cy, s * 0.85f, fill)
        canvas.drawCircle(cx, cy, s * 0.85f, stroke)
        canvas.drawCircle(cx, cy, s * 0.3f, stroke)
    }

    private fun house(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        val w = s * 0.9f
        rect.set(cx - w, cy - w * 0.15f, cx + w, cy + w)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
        path.reset()
        path.moveTo(cx - w * 1.2f, cy - w * 0.15f)
        path.lineTo(cx, cy - w * 1.1f)
        path.lineTo(cx + w * 1.2f, cy - w * 0.15f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
    }

    private fun castle(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        val w = s * 1.15f
        val h = s * 1.0f
        rect.set(cx - w, cy - h * 0.25f, cx + w, cy + h)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
        // зубцы
        val step = w * 2f / 5f
        var x = cx - w
        var i = 0
        while (x < cx + w - 0.01f) {
            if (i % 2 == 0) {
                rect.set(x, cy - h * 0.75f, x + step, cy - h * 0.25f)
                canvas.drawRect(rect, fill)
                canvas.drawRect(rect, stroke)
            }
            x += step
            i++
        }
        // флаг
        canvas.drawLine(cx, cy - h * 0.75f, cx, cy - h * 1.5f, stroke)
        path.reset()
        path.moveTo(cx, cy - h * 1.5f)
        path.lineTo(cx + w * 0.8f, cy - h * 1.3f)
        path.lineTo(cx, cy - h * 1.1f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
    }

    private fun tower(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        val w = s * 0.55f
        rect.set(cx - w, cy - s * 0.5f, cx + w, cy + s)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
        path.reset()
        path.moveTo(cx - w * 1.5f, cy - s * 0.5f)
        path.lineTo(cx, cy - s * 1.6f)
        path.lineTo(cx + w * 1.5f, cy - s * 0.5f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
    }

    private fun gate(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        val w = s * 0.95f
        rect.set(cx - w, cy - s * 0.9f, cx - w * 0.45f, cy + s)
        canvas.drawRect(rect, fill); canvas.drawRect(rect, stroke)
        rect.set(cx + w * 0.45f, cy - s * 0.9f, cx + w, cy + s)
        canvas.drawRect(rect, fill); canvas.drawRect(rect, stroke)
        rect.set(cx - w, cy - s * 1.25f, cx + w, cy - s * 0.9f)
        canvas.drawRect(rect, fill); canvas.drawRect(rect, stroke)
    }

    private fun wall(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        val w = s * 1.3f
        rect.set(cx - w, cy - s * 0.1f, cx + w, cy + s * 0.7f)
        canvas.drawRect(rect, fill); canvas.drawRect(rect, stroke)
        val step = w * 2f / 5f
        var x = cx - w
        var i = 0
        while (x < cx + w - 0.01f) {
            if (i % 2 == 0) {
                rect.set(x, cy - s * 0.6f, x + step, cy - s * 0.1f)
                canvas.drawRect(rect, fill); canvas.drawRect(rect, stroke)
            }
            x += step; i++
        }
    }

    private fun camp(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s, cy + s * 0.8f)
        path.lineTo(cx, cy - s)
        path.lineTo(cx + s, cy + s * 0.8f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx, cy - s, cx, cy + s * 0.8f, stroke)
    }

    private fun battle(canvas: Canvas, cx: Float, cy: Float, s: Float, stroke: Paint) {
        canvas.drawLine(cx - s, cy + s, cx + s, cy - s, stroke)
        canvas.drawLine(cx + s, cy + s, cx - s, cy - s, stroke)
        canvas.drawLine(cx - s * 0.75f, cy + s * 0.2f, cx - s * 0.15f, cy + s * 0.8f, stroke)
        canvas.drawLine(cx + s * 0.75f, cy + s * 0.2f, cx + s * 0.15f, cy + s * 0.8f, stroke)
    }

    private fun arena(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 1.2f, cy - s * 0.8f, cx + s * 1.2f, cy + s * 0.8f)
        canvas.drawOval(rect, fill)
        canvas.drawOval(rect, stroke)
        rect.set(cx - s * 0.6f, cy - s * 0.38f, cx + s * 0.6f, cy + s * 0.38f)
        canvas.drawOval(rect, stroke)
    }

    // ---------- море ----------

    private fun anchor(canvas: Canvas, cx: Float, cy: Float, s: Float, stroke: Paint) {
        canvas.drawCircle(cx, cy - s * 0.85f, s * 0.28f, stroke)
        canvas.drawLine(cx, cy - s * 0.55f, cx, cy + s * 0.9f, stroke)
        canvas.drawLine(cx - s * 0.7f, cy - s * 0.25f, cx + s * 0.7f, cy - s * 0.25f, stroke)
        rect.set(cx - s * 0.85f, cy - s * 0.1f, cx + s * 0.85f, cy + s)
        canvas.drawArc(rect, 20f, 140f, false, stroke)
    }

    private fun ship(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s, cy + s * 0.25f)
        path.lineTo(cx + s, cy + s * 0.25f)
        path.lineTo(cx + s * 0.6f, cy + s * 0.9f)
        path.lineTo(cx - s * 0.6f, cy + s * 0.9f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx, cy + s * 0.25f, cx, cy - s * 1.1f, stroke)
        path.reset()
        path.moveTo(cx + s * 0.06f, cy - s)
        path.lineTo(cx + s * 0.85f, cy + s * 0.05f)
        path.lineTo(cx + s * 0.06f, cy + s * 0.05f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
    }

    private fun lighthouse(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.55f, cy + s)
        path.lineTo(cx - s * 0.3f, cy - s * 0.5f)
        path.lineTo(cx + s * 0.3f, cy - s * 0.5f)
        path.lineTo(cx + s * 0.55f, cy + s)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        rect.set(cx - s * 0.42f, cy - s * 1.05f, cx + s * 0.42f, cy - s * 0.5f)
        canvas.drawRect(rect, fill); canvas.drawRect(rect, stroke)
        canvas.drawLine(cx - s * 1.1f, cy - s * 1.1f, cx - s * 0.55f, cy - s * 0.85f, stroke)
        canvas.drawLine(cx + s * 1.1f, cy - s * 1.1f, cx + s * 0.55f, cy - s * 0.85f, stroke)
    }

    private fun bridge(canvas: Canvas, cx: Float, cy: Float, s: Float, stroke: Paint) {
        rect.set(cx - s, cy - s * 0.4f, cx + s, cy + s * 0.9f)
        canvas.drawArc(rect, 180f, 180f, false, stroke)
        canvas.drawLine(cx - s * 1.15f, cy + s * 0.25f, cx - s * 0.85f, cy + s * 0.25f, stroke)
        canvas.drawLine(cx + s * 0.85f, cy + s * 0.25f, cx + s * 1.15f, cy + s * 0.25f, stroke)
    }

    private fun market(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.85f, cy - s * 0.1f, cx + s * 0.85f, cy + s * 0.9f)
        canvas.drawRect(rect, fill); canvas.drawRect(rect, stroke)
        path.reset()
        path.moveTo(cx - s * 1.15f, cy - s * 0.1f)
        path.lineTo(cx - s * 0.7f, cy - s * 0.85f)
        path.lineTo(cx + s * 0.7f, cy - s * 0.85f)
        path.lineTo(cx + s * 1.15f, cy - s * 0.1f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx - s * 0.3f, cy - s * 0.85f, cx - s * 0.5f, cy - s * 0.1f, stroke)
        canvas.drawLine(cx + s * 0.3f, cy - s * 0.85f, cx + s * 0.5f, cy - s * 0.1f, stroke)
    }

    // ---------- вера и память ----------

    private fun temple(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 1.1f, cy - s * 0.25f)
        path.lineTo(cx, cy - s * 1.15f)
        path.lineTo(cx + s * 1.1f, cy - s * 0.25f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        var x = cx - s * 0.8f
        while (x <= cx + s * 0.8f + 0.01f) {
            canvas.drawLine(x, cy - s * 0.25f, x, cy + s * 0.8f, stroke)
            x += s * 0.53f
        }
        canvas.drawLine(cx - s, cy + s * 0.8f, cx + s, cy + s * 0.8f, stroke)
    }

    private fun obelisk(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.35f, cy + s)
        path.lineTo(cx - s * 0.22f, cy - s * 0.7f)
        path.lineTo(cx, cy - s * 1.25f)
        path.lineTo(cx + s * 0.22f, cy - s * 0.7f)
        path.lineTo(cx + s * 0.35f, cy + s)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
    }

    private fun grave(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.65f, cy + s * 0.9f)
        path.lineTo(cx - s * 0.65f, cy - s * 0.2f)
        rect.set(cx - s * 0.65f, cy - s * 0.85f, cx + s * 0.65f, cy + s * 0.45f)
        path.arcTo(rect, 180f, 180f)
        path.lineTo(cx + s * 0.65f, cy + s * 0.9f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx, cy - s * 0.45f, cx, cy + s * 0.4f, stroke)
        canvas.drawLine(cx - s * 0.32f, cy - s * 0.1f, cx + s * 0.32f, cy - s * 0.1f, stroke)
    }

    // ---------- магия ----------

    private fun portal(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.75f, cy - s * 1.1f, cx + s * 0.75f, cy + s)
        canvas.drawOval(rect, fill)
        canvas.drawOval(rect, stroke)
        rect.set(cx - s * 0.42f, cy - s * 0.72f, cx + s * 0.42f, cy + s * 0.6f)
        canvas.drawOval(rect, stroke)
        canvas.drawLine(cx - s * 1.1f, cy - s * 0.9f, cx - s * 0.85f, cy - s * 1.15f, stroke)
        canvas.drawLine(cx + s * 1.1f, cy - s * 0.9f, cx + s * 0.85f, cy - s * 1.15f, stroke)
    }

    private fun stoneCircle(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        val n = 7
        for (i in 0 until n) {
            val a = (i * 2.0 * Math.PI / n).toFloat()
            val x = cx + cos(a) * s * 0.95f
            val y = cy + sin(a) * s * 0.6f
            rect.set(x - s * 0.16f, y - s * 0.36f, x + s * 0.16f, y + s * 0.3f)
            canvas.drawRect(rect, fill)
            canvas.drawRect(rect, stroke)
        }
    }

    private fun anomaly(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx, cy - s * 1.2f)
        path.quadTo(cx + s * 0.2f, cy - s * 0.2f, cx + s * 1.2f, cy)
        path.quadTo(cx + s * 0.2f, cy + s * 0.2f, cx, cy + s * 1.2f)
        path.quadTo(cx - s * 0.2f, cy + s * 0.2f, cx - s * 1.2f, cy)
        path.quadTo(cx - s * 0.2f, cy - s * 0.2f, cx, cy - s * 1.2f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
    }

    // ---------- руины и подземелья ----------

    private fun ruins(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        val heights = floatArrayOf(1.0f, 0.55f, 0.8f)
        for (i in 0..2) {
            val x = cx - s * 0.8f + i * s * 0.8f
            rect.set(x - s * 0.2f, cy + s * 0.7f - s * heights[i], x + s * 0.2f, cy + s * 0.7f)
            canvas.drawRect(rect, fill)
            canvas.drawRect(rect, stroke)
        }
        canvas.drawLine(cx - s * 1.15f, cy + s * 0.7f, cx + s * 1.15f, cy + s * 0.7f, stroke)
    }

    private fun dungeon(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.7f, cy + s * 0.9f)
        path.lineTo(cx - s * 0.7f, cy - s * 0.1f)
        rect.set(cx - s * 0.7f, cy - s * 0.8f, cx + s * 0.7f, cy + s * 0.6f)
        path.arcTo(rect, 180f, 180f)
        path.lineTo(cx + s * 0.7f, cy + s * 0.9f)
        path.close()
        val old = fill.color
        fill.color = 0xFF2B2620.toInt()
        canvas.drawPath(path, fill)
        fill.color = old
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx - s, cy + s * 0.9f, cx + s, cy + s * 0.9f, stroke)
    }

    private fun cave(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s, cy + s * 0.75f)
        path.cubicTo(cx - s * 0.9f, cy - s * 0.9f, cx + s * 0.9f, cy - s * 0.9f, cx + s, cy + s * 0.75f)
        path.close()
        val old = fill.color
        fill.color = 0xFF39322A.toInt()
        canvas.drawPath(path, fill)
        fill.color = old
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx - s * 1.2f, cy + s * 0.75f, cx + s * 1.2f, cy + s * 0.75f, stroke)
    }

    private fun mine(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 1.1f, cy + s * 0.8f)
        path.lineTo(cx, cy - s)
        path.lineTo(cx + s * 1.1f, cy + s * 0.8f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        path.reset()
        path.moveTo(cx - s * 0.4f, cy + s * 0.8f)
        path.lineTo(cx - s * 0.4f, cy + s * 0.15f)
        rect.set(cx - s * 0.4f, cy - s * 0.25f, cx + s * 0.4f, cy + s * 0.55f)
        path.arcTo(rect, 180f, 180f)
        path.lineTo(cx + s * 0.4f, cy + s * 0.8f)
        path.close()
        val old = fill.color
        fill.color = 0xFF2B2620.toInt()
        canvas.drawPath(path, fill)
        fill.color = old
    }

    // ---------- природа ----------

    private fun mountainGlyph(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 1.2f, cy + s * 0.8f)
        path.lineTo(cx - s * 0.15f, cy - s * 1.05f)
        path.lineTo(cx + s * 1.2f, cy + s * 0.8f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx - s * 0.5f, cy - s * 0.45f, cx - s * 0.15f, cy - s * 1.05f, stroke)
        canvas.drawLine(cx + s * 0.25f, cy - s * 0.45f, cx - s * 0.15f, cy - s * 1.05f, stroke)
    }

    private fun volcano(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 1.2f, cy + s * 0.8f)
        path.lineTo(cx - s * 0.42f, cy - s * 0.75f)
        path.lineTo(cx + s * 0.42f, cy - s * 0.75f)
        path.lineTo(cx + s * 1.2f, cy + s * 0.8f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        val old = stroke.color
        stroke.color = 0xFFB4502E.toInt()
        canvas.drawLine(cx - s * 0.3f, cy - s * 0.75f, cx - s * 0.15f, cy - s * 1.3f, stroke)
        canvas.drawLine(cx + s * 0.05f, cy - s * 0.75f, cx + s * 0.25f, cy - s * 1.45f, stroke)
        stroke.color = old
    }

    private fun waterfall(canvas: Canvas, cx: Float, cy: Float, s: Float, stroke: Paint) {
        canvas.drawLine(cx - s, cy - s * 0.75f, cx + s, cy - s * 0.75f, stroke)
        var x = cx - s * 0.7f
        while (x <= cx + s * 0.7f + 0.01f) {
            canvas.drawLine(x, cy - s * 0.7f, x, cy + s * 0.75f, stroke)
            x += s * 0.45f
        }
        canvas.drawLine(cx - s, cy + s * 0.95f, cx + s, cy + s * 0.95f, stroke)
    }

    private fun geyser(canvas: Canvas, cx: Float, cy: Float, s: Float, stroke: Paint) {
        canvas.drawLine(cx - s, cy + s * 0.9f, cx + s, cy + s * 0.9f, stroke)
        canvas.drawLine(cx, cy + s * 0.9f, cx - s * 0.35f, cy - s * 1.1f, stroke)
        canvas.drawLine(cx, cy + s * 0.9f, cx + s * 0.35f, cy - s * 1.1f, stroke)
        canvas.drawLine(cx, cy + s * 0.7f, cx, cy - s * 1.3f, stroke)
    }

    private fun whirlpool(canvas: Canvas, cx: Float, cy: Float, s: Float, stroke: Paint) {
        var r = s * 1.15f
        var start = 0f
        repeat(4) {
            rect.set(cx - r, cy - r, cx + r, cy + r)
            canvas.drawArc(rect, start, 270f, false, stroke)
            r *= 0.62f
            start += 120f
        }
    }

    private fun rock(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s, cy + s * 0.7f)
        path.lineTo(cx - s * 0.55f, cy - s * 0.5f)
        path.lineTo(cx + s * 0.15f, cy - s * 0.9f)
        path.lineTo(cx + s * 0.9f, cy - s * 0.2f)
        path.lineTo(cx + s, cy + s * 0.7f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
    }

    private fun treeGlyph(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        canvas.drawLine(cx, cy + s, cx, cy - s * 0.1f, stroke)
        canvas.drawCircle(cx, cy - s * 0.5f, s * 0.72f, fill)
        canvas.drawCircle(cx, cy - s * 0.5f, s * 0.72f, stroke)
    }

    private fun farm(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.8f, cy - s * 0.15f, cx + s * 0.8f, cy + s * 0.75f)
        canvas.drawRect(rect, fill); canvas.drawRect(rect, stroke)
        path.reset()
        path.moveTo(cx - s * 0.95f, cy - s * 0.15f)
        path.lineTo(cx, cy - s * 0.95f)
        path.lineTo(cx + s * 0.95f, cy - s * 0.15f)
        path.close()
        canvas.drawPath(path, fill); canvas.drawPath(path, stroke)
        canvas.drawLine(cx - s * 1.2f, cy + s * 1f, cx + s * 1.2f, cy + s * 1f, stroke)
    }

    private fun mill(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.5f, cy + s)
        path.lineTo(cx - s * 0.3f, cy - s * 0.35f)
        path.lineTo(cx + s * 0.3f, cy - s * 0.35f)
        path.lineTo(cx + s * 0.5f, cy + s)
        path.close()
        canvas.drawPath(path, fill); canvas.drawPath(path, stroke)
        val a = cy - s * 0.55f
        canvas.drawLine(cx - s * 0.95f, a - s * 0.5f, cx + s * 0.95f, a + s * 0.5f, stroke)
        canvas.drawLine(cx - s * 0.95f, a + s * 0.5f, cx + s * 0.95f, a - s * 0.5f, stroke)
    }

    private fun forge(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s, cy - s * 0.3f)
        path.lineTo(cx + s, cy - s * 0.3f)
        path.lineTo(cx + s * 0.45f, cy + s * 0.15f)
        path.lineTo(cx + s * 0.3f, cy + s * 0.8f)
        path.lineTo(cx - s * 0.3f, cy + s * 0.8f)
        path.lineTo(cx - s * 0.45f, cy + s * 0.15f)
        path.close()
        canvas.drawPath(path, fill); canvas.drawPath(path, stroke)
    }

    private fun monster(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        canvas.drawCircle(cx, cy - s * 0.2f, s * 0.8f, fill)
        canvas.drawCircle(cx, cy - s * 0.2f, s * 0.8f, stroke)
        val old = fill.color
        fill.color = 0xFF2B2620.toInt()
        canvas.drawCircle(cx - s * 0.32f, cy - s * 0.3f, s * 0.18f, fill)
        canvas.drawCircle(cx + s * 0.32f, cy - s * 0.3f, s * 0.18f, fill)
        fill.color = old
        rect.set(cx - s * 0.42f, cy + s * 0.35f, cx + s * 0.42f, cy + s * 0.95f)
        canvas.drawRect(rect, fill); canvas.drawRect(rect, stroke)
        canvas.drawLine(cx, cy + s * 0.35f, cx, cy + s * 0.95f, stroke)
    }

    private fun treasure(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.9f, cy - s * 0.1f, cx + s * 0.9f, cy + s * 0.8f)
        canvas.drawRect(rect, fill); canvas.drawRect(rect, stroke)
        rect.set(cx - s * 0.9f, cy - s * 0.75f, cx + s * 0.9f, cy + s * 0.55f)
        canvas.drawArc(rect, 180f, 180f, false, stroke)
        canvas.drawLine(cx - s * 0.9f, cy - s * 0.1f, cx + s * 0.9f, cy - s * 0.1f, stroke)
        canvas.drawCircle(cx, cy + s * 0.2f, s * 0.16f, stroke)
    }

    /** Пятиконечная (или иная) звезда. */
    private fun star(out: Path, cx: Float, cy: Float, outer: Float, inner: Float, points: Int) {
        out.reset()
        val step = Math.PI / points
        var angle = -Math.PI / 2
        for (i in 0 until points * 2) {
            val r = if (i % 2 == 0) outer else inner
            val x = cx + (cos(angle) * r).toFloat()
            val y = cy + (sin(angle) * r).toFloat()
            if (i == 0) out.moveTo(x, y) else out.lineTo(x, y)
            angle += step
        }
        out.close()
    }

    // ---------- текстуры природных зон ----------

    /** Один элемент текстуры ландшафта в точке (x, y) размером s. */
    fun drawPattern(canvas: Canvas, pattern: BiomePattern, x: Float, y: Float, s: Float, paint: Paint) {
        when (pattern) {
            BiomePattern.NONE -> Unit
            BiomePattern.TREES -> {
                canvas.drawLine(x, y + s * 0.6f, x, y, paint)
                rect.set(x - s * 0.5f, y - s * 0.75f, x + s * 0.5f, y + s * 0.15f)
                canvas.drawArc(rect, 0f, 360f, false, paint)
            }
            BiomePattern.CONIFERS -> {
                canvas.drawLine(x, y + s * 0.65f, x, y - s * 0.75f, paint)
                canvas.drawLine(x, y - s * 0.75f, x - s * 0.5f, y + s * 0.15f, paint)
                canvas.drawLine(x, y - s * 0.75f, x + s * 0.5f, y + s * 0.15f, paint)
            }
            BiomePattern.PALMS -> {
                canvas.drawLine(x, y + s * 0.7f, x - s * 0.1f, y - s * 0.4f, paint)
                canvas.drawLine(x - s * 0.1f, y - s * 0.4f, x - s * 0.7f, y - s * 0.7f, paint)
                canvas.drawLine(x - s * 0.1f, y - s * 0.4f, x + s * 0.6f, y - s * 0.75f, paint)
                canvas.drawLine(x - s * 0.1f, y - s * 0.4f, x + s * 0.5f, y - s * 0.1f, paint)
            }
            BiomePattern.DOTS -> canvas.drawPoint(x, y, paint)
            BiomePattern.DUNES -> {
                rect.set(x - s * 0.8f, y - s * 0.5f, x + s * 0.8f, y + s * 0.5f)
                canvas.drawArc(rect, 200f, 140f, false, paint)
            }
            BiomePattern.GRASS -> {
                canvas.drawLine(x, y + s * 0.4f, x - s * 0.3f, y - s * 0.5f, paint)
                canvas.drawLine(x, y + s * 0.4f, x, y - s * 0.65f, paint)
                canvas.drawLine(x, y + s * 0.4f, x + s * 0.3f, y - s * 0.5f, paint)
            }
            BiomePattern.MOUNTAINS -> {
                canvas.drawLine(x - s * 0.85f, y + s * 0.5f, x - s * 0.1f, y - s * 0.8f, paint)
                canvas.drawLine(x - s * 0.1f, y - s * 0.8f, x + s * 0.7f, y + s * 0.5f, paint)
                canvas.drawLine(x + s * 0.25f, y + s * 0.5f, x + s * 0.75f, y - s * 0.35f, paint)
                canvas.drawLine(x + s * 0.75f, y - s * 0.35f, x + s * 1.2f, y + s * 0.5f, paint)
            }
            BiomePattern.HILLS -> {
                rect.set(x - s * 0.9f, y - s * 0.3f, x + s * 0.1f, y + s * 0.7f)
                canvas.drawArc(rect, 190f, 160f, false, paint)
                rect.set(x - s * 0.05f, y - s * 0.15f, x + s * 0.85f, y + s * 0.7f)
                canvas.drawArc(rect, 190f, 160f, false, paint)
            }
            BiomePattern.SWAMP -> {
                canvas.drawLine(x - s * 0.8f, y, x + s * 0.8f, y, paint)
                canvas.drawLine(x - s * 0.45f, y + s * 0.45f, x + s * 0.35f, y + s * 0.45f, paint)
                canvas.drawLine(x - s * 0.15f, y - s * 0.65f, x - s * 0.15f, y - s * 0.05f, paint)
            }
            BiomePattern.ICE -> {
                canvas.drawLine(x - s * 0.6f, y, x + s * 0.6f, y, paint)
                canvas.drawLine(x, y - s * 0.6f, x, y + s * 0.6f, paint)
                canvas.drawLine(x - s * 0.42f, y - s * 0.42f, x + s * 0.42f, y + s * 0.42f, paint)
                canvas.drawLine(x - s * 0.42f, y + s * 0.42f, x + s * 0.42f, y - s * 0.42f, paint)
            }
            BiomePattern.ROCKS -> {
                path.reset()
                path.moveTo(x - s * 0.6f, y + s * 0.4f)
                path.lineTo(x - s * 0.15f, y - s * 0.5f)
                path.lineTo(x + s * 0.55f, y + s * 0.4f)
                path.close()
                canvas.drawPath(path, paint)
            }
            BiomePattern.WAVES -> {
                rect.set(x - s * 0.8f, y - s * 0.4f, x, y + s * 0.4f)
                canvas.drawArc(rect, 200f, 140f, false, paint)
                rect.set(x, y - s * 0.4f, x + s * 0.8f, y + s * 0.4f)
                canvas.drawArc(rect, 200f, 140f, false, paint)
            }
            BiomePattern.CRACKS -> {
                canvas.drawLine(x - s * 0.7f, y - s * 0.3f, x - s * 0.1f, y + s * 0.2f, paint)
                canvas.drawLine(x - s * 0.1f, y + s * 0.2f, x + s * 0.35f, y - s * 0.35f, paint)
                canvas.drawLine(x - s * 0.1f, y + s * 0.2f, x + s * 0.15f, y + s * 0.7f, paint)
            }
            BiomePattern.CRYSTALS -> {
                path.reset()
                path.moveTo(x, y - s * 0.8f)
                path.lineTo(x + s * 0.45f, y)
                path.lineTo(x, y + s * 0.7f)
                path.lineTo(x - s * 0.45f, y)
                path.close()
                canvas.drawPath(path, paint)
            }
            BiomePattern.FUNGI -> {
                canvas.drawLine(x, y + s * 0.6f, x, y - s * 0.05f, paint)
                rect.set(x - s * 0.6f, y - s * 0.6f, x + s * 0.6f, y + s * 0.45f)
                canvas.drawArc(rect, 180f, 180f, false, paint)
            }
            BiomePattern.LAVA -> {
                rect.set(x - s * 0.7f, y - s * 0.35f, x + s * 0.7f, y + s * 0.35f)
                canvas.drawArc(rect, 0f, 360f, false, paint)
                canvas.drawLine(x - s * 0.25f, y, x + s * 0.3f, y, paint)
            }
            BiomePattern.FIELDS -> {
                rect.set(x - s * 0.7f, y - s * 0.5f, x + s * 0.7f, y + s * 0.5f)
                canvas.drawRect(rect, paint)
                canvas.drawLine(x - s * 0.7f, y, x + s * 0.7f, y, paint)
            }
        }
    }
}
