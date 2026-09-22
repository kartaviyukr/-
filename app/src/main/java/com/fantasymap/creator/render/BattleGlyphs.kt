package com.fantasymap.creator.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.fantasymap.creator.model.Glyph
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Значки существ и обстановки боевой локации.
 * Те же правила, что у остальных значков: центр (cx, cy), s — характерный радиус.
 */
class BattleGlyphs {

    private val path = Path()
    private val rect = RectF()

    fun draw(canvas: Canvas, glyph: Glyph, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        when (glyph) {
            Glyph.HUMANOID -> humanoid(canvas, cx, cy, s, fill, stroke)
            Glyph.GOBLIN -> goblin(canvas, cx, cy, s, fill, stroke)
            Glyph.SKULL -> skull(canvas, cx, cy, s, fill, stroke)
            Glyph.PAW -> paw(canvas, cx, cy, s, fill, stroke)
            Glyph.SPIDER -> spider(canvas, cx, cy, s, fill, stroke)
            Glyph.SERPENT -> serpent(canvas, cx, cy, s, stroke)
            Glyph.BAT -> bat(canvas, cx, cy, s, fill, stroke)
            Glyph.HORNS -> horns(canvas, cx, cy, s, fill, stroke)
            Glyph.FLAME -> flame(canvas, cx, cy, s, fill, stroke)
            Glyph.WINGS -> wings(canvas, cx, cy, s, fill, stroke)
            Glyph.TENTACLES -> tentacles(canvas, cx, cy, s, fill, stroke)
            Glyph.GOLEM -> golem(canvas, cx, cy, s, fill, stroke)
            Glyph.OOZE -> ooze(canvas, cx, cy, s, fill, stroke)
            Glyph.GHOST -> ghost(canvas, cx, cy, s, fill, stroke)
            Glyph.GIANT -> giant(canvas, cx, cy, s, fill, stroke)
            Glyph.HAT -> hat(canvas, cx, cy, s, fill, stroke)
            Glyph.DAGGER -> dagger(canvas, cx, cy, s, fill, stroke)
            Glyph.BOW -> bow(canvas, cx, cy, s, stroke)
            Glyph.SUN -> sun(canvas, cx, cy, s, fill, stroke)
            Glyph.LUTE -> lute(canvas, cx, cy, s, fill, stroke)
            Glyph.LEAF -> leaf(canvas, cx, cy, s, fill, stroke)
            Glyph.SHIELD -> shield(canvas, cx, cy, s, fill, stroke)
            Glyph.LOCK -> lock(canvas, cx, cy, s, fill, stroke)
            Glyph.CHEST -> chest(canvas, cx, cy, s, fill, stroke)
            Glyph.DOOR -> door(canvas, cx, cy, s, fill, stroke)
            Glyph.STAIRS -> stairs(canvas, cx, cy, s, fill, stroke)
            Glyph.LADDER -> ladder(canvas, cx, cy, s, stroke)
            Glyph.WINDOW -> window(canvas, cx, cy, s, fill, stroke)
            Glyph.TABLE -> table(canvas, cx, cy, s, fill, stroke)
            Glyph.CHAIR -> chair(canvas, cx, cy, s, fill, stroke)
            Glyph.BED -> bed(canvas, cx, cy, s, fill, stroke)
            Glyph.CRATE -> crate(canvas, cx, cy, s, fill, stroke)
            Glyph.BOOKSHELF -> bookshelf(canvas, cx, cy, s, fill, stroke)
            Glyph.THRONE -> throne(canvas, cx, cy, s, fill, stroke)
            Glyph.ALTAR -> altar(canvas, cx, cy, s, fill, stroke)
            Glyph.COFFIN -> coffin(canvas, cx, cy, s, fill, stroke)
            Glyph.ANVIL -> anvil(canvas, cx, cy, s, fill, stroke)
            Glyph.FIREPLACE -> fireplace(canvas, cx, cy, s, fill, stroke)
            Glyph.BRAZIER -> brazier(canvas, cx, cy, s, fill, stroke)
            Glyph.TORCH -> torch(canvas, cx, cy, s, fill, stroke)
            Glyph.PILLAR -> pillar(canvas, cx, cy, s, fill, stroke)
            Glyph.CAGE -> cage(canvas, cx, cy, s, fill, stroke)
            Glyph.SACK -> sack(canvas, cx, cy, s, fill, stroke)
            Glyph.BUSH -> bush(canvas, cx, cy, s, fill, stroke)
            Glyph.STUMP -> stump(canvas, cx, cy, s, fill, stroke)
            Glyph.CAMPFIRE -> campfire(canvas, cx, cy, s, fill, stroke)
            Glyph.WEB -> web(canvas, cx, cy, s, stroke)
            Glyph.SPIKES -> spikes(canvas, cx, cy, s, fill, stroke)
            Glyph.PIT -> pit(canvas, cx, cy, s, fill, stroke)
            Glyph.BEAR_TRAP -> bearTrap(canvas, cx, cy, s, fill, stroke)
            Glyph.PLATE -> plate(canvas, cx, cy, s, fill, stroke)
            Glyph.ARROW -> arrow(canvas, cx, cy, s, fill, stroke)
            Glyph.LEVER -> lever(canvas, cx, cy, s, fill, stroke)
            Glyph.SCROLL -> scroll(canvas, cx, cy, s, fill, stroke)
            Glyph.KEY -> key(canvas, cx, cy, s, fill, stroke)
            Glyph.TARGET -> target(canvas, cx, cy, s, fill, stroke)
            else -> {
                canvas.drawCircle(cx, cy, s * 0.6f, fill)
                canvas.drawCircle(cx, cy, s * 0.6f, stroke)
            }
        }
    }

    private fun both(canvas: Canvas, p: Path, fill: Paint, stroke: Paint) {
        canvas.drawPath(p, fill)
        canvas.drawPath(p, stroke)
    }

    private fun box(canvas: Canvas, l: Float, t: Float, r: Float, b: Float, fill: Paint, stroke: Paint) {
        rect.set(l, t, r, b)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
    }

    private fun circle(canvas: Canvas, x: Float, y: Float, r: Float, fill: Paint, stroke: Paint) {
        canvas.drawCircle(x, y, r, fill)
        canvas.drawCircle(x, y, r, stroke)
    }

    // ---------------------------------------------------------------- существа

    private fun humanoid(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        circle(canvas, cx, cy - s * 0.45f, s * 0.32f, fill, stroke)
        path.reset()
        path.moveTo(cx - s * 0.6f, cy + s * 0.85f)
        path.quadTo(cx - s * 0.6f, cy - s * 0.05f, cx, cy - s * 0.05f)
        path.quadTo(cx + s * 0.6f, cy - s * 0.05f, cx + s * 0.6f, cy + s * 0.85f)
        path.close()
        both(canvas, path, fill, stroke)
    }

    private fun goblin(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.95f, cy - s * 0.45f)
        path.lineTo(cx - s * 0.4f, cy - s * 0.15f)
        path.lineTo(cx - s * 0.4f, cy + s * 0.3f)
        path.quadTo(cx, cy + s * 0.85f, cx + s * 0.4f, cy + s * 0.3f)
        path.lineTo(cx + s * 0.4f, cy - s * 0.15f)
        path.lineTo(cx + s * 0.95f, cy - s * 0.45f)
        path.lineTo(cx + s * 0.35f, cy - s * 0.55f)
        path.quadTo(cx, cy - s * 0.85f, cx - s * 0.35f, cy - s * 0.55f)
        path.close()
        both(canvas, path, fill, stroke)
        canvas.drawCircle(cx - s * 0.18f, cy - s * 0.12f, s * 0.07f, stroke)
        canvas.drawCircle(cx + s * 0.18f, cy - s * 0.12f, s * 0.07f, stroke)
        canvas.drawLine(cx - s * 0.15f, cy + s * 0.3f, cx + s * 0.15f, cy + s * 0.3f, stroke)
    }

    private fun skull(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        rect.set(cx - s * 0.7f, cy - s * 0.85f, cx + s * 0.7f, cy + s * 0.45f)
        path.addOval(rect, Path.Direction.CW)
        both(canvas, path, fill, stroke)
        box(canvas, cx - s * 0.35f, cy + s * 0.3f, cx + s * 0.35f, cy + s * 0.75f, fill, stroke)
        val eye = s * 0.18f
        canvas.drawCircle(cx - s * 0.28f, cy - s * 0.15f, eye, stroke)
        canvas.drawCircle(cx + s * 0.28f, cy - s * 0.15f, eye, stroke)
        canvas.drawLine(cx - s * 0.12f, cy + s * 0.35f, cx - s * 0.12f, cy + s * 0.72f, stroke)
        canvas.drawLine(cx + s * 0.12f, cy + s * 0.35f, cx + s * 0.12f, cy + s * 0.72f, stroke)
    }

    private fun paw(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.45f, cy - s * 0.05f, cx + s * 0.45f, cy + s * 0.75f)
        canvas.drawOval(rect, fill)
        canvas.drawOval(rect, stroke)
        val toes = listOf(-0.62f to -0.2f, -0.24f to -0.6f, 0.24f to -0.6f, 0.62f to -0.2f)
        for ((dx, dy) in toes) circle(canvas, cx + s * dx, cy + s * dy, s * 0.2f, fill, stroke)
    }

    private fun spider(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        for (side in listOf(-1f, 1f)) {
            for (i in 0 until 4) {
                val y = cy - s * 0.45f + i * s * 0.3f
                path.reset()
                path.moveTo(cx, y)
                path.lineTo(cx + side * s * 0.6f, y - s * 0.25f)
                path.lineTo(cx + side * s * 0.95f, y + s * 0.25f)
                canvas.drawPath(path, stroke)
            }
        }
        circle(canvas, cx, cy + s * 0.2f, s * 0.38f, fill, stroke)
        circle(canvas, cx, cy - s * 0.35f, s * 0.22f, fill, stroke)
    }

    private fun serpent(canvas: Canvas, cx: Float, cy: Float, s: Float, stroke: Paint) {
        val width = stroke.strokeWidth
        stroke.strokeWidth = width * 2.2f
        path.reset()
        path.moveTo(cx - s * 0.85f, cy + s * 0.6f)
        path.cubicTo(cx - s * 0.2f, cy + s * 0.9f, cx + s * 0.6f, cy + s * 0.3f, cx, cy)
        path.cubicTo(cx - s * 0.6f, cy - s * 0.3f, cx + s * 0.1f, cy - s * 0.9f, cx + s * 0.6f, cy - s * 0.6f)
        canvas.drawPath(path, stroke)
        stroke.strokeWidth = width
        canvas.drawLine(cx + s * 0.6f, cy - s * 0.6f, cx + s * 0.9f, cy - s * 0.75f, stroke)
    }

    private fun bat(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx, cy - s * 0.25f)
        path.quadTo(cx - s * 0.5f, cy - s * 0.7f, cx - s, cy - s * 0.3f)
        path.quadTo(cx - s * 0.75f, cy - s * 0.1f, cx - s * 0.7f, cy + s * 0.3f)
        path.quadTo(cx - s * 0.45f, cy + s * 0.05f, cx - s * 0.3f, cy + s * 0.35f)
        path.quadTo(cx - s * 0.15f, cy + s * 0.15f, cx, cy + s * 0.5f)
        path.quadTo(cx + s * 0.15f, cy + s * 0.15f, cx + s * 0.3f, cy + s * 0.35f)
        path.quadTo(cx + s * 0.45f, cy + s * 0.05f, cx + s * 0.7f, cy + s * 0.3f)
        path.quadTo(cx + s * 0.75f, cy - s * 0.1f, cx + s, cy - s * 0.3f)
        path.quadTo(cx + s * 0.5f, cy - s * 0.7f, cx, cy - s * 0.25f)
        path.close()
        both(canvas, path, fill, stroke)
    }

    private fun horns(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        circle(canvas, cx, cy + s * 0.2f, s * 0.5f, fill, stroke)
        for (side in listOf(-1f, 1f)) {
            path.reset()
            path.moveTo(cx + side * s * 0.3f, cy - s * 0.15f)
            path.quadTo(cx + side * s * 0.95f, cy - s * 0.2f, cx + side * s * 0.8f, cy - s * 0.95f)
            path.quadTo(cx + side * s * 0.65f, cy - s * 0.45f, cx + side * s * 0.1f, cy - s * 0.25f)
            path.close()
            both(canvas, path, fill, stroke)
        }
        canvas.drawCircle(cx - s * 0.18f, cy + s * 0.12f, s * 0.07f, stroke)
        canvas.drawCircle(cx + s * 0.18f, cy + s * 0.12f, s * 0.07f, stroke)
    }

    private fun flame(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx, cy - s)
        path.cubicTo(cx + s * 0.35f, cy - s * 0.4f, cx + s * 0.85f, cy - s * 0.1f, cx + s * 0.6f, cy + s * 0.5f)
        path.quadTo(cx + s * 0.4f, cy + s * 0.95f, cx, cy + s * 0.9f)
        path.quadTo(cx - s * 0.4f, cy + s * 0.95f, cx - s * 0.6f, cy + s * 0.5f)
        path.cubicTo(cx - s * 0.8f, cy, cx - s * 0.2f, cy - s * 0.2f, cx, cy - s)
        path.close()
        both(canvas, path, fill, stroke)
        path.reset()
        path.moveTo(cx, cy - s * 0.1f)
        path.quadTo(cx + s * 0.35f, cy + s * 0.3f, cx, cy + s * 0.65f)
        path.quadTo(cx - s * 0.35f, cy + s * 0.3f, cx, cy - s * 0.1f)
        canvas.drawPath(path, stroke)
    }

    private fun wings(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        for (side in listOf(-1f, 1f)) {
            path.reset()
            path.moveTo(cx + side * s * 0.1f, cy)
            path.quadTo(cx + side * s * 0.6f, cy - s * 0.9f, cx + side * s, cy - s * 0.6f)
            path.lineTo(cx + side * s * 0.75f, cy - s * 0.3f)
            path.lineTo(cx + side * s * 0.9f, cy - s * 0.05f)
            path.lineTo(cx + side * s * 0.6f, cy + s * 0.05f)
            path.lineTo(cx + side * s * 0.65f, cy + s * 0.35f)
            path.quadTo(cx + side * s * 0.3f, cy + s * 0.3f, cx + side * s * 0.1f, cy)
            path.close()
            both(canvas, path, fill, stroke)
        }
        rect.set(cx - s * 0.15f, cy - s * 0.35f, cx + s * 0.15f, cy + s * 0.55f)
        canvas.drawOval(rect, fill)
        canvas.drawOval(rect, stroke)
    }

    private fun tentacles(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        val width = stroke.strokeWidth
        stroke.strokeWidth = width * 1.6f
        for (i in 0 until 5) {
            val x = cx - s * 0.6f + i * s * 0.3f
            path.reset()
            path.moveTo(x, cy)
            path.quadTo(x + s * 0.25f * (if (i % 2 == 0) 1 else -1), cy + s * 0.5f, x, cy + s * 0.9f)
            canvas.drawPath(path, stroke)
        }
        stroke.strokeWidth = width
        rect.set(cx - s * 0.7f, cy - s * 0.9f, cx + s * 0.7f, cy + s * 0.2f)
        canvas.drawOval(rect, fill)
        canvas.drawOval(rect, stroke)
        canvas.drawCircle(cx, cy - s * 0.35f, s * 0.18f, stroke)
    }

    private fun golem(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        box(canvas, cx - s * 0.3f, cy - s * 0.95f, cx + s * 0.3f, cy - s * 0.45f, fill, stroke)
        box(canvas, cx - s * 0.6f, cy - s * 0.4f, cx + s * 0.6f, cy + s * 0.35f, fill, stroke)
        box(canvas, cx - s * 0.95f, cy - s * 0.4f, cx - s * 0.65f, cy + s * 0.25f, fill, stroke)
        box(canvas, cx + s * 0.65f, cy - s * 0.4f, cx + s * 0.95f, cy + s * 0.25f, fill, stroke)
        box(canvas, cx - s * 0.5f, cy + s * 0.4f, cx - s * 0.1f, cy + s * 0.95f, fill, stroke)
        box(canvas, cx + s * 0.1f, cy + s * 0.4f, cx + s * 0.5f, cy + s * 0.95f, fill, stroke)
    }

    private fun ooze(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.95f, cy + s * 0.7f)
        path.cubicTo(cx - s * 0.9f, cy - s * 0.2f, cx - s * 0.4f, cy - s * 0.95f, cx, cy - s * 0.8f)
        path.cubicTo(cx + s * 0.5f, cy - s * 0.95f, cx + s * 0.95f, cy - s * 0.1f, cx + s * 0.95f, cy + s * 0.7f)
        path.quadTo(cx + s * 0.6f, cy + s * 0.5f, cx + s * 0.35f, cy + s * 0.75f)
        path.quadTo(cx, cy + s * 0.5f, cx - s * 0.35f, cy + s * 0.75f)
        path.quadTo(cx - s * 0.6f, cy + s * 0.5f, cx - s * 0.95f, cy + s * 0.7f)
        path.close()
        both(canvas, path, fill, stroke)
        canvas.drawCircle(cx - s * 0.25f, cy - s * 0.2f, s * 0.1f, stroke)
        canvas.drawCircle(cx + s * 0.3f, cy - s * 0.05f, s * 0.14f, stroke)
    }

    private fun ghost(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.65f, cy + s * 0.9f)
        path.lineTo(cx - s * 0.65f, cy - s * 0.2f)
        path.quadTo(cx - s * 0.65f, cy - s * 0.95f, cx, cy - s * 0.95f)
        path.quadTo(cx + s * 0.65f, cy - s * 0.95f, cx + s * 0.65f, cy - s * 0.2f)
        path.lineTo(cx + s * 0.65f, cy + s * 0.9f)
        path.lineTo(cx + s * 0.32f, cy + s * 0.6f)
        path.lineTo(cx, cy + s * 0.9f)
        path.lineTo(cx - s * 0.32f, cy + s * 0.6f)
        path.close()
        both(canvas, path, fill, stroke)
        canvas.drawCircle(cx - s * 0.22f, cy - s * 0.3f, s * 0.1f, stroke)
        canvas.drawCircle(cx + s * 0.22f, cy - s * 0.3f, s * 0.1f, stroke)
    }

    private fun giant(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        circle(canvas, cx, cy - s * 0.6f, s * 0.28f, fill, stroke)
        path.reset()
        path.moveTo(cx - s * 0.9f, cy + s * 0.95f)
        path.lineTo(cx - s * 0.95f, cy - s * 0.1f)
        path.quadTo(cx, cy - s * 0.45f, cx + s * 0.95f, cy - s * 0.1f)
        path.lineTo(cx + s * 0.9f, cy + s * 0.95f)
        path.close()
        both(canvas, path, fill, stroke)
        canvas.drawLine(cx, cy - s * 0.2f, cx, cy + s * 0.95f, stroke)
    }

    private fun hat(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.95f, cy + s * 0.35f, cx + s * 0.95f, cy + s * 0.75f)
        canvas.drawOval(rect, fill)
        canvas.drawOval(rect, stroke)
        path.reset()
        path.moveTo(cx - s * 0.5f, cy + s * 0.5f)
        path.lineTo(cx + s * 0.1f, cy - s * 0.95f)
        path.quadTo(cx + s * 0.3f, cy - s * 0.6f, cx + s * 0.2f, cy - s * 0.45f)
        path.lineTo(cx + s * 0.5f, cy + s * 0.5f)
        path.close()
        both(canvas, path, fill, stroke)
        canvas.drawCircle(cx + s * 0.05f, cy - s * 0.05f, s * 0.08f, stroke)
    }

    private fun dagger(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx, cy - s)
        path.lineTo(cx + s * 0.18f, cy + s * 0.15f)
        path.lineTo(cx - s * 0.18f, cy + s * 0.15f)
        path.close()
        both(canvas, path, fill, stroke)
        canvas.drawLine(cx - s * 0.45f, cy + s * 0.2f, cx + s * 0.45f, cy + s * 0.2f, stroke)
        canvas.drawLine(cx, cy + s * 0.2f, cx, cy + s * 0.8f, stroke)
        circle(canvas, cx, cy + s * 0.88f, s * 0.1f, fill, stroke)
    }

    private fun bow(canvas: Canvas, cx: Float, cy: Float, s: Float, stroke: Paint) {
        rect.set(cx - s * 0.9f, cy - s * 0.95f, cx + s * 0.3f, cy + s * 0.95f)
        canvas.drawArc(rect, -80f, 160f, false, stroke)
        canvas.drawLine(cx - s * 0.2f, cy - s * 0.9f, cx - s * 0.2f, cy + s * 0.9f, stroke)
        canvas.drawLine(cx - s * 0.6f, cy, cx + s * 0.9f, cy, stroke)
        canvas.drawLine(cx + s * 0.9f, cy, cx + s * 0.65f, cy - s * 0.15f, stroke)
        canvas.drawLine(cx + s * 0.9f, cy, cx + s * 0.65f, cy + s * 0.15f, stroke)
    }

    private fun sun(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        for (i in 0 until 8) {
            val a = i * PI.toFloat() / 4f
            canvas.drawLine(
                cx + cos(a) * s * 0.55f, cy + sin(a) * s * 0.55f,
                cx + cos(a) * s * 0.95f, cy + sin(a) * s * 0.95f, stroke
            )
        }
        circle(canvas, cx, cy, s * 0.42f, fill, stroke)
    }

    private fun lute(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.55f, cy - s * 0.1f, cx + s * 0.55f, cy + s * 0.95f)
        canvas.drawOval(rect, fill)
        canvas.drawOval(rect, stroke)
        box(canvas, cx - s * 0.1f, cy - s * 0.95f, cx + s * 0.1f, cy - s * 0.05f, fill, stroke)
        canvas.drawCircle(cx, cy + s * 0.45f, s * 0.15f, stroke)
    }

    private fun leaf(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.7f, cy + s * 0.7f)
        path.quadTo(cx - s * 0.8f, cy - s * 0.6f, cx + s * 0.8f, cy - s * 0.8f)
        path.quadTo(cx + s * 0.6f, cy + s * 0.8f, cx - s * 0.7f, cy + s * 0.7f)
        path.close()
        both(canvas, path, fill, stroke)
        canvas.drawLine(cx - s * 0.9f, cy + s * 0.9f, cx + s * 0.4f, cy - s * 0.4f, stroke)
    }

    private fun shield(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.75f, cy - s * 0.75f)
        path.lineTo(cx + s * 0.75f, cy - s * 0.75f)
        path.lineTo(cx + s * 0.75f, cy - s * 0.1f)
        path.quadTo(cx + s * 0.7f, cy + s * 0.6f, cx, cy + s * 0.95f)
        path.quadTo(cx - s * 0.7f, cy + s * 0.6f, cx - s * 0.75f, cy - s * 0.1f)
        path.close()
        both(canvas, path, fill, stroke)
        canvas.drawLine(cx, cy - s * 0.75f, cx, cy + s * 0.9f, stroke)
        canvas.drawLine(cx - s * 0.75f, cy - s * 0.15f, cx + s * 0.75f, cy - s * 0.15f, stroke)
    }

    private fun lock(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.45f, cy - s * 0.9f, cx + s * 0.45f, cy)
        canvas.drawArc(rect, 180f, 180f, false, stroke)
        canvas.drawLine(cx - s * 0.45f, cy - s * 0.45f, cx - s * 0.45f, cy - s * 0.1f, stroke)
        canvas.drawLine(cx + s * 0.45f, cy - s * 0.45f, cx + s * 0.45f, cy - s * 0.1f, stroke)
        box(canvas, cx - s * 0.7f, cy - s * 0.1f, cx + s * 0.7f, cy + s * 0.85f, fill, stroke)
        canvas.drawCircle(cx, cy + s * 0.3f, s * 0.12f, stroke)
        canvas.drawLine(cx, cy + s * 0.4f, cx, cy + s * 0.6f, stroke)
    }

    private fun chest(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        box(canvas, cx - s * 0.9f, cy - s * 0.2f, cx + s * 0.9f, cy + s * 0.7f, fill, stroke)
        path.reset()
        path.moveTo(cx - s * 0.9f, cy - s * 0.2f)
        path.quadTo(cx - s * 0.9f, cy - s * 0.8f, cx, cy - s * 0.8f)
        path.quadTo(cx + s * 0.9f, cy - s * 0.8f, cx + s * 0.9f, cy - s * 0.2f)
        path.close()
        both(canvas, path, fill, stroke)
        box(canvas, cx - s * 0.15f, cy - s * 0.3f, cx + s * 0.15f, cy + s * 0.1f, fill, stroke)
    }

    // ---------------------------------------------------------------- обстановка

    private fun door(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        // Створка в проёме и дуга, по которой она открывается.
        box(canvas, cx - s * 0.95f, cy + s * 0.2f, cx + s * 0.95f, cy + s * 0.5f, fill, stroke)
        canvas.drawLine(cx - s * 0.95f, cy + s * 0.2f, cx - s * 0.95f, cy - s * 0.95f, stroke)
        rect.set(cx - s * 2.1f, cy - s * 0.95f, cx + s * 0.2f, cy + s * 1.35f)
        canvas.drawArc(rect, -90f, 90f, false, stroke)
    }

    private fun stairs(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        box(canvas, cx - s * 0.8f, cy - s * 0.9f, cx + s * 0.8f, cy + s * 0.9f, fill, stroke)
        for (i in 1 until 6) {
            val y = cy - s * 0.9f + i * s * 0.3f
            canvas.drawLine(cx - s * 0.8f, y, cx + s * 0.8f, y, stroke)
        }
    }

    private fun ladder(canvas: Canvas, cx: Float, cy: Float, s: Float, stroke: Paint) {
        canvas.drawLine(cx - s * 0.4f, cy - s, cx - s * 0.4f, cy + s, stroke)
        canvas.drawLine(cx + s * 0.4f, cy - s, cx + s * 0.4f, cy + s, stroke)
        for (i in 0 until 5) {
            val y = cy - s * 0.8f + i * s * 0.4f
            canvas.drawLine(cx - s * 0.4f, y, cx + s * 0.4f, y, stroke)
        }
    }

    private fun window(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        box(canvas, cx - s * 0.95f, cy - s * 0.2f, cx + s * 0.95f, cy + s * 0.2f, fill, stroke)
        canvas.drawLine(cx - s * 0.95f, cy, cx + s * 0.95f, cy, stroke)
    }

    private fun table(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.95f, cy - s * 0.55f, cx + s * 0.95f, cy + s * 0.55f)
        canvas.drawRoundRect(rect, s * 0.12f, s * 0.12f, fill)
        canvas.drawRoundRect(rect, s * 0.12f, s * 0.12f, stroke)
        canvas.drawLine(cx - s * 0.7f, cy, cx + s * 0.7f, cy, stroke)
    }

    private fun chair(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        box(canvas, cx - s * 0.6f, cy - s * 0.4f, cx + s * 0.6f, cy + s * 0.7f, fill, stroke)
        box(canvas, cx - s * 0.6f, cy - s * 0.8f, cx + s * 0.6f, cy - s * 0.45f, fill, stroke)
    }

    private fun bed(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        box(canvas, cx - s * 0.6f, cy - s * 0.95f, cx + s * 0.6f, cy + s * 0.95f, fill, stroke)
        box(canvas, cx - s * 0.45f, cy - s * 0.85f, cx + s * 0.45f, cy - s * 0.5f, fill, stroke)
        canvas.drawLine(cx - s * 0.6f, cy - s * 0.2f, cx + s * 0.6f, cy - s * 0.2f, stroke)
    }

    private fun crate(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        box(canvas, cx - s * 0.75f, cy - s * 0.75f, cx + s * 0.75f, cy + s * 0.75f, fill, stroke)
        canvas.drawLine(cx - s * 0.75f, cy - s * 0.75f, cx + s * 0.75f, cy + s * 0.75f, stroke)
        canvas.drawLine(cx + s * 0.75f, cy - s * 0.75f, cx - s * 0.75f, cy + s * 0.75f, stroke)
    }

    private fun bookshelf(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        box(canvas, cx - s * 0.95f, cy - s * 0.4f, cx + s * 0.95f, cy + s * 0.4f, fill, stroke)
        var x = cx - s * 0.8f
        var i = 0
        while (x < cx + s * 0.8f) {
            val top = if (i % 3 == 0) cy - s * 0.3f else cy - s * 0.2f
            canvas.drawLine(x, top, x, cy + s * 0.3f, stroke)
            x += s * 0.2f
            i++
        }
    }

    private fun throne(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.6f, cy + s * 0.9f)
        path.lineTo(cx - s * 0.6f, cy - s * 0.5f)
        path.lineTo(cx - s * 0.35f, cy - s * 0.95f)
        path.lineTo(cx, cy - s * 0.65f)
        path.lineTo(cx + s * 0.35f, cy - s * 0.95f)
        path.lineTo(cx + s * 0.6f, cy - s * 0.5f)
        path.lineTo(cx + s * 0.6f, cy + s * 0.9f)
        path.close()
        both(canvas, path, fill, stroke)
        box(canvas, cx - s * 0.85f, cy + s * 0.05f, cx + s * 0.85f, cy + s * 0.45f, fill, stroke)
    }

    private fun altar(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        box(canvas, cx - s * 0.9f, cy - s * 0.1f, cx + s * 0.9f, cy + s * 0.2f, fill, stroke)
        box(canvas, cx - s * 0.65f, cy + s * 0.2f, cx + s * 0.65f, cy + s * 0.8f, fill, stroke)
        canvas.drawLine(cx - s * 0.4f, cy - s * 0.1f, cx - s * 0.4f, cy - s * 0.5f, stroke)
        canvas.drawLine(cx + s * 0.4f, cy - s * 0.1f, cx + s * 0.4f, cy - s * 0.5f, stroke)
        canvas.drawCircle(cx - s * 0.4f, cy - s * 0.6f, s * 0.08f, stroke)
        canvas.drawCircle(cx + s * 0.4f, cy - s * 0.6f, s * 0.08f, stroke)
    }

    private fun coffin(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.25f, cy - s * 0.95f)
        path.lineTo(cx + s * 0.25f, cy - s * 0.95f)
        path.lineTo(cx + s * 0.5f, cy - s * 0.45f)
        path.lineTo(cx + s * 0.32f, cy + s * 0.95f)
        path.lineTo(cx - s * 0.32f, cy + s * 0.95f)
        path.lineTo(cx - s * 0.5f, cy - s * 0.45f)
        path.close()
        both(canvas, path, fill, stroke)
        canvas.drawLine(cx, cy - s * 0.55f, cx, cy + s * 0.1f, stroke)
        canvas.drawLine(cx - s * 0.2f, cy - s * 0.35f, cx + s * 0.2f, cy - s * 0.35f, stroke)
    }

    private fun anvil(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.95f, cy - s * 0.45f)
        path.lineTo(cx + s * 0.75f, cy - s * 0.45f)
        path.lineTo(cx + s * 0.75f, cy - s * 0.1f)
        path.lineTo(cx + s * 0.3f, cy + s * 0.1f)
        path.lineTo(cx + s * 0.5f, cy + s * 0.7f)
        path.lineTo(cx - s * 0.5f, cy + s * 0.7f)
        path.lineTo(cx - s * 0.3f, cy + s * 0.1f)
        path.quadTo(cx - s * 0.8f, cy, cx - s * 0.95f, cy - s * 0.45f)
        path.close()
        both(canvas, path, fill, stroke)
    }

    private fun fireplace(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        box(canvas, cx - s * 0.95f, cy - s * 0.6f, cx + s * 0.95f, cy + s * 0.6f, fill, stroke)
        rect.set(cx - s * 0.55f, cy - s * 0.3f, cx + s * 0.55f, cy + s * 0.6f)
        canvas.drawRect(rect, stroke)
        flame(canvas, cx, cy + s * 0.2f, s * 0.3f, fill, stroke)
    }

    private fun brazier(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        circle(canvas, cx, cy, s * 0.7f, fill, stroke)
        flame(canvas, cx, cy, s * 0.45f, fill, stroke)
    }

    private fun torch(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        box(canvas, cx - s * 0.12f, cy - s * 0.1f, cx + s * 0.12f, cy + s * 0.95f, fill, stroke)
        flame(canvas, cx, cy - s * 0.45f, s * 0.45f, fill, stroke)
    }

    private fun pillar(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        box(canvas, cx - s * 0.85f, cy - s * 0.85f, cx + s * 0.85f, cy + s * 0.85f, fill, stroke)
        circle(canvas, cx, cy, s * 0.6f, fill, stroke)
    }

    private fun cage(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        box(canvas, cx - s * 0.8f, cy - s * 0.8f, cx + s * 0.8f, cy + s * 0.8f, fill, stroke)
        for (i in 1 until 4) {
            val x = cx - s * 0.8f + i * s * 0.4f
            canvas.drawLine(x, cy - s * 0.8f, x, cy + s * 0.8f, stroke)
        }
    }

    private fun sack(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.25f, cy - s * 0.6f)
        path.quadTo(cx - s * 0.95f, cy - s * 0.1f, cx - s * 0.7f, cy + s * 0.7f)
        path.quadTo(cx, cy + s * 0.95f, cx + s * 0.7f, cy + s * 0.7f)
        path.quadTo(cx + s * 0.95f, cy - s * 0.1f, cx + s * 0.25f, cy - s * 0.6f)
        path.close()
        both(canvas, path, fill, stroke)
        canvas.drawLine(cx - s * 0.3f, cy - s * 0.75f, cx + s * 0.3f, cy - s * 0.75f, stroke)
    }

    private fun bush(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        val blobs = listOf(-0.45f to 0.15f, 0.45f to 0.15f, 0f to -0.35f, 0f to 0.35f)
        for ((dx, dy) in blobs) canvas.drawCircle(cx + dx * s, cy + dy * s, s * 0.5f, fill)
        for ((dx, dy) in blobs) canvas.drawCircle(cx + dx * s, cy + dy * s, s * 0.5f, stroke)
        for ((dx, dy) in blobs) canvas.drawCircle(cx + dx * s, cy + dy * s, s * 0.48f, fill)
    }

    private fun stump(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        circle(canvas, cx, cy, s * 0.8f, fill, stroke)
        canvas.drawCircle(cx, cy, s * 0.5f, stroke)
        canvas.drawCircle(cx, cy, s * 0.2f, stroke)
    }

    private fun campfire(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        for (i in 0 until 6) {
            val a = i * PI.toFloat() / 3f
            circle(canvas, cx + cos(a) * s * 0.75f, cy + sin(a) * s * 0.75f, s * 0.2f, fill, stroke)
        }
        flame(canvas, cx, cy, s * 0.5f, fill, stroke)
    }

    private fun web(canvas: Canvas, cx: Float, cy: Float, s: Float, stroke: Paint) {
        for (i in 0 until 8) {
            val a = i * PI.toFloat() / 4f
            canvas.drawLine(cx, cy, cx + cos(a) * s, cy + sin(a) * s, stroke)
        }
        for (r in listOf(0.35f, 0.65f, 0.95f)) {
            path.reset()
            for (i in 0..8) {
                val a = i * PI.toFloat() / 4f
                val x = cx + cos(a) * s * r
                val y = cy + sin(a) * s * r
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            canvas.drawPath(path, stroke)
        }
    }

    private fun spikes(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        box(canvas, cx - s * 0.9f, cy - s * 0.9f, cx + s * 0.9f, cy + s * 0.9f, fill, stroke)
        for (row in 0 until 3) {
            for (col in 0 until 3) {
                val x = cx - s * 0.6f + col * s * 0.6f
                val y = cy - s * 0.6f + row * s * 0.6f
                path.reset()
                path.moveTo(x, y - s * 0.22f)
                path.lineTo(x + s * 0.18f, y + s * 0.18f)
                path.lineTo(x - s * 0.18f, y + s * 0.18f)
                path.close()
                canvas.drawPath(path, stroke)
            }
        }
    }

    private fun pit(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        box(canvas, cx - s * 0.9f, cy - s * 0.9f, cx + s * 0.9f, cy + s * 0.9f, fill, stroke)
        box(canvas, cx - s * 0.55f, cy - s * 0.55f, cx + s * 0.55f, cy + s * 0.55f, fill, stroke)
        canvas.drawLine(cx - s * 0.9f, cy - s * 0.9f, cx - s * 0.55f, cy - s * 0.55f, stroke)
        canvas.drawLine(cx + s * 0.9f, cy - s * 0.9f, cx + s * 0.55f, cy - s * 0.55f, stroke)
        canvas.drawLine(cx - s * 0.9f, cy + s * 0.9f, cx - s * 0.55f, cy + s * 0.55f, stroke)
        canvas.drawLine(cx + s * 0.9f, cy + s * 0.9f, cx + s * 0.55f, cy + s * 0.55f, stroke)
    }

    private fun bearTrap(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        circle(canvas, cx, cy, s * 0.85f, fill, stroke)
        for (i in 0 until 10) {
            val a = i * PI.toFloat() / 5f
            canvas.drawLine(
                cx + cos(a) * s * 0.85f, cy + sin(a) * s * 0.85f,
                cx + cos(a) * s * 0.5f, cy + sin(a) * s * 0.5f, stroke
            )
        }
        canvas.drawCircle(cx, cy, s * 0.2f, stroke)
    }

    private fun plate(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        box(canvas, cx - s * 0.75f, cy - s * 0.75f, cx + s * 0.75f, cy + s * 0.75f, fill, stroke)
        box(canvas, cx - s * 0.45f, cy - s * 0.45f, cx + s * 0.45f, cy + s * 0.45f, fill, stroke)
    }

    private fun arrow(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx + s, cy)
        path.lineTo(cx + s * 0.2f, cy - s * 0.65f)
        path.lineTo(cx + s * 0.2f, cy - s * 0.25f)
        path.lineTo(cx - s * 0.9f, cy - s * 0.25f)
        path.lineTo(cx - s * 0.9f, cy + s * 0.25f)
        path.lineTo(cx + s * 0.2f, cy + s * 0.25f)
        path.lineTo(cx + s * 0.2f, cy + s * 0.65f)
        path.close()
        both(canvas, path, fill, stroke)
    }

    private fun lever(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        box(canvas, cx - s * 0.6f, cy + s * 0.3f, cx + s * 0.6f, cy + s * 0.8f, fill, stroke)
        canvas.drawLine(cx, cy + s * 0.3f, cx + s * 0.55f, cy - s * 0.65f, stroke)
        circle(canvas, cx + s * 0.6f, cy - s * 0.72f, s * 0.2f, fill, stroke)
    }

    private fun scroll(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        box(canvas, cx - s * 0.6f, cy - s * 0.7f, cx + s * 0.6f, cy + s * 0.7f, fill, stroke)
        rect.set(cx - s * 0.8f, cy - s * 0.9f, cx + s * 0.8f, cy - s * 0.55f)
        canvas.drawOval(rect, fill)
        canvas.drawOval(rect, stroke)
        rect.set(cx - s * 0.8f, cy + s * 0.55f, cx + s * 0.8f, cy + s * 0.9f)
        canvas.drawOval(rect, fill)
        canvas.drawOval(rect, stroke)
        for (i in 0 until 3) {
            val y = cy - s * 0.3f + i * s * 0.25f
            canvas.drawLine(cx - s * 0.4f, y, cx + s * 0.4f, y, stroke)
        }
    }

    private fun key(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        circle(canvas, cx - s * 0.55f, cy, s * 0.35f, fill, stroke)
        canvas.drawCircle(cx - s * 0.55f, cy, s * 0.12f, stroke)
        canvas.drawLine(cx - s * 0.2f, cy, cx + s * 0.95f, cy, stroke)
        canvas.drawLine(cx + s * 0.6f, cy, cx + s * 0.6f, cy + s * 0.3f, stroke)
        canvas.drawLine(cx + s * 0.85f, cy, cx + s * 0.85f, cy + s * 0.3f, stroke)
    }

    private fun target(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        circle(canvas, cx, cy, s * 0.9f, fill, stroke)
        canvas.drawCircle(cx, cy, s * 0.6f, stroke)
        canvas.drawCircle(cx, cy, s * 0.3f, stroke)
        canvas.drawLine(cx - s, cy, cx + s, cy, stroke)
        canvas.drawLine(cx, cy - s, cx, cy + s, stroke)
    }
}
