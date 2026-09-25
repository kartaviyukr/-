package com.fantasymap.creator.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.fantasymap.creator.model.BiomePattern
import com.fantasymap.creator.model.Glyph
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Рисование значков объектов и текстур ландшафта.
 * Экземпляр не потокобезопасен: один экземпляр — один поток отрисовки.
 */
class Glyphs {

    private val path = Path()
    private val rect = RectF()
    private val battle = BattleGlyphs()
    private val art = PatternArt()

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
            Glyph.DRAGON -> dragon(canvas, cx, cy, s, fill, stroke)
            Glyph.BEAST -> beast(canvas, cx, cy, s, fill, stroke)
            Glyph.NEST -> nest(canvas, cx, cy, s, fill, stroke)
            Glyph.CRYSTAL -> crystal(canvas, cx, cy, s, fill, stroke)
            Glyph.MUSHROOM -> mushroom(canvas, cx, cy, s, fill, stroke)
            Glyph.DEAD_TREE -> deadTree(canvas, cx, cy, s, stroke)
            Glyph.FLOWER -> flower(canvas, cx, cy, s, fill, stroke)
            Glyph.BEACON -> beacon(canvas, cx, cy, s, fill, stroke)
            Glyph.STATUE -> statue(canvas, cx, cy, s, fill, stroke)
            Glyph.WELL -> well(canvas, cx, cy, s, fill, stroke)
            Glyph.AIRSHIP -> airship(canvas, cx, cy, s, fill, stroke)
            Glyph.EYE -> eye(canvas, cx, cy, s, fill, stroke)
            Glyph.SPIRE -> spire(canvas, cx, cy, s, fill, stroke)
            Glyph.BANNER -> banner(canvas, cx, cy, s, fill, stroke)
            Glyph.SWORD -> sword(canvas, cx, cy, s, fill, stroke)
            Glyph.BOOK -> book(canvas, cx, cy, s, fill, stroke)
            Glyph.POTION -> potion(canvas, cx, cy, s, fill, stroke)
            Glyph.CROWN -> crown(canvas, cx, cy, s, fill, stroke)
            Glyph.OBSERVATORY -> observatory(canvas, cx, cy, s, fill, stroke)
            Glyph.ARCH -> arch(canvas, cx, cy, s, fill, stroke)
            Glyph.CAULDRON -> cauldron(canvas, cx, cy, s, fill, stroke)
            Glyph.TOTEM -> totem(canvas, cx, cy, s, fill, stroke)
            Glyph.BONES -> bones(canvas, cx, cy, s, fill, stroke)
            Glyph.FLOATING_ROCK -> floatingRock(canvas, cx, cy, s, fill, stroke)
            Glyph.RUNE_STONE -> runeStone(canvas, cx, cy, s, fill, stroke)
            Glyph.INGOT -> ingot(canvas, cx, cy, s, fill, stroke)
            Glyph.COINS -> coins(canvas, cx, cy, s, fill, stroke)
            Glyph.GEM -> gem(canvas, cx, cy, s, fill, stroke)
            Glyph.MITHRIL -> mithril(canvas, cx, cy, s, fill, stroke)
            Glyph.SALT -> saltCube(canvas, cx, cy, s, fill, stroke)
            Glyph.COAL -> coal(canvas, cx, cy, s, fill, stroke)
            Glyph.SULFUR -> sulfur(canvas, cx, cy, s, fill, stroke)
            Glyph.MARBLE -> marble(canvas, cx, cy, s, fill, stroke)
            Glyph.STONE_BLOCKS -> stoneBlocks(canvas, cx, cy, s, fill, stroke)
            Glyph.TIMBER -> timber(canvas, cx, cy, s, fill, stroke)
            Glyph.WHEAT -> wheat(canvas, cx, cy, s, stroke)
            Glyph.GRAPES -> grapes(canvas, cx, cy, s, fill, stroke)
            Glyph.FISH -> fish(canvas, cx, cy, s, fill, stroke)
            Glyph.PEARL -> pearl(canvas, cx, cy, s, fill, stroke)
            Glyph.AMBER -> amber(canvas, cx, cy, s, fill, stroke)
            Glyph.FUR -> fur(canvas, cx, cy, s, fill, stroke)
            Glyph.WOOL -> wool(canvas, cx, cy, s, fill, stroke)
            Glyph.SILK -> silk(canvas, cx, cy, s, fill, stroke)
            Glyph.SPICE -> spice(canvas, cx, cy, s, fill, stroke)
            Glyph.HERBS -> herbs(canvas, cx, cy, s, fill, stroke)
            Glyph.HORSESHOE -> horseshoe(canvas, cx, cy, s, stroke)
            Glyph.CATTLE -> cattle(canvas, cx, cy, s, fill, stroke)
            Glyph.OIL -> oilDrop(canvas, cx, cy, s, fill, stroke)
            Glyph.INN_SIGN -> innSign(canvas, cx, cy, s, fill, stroke)
            Glyph.FOUNTAIN -> fountain(canvas, cx, cy, s, fill, stroke)
            Glyph.LANTERN -> lantern(canvas, cx, cy, s, fill, stroke)
            Glyph.SIGNPOST -> signpost(canvas, cx, cy, s, fill, stroke)
            Glyph.CART -> cart(canvas, cx, cy, s, fill, stroke)
            Glyph.STALL -> stall(canvas, cx, cy, s, fill, stroke)
            Glyph.BARREL -> barrel(canvas, cx, cy, s, fill, stroke)
            Glyph.PILLORY -> pillory(canvas, cx, cy, s, fill, stroke)
            Glyph.GALLOWS -> gallows(canvas, cx, cy, s, stroke)
            Glyph.CLOCK -> clock(canvas, cx, cy, s, fill, stroke)
            Glyph.BELL -> bell(canvas, cx, cy, s, fill, stroke)
            Glyph.HATCH -> hatch(canvas, cx, cy, s, fill, stroke)
            Glyph.PLAQUE -> plaque(canvas, cx, cy, s, fill, stroke)
            Glyph.DRAWBRIDGE_GLYPH -> drawbridge(canvas, cx, cy, s, fill, stroke)
            Glyph.PORTCULLIS -> portcullis(canvas, cx, cy, s, fill, stroke)
            Glyph.HUMANOID,
            Glyph.GOBLIN,
            Glyph.SKULL,
            Glyph.PAW,
            Glyph.SPIDER,
            Glyph.SERPENT,
            Glyph.BAT,
            Glyph.HORNS,
            Glyph.FLAME,
            Glyph.WINGS,
            Glyph.TENTACLES,
            Glyph.GOLEM,
            Glyph.OOZE,
            Glyph.GHOST,
            Glyph.GIANT,
            Glyph.HAT,
            Glyph.DAGGER,
            Glyph.BOW,
            Glyph.SUN,
            Glyph.LUTE,
            Glyph.LEAF,
            Glyph.SHIELD,
            Glyph.LOCK,
            Glyph.CHEST,
            Glyph.DOOR,
            Glyph.STAIRS,
            Glyph.LADDER,
            Glyph.WINDOW,
            Glyph.TABLE,
            Glyph.CHAIR,
            Glyph.BED,
            Glyph.CRATE,
            Glyph.BOOKSHELF,
            Glyph.THRONE,
            Glyph.ALTAR,
            Glyph.COFFIN,
            Glyph.ANVIL,
            Glyph.FIREPLACE,
            Glyph.BRAZIER,
            Glyph.TORCH,
            Glyph.PILLAR,
            Glyph.CAGE,
            Glyph.SACK,
            Glyph.BUSH,
            Glyph.STUMP,
            Glyph.CAMPFIRE,
            Glyph.WEB,
            Glyph.SPIKES,
            Glyph.PIT,
            Glyph.BEAR_TRAP,
            Glyph.PLATE,
            Glyph.ARROW,
            Glyph.LEVER,
            Glyph.SCROLL,
            Glyph.KEY,
            Glyph.TARGET -> battle.draw(canvas, glyph, cx, cy, s, fill, stroke)
        }
    }

    // ---------- городские мелочи ----------

    private fun fountain(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s, cy + s * 0.25f, cx + s, cy + s)
        canvas.drawOval(rect, fill)
        canvas.drawOval(rect, stroke)
        canvas.drawLine(cx, cy + s * 0.4f, cx, cy - s * 0.3f, stroke)
        rect.set(cx - s * 0.5f, cy - s * 0.45f, cx + s * 0.5f, cy + s * 0.05f)
        canvas.drawOval(rect, fill)
        canvas.drawOval(rect, stroke)
        canvas.drawLine(cx, cy - s * 0.45f, cx - s * 0.5f, cy - s * 1.1f, stroke)
        canvas.drawLine(cx, cy - s * 0.45f, cx + s * 0.5f, cy - s * 1.1f, stroke)
        canvas.drawLine(cx, cy - s * 0.5f, cx, cy - s * 1.2f, stroke)
    }

    private fun lantern(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        canvas.drawLine(cx, cy + s, cx, cy - s * 0.35f, stroke)
        path.reset()
        path.moveTo(cx - s * 0.45f, cy - s * 0.35f)
        path.lineTo(cx + s * 0.45f, cy - s * 0.35f)
        path.lineTo(cx + s * 0.3f, cy - s * 0.95f)
        path.lineTo(cx - s * 0.3f, cy - s * 0.95f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx, cy - s * 0.95f, cx, cy - s * 1.2f, stroke)
        canvas.drawLine(cx - s * 0.6f, cy + s, cx + s * 0.6f, cy + s, stroke)
    }

    private fun signpost(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        canvas.drawLine(cx, cy + s, cx, cy - s * 1.1f, stroke)
        path.reset()
        path.moveTo(cx, cy - s * 0.95f)
        path.lineTo(cx + s, cy - s * 0.95f)
        path.lineTo(cx + s * 1.25f, cy - s * 0.65f)
        path.lineTo(cx + s, cy - s * 0.35f)
        path.lineTo(cx, cy - s * 0.35f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        path.reset()
        path.moveTo(cx, cy - s * 0.2f)
        path.lineTo(cx - s * 0.85f, cy - s * 0.2f)
        path.lineTo(cx - s * 1.1f, cy + s * 0.1f)
        path.lineTo(cx - s * 0.85f, cy + s * 0.4f)
        path.lineTo(cx, cy + s * 0.4f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
    }

    private fun cart(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.85f, cy - s * 0.45f, cx + s * 0.75f, cy + s * 0.25f)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
        canvas.drawCircle(cx - s * 0.45f, cy + s * 0.6f, s * 0.35f, fill)
        canvas.drawCircle(cx - s * 0.45f, cy + s * 0.6f, s * 0.35f, stroke)
        canvas.drawCircle(cx + s * 0.4f, cy + s * 0.6f, s * 0.35f, fill)
        canvas.drawCircle(cx + s * 0.4f, cy + s * 0.6f, s * 0.35f, stroke)
        canvas.drawLine(cx + s * 0.75f, cy - s * 0.3f, cx + s * 1.25f, cy - s * 0.55f, stroke)
    }

    private fun stall(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.8f, cy + s * 0.1f, cx + s * 0.8f, cy + s * 0.9f)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
        path.reset()
        path.moveTo(cx - s * 1.1f, cy + s * 0.1f)
        path.lineTo(cx - s * 0.75f, cy - s * 0.65f)
        path.lineTo(cx + s * 0.75f, cy - s * 0.65f)
        path.lineTo(cx + s * 1.1f, cy + s * 0.1f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx - s * 0.25f, cy - s * 0.65f, cx - s * 0.45f, cy + s * 0.1f, stroke)
        canvas.drawLine(cx + s * 0.25f, cy - s * 0.65f, cx + s * 0.45f, cy + s * 0.1f, stroke)
    }

    private fun barrel(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.6f, cy - s * 0.75f, cx + s * 0.6f, cy + s * 0.9f)
        canvas.drawOval(rect, fill)
        canvas.drawOval(rect, stroke)
        canvas.drawLine(cx - s * 0.58f, cy - s * 0.2f, cx + s * 0.58f, cy - s * 0.2f, stroke)
        canvas.drawLine(cx - s * 0.58f, cy + s * 0.35f, cx + s * 0.58f, cy + s * 0.35f, stroke)
        rect.set(cx - s * 0.42f, cy - s * 0.95f, cx + s * 0.42f, cy - s * 0.55f)
        canvas.drawOval(rect, stroke)
    }

    private fun pillory(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        canvas.drawLine(cx, cy + s, cx, cy - s * 0.55f, stroke)
        rect.set(cx - s * 0.9f, cy - s * 0.95f, cx + s * 0.9f, cy - s * 0.5f)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
        canvas.drawCircle(cx, cy - s * 0.72f, s * 0.18f, stroke)
        canvas.drawCircle(cx - s * 0.55f, cy - s * 0.72f, s * 0.13f, stroke)
        canvas.drawCircle(cx + s * 0.55f, cy - s * 0.72f, s * 0.13f, stroke)
        canvas.drawLine(cx - s * 0.6f, cy + s, cx + s * 0.6f, cy + s, stroke)
    }

    private fun gallows(canvas: Canvas, cx: Float, cy: Float, s: Float, stroke: Paint) {
        canvas.drawLine(cx - s * 0.7f, cy + s, cx - s * 0.7f, cy - s * 1.1f, stroke)
        canvas.drawLine(cx - s * 0.7f, cy - s * 1.1f, cx + s * 0.75f, cy - s * 1.1f, stroke)
        canvas.drawLine(cx - s * 0.7f, cy - s * 0.7f, cx - s * 0.25f, cy - s * 1.1f, stroke)
        canvas.drawLine(cx + s * 0.55f, cy - s * 1.1f, cx + s * 0.55f, cy - s * 0.4f, stroke)
        canvas.drawCircle(cx + s * 0.55f, cy - s * 0.22f, s * 0.2f, stroke)
        canvas.drawLine(cx - s * 1.05f, cy + s, cx + s * 0.4f, cy + s, stroke)
    }

    private fun clock(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        canvas.drawCircle(cx, cy, s * 0.95f, fill)
        canvas.drawCircle(cx, cy, s * 0.95f, stroke)
        canvas.drawLine(cx, cy, cx, cy - s * 0.6f, stroke)
        canvas.drawLine(cx, cy, cx + s * 0.45f, cy + s * 0.2f, stroke)
        canvas.drawLine(cx, cy - s * 0.95f, cx, cy - s * 0.75f, stroke)
        canvas.drawLine(cx + s * 0.95f, cy, cx + s * 0.75f, cy, stroke)
    }

    private fun bell(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.8f, cy + s * 0.55f)
        path.quadTo(cx - s * 0.7f, cy - s * 0.85f, cx, cy - s * 0.95f)
        path.quadTo(cx + s * 0.7f, cy - s * 0.85f, cx + s * 0.8f, cy + s * 0.55f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx - s * 0.95f, cy + s * 0.55f, cx + s * 0.95f, cy + s * 0.55f, stroke)
        canvas.drawCircle(cx, cy + s * 0.8f, s * 0.18f, fill)
        canvas.drawCircle(cx, cy + s * 0.8f, s * 0.18f, stroke)
    }

    private fun hatch(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.85f, cy - s * 0.55f, cx + s * 0.85f, cy + s * 0.55f)
        canvas.drawOval(rect, fill)
        canvas.drawOval(rect, stroke)
        canvas.drawLine(cx - s * 0.5f, cy - s * 0.2f, cx + s * 0.5f, cy - s * 0.2f, stroke)
        canvas.drawLine(cx - s * 0.5f, cy + s * 0.15f, cx + s * 0.5f, cy + s * 0.15f, stroke)
    }

    private fun plaque(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        canvas.drawLine(cx - s * 0.5f, cy + s, cx - s * 0.5f, cy + s * 0.4f, stroke)
        canvas.drawLine(cx + s * 0.5f, cy + s, cx + s * 0.5f, cy + s * 0.4f, stroke)
        rect.set(cx - s * 0.9f, cy - s * 0.85f, cx + s * 0.9f, cy + s * 0.45f)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
        canvas.drawLine(cx - s * 0.6f, cy - s * 0.5f, cx + s * 0.6f, cy - s * 0.5f, stroke)
        canvas.drawLine(cx - s * 0.6f, cy - s * 0.15f, cx + s * 0.6f, cy - s * 0.15f, stroke)
        canvas.drawLine(cx - s * 0.6f, cy + s * 0.2f, cx + s * 0.25f, cy + s * 0.2f, stroke)
    }

    private fun drawbridge(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 1.1f, cy - s * 0.95f, cx - s * 0.6f, cy + s * 0.9f)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
        path.reset()
        path.moveTo(cx - s * 0.6f, cy + s * 0.55f)
        path.lineTo(cx + s * 0.95f, cy - s * 0.35f)
        path.lineTo(cx + s * 1.1f, cy - s * 0.02f)
        path.lineTo(cx - s * 0.55f, cy + s * 0.9f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx - s * 0.85f, cy - s * 0.95f, cx + s * 0.95f, cy - s * 0.35f, stroke)
    }

    private fun portcullis(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.95f, cy + s * 0.95f)
        path.lineTo(cx - s * 0.95f, cy - s * 0.2f)
        rect.set(cx - s * 0.95f, cy - s * 1.15f, cx + s * 0.95f, cy + s * 0.75f)
        path.arcTo(rect, 180f, 180f)
        path.lineTo(cx + s * 0.95f, cy + s * 0.95f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        var x = cx - s * 0.6f
        while (x <= cx + s * 0.6f + 0.01f) {
            canvas.drawLine(x, cy - s * 0.6f, x, cy + s * 0.95f, stroke)
            x += s * 0.4f
        }
        canvas.drawLine(cx - s * 0.9f, cy - s * 0.15f, cx + s * 0.9f, cy - s * 0.15f, stroke)
        canvas.drawLine(cx - s * 0.9f, cy + s * 0.4f, cx + s * 0.9f, cy + s * 0.4f, stroke)
    }

    /** Вывеска трактира: столб, кронштейн и качающаяся доска. */
    private fun innSign(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        canvas.drawLine(cx - s * 0.75f, cy + s, cx - s * 0.75f, cy - s, stroke)
        canvas.drawLine(cx - s * 0.75f, cy - s, cx + s * 0.55f, cy - s, stroke)
        canvas.drawLine(cx + s * 0.35f, cy - s, cx + s * 0.35f, cy - s * 0.6f, stroke)
        rect.set(cx - s * 0.35f, cy - s * 0.6f, cx + s * 0.9f, cy + s * 0.35f)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
        canvas.drawLine(cx - s * 0.15f, cy - s * 0.3f, cx + s * 0.7f, cy - s * 0.3f, stroke)
        canvas.drawLine(cx - s * 0.15f, cy + s * 0.05f, cx + s * 0.7f, cy + s * 0.05f, stroke)
    }

    // ---------- знаки ресурсов ----------

    private fun ingot(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.9f, cy + s * 0.55f)
        path.lineTo(cx - s * 0.6f, cy - s * 0.35f)
        path.lineTo(cx + s * 0.6f, cy - s * 0.35f)
        path.lineTo(cx + s * 0.9f, cy + s * 0.55f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx - s * 0.6f, cy - s * 0.35f, cx + s * 0.6f, cy - s * 0.35f, stroke)
    }

    private fun coins(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        for (i in 0..2) {
            val y = cy + s * 0.45f - i * s * 0.36f
            rect.set(cx - s * 0.75f, y - s * 0.22f, cx + s * 0.75f, y + s * 0.22f)
            canvas.drawOval(rect, fill)
            canvas.drawOval(rect, stroke)
        }
    }

    private fun gem(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx, cy - s * 0.85f)
        path.lineTo(cx + s * 0.8f, cy - s * 0.15f)
        path.lineTo(cx, cy + s * 0.85f)
        path.lineTo(cx - s * 0.8f, cy - s * 0.15f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx - s * 0.8f, cy - s * 0.15f, cx + s * 0.8f, cy - s * 0.15f, stroke)
        canvas.drawLine(cx - s * 0.4f, cy - s * 0.15f, cx, cy - s * 0.85f, stroke)
        canvas.drawLine(cx + s * 0.4f, cy - s * 0.15f, cx, cy - s * 0.85f, stroke)
    }

    private fun mithril(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        gem(canvas, cx, cy, s * 0.9f, fill, stroke)
        canvas.drawLine(cx + s * 0.75f, cy - s * 0.95f, cx + s * 1.15f, cy - s * 0.55f, stroke)
        canvas.drawLine(cx + s * 1.15f, cy - s * 0.95f, cx + s * 0.75f, cy - s * 0.55f, stroke)
    }

    private fun saltCube(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.7f, cy - s * 0.5f, cx + s * 0.55f, cy + s * 0.75f)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
        path.reset()
        path.moveTo(cx - s * 0.7f, cy - s * 0.5f)
        path.lineTo(cx - s * 0.4f, cy - s * 0.85f)
        path.lineTo(cx + s * 0.85f, cy - s * 0.85f)
        path.lineTo(cx + s * 0.55f, cy - s * 0.5f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx + s * 0.55f, cy - s * 0.5f, cx + s * 0.85f, cy - s * 0.85f, stroke)
        canvas.drawLine(cx + s * 0.55f, cy + s * 0.75f, cx + s * 0.85f, cy + s * 0.4f, stroke)
        canvas.drawLine(cx + s * 0.85f, cy - s * 0.85f, cx + s * 0.85f, cy + s * 0.4f, stroke)
    }

    private fun coal(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        val old = fill.color
        fill.color = 0xFF3A3632.toInt()
        path.reset()
        path.moveTo(cx - s * 0.85f, cy + s * 0.5f)
        path.lineTo(cx - s * 0.5f, cy - s * 0.45f)
        path.lineTo(cx + s * 0.25f, cy - s * 0.75f)
        path.lineTo(cx + s * 0.85f, cy - s * 0.05f)
        path.lineTo(cx + s * 0.5f, cy + s * 0.65f)
        path.close()
        canvas.drawPath(path, fill)
        fill.color = old
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx - s * 0.3f, cy + s * 0.35f, cx + s * 0.2f, cy - s * 0.25f, stroke)
    }

    private fun sulfur(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.75f, cy + s * 0.65f)
        path.lineTo(cx - s * 0.25f, cy - s * 0.35f)
        path.lineTo(cx + s * 0.25f, cy + s * 0.65f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        path.reset()
        path.moveTo(cx + s * 0.15f, cy + s * 0.65f)
        path.lineTo(cx + s * 0.55f, cy - s * 0.05f)
        path.lineTo(cx + s * 0.9f, cy + s * 0.65f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx - s * 0.1f, cy - s * 0.65f, cx + s * 0.15f, cy - s * 1.05f, stroke)
        canvas.drawLine(cx + s * 0.45f, cy - s * 0.45f, cx + s * 0.7f, cy - s * 0.85f, stroke)
    }

    private fun marble(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.5f, cy - s * 0.7f, cx + s * 0.5f, cy + s * 0.7f)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
        rect.set(cx - s * 0.75f, cy + s * 0.7f, cx + s * 0.75f, cy + s)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
        rect.set(cx - s * 0.75f, cy - s, cx + s * 0.75f, cy - s * 0.7f)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
        canvas.drawLine(cx - s * 0.18f, cy - s * 0.7f, cx - s * 0.18f, cy + s * 0.7f, stroke)
        canvas.drawLine(cx + s * 0.18f, cy - s * 0.7f, cx + s * 0.18f, cy + s * 0.7f, stroke)
    }

    private fun stoneBlocks(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.85f, cy + s * 0.05f, cx + s * 0.05f, cy + s * 0.7f)
        canvas.drawRect(rect, fill); canvas.drawRect(rect, stroke)
        rect.set(cx + s * 0.05f, cy + s * 0.05f, cx + s * 0.85f, cy + s * 0.7f)
        canvas.drawRect(rect, fill); canvas.drawRect(rect, stroke)
        rect.set(cx - s * 0.5f, cy - s * 0.6f, cx + s * 0.45f, cy + s * 0.05f)
        canvas.drawRect(rect, fill); canvas.drawRect(rect, stroke)
    }

    private fun timber(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        canvas.drawCircle(cx - s * 0.45f, cy + s * 0.4f, s * 0.42f, fill)
        canvas.drawCircle(cx - s * 0.45f, cy + s * 0.4f, s * 0.42f, stroke)
        canvas.drawCircle(cx + s * 0.45f, cy + s * 0.4f, s * 0.42f, fill)
        canvas.drawCircle(cx + s * 0.45f, cy + s * 0.4f, s * 0.42f, stroke)
        canvas.drawCircle(cx, cy - s * 0.35f, s * 0.42f, fill)
        canvas.drawCircle(cx, cy - s * 0.35f, s * 0.42f, stroke)
        canvas.drawCircle(cx, cy - s * 0.35f, s * 0.14f, stroke)
    }

    private fun wheat(canvas: Canvas, cx: Float, cy: Float, s: Float, stroke: Paint) {
        canvas.drawLine(cx, cy + s * 0.9f, cx, cy - s * 0.9f, stroke)
        for (i in 0..3) {
            val y = cy - s * 0.75f + i * s * 0.4f
            canvas.drawLine(cx, y, cx - s * 0.6f, y + s * 0.3f, stroke)
            canvas.drawLine(cx, y, cx + s * 0.6f, y + s * 0.3f, stroke)
        }
    }

    private fun grapes(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        val positions = arrayOf(
            floatArrayOf(-0.42f, 0.05f), floatArrayOf(0.42f, 0.05f), floatArrayOf(0f, 0.05f),
            floatArrayOf(-0.22f, 0.5f), floatArrayOf(0.22f, 0.5f), floatArrayOf(0f, 0.92f)
        )
        for (position in positions) {
            canvas.drawCircle(cx + position[0] * s, cy + position[1] * s, s * 0.26f, fill)
            canvas.drawCircle(cx + position[0] * s, cy + position[1] * s, s * 0.26f, stroke)
        }
        canvas.drawLine(cx, cy - s * 0.2f, cx, cy - s * 0.85f, stroke)
        canvas.drawLine(cx, cy - s * 0.85f, cx + s * 0.55f, cy - s * 1.05f, stroke)
    }

    private fun fish(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.5f, cy)
        path.quadTo(cx + s * 0.1f, cy - s * 0.75f, cx + s * 0.75f, cy)
        path.quadTo(cx + s * 0.1f, cy + s * 0.75f, cx - s * 0.5f, cy)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        path.reset()
        path.moveTo(cx - s * 0.5f, cy)
        path.lineTo(cx - s, cy - s * 0.45f)
        path.lineTo(cx - s, cy + s * 0.45f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawCircle(cx + s * 0.42f, cy - s * 0.1f, s * 0.09f, stroke)
    }

    private fun pearl(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.95f, cy - s * 0.55f, cx + s * 0.95f, cy + s * 0.95f)
        canvas.drawArc(rect, 180f, 180f, false, fill)
        canvas.drawArc(rect, 180f, 180f, false, stroke)
        canvas.drawLine(cx - s * 0.95f, cy + s * 0.2f, cx + s * 0.95f, cy + s * 0.2f, stroke)
        canvas.drawCircle(cx, cy - s * 0.12f, s * 0.3f, fill)
        canvas.drawCircle(cx, cy - s * 0.12f, s * 0.3f, stroke)
    }

    private fun amber(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx, cy - s * 0.95f)
        path.quadTo(cx + s * 0.85f, cy + s * 0.1f, cx, cy + s * 0.9f)
        path.quadTo(cx - s * 0.85f, cy + s * 0.1f, cx, cy - s * 0.95f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx - s * 0.15f, cy + s * 0.05f, cx + s * 0.2f, cy + s * 0.05f, stroke)
        canvas.drawLine(cx + s * 0.02f, cy - s * 0.2f, cx + s * 0.02f, cy + s * 0.3f, stroke)
    }

    private fun fur(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.55f, cy - s * 0.85f)
        path.lineTo(cx - s * 0.95f, cy - s * 0.35f)
        path.lineTo(cx - s * 0.6f, cy + s * 0.15f)
        path.lineTo(cx - s * 0.75f, cy + s * 0.9f)
        path.lineTo(cx + s * 0.75f, cy + s * 0.9f)
        path.lineTo(cx + s * 0.6f, cy + s * 0.15f)
        path.lineTo(cx + s * 0.95f, cy - s * 0.35f)
        path.lineTo(cx + s * 0.55f, cy - s * 0.85f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
    }

    private fun wool(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        canvas.drawCircle(cx - s * 0.35f, cy - s * 0.1f, s * 0.45f, fill)
        canvas.drawCircle(cx + s * 0.35f, cy - s * 0.1f, s * 0.45f, fill)
        canvas.drawCircle(cx, cy + s * 0.3f, s * 0.5f, fill)
        canvas.drawCircle(cx - s * 0.35f, cy - s * 0.1f, s * 0.45f, stroke)
        canvas.drawCircle(cx + s * 0.35f, cy - s * 0.1f, s * 0.45f, stroke)
        canvas.drawCircle(cx, cy + s * 0.3f, s * 0.5f, stroke)
    }

    private fun silk(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.45f, cy - s * 0.7f, cx + s * 0.45f, cy + s * 0.7f)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
        canvas.drawLine(cx - s * 0.75f, cy - s * 0.7f, cx + s * 0.75f, cy - s * 0.7f, stroke)
        canvas.drawLine(cx - s * 0.75f, cy + s * 0.7f, cx + s * 0.75f, cy + s * 0.7f, stroke)
        canvas.drawLine(cx - s * 0.45f, cy - s * 0.25f, cx + s * 0.45f, cy - s * 0.05f, stroke)
        canvas.drawLine(cx - s * 0.45f, cy + s * 0.15f, cx + s * 0.45f, cy + s * 0.35f, stroke)
    }

    private fun spice(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.9f, cy + s * 0.7f)
        path.quadTo(cx, cy - s * 0.95f, cx + s * 0.9f, cy + s * 0.7f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawPoint(cx - s * 0.3f, cy + s * 0.3f, stroke)
        canvas.drawPoint(cx + s * 0.25f, cy + s * 0.45f, stroke)
        canvas.drawPoint(cx, cy - s * 0.05f, stroke)
    }

    private fun herbs(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        canvas.drawLine(cx, cy + s * 0.9f, cx, cy - s * 0.4f, stroke)
        fun leaf(dx: Float, dy: Float) {
            path.reset()
            path.moveTo(cx, cy + dy)
            path.quadTo(cx + dx * 0.6f, cy + dy - s * 0.55f, cx + dx, cy + dy - s * 0.15f)
            path.quadTo(cx + dx * 0.55f, cy + dy + s * 0.2f, cx, cy + dy)
            path.close()
            canvas.drawPath(path, fill)
            canvas.drawPath(path, stroke)
        }
        leaf(-s * 0.85f, s * 0.35f)
        leaf(s * 0.85f, s * 0.35f)
        leaf(-s * 0.7f, -s * 0.3f)
        leaf(s * 0.7f, -s * 0.3f)
    }

    private fun horseshoe(canvas: Canvas, cx: Float, cy: Float, s: Float, stroke: Paint) {
        rect.set(cx - s * 0.8f, cy - s * 0.9f, cx + s * 0.8f, cy + s * 0.7f)
        canvas.drawArc(rect, 150f, 240f, false, stroke)
        canvas.drawLine(cx - s * 0.7f, cy + s * 0.4f, cx - s * 0.55f, cy + s * 0.85f, stroke)
        canvas.drawLine(cx + s * 0.7f, cy + s * 0.4f, cx + s * 0.55f, cy + s * 0.85f, stroke)
    }

    private fun cattle(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        canvas.drawCircle(cx, cy + s * 0.15f, s * 0.6f, fill)
        canvas.drawCircle(cx, cy + s * 0.15f, s * 0.6f, stroke)
        path.reset()
        path.moveTo(cx - s * 0.5f, cy - s * 0.25f)
        path.quadTo(cx - s * 1.1f, cy - s * 0.6f, cx - s * 0.85f, cy - s * 1.0f)
        canvas.drawPath(path, stroke)
        path.reset()
        path.moveTo(cx + s * 0.5f, cy - s * 0.25f)
        path.quadTo(cx + s * 1.1f, cy - s * 0.6f, cx + s * 0.85f, cy - s * 1.0f)
        canvas.drawPath(path, stroke)
        canvas.drawCircle(cx - s * 0.2f, cy + s * 0.05f, s * 0.08f, stroke)
        canvas.drawCircle(cx + s * 0.2f, cy + s * 0.05f, s * 0.08f, stroke)
    }

    private fun oilDrop(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx, cy - s * 0.95f)
        path.quadTo(cx + s * 0.9f, cy + s * 0.15f, cx, cy + s * 0.9f)
        path.quadTo(cx - s * 0.9f, cy + s * 0.15f, cx, cy - s * 0.95f)
        path.close()
        val old = fill.color
        fill.color = 0xFF4A3F33.toInt()
        canvas.drawPath(path, fill)
        fill.color = old
        canvas.drawPath(path, stroke)
    }

    // ---------- новые значки ----------

    private fun dragon(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 1.1f, cy + s * 0.95f)
        path.quadTo(cx - s * 0.2f, cy + s * 0.45f, cx + s * 0.5f, cy + s * 0.15f)
        path.quadTo(cx + s * 1.0f, cy - s * 0.05f, cx + s * 1.2f, cy - s * 0.65f)
        path.lineTo(cx + s * 0.78f, cy - s * 0.5f)
        path.quadTo(cx + s * 0.7f, cy + s * 0.05f, cx + s * 0.15f, cy + s * 0.5f)
        path.quadTo(cx - s * 0.45f, cy + s * 0.85f, cx - s * 1.1f, cy + s * 0.95f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)

        path.reset()
        path.moveTo(cx + s * 0.05f, cy + s * 0.25f)
        path.lineTo(cx - s * 0.55f, cy - s * 1.15f)
        path.lineTo(cx + s * 0.55f, cy - s * 0.45f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)

        path.reset()
        path.moveTo(cx + s * 0.25f, cy + s * 0.15f)
        path.lineTo(cx + s * 0.95f, cy - s * 1.05f)
        path.lineTo(cx + s * 1.0f, cy - s * 0.2f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
    }

    private fun beast(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.85f, cy - s * 0.3f, cx + s * 0.6f, cy + s * 0.4f)
        canvas.drawOval(rect, fill)
        canvas.drawOval(rect, stroke)
        canvas.drawCircle(cx + s * 0.8f, cy - s * 0.45f, s * 0.34f, fill)
        canvas.drawCircle(cx + s * 0.8f, cy - s * 0.45f, s * 0.34f, stroke)
        canvas.drawLine(cx + s * 0.62f, cy - s * 0.25f, cx + s * 0.8f, cy - s * 0.45f, stroke)
        var x = cx - s * 0.62f
        while (x <= cx + s * 0.42f + 0.01f) {
            canvas.drawLine(x, cy + s * 0.3f, x, cy + s * 0.95f, stroke)
            x += s * 0.35f
        }
        canvas.drawLine(cx - s * 0.85f, cy - s * 0.05f, cx - s * 1.25f, cy - s * 0.5f, stroke)
    }

    private fun nest(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        canvas.drawCircle(cx - s * 0.3f, cy - s * 0.1f, s * 0.26f, fill)
        canvas.drawCircle(cx - s * 0.3f, cy - s * 0.1f, s * 0.26f, stroke)
        canvas.drawCircle(cx + s * 0.3f, cy - s * 0.1f, s * 0.26f, fill)
        canvas.drawCircle(cx + s * 0.3f, cy - s * 0.1f, s * 0.26f, stroke)
        canvas.drawCircle(cx, cy - s * 0.42f, s * 0.26f, fill)
        canvas.drawCircle(cx, cy - s * 0.42f, s * 0.26f, stroke)
        rect.set(cx - s * 1.05f, cy - s * 0.45f, cx + s * 1.05f, cy + s * 0.95f)
        canvas.drawArc(rect, 0f, 180f, false, fill)
        canvas.drawArc(rect, 0f, 180f, false, stroke)
        canvas.drawLine(cx - s * 1.05f, cy + s * 0.25f, cx + s * 1.05f, cy + s * 0.25f, stroke)
    }

    private fun crystal(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        fun shard(ox: Float, half: Float, top: Float) {
            path.reset()
            path.moveTo(cx + ox, cy + s * 0.9f)
            path.lineTo(cx + ox - half, cy + s * 0.1f)
            path.lineTo(cx + ox, cy - top)
            path.lineTo(cx + ox + half, cy + s * 0.1f)
            path.close()
            canvas.drawPath(path, fill)
            canvas.drawPath(path, stroke)
        }
        shard(-s * 0.6f, s * 0.28f, s * 0.5f)
        shard(s * 0.6f, s * 0.26f, s * 0.35f)
        shard(0f, s * 0.36f, s * 1.15f)
    }

    private fun mushroom(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.26f, cy - s * 0.1f, cx + s * 0.26f, cy + s * 0.95f)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
        rect.set(cx - s * 1.05f, cy - s * 1.0f, cx + s * 1.05f, cy + s * 0.35f)
        canvas.drawArc(rect, 180f, 180f, false, fill)
        canvas.drawArc(rect, 180f, 180f, false, stroke)
        canvas.drawLine(cx - s * 1.05f, cy - s * 0.32f, cx + s * 1.05f, cy - s * 0.32f, stroke)
        canvas.drawCircle(cx - s * 0.42f, cy - s * 0.62f, s * 0.14f, stroke)
        canvas.drawCircle(cx + s * 0.36f, cy - s * 0.72f, s * 0.12f, stroke)
    }

    private fun deadTree(canvas: Canvas, cx: Float, cy: Float, s: Float, stroke: Paint) {
        canvas.drawLine(cx, cy + s, cx, cy - s * 1.1f, stroke)
        canvas.drawLine(cx, cy - s * 0.15f, cx - s * 0.75f, cy - s * 0.75f, stroke)
        canvas.drawLine(cx, cy - s * 0.45f, cx + s * 0.7f, cy - s * 0.95f, stroke)
        canvas.drawLine(cx, cy + s * 0.25f, cx + s * 0.5f, cy - s * 0.15f, stroke)
        canvas.drawLine(cx - s * 0.75f, cy - s * 0.75f, cx - s * 0.95f, cy - s * 1.15f, stroke)
    }

    private fun flower(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        canvas.drawLine(cx, cy + s, cx, cy + s * 0.1f, stroke)
        for (i in 0 until 5) {
            val a = (-Math.PI / 2 + i * 2.0 * Math.PI / 5).toFloat()
            val x = cx + cos(a) * s * 0.55f
            val y = cy - s * 0.25f + sin(a) * s * 0.55f
            canvas.drawCircle(x, y, s * 0.34f, fill)
            canvas.drawCircle(x, y, s * 0.34f, stroke)
        }
        canvas.drawCircle(cx, cy - s * 0.25f, s * 0.24f, stroke)
    }

    private fun beacon(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.55f, cy + s)
        path.lineTo(cx - s * 0.3f, cy + s * 0.15f)
        path.lineTo(cx + s * 0.3f, cy + s * 0.15f)
        path.lineTo(cx + s * 0.55f, cy + s)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx - s * 0.75f, cy + s * 0.15f, cx + s * 0.75f, cy + s * 0.15f, stroke)
        path.reset()
        path.moveTo(cx, cy - s * 1.25f)
        path.quadTo(cx + s * 0.6f, cy - s * 0.4f, cx + s * 0.25f, cy + s * 0.1f)
        path.quadTo(cx, cy - s * 0.2f, cx - s * 0.25f, cy + s * 0.1f)
        path.quadTo(cx - s * 0.6f, cy - s * 0.4f, cx, cy - s * 1.25f)
        path.close()
        val old = fill.color
        fill.color = 0xFFE0913F.toInt()
        canvas.drawPath(path, fill)
        fill.color = old
        canvas.drawPath(path, stroke)
    }

    private fun statue(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.8f, cy + s * 0.65f, cx + s * 0.8f, cy + s)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
        rect.set(cx - s * 0.32f, cy - s * 0.35f, cx + s * 0.32f, cy + s * 0.65f)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
        canvas.drawCircle(cx, cy - s * 0.62f, s * 0.3f, fill)
        canvas.drawCircle(cx, cy - s * 0.62f, s * 0.3f, stroke)
        canvas.drawLine(cx - s * 0.32f, cy - s * 0.15f, cx - s * 0.8f, cy - s * 0.55f, stroke)
        canvas.drawLine(cx + s * 0.32f, cy - s * 0.15f, cx + s * 0.75f, cy + s * 0.15f, stroke)
    }

    private fun well(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.75f, cy + s * 0.2f, cx + s * 0.75f, cy + s)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
        rect.set(cx - s * 0.75f, cy + s * 0.02f, cx + s * 0.75f, cy + s * 0.4f)
        canvas.drawOval(rect, fill)
        canvas.drawOval(rect, stroke)
        canvas.drawLine(cx - s * 0.6f, cy + s * 0.2f, cx - s * 0.6f, cy - s * 0.55f, stroke)
        canvas.drawLine(cx + s * 0.6f, cy + s * 0.2f, cx + s * 0.6f, cy - s * 0.55f, stroke)
        path.reset()
        path.moveTo(cx - s * 0.95f, cy - s * 0.5f)
        path.lineTo(cx, cy - s * 1.15f)
        path.lineTo(cx + s * 0.95f, cy - s * 0.5f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
    }

    private fun airship(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 1.15f, cy - s * 0.9f, cx + s * 1.15f, cy + s * 0.1f)
        canvas.drawOval(rect, fill)
        canvas.drawOval(rect, stroke)
        canvas.drawLine(cx - s * 0.8f, cy - s * 0.75f, cx - s * 0.5f, cy + s * 0.02f, stroke)
        canvas.drawLine(cx + s * 0.8f, cy - s * 0.75f, cx + s * 0.5f, cy + s * 0.02f, stroke)
        rect.set(cx - s * 0.42f, cy + s * 0.32f, cx + s * 0.42f, cy + s * 0.85f)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
        canvas.drawLine(cx - s * 0.3f, cy + s * 0.1f, cx - s * 0.3f, cy + s * 0.32f, stroke)
        canvas.drawLine(cx + s * 0.3f, cy + s * 0.1f, cx + s * 0.3f, cy + s * 0.32f, stroke)
    }

    private fun eye(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 1.15f, cy)
        path.quadTo(cx, cy - s * 0.95f, cx + s * 1.15f, cy)
        path.quadTo(cx, cy + s * 0.95f, cx - s * 1.15f, cy)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawCircle(cx, cy, s * 0.38f, stroke)
        val old = fill.color
        fill.color = 0xFF2B2620.toInt()
        canvas.drawCircle(cx, cy, s * 0.18f, fill)
        fill.color = old
    }

    private fun spire(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.45f, cy + s * 0.9f)
        path.lineTo(cx - s * 0.12f, cy - s * 1.3f)
        path.lineTo(cx + s * 0.2f, cy + s * 0.9f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        path.reset()
        path.moveTo(cx + s * 0.25f, cy + s * 0.9f)
        path.lineTo(cx + s * 0.62f, cy - s * 0.45f)
        path.lineTo(cx + s * 0.9f, cy + s * 0.9f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
    }

    private fun banner(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        canvas.drawLine(cx - s * 0.5f, cy + s, cx - s * 0.5f, cy - s * 1.2f, stroke)
        path.reset()
        path.moveTo(cx - s * 0.5f, cy - s * 1.2f)
        path.lineTo(cx + s * 0.95f, cy - s * 1.0f)
        path.lineTo(cx + s * 0.6f, cy - s * 0.55f)
        path.lineTo(cx + s * 0.95f, cy - s * 0.1f)
        path.lineTo(cx - s * 0.5f, cy - s * 0.3f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
    }

    private fun sword(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.2f, cy - s * 0.35f)
        path.lineTo(cx + s * 0.2f, cy - s * 0.35f)
        path.lineTo(cx, cy + s * 1.1f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx - s * 0.7f, cy - s * 0.35f, cx + s * 0.7f, cy - s * 0.35f, stroke)
        canvas.drawLine(cx, cy - s * 0.35f, cx, cy - s * 1.05f, stroke)
        canvas.drawCircle(cx, cy - s * 1.15f, s * 0.2f, fill)
        canvas.drawCircle(cx, cy - s * 1.15f, s * 0.2f, stroke)
    }

    private fun book(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx, cy - s * 0.5f)
        path.quadTo(cx - s * 0.5f, cy - s * 0.85f, cx - s * 1.1f, cy - s * 0.6f)
        path.lineTo(cx - s * 1.1f, cy + s * 0.55f)
        path.quadTo(cx - s * 0.5f, cy + s * 0.25f, cx, cy + s * 0.65f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        path.reset()
        path.moveTo(cx, cy - s * 0.5f)
        path.quadTo(cx + s * 0.5f, cy - s * 0.85f, cx + s * 1.1f, cy - s * 0.6f)
        path.lineTo(cx + s * 1.1f, cy + s * 0.55f)
        path.quadTo(cx + s * 0.5f, cy + s * 0.25f, cx, cy + s * 0.65f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx, cy - s * 0.5f, cx, cy + s * 0.65f, stroke)
    }

    private fun potion(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.22f, cy - s * 1.0f)
        path.lineTo(cx - s * 0.22f, cy - s * 0.35f)
        path.quadTo(cx - s * 0.95f, cy + s * 0.1f, cx - s * 0.6f, cy + s * 0.8f)
        path.lineTo(cx + s * 0.6f, cy + s * 0.8f)
        path.quadTo(cx + s * 0.95f, cy + s * 0.1f, cx + s * 0.22f, cy - s * 0.35f)
        path.lineTo(cx + s * 0.22f, cy - s * 1.0f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx - s * 0.4f, cy - s * 1.0f, cx + s * 0.4f, cy - s * 1.0f, stroke)
        canvas.drawLine(cx - s * 0.62f, cy + s * 0.32f, cx + s * 0.62f, cy + s * 0.32f, stroke)
    }

    private fun crown(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s, cy + s * 0.55f)
        path.lineTo(cx - s * 1.1f, cy - s * 0.75f)
        path.lineTo(cx - s * 0.5f, cy - s * 0.1f)
        path.lineTo(cx, cy - s * 0.95f)
        path.lineTo(cx + s * 0.5f, cy - s * 0.1f)
        path.lineTo(cx + s * 1.1f, cy - s * 0.75f)
        path.lineTo(cx + s, cy + s * 0.55f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx - s, cy + s * 0.2f, cx + s, cy + s * 0.2f, stroke)
    }

    private fun observatory(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.75f, cy + s * 0.05f, cx + s * 0.75f, cy + s)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
        rect.set(cx - s * 0.95f, cy - s * 0.95f, cx + s * 0.95f, cy + s * 0.45f)
        canvas.drawArc(rect, 180f, 180f, false, fill)
        canvas.drawArc(rect, 180f, 180f, false, stroke)
        canvas.drawLine(cx - s * 0.95f, cy - s * 0.25f, cx + s * 0.95f, cy - s * 0.25f, stroke)
        canvas.drawLine(cx + s * 0.1f, cy - s * 0.35f, cx + s * 0.85f, cy - s * 0.95f, stroke)
    }

    private fun arch(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.95f, cy - s * 0.1f, cx - s * 0.5f, cy + s)
        canvas.drawRect(rect, fill); canvas.drawRect(rect, stroke)
        rect.set(cx + s * 0.5f, cy - s * 0.1f, cx + s * 0.95f, cy + s)
        canvas.drawRect(rect, fill); canvas.drawRect(rect, stroke)
        rect.set(cx - s * 0.95f, cy - s * 1.1f, cx + s * 0.95f, cy + s * 0.1f)
        canvas.drawArc(rect, 180f, 180f, false, fill)
        canvas.drawArc(rect, 180f, 180f, false, stroke)
    }

    private fun cauldron(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.9f, cy - s * 0.55f, cx + s * 0.9f, cy + s * 0.85f)
        canvas.drawArc(rect, 0f, 180f, false, fill)
        canvas.drawArc(rect, 0f, 180f, false, stroke)
        rect.set(cx - s * 0.95f, cy - s * 0.7f, cx + s * 0.95f, cy - s * 0.25f)
        canvas.drawOval(rect, fill)
        canvas.drawOval(rect, stroke)
        canvas.drawLine(cx - s * 0.5f, cy + s * 0.6f, cx - s * 0.65f, cy + s, stroke)
        canvas.drawLine(cx + s * 0.5f, cy + s * 0.6f, cx + s * 0.65f, cy + s, stroke)
    }

    private fun totem(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        rect.set(cx - s * 0.42f, cy - s * 1.15f, cx + s * 0.42f, cy + s)
        canvas.drawRect(rect, fill)
        canvas.drawRect(rect, stroke)
        canvas.drawLine(cx - s * 0.42f, cy - s * 0.45f, cx + s * 0.42f, cy - s * 0.45f, stroke)
        canvas.drawLine(cx - s * 0.42f, cy + s * 0.25f, cx + s * 0.42f, cy + s * 0.25f, stroke)
        canvas.drawCircle(cx - s * 0.16f, cy - s * 0.82f, s * 0.09f, stroke)
        canvas.drawCircle(cx + s * 0.16f, cy - s * 0.82f, s * 0.09f, stroke)
        canvas.drawLine(cx - s * 0.42f, cy - s * 1.0f, cx - s * 0.95f, cy - s * 0.7f, stroke)
        canvas.drawLine(cx + s * 0.42f, cy - s * 1.0f, cx + s * 0.95f, cy - s * 0.7f, stroke)
    }

    private fun bones(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        fun bone(angle: Float) {
            val dx = cos(angle) * s * 0.8f
            val dy = sin(angle) * s * 0.8f
            canvas.drawLine(cx - dx, cy - dy, cx + dx, cy + dy, stroke)
            val nx = -sin(angle) * s * 0.2f
            val ny = cos(angle) * s * 0.2f
            canvas.drawCircle(cx - dx + nx, cy - dy + ny, s * 0.2f, fill)
            canvas.drawCircle(cx - dx + nx, cy - dy + ny, s * 0.2f, stroke)
            canvas.drawCircle(cx - dx - nx, cy - dy - ny, s * 0.2f, fill)
            canvas.drawCircle(cx - dx - nx, cy - dy - ny, s * 0.2f, stroke)
            canvas.drawCircle(cx + dx + nx, cy + dy + ny, s * 0.2f, fill)
            canvas.drawCircle(cx + dx + nx, cy + dy + ny, s * 0.2f, stroke)
            canvas.drawCircle(cx + dx - nx, cy + dy - ny, s * 0.2f, fill)
            canvas.drawCircle(cx + dx - nx, cy + dy - ny, s * 0.2f, stroke)
        }
        bone(0.7f)
        bone(-0.7f)
    }

    private fun floatingRock(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 1.0f, cy - s * 0.35f)
        path.lineTo(cx - s * 0.5f, cy - s * 0.85f)
        path.lineTo(cx + s * 0.6f, cy - s * 0.75f)
        path.lineTo(cx + s * 1.0f, cy - s * 0.25f)
        path.lineTo(cx + s * 0.2f, cy + s * 0.55f)
        path.lineTo(cx - s * 0.45f, cy + s * 0.2f)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx - s * 0.55f, cy + s * 0.75f, cx + s * 0.55f, cy + s * 0.75f, stroke)
        canvas.drawLine(cx - s * 0.3f, cy + s * 1.0f, cx + s * 0.3f, cy + s * 1.0f, stroke)
    }

    private fun runeStone(canvas: Canvas, cx: Float, cy: Float, s: Float, fill: Paint, stroke: Paint) {
        path.reset()
        path.moveTo(cx - s * 0.55f, cy + s)
        path.lineTo(cx - s * 0.5f, cy - s * 0.6f)
        path.quadTo(cx, cy - s * 1.3f, cx + s * 0.5f, cy - s * 0.6f)
        path.lineTo(cx + s * 0.55f, cy + s)
        path.close()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLine(cx, cy - s * 0.55f, cx, cy + s * 0.6f, stroke)
        canvas.drawLine(cx, cy - s * 0.15f, cx - s * 0.32f, cy - s * 0.45f, stroke)
        canvas.drawLine(cx, cy + s * 0.15f, cx + s * 0.32f, cy - s * 0.15f, stroke)
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
    /**
     * Узор зоны. variant — случайное число 0..1, своё для каждой клетки:
     * по нему редкий узор решает, рисовать ли крупный знак или мелочь.
     */
    fun drawPattern(
        canvas: Canvas,
        pattern: BiomePattern,
        x: Float,
        y: Float,
        s: Float,
        paint: Paint,
        variant: Float = 0.5f
    ) {
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
            BiomePattern.RUNES -> {
                canvas.drawLine(x, y - s * 0.65f, x, y + s * 0.65f, paint)
                canvas.drawLine(x, y - s * 0.15f, x - s * 0.45f, y - s * 0.55f, paint)
                canvas.drawLine(x, y + s * 0.15f, x + s * 0.45f, y - s * 0.25f, paint)
            }
            BiomePattern.BONES -> {
                canvas.drawLine(x - s * 0.5f, y - s * 0.25f, x + s * 0.5f, y + s * 0.25f, paint)
                canvas.drawCircle(x - s * 0.55f, y - s * 0.35f, s * 0.14f, paint)
                canvas.drawCircle(x + s * 0.55f, y + s * 0.35f, s * 0.14f, paint)
            }
            BiomePattern.STARS -> {
                canvas.drawLine(x, y - s * 0.6f, x, y + s * 0.6f, paint)
                canvas.drawLine(x - s * 0.6f, y, x + s * 0.6f, y, paint)
                canvas.drawLine(x - s * 0.28f, y - s * 0.28f, x + s * 0.28f, y + s * 0.28f, paint)
                canvas.drawLine(x - s * 0.28f, y + s * 0.28f, x + s * 0.28f, y - s * 0.28f, paint)
            }
            BiomePattern.SPIRES -> {
                canvas.drawLine(x - s * 0.45f, y + s * 0.5f, x - s * 0.2f, y - s * 0.7f, paint)
                canvas.drawLine(x - s * 0.2f, y - s * 0.7f, x + s * 0.05f, y + s * 0.5f, paint)
                canvas.drawLine(x + s * 0.25f, y + s * 0.5f, x + s * 0.45f, y - s * 0.3f, paint)
            }
            BiomePattern.EYES -> {
                rect.set(x - s * 0.6f, y - s * 0.35f, x + s * 0.6f, y + s * 0.35f)
                canvas.drawOval(rect, paint)
                canvas.drawPoint(x, y, paint)
            }
            BiomePattern.FEATHERS -> {
                rect.set(x - s * 0.5f, y - s * 0.6f, x + s * 0.5f, y + s * 0.6f)
                canvas.drawArc(rect, 250f, 220f, false, paint)
                canvas.drawLine(x, y - s * 0.55f, x, y + s * 0.55f, paint)
            }
            BiomePattern.FIELDS -> {
                rect.set(x - s * 0.7f, y - s * 0.5f, x + s * 0.7f, y + s * 0.5f)
                canvas.drawRect(rect, paint)
                canvas.drawLine(x - s * 0.7f, y, x + s * 0.7f, y, paint)
            }
            BiomePattern.PLANKS -> {
                canvas.drawLine(x - s, y - s * 0.35f, x + s, y - s * 0.35f, paint)
                canvas.drawLine(x - s, y + s * 0.35f, x + s, y + s * 0.35f, paint)
                canvas.drawLine(x + s * 0.3f, y - s * 0.35f, x + s * 0.3f, y + s * 0.35f, paint)
            }
            BiomePattern.TILES -> {
                rect.set(x - s * 0.8f, y - s * 0.8f, x + s * 0.8f, y + s * 0.8f)
                canvas.drawRect(rect, paint)
            }
            BiomePattern.COBBLES -> {
                rect.set(x - s * 0.6f, y - s * 0.45f, x + s * 0.6f, y + s * 0.45f)
                canvas.drawRoundRect(rect, s * 0.3f, s * 0.3f, paint)
            }
            else -> naturePattern(canvas, pattern, x, y, s, paint, variant)
        }
    }

    // ---------- особые узоры природных и городских зон ----------

    private fun roundTree(canvas: Canvas, x: Float, y: Float, s: Float, paint: Paint) {
        canvas.drawLine(x, y + s * 0.6f, x, y, paint)
        rect.set(x - s * 0.5f, y - s * 0.75f, x + s * 0.5f, y + s * 0.15f)
        canvas.drawArc(rect, 0f, 360f, false, paint)
    }

    private fun conifer(canvas: Canvas, x: Float, y: Float, s: Float, paint: Paint) {
        canvas.drawLine(x, y + s * 0.65f, x, y - s * 0.75f, paint)
        canvas.drawLine(x, y - s * 0.75f, x - s * 0.5f, y + s * 0.15f, paint)
        canvas.drawLine(x, y - s * 0.75f, x + s * 0.5f, y + s * 0.15f, paint)
        canvas.drawLine(x - s * 0.5f, y + s * 0.15f, x + s * 0.5f, y + s * 0.15f, paint)
    }

    private fun tuft(canvas: Canvas, x: Float, y: Float, s: Float, paint: Paint) {
        canvas.drawLine(x, y + s * 0.3f, x - s * 0.25f, y - s * 0.3f, paint)
        canvas.drawLine(x, y + s * 0.3f, x, y - s * 0.45f, paint)
        canvas.drawLine(x, y + s * 0.3f, x + s * 0.25f, y - s * 0.3f, paint)
    }

    /** Временно другой цвет и толщина для яркой детали узора. */
    private inline fun accent(paint: Paint, color: Int, widthScale: Float, block: () -> Unit) {
        val oldColor = paint.color
        val oldWidth = paint.strokeWidth
        paint.color = color
        paint.strokeWidth = oldWidth * widthScale
        block()
        paint.color = oldColor
        paint.strokeWidth = oldWidth
    }

    private fun naturePattern(
        canvas: Canvas,
        pattern: BiomePattern,
        x: Float,
        y: Float,
        s: Float,
        paint: Paint,
        variant: Float
    ) {
        when (pattern) {
            // Смешанный лес: лиственные и хвойные вперемешку.
            BiomePattern.MIXED_TREES -> if (variant < 0.5f) roundTree(canvas, x, y, s, paint) else conifer(canvas, x, y, s, paint)

            // Муссонный лес: раскидистая крона-зонтик с плетями лиан.
            BiomePattern.MONSOON -> {
                canvas.drawLine(x, y + s * 0.7f, x, y - s * 0.2f, paint)
                rect.set(x - s * 0.8f, y - s * 0.75f, x + s * 0.8f, y + s * 0.05f)
                canvas.drawArc(rect, 180f, 180f, true, paint)
                canvas.drawLine(x - s * 0.55f, y - s * 0.35f, x - s * 0.55f, y + s * 0.15f, paint)
                canvas.drawLine(x + s * 0.55f, y - s * 0.35f, x + s * 0.55f, y + s * 0.2f, paint)
            }

            // Редколесье: изредка дерево, остальное — травы.
            BiomePattern.SPARSE_TREES -> if (variant < 0.25f) roundTree(canvas, x, y, s, paint) else tuft(canvas, x, y, s * 0.75f, paint)

            // Оливковые рощи: низкие кривые деревца с плодами, рядами.
            BiomePattern.OLIVES -> {
                path.reset()
                path.moveTo(x - s * 0.1f, y + s * 0.6f)
                path.quadTo(x + s * 0.2f, y + s * 0.2f, x - s * 0.05f, y - s * 0.05f)
                canvas.drawPath(path, paint)
                rect.set(x - s * 0.55f, y - s * 0.6f, x + s * 0.55f, y + s * 0.05f)
                canvas.drawOval(rect, paint)
                canvas.drawCircle(x - s * 0.2f, y - s * 0.3f, s * 0.07f, paint)
                canvas.drawCircle(x + s * 0.18f, y - s * 0.22f, s * 0.07f, paint)
                canvas.drawCircle(x, y - s * 0.45f, s * 0.07f, paint)
            }

            // Прерия: высокая волнистая трава, клонящаяся по ветру, редкие цветы.
            BiomePattern.TALL_GRASS -> {
                for (i in -1..1) {
                    path.reset()
                    path.moveTo(x + i * s * 0.25f, y + s * 0.5f)
                    path.quadTo(x + i * s * 0.25f + s * 0.1f, y - s * 0.3f, x + i * s * 0.25f + s * 0.5f, y - s * 0.75f)
                    canvas.drawPath(path, paint)
                }
                if (variant < 0.2f) canvas.drawCircle(x + s * 0.75f, y - s * 0.8f, s * 0.1f, paint)
            }

            // Кочкарник: бугорки с пучками травы.
            BiomePattern.TUSSOCKS -> {
                rect.set(x - s * 0.5f, y - s * 0.1f, x + s * 0.5f, y + s * 0.5f)
                canvas.drawArc(rect, 180f, 180f, false, paint)
                canvas.drawLine(x - s * 0.15f, y, x - s * 0.3f, y - s * 0.4f, paint)
                canvas.drawLine(x, y - s * 0.05f, x, y - s * 0.5f, paint)
                canvas.drawLine(x + s * 0.15f, y, x + s * 0.3f, y - s * 0.4f, paint)
            }

            // Ледник: трещины-разломы во льду.
            BiomePattern.CREVASSES -> {
                path.reset()
                path.moveTo(x - s * 0.8f, y - s * 0.1f)
                path.lineTo(x - s * 0.3f, y - s * 0.25f)
                path.lineTo(x + s * 0.1f, y + s * 0.05f)
                path.lineTo(x + s * 0.7f, y - s * 0.15f)
                canvas.drawPath(path, paint)
                canvas.drawLine(x - s * 0.5f, y + s * 0.25f, x + s * 0.3f, y + s * 0.35f, paint)
            }

            // Предгорья: невысокая вершинка за пологим холмом.
            BiomePattern.FOOTHILLS -> {
                canvas.drawLine(x - s * 0.45f, y, x - s * 0.05f, y - s * 0.6f, paint)
                canvas.drawLine(x - s * 0.05f, y - s * 0.6f, x + s * 0.3f, y - s * 0.05f, paint)
                rect.set(x - s * 0.9f, y - s * 0.15f, x + s * 0.9f, y + s * 0.65f)
                canvas.drawArc(rect, 195f, 150f, false, paint)
            }

            // Карст: воронки провалов и каменные столбы.
            BiomePattern.KARST -> if (variant < 0.4f) {
                rect.set(x - s * 0.18f, y - s * 0.7f, x + s * 0.18f, y + s * 0.5f)
                canvas.drawRoundRect(rect, s * 0.18f, s * 0.18f, paint)
                canvas.drawLine(x - s * 0.45f, y + s * 0.5f, x + s * 0.45f, y + s * 0.5f, paint)
            } else {
                canvas.drawCircle(x, y, s * 0.45f, paint)
                canvas.drawCircle(x, y, s * 0.18f, paint)
            }

            // Столовые горы: плоская вершина, отвесные склоны, слои породы.
            BiomePattern.MESAS -> {
                path.reset()
                path.moveTo(x - s * 0.9f, y + s * 0.45f)
                path.lineTo(x - s * 0.5f, y - s * 0.4f)
                path.lineTo(x + s * 0.5f, y - s * 0.4f)
                path.lineTo(x + s * 0.9f, y + s * 0.45f)
                canvas.drawPath(path, paint)
                canvas.drawLine(x - s * 0.62f, y - s * 0.1f, x + s * 0.62f, y - s * 0.1f, paint)
            }

            // Топи: рогоз над открытой водой.
            BiomePattern.REEDS -> {
                for (i in -1..1) {
                    val bx = x + i * s * 0.28f
                    canvas.drawLine(bx, y + s * 0.4f, bx, y - s * 0.55f + abs(i) * s * 0.15f, paint)
                    rect.set(bx - s * 0.08f, y - s * 0.6f + abs(i) * s * 0.15f, bx + s * 0.08f, y - s * 0.25f + abs(i) * s * 0.15f)
                    canvas.drawOval(rect, paint)
                }
                canvas.drawLine(x - s * 0.7f, y + s * 0.45f, x - s * 0.35f, y + s * 0.45f, paint)
                canvas.drawLine(x + s * 0.35f, y + s * 0.45f, x + s * 0.7f, y + s * 0.45f, paint)
            }

            // Драконьи пустоши: изредка парящий дракон, в остальном — оплавленные камни.
            BiomePattern.DRAGONS -> if (variant < 0.16f) {
                val d = s * 2.2f
                accent(paint, 0xFF7A1E14.toInt(), 1.4f) {
                    path.reset()
                    path.moveTo(x - d * 0.8f, y + d * 0.1f)
                    path.quadTo(x - d * 0.3f, y - d * 0.05f, x, y)
                    path.quadTo(x + d * 0.35f, y + d * 0.05f, x + d * 0.6f, y - d * 0.15f)
                    canvas.drawPath(path, paint)
                    canvas.drawLine(x + d * 0.6f, y - d * 0.15f, x + d * 0.8f, y - d * 0.1f, paint)
                    path.reset()
                    path.moveTo(x - d * 0.1f, y)
                    path.lineTo(x - d * 0.35f, y - d * 0.6f)
                    path.lineTo(x + d * 0.05f, y - d * 0.35f)
                    path.lineTo(x + d * 0.2f, y - d * 0.65f)
                    path.lineTo(x + d * 0.2f, y)
                    canvas.drawPath(path, paint)
                    canvas.drawLine(x - d * 0.8f, y + d * 0.1f, x - d * 0.95f, y + d * 0.25f, paint)
                }
            } else {
                canvas.drawLine(x - s * 0.4f, y + s * 0.3f, x - s * 0.1f, y - s * 0.2f, paint)
                canvas.drawLine(x - s * 0.1f, y - s * 0.2f, x + s * 0.3f, y + s * 0.3f, paint)
                canvas.drawPoint(x + s * 0.5f, y - s * 0.2f, paint)
            }

            // Парящие острова: скала-перевёрнутый конус и облачко под ней.
            BiomePattern.FLOATING -> {
                path.reset()
                path.moveTo(x - s * 0.7f, y - s * 0.4f)
                path.lineTo(x + s * 0.7f, y - s * 0.4f)
                path.lineTo(x + s * 0.1f, y + s * 0.4f)
                path.close()
                canvas.drawPath(path, paint)
                canvas.drawLine(x - s * 0.3f, y - s * 0.4f, x - s * 0.3f, y - s * 0.7f, paint)
                rect.set(x - s * 0.9f, y + s * 0.45f, x - s * 0.2f, y + s * 0.85f)
                canvas.drawArc(rect, 180f, 180f, false, paint)
                rect.set(x - s * 0.35f, y + s * 0.5f, x + s * 0.35f, y + s * 0.85f)
                canvas.drawArc(rect, 180f, 180f, false, paint)
            }

            // Грозовая степь: молнии над травой.
            BiomePattern.LIGHTNING -> if (variant < 0.3f) {
                accent(paint, 0xFFE8C530.toInt(), 1.6f) {
                    path.reset()
                    path.moveTo(x + s * 0.2f, y - s * 0.9f)
                    path.lineTo(x - s * 0.2f, y - s * 0.1f)
                    path.lineTo(x + s * 0.15f, y - s * 0.1f)
                    path.lineTo(x - s * 0.25f, y + s * 0.8f)
                    canvas.drawPath(path, paint)
                }
            } else {
                tuft(canvas, x, y, s * 0.8f, paint)
            }

            // Хищные джунгли: цветы-ловушки с зубастыми челюстями.
            BiomePattern.FLYTRAPS -> {
                canvas.drawLine(x, y + s * 0.7f, x, y - s * 0.05f, paint)
                rect.set(x - s * 0.6f, y - s * 0.75f, x + s * 0.6f, y + s * 0.05f)
                canvas.drawArc(rect, 200f, 140f, false, paint)
                canvas.drawArc(rect, 20f, 140f, false, paint)
                for (i in -1..1) {
                    canvas.drawLine(x + i * s * 0.25f, y - s * 0.62f, x + i * s * 0.25f, y - s * 0.45f, paint)
                    canvas.drawLine(x + i * s * 0.25f, y - s * 0.08f, x + i * s * 0.25f, y - s * 0.25f, paint)
                }
                canvas.drawLine(x, y + s * 0.3f, x - s * 0.4f, y + s * 0.1f, paint)
            }

            // Расколотые земли: длинные ломаные разломы с ответвлениями.
            BiomePattern.FAULTS -> accent(paint, paint.color, 1.5f) {
                path.reset()
                path.moveTo(x - s * 1.4f, y - s * 0.3f)
                path.lineTo(x - s * 0.7f, y + s * 0.1f)
                path.lineTo(x - s * 0.2f, y - s * 0.2f)
                path.lineTo(x + s * 0.4f, y + s * 0.3f)
                path.lineTo(x + s * 1.3f, y + s * 0.05f)
                canvas.drawPath(path, paint)
                canvas.drawLine(x - s * 0.2f, y - s * 0.2f, x - s * 0.1f, y - s * 0.75f, paint)
                canvas.drawLine(x + s * 0.4f, y + s * 0.3f, x + s * 0.55f, y + s * 0.8f, paint)
            }

            // Кости титанов: огромные рёбра и хребет, изредка.
            BiomePattern.TITAN_BONES -> if (variant < 0.3f) {
                val d = s * 2.6f
                accent(paint, paint.color, 1.6f) {
                    canvas.drawLine(x - d, y, x + d, y, paint)
                    for (i in -2..2) {
                        val bx = x + i * d * 0.32f
                        val h = d * (0.75f - abs(i) * 0.12f)
                        rect.set(bx - d * 0.18f, y - h, bx + d * 0.18f, y + h)
                        canvas.drawArc(rect, 110f, 140f, false, paint)
                    }
                    canvas.drawCircle(x + d * 1.1f, y, d * 0.22f, paint)
                }
            } else if (variant < 0.55f) {
                canvas.drawLine(x - s * 0.4f, y, x + s * 0.4f, y, paint)
                canvas.drawCircle(x - s * 0.45f, y, s * 0.1f, paint)
                canvas.drawCircle(x + s * 0.45f, y, s * 0.1f, paint)
            }

            // Плодовый сад: ровные ряды деревьев с плодами.
            BiomePattern.ORCHARD -> {
                roundTree(canvas, x, y, s * 0.9f, paint)
                accent(paint, 0xFFB5402E.toInt(), 1f) {
                    canvas.drawCircle(x - s * 0.18f, y - s * 0.35f, s * 0.08f, paint)
                    canvas.drawCircle(x + s * 0.2f, y - s * 0.3f, s * 0.08f, paint)
                    canvas.drawCircle(x, y - s * 0.55f, s * 0.08f, paint)
                }
            }

            // Сады: цветы и живые изгороди.
            BiomePattern.FLOWERS -> if (variant < 0.35f) {
                rect.set(x - s * 0.7f, y - s * 0.25f, x + s * 0.7f, y + s * 0.25f)
                canvas.drawRoundRect(rect, s * 0.25f, s * 0.25f, paint)
            } else {
                accent(paint, 0xFFC0507A.toInt(), 1f) {
                    for (i in 0 until 5) {
                        val a = i * 1.2566f
                        canvas.drawCircle(x + cos(a) * s * 0.22f, y + sin(a) * s * 0.22f, s * 0.1f, paint)
                    }
                }
                canvas.drawLine(x, y + s * 0.25f, x, y + s * 0.6f, paint)
            }

            // Кладбище: ровные ряды надгробий и крестов.
            BiomePattern.GRAVES -> if (variant < 0.5f) {
                path.reset()
                path.moveTo(x - s * 0.3f, y + s * 0.45f)
                path.lineTo(x - s * 0.3f, y - s * 0.2f)
                path.quadTo(x - s * 0.3f, y - s * 0.55f, x, y - s * 0.55f)
                path.quadTo(x + s * 0.3f, y - s * 0.55f, x + s * 0.3f, y - s * 0.2f)
                path.lineTo(x + s * 0.3f, y + s * 0.45f)
                canvas.drawPath(path, paint)
            } else {
                canvas.drawLine(x, y + s * 0.5f, x, y - s * 0.55f, paint)
                canvas.drawLine(x - s * 0.3f, y - s * 0.25f, x + s * 0.3f, y - s * 0.25f, paint)
            }

            else -> art.draw(canvas, pattern, x, y, s, paint, variant)
        }
    }
}
