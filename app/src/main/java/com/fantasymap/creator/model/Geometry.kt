package com.fantasymap.creator.model

import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/** Точка/вектор в мировых координатах карты. */
@Serializable
data class Vec(val x: Float, val y: Float) {
    operator fun plus(other: Vec) = Vec(x + other.x, y + other.y)
    operator fun minus(other: Vec) = Vec(x - other.x, y - other.y)
    operator fun times(k: Float) = Vec(x * k, y * k)

    fun length(): Float = sqrt(x * x + y * y)
    fun distanceTo(other: Vec): Float = (this - other).length()
}

/** Прямоугольник в мировых координатах. */
data class BBox(val minX: Float, val minY: Float, val maxX: Float, val maxY: Float) {
    val width: Float get() = maxX - minX
    val height: Float get() = maxY - minY
    val centerX: Float get() = (minX + maxX) * 0.5f
    val centerY: Float get() = (minY + maxY) * 0.5f

    fun contains(p: Vec): Boolean = p.x in minX..maxX && p.y in minY..maxY

    fun intersects(other: BBox): Boolean =
        minX <= other.maxX && maxX >= other.minX && minY <= other.maxY && maxY >= other.minY

    fun expand(margin: Float) = BBox(minX - margin, minY - margin, maxX + margin, maxY + margin)

    companion object {
        val EMPTY = BBox(0f, 0f, 0f, 0f)
    }
}

object Geometry {

    fun bounds(points: List<Vec>): BBox {
        if (points.isEmpty()) return BBox.EMPTY
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        for (p in points) {
            minX = min(minX, p.x); minY = min(minY, p.y)
            maxX = max(maxX, p.x); maxY = max(maxY, p.y)
        }
        return BBox(minX, minY, maxX, maxY)
    }

    /** Луч вправо: чётное число пересечений — точка снаружи. */
    fun pointInPolygon(p: Vec, polygon: List<Vec>): Boolean {
        if (polygon.size < 3) return false
        var inside = false
        var j = polygon.size - 1
        for (i in polygon.indices) {
            val a = polygon[i]
            val b = polygon[j]
            if ((a.y > p.y) != (b.y > p.y)) {
                val t = (p.y - a.y) / (b.y - a.y)
                if (p.x < a.x + t * (b.x - a.x)) inside = !inside
            }
            j = i
        }
        return inside
    }

    fun distanceToSegment(p: Vec, a: Vec, b: Vec): Float {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val lenSq = dx * dx + dy * dy
        if (lenSq < 1e-6f) return p.distanceTo(a)
        var t = ((p.x - a.x) * dx + (p.y - a.y) * dy) / lenSq
        t = t.coerceIn(0f, 1f)
        return p.distanceTo(Vec(a.x + t * dx, a.y + t * dy))
    }

    fun distanceToPolyline(p: Vec, points: List<Vec>): Float {
        if (points.isEmpty()) return Float.MAX_VALUE
        if (points.size == 1) return p.distanceTo(points[0])
        var best = Float.MAX_VALUE
        for (i in 0 until points.size - 1) {
            best = min(best, distanceToSegment(p, points[i], points[i + 1]))
        }
        return best
    }

    /** Расстояние до контура замкнутого полигона (включая замыкающее ребро). */
    fun distanceToPolygonOutline(p: Vec, polygon: List<Vec>): Float {
        if (polygon.size < 2) return distanceToPolyline(p, polygon)
        var best = distanceToPolyline(p, polygon)
        best = min(best, distanceToSegment(p, polygon.last(), polygon.first()))
        return best
    }

    fun signedArea(polygon: List<Vec>): Float {
        if (polygon.size < 3) return 0f
        var sum = 0f
        var j = polygon.size - 1
        for (i in polygon.indices) {
            sum += (polygon[j].x + polygon[i].x) * (polygon[j].y - polygon[i].y)
            j = i
        }
        return sum * 0.5f
    }

    fun area(polygon: List<Vec>): Float = abs(signedArea(polygon))

    /** Центроид многоугольника; при вырожденной площади — среднее точек. */
    fun centroid(polygon: List<Vec>): Vec {
        if (polygon.isEmpty()) return Vec(0f, 0f)
        val a = signedArea(polygon)
        if (abs(a) < 1e-4f) {
            var sx = 0f
            var sy = 0f
            for (p in polygon) { sx += p.x; sy += p.y }
            return Vec(sx / polygon.size, sy / polygon.size)
        }
        var cx = 0f
        var cy = 0f
        var j = polygon.size - 1
        for (i in polygon.indices) {
            val cross = polygon[j].x * polygon[i].y - polygon[i].x * polygon[j].y
            cx += (polygon[j].x + polygon[i].x) * cross
            cy += (polygon[j].y + polygon[i].y) * cross
            j = i
        }
        val k = 1f / (6f * a)
        return Vec(cx * k, cy * k)
    }

    fun length(points: List<Vec>): Float {
        var total = 0f
        for (i in 0 until points.size - 1) total += points[i].distanceTo(points[i + 1])
        return total
    }

    /** Упрощение Дугласа-Пекера — убирает дрожание пальца. */
    fun simplify(points: List<Vec>, tolerance: Float): List<Vec> {
        if (points.size < 3) return points
        val keep = BooleanArray(points.size)
        keep[0] = true
        keep[points.size - 1] = true
        simplifySection(points, 0, points.size - 1, tolerance, keep)
        val result = ArrayList<Vec>(points.size)
        for (i in points.indices) if (keep[i]) result.add(points[i])
        return result
    }

    private fun simplifySection(
        points: List<Vec>,
        first: Int,
        last: Int,
        tolerance: Float,
        keep: BooleanArray
    ) {
        if (last <= first + 1) return
        var maxDist = 0f
        var index = first
        for (i in first + 1 until last) {
            val d = distanceToSegment(points[i], points[first], points[last])
            if (d > maxDist) {
                maxDist = d
                index = i
            }
        }
        if (maxDist > tolerance) {
            keep[index] = true
            simplifySection(points, first, index, tolerance, keep)
            simplifySection(points, index, last, tolerance, keep)
        }
    }

    /** Равномерная передискретизация линии с шагом step. */
    fun resample(points: List<Vec>, step: Float): List<Vec> {
        if (points.size < 2 || step <= 0f) return points
        val result = ArrayList<Vec>()
        result.add(points[0])
        var carry = 0f
        for (i in 0 until points.size - 1) {
            val a = points[i]
            val b = points[i + 1]
            val segLen = a.distanceTo(b)
            if (segLen < 1e-5f) continue
            var t = step - carry
            while (t <= segLen) {
                val k = t / segLen
                result.add(Vec(a.x + (b.x - a.x) * k, a.y + (b.y - a.y) * k))
                t += step
            }
            carry = (carry + segLen) % step
        }
        if (result.last().distanceTo(points.last()) > step * 0.4f) result.add(points.last())
        return result
    }

    /** Сглаживание Чайкина для открытой линии (берег, река, дорога). */
    fun smoothOpen(points: List<Vec>, iterations: Int = 2): List<Vec> {
        if (points.size < 3 || iterations <= 0) return points
        var current = points
        repeat(iterations) {
            val next = ArrayList<Vec>(current.size * 2)
            next.add(current.first())
            for (i in 0 until current.size - 1) {
                val a = current[i]
                val b = current[i + 1]
                next.add(Vec(a.x * 0.75f + b.x * 0.25f, a.y * 0.75f + b.y * 0.25f))
                next.add(Vec(a.x * 0.25f + b.x * 0.75f, a.y * 0.25f + b.y * 0.75f))
            }
            next.add(current.last())
            current = next
        }
        return current
    }

    /** Сглаживание Чайкина для замкнутого контура. */
    fun smoothClosed(points: List<Vec>, iterations: Int = 2): List<Vec> {
        if (points.size < 4 || iterations <= 0) return points
        var current = points
        repeat(iterations) {
            val next = ArrayList<Vec>(current.size * 2)
            for (i in current.indices) {
                val a = current[i]
                val b = current[(i + 1) % current.size]
                next.add(Vec(a.x * 0.75f + b.x * 0.25f, a.y * 0.75f + b.y * 0.25f))
                next.add(Vec(a.x * 0.25f + b.x * 0.75f, a.y * 0.25f + b.y * 0.75f))
            }
            current = next
        }
        return current
    }

    /** Детерминированный псевдослучайный шум [0,1) по координатам — для текстур и глифов. */
    fun hashNoise(x: Int, y: Int, seed: Int): Float {
        var h = x * 374761393 + y * 668265263 + seed * 1274126177
        h = (h xor (h shr 13)) * 1274126177
        h = h xor (h shr 16)
        return (h and 0x7FFFFFFF) / 2147483647f
    }

    /** Точка внутри мирового прямоугольника? */
    fun clampToWorld(p: Vec, width: Float, height: Float): Vec =
        Vec(p.x.coerceIn(0f, width), p.y.coerceIn(0f, height))
}
