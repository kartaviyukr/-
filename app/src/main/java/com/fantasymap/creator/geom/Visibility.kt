package com.fantasymap.creator.geom

import com.fantasymap.creator.model.Dnd
import com.fantasymap.creator.model.Geometry
import com.fantasymap.creator.model.LineFeatureType
import com.fantasymap.creator.model.MapProject
import com.fantasymap.creator.model.MarkerType
import com.fantasymap.creator.model.TokenFaction
import com.fantasymap.creator.model.Vec
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sin

/**
 * Зрение на боевой карте: что видит существо, если стены и закрытые двери
 * загораживают обзор. Считается лучами к концам стен — как «динамическое
 * освещение» в виртуальных столах.
 */
object Visibility {

    /** Стены, сквозь которые не видно. Решётки, заборы, уступы и верёвки обзор не закрывают. */
    val blocking: Set<LineFeatureType> = setOf(
        LineFeatureType.DUNGEON_WALL, LineFeatureType.BRICK_WALL, LineFeatureType.TIMBER_WALL,
        LineFeatureType.CAVE_WALL, LineFeatureType.STAKE_WALL, LineFeatureType.RUBBLE_LINE,
        LineFeatureType.HEDGE, LineFeatureType.CURTAIN_WALL
    )

    /** Двери, которые закрывают проём, пока закрыты. Решётки и арки не закрывают. */
    val doors: Set<MarkerType> = setOf(
        MarkerType.B_DOOR, MarkerType.B_DOUBLE_DOOR, MarkerType.B_LOCKED_DOOR,
        MarkerType.B_SECRET_DOOR, MarkerType.B_IRON_DOOR, MarkerType.B_CURTAIN
    )

    class Segment(val a: Vec, val b: Vec)

    /** Всё, что загораживает обзор: стены (и тайные тоже) и закрытые двери. */
    fun segments(project: MapProject): List<Segment> {
        val out = ArrayList<Segment>()
        for (line in project.lines) {
            if (line.type !in blocking || line.points.size < 2) continue
            for (i in 0 until line.points.size - 1) out.add(Segment(line.points[i], line.points[i + 1]))
        }
        val cell = project.gridCell
        for (marker in project.markers) {
            if (marker.type !in doors || marker.open) continue
            doorSegment(project, marker.pos, cell)?.let { out.add(it) }
        }
        return out
    }

    /**
     * Створка двери: между двумя ближайшими концами стен по обе стороны от двери.
     * Если стен рядом нет — отрезок в клетку поперёк ближайшей стены.
     */
    private fun doorSegment(project: MapProject, pos: Vec, cell: Float): Segment? {
        val reach = cell * 1.6f
        val ends = ArrayList<Vec>()
        for (line in project.lines) {
            if (line.type !in blocking || line.points.size < 2) continue
            for (end in listOf(line.points.first(), line.points.last())) {
                if (end.distanceTo(pos) <= reach) ends.add(end)
            }
        }
        if (ends.size >= 2) {
            val first = ends.minByOrNull { it.distanceTo(pos) }!!
            // Второй конец — по другую сторону двери.
            val second = ends.filter { it != first }.minByOrNull { candidate ->
                val dx1 = first.x - pos.x
                val dy1 = first.y - pos.y
                val dx2 = candidate.x - pos.x
                val dy2 = candidate.y - pos.y
                val same = dx1 * dx2 + dy1 * dy2 > 0f
                candidate.distanceTo(pos) + if (same) reach * 10f else 0f
            }
            if (second != null) return Segment(first, second)
        }
        return Segment(Vec(pos.x - cell * 0.5f, pos.y), Vec(pos.x + cell * 0.5f, pos.y))
    }

    /** Многоугольник видимого из точки origin в радиусе radius. */
    fun polygon(origin: Vec, radius: Float, segments: List<Segment>): List<Vec> {
        val near = segments.filter {
            Geometry.distanceToSegment(origin, it.a, it.b) < radius
        }
        val angles = ArrayList<Float>()
        val steps = 72
        for (i in 0 until steps) angles.add((i * 2.0 * Math.PI / steps).toFloat())
        for (s in near) {
            for (p in listOf(s.a, s.b)) {
                if (p.distanceTo(origin) > radius * 1.05f) continue
                val a = atan2(p.y - origin.y, p.x - origin.x)
                angles.add(a - 0.0005f)
                angles.add(a)
                angles.add(a + 0.0005f)
            }
        }
        angles.sort()
        return angles.map { angle ->
            val dx = cos(angle)
            val dy = sin(angle)
            var best = radius
            for (s in near) {
                val t = rayHit(origin, dx, dy, s.a, s.b) ?: continue
                if (t < best) best = t
            }
            Vec(origin.x + dx * best, origin.y + dy * best)
        }
    }

    /** Расстояние по лучу до отрезка или null. */
    private fun rayHit(o: Vec, dx: Float, dy: Float, a: Vec, b: Vec): Float? {
        val sx = b.x - a.x
        val sy = b.y - a.y
        val denom = dx * sy - dy * sx
        if (kotlin.math.abs(denom) < 1e-9f) return null
        val qx = a.x - o.x
        val qy = a.y - o.y
        val t = (qx * sy - qy * sx) / denom
        val u = (qx * dy - qy * dx) / denom
        if (t < 0f || u < 0f || u > 1f) return null
        return t
    }

    /** Точка внутри многоугольника видимости. */
    fun sees(polygon: List<Vec>, p: Vec): Boolean = polygon.size >= 3 && Geometry.pointInPolygon(p, polygon)

    /** Зрение каждого героя на карте. */
    fun heroViews(project: MapProject): List<List<Vec>> {
        val segments = segments(project)
        val cell = project.gridCell
        return project.tokens.filter { it.faction == TokenFaction.HERO && !it.dead }.map { hero ->
            val feet = Dnd.sheetOf(hero).vision.coerceAtLeast(10)
            val radius = feet / max(1, project.feetPerCell).toFloat() * cell
            polygon(hero.pos, radius, segments)
        }
    }

    /** Клетки, попавшие в поле зрения героев, — добавить к изученным. */
    fun visibleCells(project: MapProject, views: List<List<Vec>>): Set<Long> {
        val cell = project.gridCell
        if (cell <= 0f) return emptySet()
        val out = HashSet<Long>()
        for (poly in views) {
            if (poly.size < 3) continue
            val box = Geometry.bounds(poly)
            val x0 = floor(box.minX / cell).toInt()
            val x1 = floor(box.maxX / cell).toInt()
            val y0 = floor(box.minY / cell).toInt()
            val y1 = floor(box.maxY / cell).toInt()
            for (cx in x0..x1) for (cy in y0..y1) {
                val center = Vec((cx + 0.5f) * cell, (cy + 0.5f) * cell)
                if (Geometry.pointInPolygon(center, poly)) out.add(Dnd.cellKey(cx, cy))
            }
        }
        return out
    }
}
