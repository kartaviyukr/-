package com.fantasymap.creator.geom

import com.fantasymap.creator.model.LineFeature
import com.fantasymap.creator.model.LineFeatureType
import com.fantasymap.creator.model.Vec
import kotlin.math.max

/**
 * Сводит стены в единые сооружения.
 *
 *  - Два конца стен одного вида рядом — стены сливаются в одну линию.
 *  - Оба конца одной стены рядом — стена замыкается в кольцо.
 *  - Концы стен разного вида рядом — сходятся в общую точку.
 *  - Конец рядом с серединой другой стены — упирается в неё (стык буквой Т).
 */
object WallJoiner {

    /** Какие линии считаются стенами. */
    val walls: Set<LineFeatureType> = setOf(
        LineFeatureType.CITY_WALL, LineFeatureType.INNER_WALL, LineFeatureType.PALISADE,
        LineFeatureType.HIGH_WALL, LineFeatureType.DOUBLE_WALL, LineFeatureType.TOWERED_WALL,
        LineFeatureType.RUINED_WALL, LineFeatureType.WOODEN_WALL, LineFeatureType.GREAT_WALL,
        LineFeatureType.ICE_WALL, LineFeatureType.HEDGE_WALL, LineFeatureType.MAGIC_WARD,
        LineFeatureType.DUNGEON_WALL, LineFeatureType.BRICK_WALL, LineFeatureType.TIMBER_WALL,
        LineFeatureType.CAVE_WALL, LineFeatureType.IRON_BARS, LineFeatureType.FENCE,
        LineFeatureType.STAKE_WALL, LineFeatureType.RUBBLE_LINE, LineFeatureType.LEDGE,
        LineFeatureType.MAGIC_BARRIER, LineFeatureType.HEDGE, LineFeatureType.CURTAIN_WALL
    )

    data class Result(val lines: List<LineFeature>, val joins: Int)

    private fun closed(points: List<Vec>) = points.size > 3 && points.first().distanceTo(points.last()) < 0.001f

    /**
     * @param keepOpen двери, ворота, окна: проём с ними не заращивается
     */
    fun join(
        lines: List<LineFeature>,
        tolerance: Float,
        keepOpen: List<Vec> = emptyList(),
        tJunctions: Boolean = true
    ): Result {
        val work = lines.toMutableList()
        var joins = 0
        fun doorway(p: Vec, q: Vec): Boolean {
            val mid = middle(p, q)
            val reach = max(p.distanceTo(q) * 0.6f, tolerance * 0.3f)
            return keepOpen.any { it.distanceTo(mid) <= reach }
        }
        fun isWall(f: LineFeature) = f.type in walls && f.points.size >= 2 && !closed(f.points)

        // 1. Слить стены одного вида конец к концу и замкнуть почти замкнутые.
        var changed = true
        while (changed) {
            changed = false
            loop@ for (i in work.indices) {
                val a = work[i]
                if (!isWall(a)) continue
                if (a.points.size > 2 && a.points.first().distanceTo(a.points.last()) <= tolerance &&
                    !doorway(a.points.first(), a.points.last())
                ) {
                    val mid = middle(a.points.first(), a.points.last())
                    val pts = a.points.toMutableList()
                    pts[0] = mid
                    pts[pts.size - 1] = mid
                    work[i] = a.copy(points = pts)
                    joins++
                    changed = true
                    break@loop
                }
                for (j in work.indices) {
                    if (j == i) continue
                    val b = work[j]
                    if (!isWall(b) || b.type != a.type) continue
                    val merged = mergeEnds(a.points, b.points, tolerance, ::doorway) ?: continue
                    work[i] = a.copy(points = merged)
                    work.removeAt(j)
                    joins++
                    changed = true
                    break@loop
                }
            }
        }

        // 2. Концы стен разного вида — в общую точку; конец у середины стены — на неё.
        for (i in work.indices) {
            val a = work[i]
            if (!isWall(a)) continue
            val pts = a.points.toMutableList()
            for (atStart in listOf(true, false)) {
                val end = if (atStart) pts.first() else pts.last()
                var target: Vec? = null
                var best = tolerance
                var otherEnd: Pair<Int, Boolean>? = null
                for (j in work.indices) {
                    if (j == i) continue
                    val b = work[j]
                    if (b.type !in walls || b.points.size < 2) continue
                    for (other in listOf(b.points.first() to true, b.points.last() to false)) {
                        val d = end.distanceTo(other.first)
                        if (d in 0.0001f..best && !doorway(end, other.first)) {
                            best = d
                            target = middle(end, other.first)
                            otherEnd = j to other.second
                        }
                    }
                    if (otherEnd?.first == j || !tJunctions) continue
                    for (k in 0 until b.points.size - 1) {
                        val q = StreetNetwork.closest(end, b.points[k], b.points[k + 1])
                        val d = end.distanceTo(q)
                        if (d in 0.0001f..best && !doorway(end, q)) {
                            best = d
                            target = q
                            otherEnd = null
                        }
                    }
                }
                val point = target ?: continue
                if (atStart) pts[0] = point else pts[pts.size - 1] = point
                val oe = otherEnd
                if (oe != null) {
                    val b = work[oe.first]
                    val bp = b.points.toMutableList()
                    if (oe.second) bp[0] = point else bp[bp.size - 1] = point
                    work[oe.first] = b.copy(points = bp)
                }
                joins++
            }
            work[i] = work[i].copy(points = pts)
        }
        return Result(work, joins)
    }

    /**
     * Куда прилипнет точка: к ближайшему свободному концу стены, к началу
     * рисуемой стены (замкнуть), иначе к ближайшей точке стены (стык Т).
     */
    fun snapTarget(lines: List<LineFeature>, p: Vec, tolerance: Float, ownStart: Vec? = null): Vec? {
        var best: Vec? = null
        var bestDistance = tolerance
        for (f in lines) {
            if (f.type !in walls || f.points.size < 2 || closed(f.points)) continue
            for (end in listOf(f.points.first(), f.points.last())) {
                val d = p.distanceTo(end)
                if (d <= bestDistance) {
                    bestDistance = d
                    best = end
                }
            }
        }
        if (best != null) return best
        if (ownStart != null && p.distanceTo(ownStart) <= tolerance) return ownStart
        for (f in lines) {
            if (f.type !in walls || f.points.size < 2) continue
            for (k in 0 until f.points.size - 1) {
                val q = StreetNetwork.closest(p, f.points[k], f.points[k + 1])
                val d = p.distanceTo(q)
                if (d <= bestDistance) {
                    bestDistance = d
                    best = q
                }
            }
        }
        return best
    }

    /**
     * Пристроить только что нарисованную стену: её концы, подведённые к концам
     * других стен, прилипают к ним, и стены одного вида сливаются в одну;
     * конец у самого начала стены замыкает её в кольцо; конец у середины
     * другой стены упирается в неё. Старые стены не сдвигаются.
     */
    fun attach(lines: List<LineFeature>, added: LineFeature, tolerance: Float): Result {
        if (added.type !in walls || added.points.size < 2) return Result(lines + added, 0)
        val pts = added.points.toMutableList()
        var joins = 0
        // Конец у собственного начала — замкнуть.
        if (pts.size > 2 && pts.first().distanceTo(pts.last()) <= tolerance) {
            pts[pts.size - 1] = pts.first()
            return Result(lines + added.copy(points = pts), 1)
        }
        val others = lines.filter { it.type in walls }
        snapTarget(others, pts.first(), tolerance)?.let { pts[0] = it; joins++ }
        snapTarget(others, pts.last(), tolerance)?.let { pts[pts.size - 1] = it; joins++ }

        // Слить со стенами того же вида, к концам которых прилипли.
        val work = lines.toMutableList()
        var current = pts.toList()
        var merged = true
        while (merged) {
            merged = false
            for (i in work.indices) {
                val b = work[i]
                if (b.type != added.type || b.points.size < 2 || closed(b.points)) continue
                val ends = listOf(b.points.first(), b.points.last())
                if (ends.none { it == current.first() || it == current.last() }) continue
                val joined = mergeEnds(b.points, current, 0.001f) { _, _ -> false } ?: continue
                current = joined
                work.removeAt(i)
                merged = true
                break
            }
        }
        // Оба конца прилипли к одной стене — получилось кольцо.
        if (current.size > 3 && current.first().distanceTo(current.last()) < 0.001f) {
            current = current.dropLast(1) + current.first()
        }
        return Result(work + added.copy(points = current), joins)
    }

    /** Слить две ломаные, если какие-то их концы рядом; иначе null. */
    private fun mergeEnds(a: List<Vec>, b: List<Vec>, tolerance: Float, doorway: (Vec, Vec) -> Boolean): List<Vec>? {
        val options = listOf(
            Triple(a.last(), b.first(), { a + b.drop(1) }),
            Triple(a.last(), b.last(), { a + b.reversed().drop(1) }),
            Triple(a.first(), b.last(), { b + a.drop(1) }),
            Triple(a.first(), b.first(), { b.reversed() + a.drop(1) })
        )
        val best = options.minByOrNull { it.first.distanceTo(it.second) } ?: return null
        if (best.first.distanceTo(best.second) > tolerance) return null
        if (doorway(best.first, best.second)) return null
        val merged = best.third().toMutableList()
        return merged
    }

    private fun middle(a: Vec, b: Vec) = Vec((a.x + b.x) / 2f, (a.y + b.y) / 2f)
}
