package ru.velopodmoskovye.domain.fog

import ru.velopodmoskovye.domain.geo.Geo
import ru.velopodmoskovye.domain.model.Destination
import ru.velopodmoskovye.domain.model.GeoPoint
import ru.velopodmoskovye.domain.model.SpiralPack
import ru.velopodmoskovye.domain.progress.Geofence
import ru.velopodmoskovye.domain.progress.ProgressCalculator

/** Какие зоны карты открыты при текущих посещениях. */
object OpenZones {
    const val KREMLIN_RADIUS_M = 400.0

    /** Граница места: из контента, иначе оболочка вокруг точек маршрута с запасом 300 м. */
    fun placeRing(dest: Destination): List<GeoPoint> =
        dest.boundaryGeoJson?.outer ?: Geo.convexHull(
            (dest.stops.map { it.coords } + dest.startPoint.coords + dest.endPoint.coords).flatMap { Geo.circle(it, 300.0, 12) },
        )

    fun compute(
        spiral: SpiralPack,
        destinations: Map<String, Destination>,
        visited: Set<String>,
        defaultRadiusM: Int = Geofence.DEFAULT_RADIUS_M,
    ): List<Zone> {
        val zones = mutableListOf<Zone>()
        val completeDest = mutableSetOf<String>()
        for (dest in destinations.values) {
            val p = ProgressCalculator.destination(dest, visited)
            if (p.complete) {
                completeDest += dest.id
                zones += Zone("place:${dest.id}", placeRing(dest), Zone.Level.PLACE)
            } else {
                dest.stops.filter { it.id in visited }.forEach { s ->
                    val ring = s.zonePolygon?.outer ?: Geo.circle(s.coords, Geofence.radiusFor(s, defaultRadiusM).toDouble())
                    zones += Zone("stop:${s.id}", ring, Zone.Level.STOP)
                }
            }
        }
        // Пройденный округ закрашивается целиком (граница округа или все его места).
        val route = spiral.places.filter { !it.finish }
        for (region in spiral.regions) {
            val places = route.filter { it.regionId == region.id }
            if (places.isEmpty() || !places.all { it.destinationId != null && it.destinationId in completeDest }) continue
            val ring = region.boundaryGeoJson?.outer ?: continue
            zones += Zone("region:${region.id}", ring, Zone.Level.REGION)
        }
        if (ProgressCalculator.KREMLIN_STOP_ID in visited) {
            zones += Zone("kremlin", Geo.circle(Geo.KREMLIN, KREMLIN_RADIUS_M), Zone.Level.PLACE)
        }
        return zones
    }
}
