package ru.velopodmoskovye.domain.geo

import ru.velopodmoskovye.domain.model.GeoPoint
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object Geo {
    const val EARTH_RADIUS_M = 6_371_008.8

    /** Кремль — центр спирали и её финиш. */
    val KREMLIN = GeoPoint(55.752, 37.618)

    fun distanceM(a: GeoPoint, b: GeoPoint): Double {
        val p1 = a.lat.rad
        val p2 = b.lat.rad
        val dp = p2 - p1
        val dl = (b.lon - a.lon).rad
        val h = sin(dp / 2).let { it * it } + cos(p1) * cos(p2) * sin(dl / 2).let { it * it }
        return 2 * EARTH_RADIUS_M * asin(sqrt(h.coerceIn(0.0, 1.0)))
    }

    fun distanceKm(a: GeoPoint, b: GeoPoint): Double = distanceM(a, b) / 1000.0

    /** Начальный азимут из a в b, градусы 0..360, по часовой стрелке от севера. */
    fun bearingDeg(a: GeoPoint, b: GeoPoint): Double {
        val p1 = a.lat.rad
        val p2 = b.lat.rad
        val dl = (b.lon - a.lon).rad
        val y = sin(dl) * cos(p2)
        val x = cos(p1) * sin(p2) - sin(p1) * cos(p2) * cos(dl)
        return (atan2(y, x).deg + 360.0) % 360.0
    }

    /** Точка на расстоянии distM от center по азимуту bearing. */
    fun destination(center: GeoPoint, bearing: Double, distM: Double): GeoPoint {
        val d = distM / EARTH_RADIUS_M
        val b = bearing.rad
        val p1 = center.lat.rad
        val l1 = center.lon.rad
        val p2 = asin(sin(p1) * cos(d) + cos(p1) * sin(d) * cos(b))
        val l2 = l1 + atan2(sin(b) * sin(d) * cos(p1), cos(d) - sin(p1) * sin(p2))
        return GeoPoint(p2.deg, ((l2.deg + 540.0) % 360.0) - 180.0)
    }

    /** Круг как многоугольник (для «тумана» и зон точек). */
    fun circle(center: GeoPoint, radiusM: Double, segments: Int = 32): List<GeoPoint> =
        (0 until segments).map { destination(center, 360.0 * it / segments, radiusM) }

    /** Длина ломаной, м. */
    fun pathLengthM(points: List<GeoPoint>): Double =
        points.zipWithNext().sumOf { (a, b) -> distanceM(a, b) }

    /** Попадание точки в многоугольник (ray casting, кольцо без замыкания или с ним). */
    fun contains(ring: List<GeoPoint>, p: GeoPoint): Boolean {
        var inside = false
        var j = ring.size - 1
        for (i in ring.indices) {
            val a = ring[i]
            val b = ring[j]
            if ((a.lat > p.lat) != (b.lat > p.lat)) {
                val x = (b.lon - a.lon) * (p.lat - a.lat) / (b.lat - a.lat) + a.lon
                if (p.lon < x) inside = !inside
            }
            j = i
        }
        return inside
    }

    /** Пересекаются ли отрезки ab и cd (в плоском приближении — для коротких отрезков достаточно). */
    fun segmentsIntersect(a: GeoPoint, b: GeoPoint, c: GeoPoint, d: GeoPoint): Boolean {
        fun orient(p: GeoPoint, q: GeoPoint, r: GeoPoint): Double =
            (q.lon - p.lon) * (r.lat - p.lat) - (q.lat - p.lat) * (r.lon - p.lon)
        val d1 = orient(c, d, a)
        val d2 = orient(c, d, b)
        val d3 = orient(a, b, c)
        val d4 = orient(a, b, d)
        return ((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) && ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))
    }

    /** Сколько раз отрезок ab пересекает ломаную line. */
    fun crossings(a: GeoPoint, b: GeoPoint, line: List<GeoPoint>): Int =
        line.zipWithNext().count { (c, d) -> segmentsIntersect(a, b, c, d) }

    /** Выпуклая оболочка (монотонная цепь Эндрю), против часовой стрелки. */
    fun convexHull(points: List<GeoPoint>): List<GeoPoint> {
        val pts = points.distinct().sortedWith(compareBy({ it.lon }, { it.lat }))
        if (pts.size < 3) return pts
        fun cross(o: GeoPoint, a: GeoPoint, b: GeoPoint) =
            (a.lon - o.lon) * (b.lat - o.lat) - (a.lat - o.lat) * (b.lon - o.lon)
        val lower = mutableListOf<GeoPoint>()
        for (p in pts) {
            while (lower.size >= 2 && cross(lower[lower.size - 2], lower.last(), p) <= 0) lower.removeAt(lower.lastIndex)
            lower.add(p)
        }
        val upper = mutableListOf<GeoPoint>()
        for (p in pts.asReversed()) {
            while (upper.size >= 2 && cross(upper[upper.size - 2], upper.last(), p) <= 0) upper.removeAt(upper.lastIndex)
            upper.add(p)
        }
        return lower.dropLast(1) + upper.dropLast(1)
    }

    fun centroid(points: List<GeoPoint>): GeoPoint =
        GeoPoint(points.sumOf { it.lat } / points.size, points.sumOf { it.lon } / points.size)

    private val Double.rad get() = this * PI / 180.0
    private val Double.deg get() = this * 180.0 / PI
}
