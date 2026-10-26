package ru.velopodmoskovye.domain.fog

import ru.velopodmoskovye.domain.geo.Geo
import ru.velopodmoskovye.domain.model.GeoPoint
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

/** Открытая зона: круг точки, полигон места или округа. */
data class Zone(val id: String, val ring: List<GeoPoint>, val level: Level) {
    enum class Level { STOP, PLACE, REGION }
}

/**
 * «Туман войны»: вся Москва и область под полупрозрачной заливкой с дырами на месте
 * открытых зон. Перекрывающиеся дыры ломают триангуляцию заливки, поэтому
 * пересекающиеся зоны сливаются в общую выпуклую оболочку.
 */
object Fog {
    /** Охват Москвы и Московской области с запасом. */
    val OBLAST_BOUNDS = listOf(
        GeoPoint(54.15, 35.05), GeoPoint(54.15, 40.35), GeoPoint(57.05, 40.35), GeoPoint(57.05, 35.05),
    )

    fun mergeOverlapping(zones: List<List<GeoPoint>>): List<List<GeoPoint>> {
        var groups = zones.filter { it.size >= 3 }
        while (true) {
            val circles = groups.map { bounding(it) }
            val parent = IntArray(groups.size) { it }
            fun find(x: Int): Int = if (parent[x] == x) x else find(parent[x]).also { parent[x] = it }
            var merged = false
            for (i in groups.indices) for (j in i + 1 until groups.size) {
                val (ci, ri) = circles[i]
                val (cj, rj) = circles[j]
                if (Geo.distanceM(ci, cj) < ri + rj && find(i) != find(j)) {
                    parent[find(i)] = find(j)
                    merged = true
                }
            }
            if (!merged) return groups
            groups = groups.indices.groupBy { find(it) }.values.map { idx ->
                if (idx.size == 1) groups[idx[0]] else Geo.convexHull(idx.flatMap { groups[it] })
            }
        }
    }

    /** Ограничивающая окружность: центр и радиус, м. */
    fun bounding(ring: List<GeoPoint>): Pair<GeoPoint, Double> {
        val c = Geo.centroid(ring)
        return c to ring.maxOf { Geo.distanceM(c, it) }
    }

    /** Площадь многоугольника, км² (равнопромежуточная проекция — хватает для статистики). */
    fun areaKm2(ring: List<GeoPoint>): Double {
        if (ring.size < 3) return 0.0
        val lat0 = ring.sumOf { it.lat } / ring.size * PI / 180
        val kx = 111.32 * cos(lat0)
        val ky = 110.57
        var s = 0.0
        for (i in ring.indices) {
            val a = ring[i]
            val b = ring[(i + 1) % ring.size]
            s += (a.lon * kx) * (b.lat * ky) - (b.lon * kx) * (a.lat * ky)
        }
        return abs(s) / 2
    }

    /** GeoJSON тумана: один полигон с дырами. */
    fun fogGeoJson(openZones: List<List<GeoPoint>>): String {
        val holes = mergeOverlapping(openZones)
        val rings = listOf(OBLAST_BOUNDS) + holes
        return """{"type":"Feature","properties":{},"geometry":{"type":"Polygon","coordinates":[${rings.joinToString(",") { ringJson(it) }}]}}"""
    }

    /** Открытые зоны с уровнем — для раскраски цветом прогресса. */
    fun zonesGeoJson(zones: List<Zone>): String = featureCollection(zones.map { z ->
        """{"type":"Feature","properties":{"id":${str(z.id)},"level":${str(z.level.name)}},"geometry":{"type":"Polygon","coordinates":[${ringJson(z.ring)}]}}"""
    })

    fun featureCollection(features: List<String>): String = """{"type":"FeatureCollection","features":[${features.joinToString(",")}]}"""

    fun ringJson(ring: List<GeoPoint>): String {
        val closed = if (ring.first() != ring.last()) ring + ring.first() else ring
        return closed.joinToString(",", "[", "]") { "[${num(it.lon)},${num(it.lat)}]" }
    }

    fun num(d: Double): String = "%.6f".format(java.util.Locale.US, d)

    fun str(s: String): String = buildString {
        append('"')
        for (ch in s) when (ch) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\n' -> append("\\n")
            else -> if (ch < ' ') append("\\u%04x".format(ch.code)) else append(ch)
        }
        append('"')
    }
}
