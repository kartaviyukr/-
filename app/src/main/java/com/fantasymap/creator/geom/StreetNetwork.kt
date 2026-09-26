package com.fantasymap.creator.geom

import com.fantasymap.creator.model.Geometry
import com.fantasymap.creator.model.Road
import com.fantasymap.creator.model.RoadType
import com.fantasymap.creator.model.Vec
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Сводит улицы в одну сеть:
 *  1. хвостики, торчащие за перекрёсток, обрезаются по перекрёстку;
 *  2. оборванный конец, не дотянувший до соседней улицы, дотягивается до неё
 *     (к ближайшей точке рядом или прямо вперёд по ходу улицы);
 *  3. оторванные куски сети соединяются с основной сетью короткой связкой.
 * Концы, уже лежащие на другой улице, не трогаются — перекрёстки получаются точными.
 */
object StreetNetwork {

    /** Водные и воздушные пути — не улицы, их не сваривают. */
    fun weldable(type: RoadType): Boolean = type != RoadType.SEA_ROUTE && type != RoadType.SKY_ROUTE &&
        type != RoadType.RIVER_ROUTE && type != RoadType.CITY_CANAL_WAY && type != RoadType.UNDERGROUND_ROAD

    /**
     * @param roads улицы, которые можно менять
     * @param anchors улицы, к которым можно пристыковаться, но нельзя менять
     * @param stub хвостик за перекрёстком короче этого обрезается
     * @param snap в таком радиусе конец притягивается к любой улице
     * @param ray так далеко конец тянется прямо вперёд, пока не упрётся в улицу
     * @param link самая длинная связка между оторванными кусками сети
     * @param allowEnd можно ли трогать этот конец (например, только у границы квартала)
     */
    fun weld(
        roads: List<Road>,
        anchors: List<Road>,
        stub: Float,
        snap: Float,
        ray: Float,
        link: Float,
        allowEnd: (Vec) -> Boolean = { true }
    ): List<Road> {
        val lines = roads.map { it.points.toMutableList() }.toMutableList()
        val movable = roads.map { weldable(it.type) && it.points.size >= 2 }
        val fixed = anchors.filter { weldable(it.type) && it.points.size >= 2 }.map { it.points }

        fun others(self: Int): Sequence<List<Vec>> = sequence {
            for ((i, line) in lines.withIndex()) if (i != self && movable[i] && line.size >= 2) yield(line)
            yieldAll(fixed)
        }

        fun halfWidth(i: Int) = roads[i].type.width * 0.5f

        // 1. Обрезать хвостики за перекрёстком.
        for (i in lines.indices) {
            if (!movable[i]) continue
            for (atStart in listOf(true, false)) {
                val line = lines[i]
                if (line.size < 2) continue
                val ordered = if (atStart) line else line.asReversed()
                var walked = 0f
                var cut: Pair<Int, Vec>? = null
                loop@ for (k in 0 until ordered.size - 1) {
                    val a = ordered[k]
                    val b = ordered[k + 1]
                    var bestT = Float.MAX_VALUE
                    var bestPoint: Vec? = null
                    for (other in others(i)) {
                        for (m in 0 until other.size - 1) {
                            val hit = intersection(a, b, other[m], other[m + 1]) ?: continue
                            val t = a.distanceTo(hit)
                            if (t < bestT && t > 0.001f) {
                                bestT = t
                                bestPoint = hit
                            }
                        }
                    }
                    if (bestPoint != null) {
                        if (walked + bestT < stub) cut = k to bestPoint
                        break@loop
                    }
                    walked += a.distanceTo(b)
                    if (walked > stub) break
                }
                val (k, point) = cut ?: continue
                val rest = ordered.subList(k + 1, ordered.size).toMutableList()
                rest.add(0, point)
                if (Geometry.length(rest) < stub) continue
                lines[i] = if (atStart) rest else rest.asReversed().toMutableList()
            }
        }

        // 2. Дотянуть оборванные концы.
        for (i in lines.indices) {
            if (!movable[i]) continue
            for (atStart in listOf(true, false)) {
                val line = lines[i]
                if (line.size < 2) continue
                val end = if (atStart) line.first() else line.last()
                val prev = if (atStart) line[1] else line[line.size - 2]
                if (!allowEnd(end)) continue
                val touching = others(i).any { Geometry.distanceToPolyline(end, it) <= halfWidth(i) + 0.01f }
                if (touching) continue
                val dx = end.x - prev.x
                val dy = end.y - prev.y
                val len = max(0.0001f, sqrt(dx * dx + dy * dy))
                val dirX = dx / len
                val dirY = dy / len

                var target: Vec? = null
                var bestScore = Float.MAX_VALUE
                for (other in others(i)) {
                    for (m in 0 until other.size - 1) {
                        val q = closest(end, other[m], other[m + 1])
                        val d = end.distanceTo(q)
                        if (d > snap || d < 0.001f) continue
                        val facing = ((q.x - end.x) * dirX + (q.y - end.y) * dirY) / d
                        if (facing < -0.2f) continue
                        val score = d * (1.6f - facing * 0.6f)
                        if (score < bestScore) {
                            bestScore = score
                            target = q
                        }
                    }
                }
                if (target == null) {
                    val far = Vec(end.x + dirX * ray, end.y + dirY * ray)
                    var bestT = Float.MAX_VALUE
                    for (other in others(i)) {
                        for (m in 0 until other.size - 1) {
                            val hit = intersection(end, far, other[m], other[m + 1]) ?: continue
                            val t = end.distanceTo(hit)
                            if (t < bestT) {
                                bestT = t
                                target = hit
                            }
                        }
                    }
                }
                val point = target ?: continue
                if (atStart) line.add(0, point) else line.add(point)
            }
        }

        // 3. Связать оторванные куски сети с основной.
        val extra = ArrayList<Road>()
        val parts = components(lines, movable, fixed, roads)
        if (parts.size > 1 && link > 0f) {
            val main = parts.maxByOrNull { part -> if (part.anchored) Float.MAX_VALUE else part.length }!!
            val joined = ArrayList<List<Vec>>()
            joined.addAll(main.lines)
            for (part in parts.sortedBy { it.length }.reversed()) {
                if (part === main) continue
                var best: Pair<Vec, Vec>? = null
                var bestDistance = link
                for (piece in part.lines) {
                    for (p in Geometry.resample(piece, max(0.5f, link / 12f)) + piece.last()) {
                        for (other in joined) {
                            for (m in 0 until other.size - 1) {
                                val q = closest(p, other[m], other[m + 1])
                                val d = p.distanceTo(q)
                                if (d < bestDistance) {
                                    bestDistance = d
                                    best = p to q
                                }
                            }
                        }
                    }
                }
                val (from, to) = best ?: continue
                if (from.distanceTo(to) > 0.01f) {
                    extra.add(Road(type = part.type, points = listOf(from, to)))
                }
                joined.addAll(part.lines)
            }
        }

        return roads.mapIndexed { i, road -> if (movable[i]) road.copy(points = lines[i].toList()) else road } + extra
    }

    private class Part(val lines: List<List<Vec>>, val anchored: Boolean, val length: Float, val type: RoadType)

    /** Куски сети: улицы, касающиеся друг друга, — в одном куске. */
    private fun components(
        lines: List<List<Vec>>,
        movable: List<Boolean>,
        fixed: List<List<Vec>>,
        roads: List<Road>
    ): List<Part> {
        val all = ArrayList<List<Vec>>()
        val index = ArrayList<Int>()
        for ((i, line) in lines.withIndex()) if (movable[i] && line.size >= 2) {
            all.add(line)
            index.add(i)
        }
        val movableCount = all.size
        all.addAll(fixed)
        val n = all.size
        val parent = IntArray(n) { it }
        fun find(x: Int): Int {
            var r = x
            while (parent[r] != r) r = parent[r]
            return r
        }
        val boxes = all.map { Geometry.bounds(it) }
        for (a in 0 until n) {
            for (b in a + 1 until n) {
                if (a >= movableCount && b >= movableCount) continue
                if (!boxes[a].expand(1f).intersects(boxes[b])) continue
                if (touch(all[a], all[b])) {
                    val ra = find(a)
                    val rb = find(b)
                    if (ra != rb) parent[ra] = rb
                }
            }
        }
        val groups = (0 until n).groupBy { find(it) }
        return groups.values.mapNotNull { members ->
            val own = members.filter { it < movableCount }
            if (own.isEmpty()) return@mapNotNull null
            val anchored = members.any { it >= movableCount }
            val widest = own.maxByOrNull { roads[index[it]].type.width }!!
            Part(
                lines = members.map { all[it] },
                anchored = anchored,
                length = own.sumOf { Geometry.length(all[it]).toDouble() }.toFloat(),
                type = roads[index[widest]].type
            )
        }
    }

    private fun touch(a: List<Vec>, b: List<Vec>): Boolean {
        val eps = 0.05f
        for (p in listOf(a.first(), a.last())) if (Geometry.distanceToPolyline(p, b) < eps) return true
        for (p in listOf(b.first(), b.last())) if (Geometry.distanceToPolyline(p, a) < eps) return true
        for (i in 0 until a.size - 1) {
            for (j in 0 until b.size - 1) {
                if (intersection(a[i], a[i + 1], b[j], b[j + 1]) != null) return true
            }
        }
        return false
    }

    fun closest(p: Vec, a: Vec, b: Vec): Vec {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val lengthSq = dx * dx + dy * dy
        if (lengthSq < 0.000001f) return a
        val t = (((p.x - a.x) * dx + (p.y - a.y) * dy) / lengthSq).coerceIn(0f, 1f)
        return Vec(a.x + dx * t, a.y + dy * t)
    }

    /** Точка пересечения отрезков ab и cd или null. */
    fun intersection(a: Vec, b: Vec, c: Vec, d: Vec): Vec? {
        val rx = b.x - a.x
        val ry = b.y - a.y
        val sx = d.x - c.x
        val sy = d.y - c.y
        val denom = rx * sy - ry * sx
        if (kotlin.math.abs(denom) < 1e-9f) return null
        val qx = c.x - a.x
        val qy = c.y - a.y
        val t = (qx * sy - qy * sx) / denom
        val u = (qx * ry - qy * rx) / denom
        if (t < 0f || t > 1f || u < 0f || u > 1f) return null
        return Vec(a.x + rx * t, a.y + ry * t)
    }
}
