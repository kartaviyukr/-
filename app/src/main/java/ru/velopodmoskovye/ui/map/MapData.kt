package ru.velopodmoskovye.ui.map

import ru.velopodmoskovye.domain.fog.Fog
import ru.velopodmoskovye.domain.fog.Zone
import ru.velopodmoskovye.domain.model.Destination
import ru.velopodmoskovye.domain.model.GeoPoint

/** GeoJSON для слоёв карты. */
object MapData {
    fun points(points: List<Pair<String, GeoPoint>>): String = Fog.featureCollection(points.map { (id, p) ->
        """{"type":"Feature","properties":{"id":${Fog.str(id)}},"geometry":{"type":"Point","coordinates":[${Fog.num(p.lon)},${Fog.num(p.lat)}]}}"""
    })

    fun line(points: List<GeoPoint>): String = Fog.featureCollection(
        if (points.size < 2) emptyList() else listOf(
            """{"type":"Feature","properties":{},"geometry":{"type":"LineString","coordinates":[${points.joinToString(",") { "[${Fog.num(it.lon)},${Fog.num(it.lat)}]" }}]}}""",
        ),
    )

    fun zones(zones: List<Zone>, level: Zone.Level): String = Fog.zonesGeoJson(zones.filter { it.level == level })

    /** Схема маршрута по порядку точек (прямыми — это не велотрасса, вести будет Google Maps). */
    fun routePoints(dest: Destination): List<GeoPoint> =
        listOf(dest.startPoint.coords) + dest.stops.sortedBy { it.order }.map { it.coords } + dest.endPoint.coords

    fun routeLayers(dest: Destination, visited: Set<String>, prefix: String = "route"): List<MapLayerSpec> {
        val stops = dest.stops
        return listOf(
            MapLayerSpec("$prefix-line", line(routePoints(dest)), MapLayerSpec.Kind.LINE, "#1F6F4A", opacity = 0.8f, width = 4f, dashed = true),
            MapLayerSpec("$prefix-ends", points(listOf("start" to dest.startPoint.coords, "end" to dest.endPoint.coords)),
                MapLayerSpec.Kind.CIRCLE, "#3B6470", radius = 7f),
            MapLayerSpec("$prefix-todo", points(stops.filter { it.id !in visited }.map { it.id to it.coords }),
                MapLayerSpec.Kind.CIRCLE, "#8A5A00", radius = 7f),
            MapLayerSpec("$prefix-done", points(stops.filter { it.id in visited }.map { it.id to it.coords }),
                MapLayerSpec.Kind.CIRCLE, "#2E7D32", radius = 7f),
        )
    }

    fun me(p: GeoPoint?): MapLayerSpec =
        MapLayerSpec("me", points(listOfNotNull(p?.let { "me" to it })), MapLayerSpec.Kind.CIRCLE, "#1565C0", radius = 8f)
}
