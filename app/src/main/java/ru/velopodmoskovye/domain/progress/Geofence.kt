package ru.velopodmoskovye.domain.progress

import ru.velopodmoskovye.domain.geo.Geo
import ru.velopodmoskovye.domain.model.GeoPoint
import ru.velopodmoskovye.domain.model.Stop

/** Координата с точностью — то, что отдаёт FusedLocationProvider. */
data class Fix(val point: GeoPoint, val accuracyM: Double, val timeMs: Long)

object Geofence {
    const val DEFAULT_RADIUS_M = 150
    /** Хуже этой точности автоматическую отметку не ставим — слишком легко «попасть» из соседнего квартала. */
    const val MAX_ACCURACY_M = 100.0
    /** Ручная отметка «я здесь» разрешена, только если до точки не дальше этого. */
    const val MANUAL_MAX_DISTANCE_M = 1_000.0

    fun radiusFor(stop: Stop, defaultRadiusM: Int = DEFAULT_RADIUS_M): Int = stop.zoneRadiusM ?: defaultRadiusM

    fun isInside(stop: Stop, fix: Fix, defaultRadiusM: Int = DEFAULT_RADIUS_M): Boolean {
        if (fix.accuracyM > MAX_ACCURACY_M) return false
        stop.zonePolygon?.let { poly ->
            if (Geo.contains(poly.outer, fix.point)) return true
        }
        val radius = radiusFor(stop, defaultRadiusM).toDouble()
        // Погрешность прощаем частично: не больше трети радиуса.
        val slack = minOf(fix.accuracyM, radius / 3)
        return Geo.distanceM(stop.coords, fix.point) <= radius + slack
    }

    /** Первая непосещённая точка из списка, в зоне которой мы сейчас. */
    fun hit(stops: List<Stop>, visited: Set<String>, fix: Fix, defaultRadiusM: Int = DEFAULT_RADIUS_M): List<Stop> =
        stops.filter { it.id !in visited && isInside(it, fix, defaultRadiusM) }

    sealed interface ManualResult {
        data class Allowed(val distanceM: Double) : ManualResult
        data class TooFar(val distanceM: Double) : ManualResult
        data object NoFix : ManualResult
    }

    fun manualCheck(stop: Stop, fix: Fix?): ManualResult {
        if (fix == null) return ManualResult.NoFix
        val d = Geo.distanceM(stop.coords, fix.point)
        return if (d - minOf(fix.accuracyM, 200.0) <= MANUAL_MAX_DISTANCE_M) ManualResult.Allowed(d) else ManualResult.TooFar(d)
    }
}
