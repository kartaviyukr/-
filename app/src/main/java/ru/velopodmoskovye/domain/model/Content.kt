package ru.velopodmoskovye.domain.model

import kotlinx.serialization.Serializable

/** Точка на карте. В JSON — {"lat": .., "lon": ..}. */
@Serializable
data class GeoPoint(val lat: Double, val lon: Double)

/** Полигон в формате GeoJSON: кольца из пар [lon, lat]; первое кольцо — внешнее. */
@Serializable
data class GeoPolygon(
    val type: String = "Polygon",
    val coordinates: List<List<List<Double>>>,
) {
    val outer: List<GeoPoint> get() = coordinates.first().map { GeoPoint(lat = it[1], lon = it[0]) }

    companion object {
        fun of(ring: List<GeoPoint>): GeoPolygon {
            val closed = if (ring.isNotEmpty() && ring.first() != ring.last()) ring + ring.first() else ring
            return GeoPolygon(coordinates = listOf(closed.map { listOf(it.lon, it.lat) }))
        }
    }
}

@Serializable
data class ContentManifest(
    val version: Int,
    val updated: String,
    val spiral: String,
    val destinations: List<String>,
)

@Serializable
data class RingDef(val index: Int, val name: String, val minKm: Double, val maxKm: Double?)

@Serializable
data class Region(
    val id: String,
    val name: String,
    /** "МО_городской_округ" | "МО_муниципальный_округ" | "Москва_АО" | "соседняя_область" */
    val type: String,
    val adminCenter: String? = null,
    val distanceFromKremlinKm: Double? = null,
    val boundaryGeoJson: GeoPolygon? = null,
    /** Тип округа подтверждён по официальному источнику. */
    val verified: Boolean = false,
)

/** Место в общей спирали (все 183 точки), даже если для него ещё нет контента. */
@Serializable
data class SpiralPlace(
    val id: String,
    val num: Int,
    val name: String,
    val kindNote: String,
    val regionId: String,
    /** 3 — ★ обязательно, 2 — ☆ очень желательно, 1 — по пути. */
    val mustHave: Int,
    val coords: GeoPoint,
    val distanceFromKremlinKm: Double,
    val azimuthDeg: Double,
    val ring: Int,
    /** id файла с маршрутом, если контент уже написан. */
    val destinationId: String? = null,
    /** Старт спирали — ЦАО. */
    val start: Boolean = false,
    /** Административный округ Москвы внутри МКАД (кольцо 0). */
    val insideMkad: Boolean = false,
)

@Serializable
data class Superlative(val id: String, val title: String, val placeId: String? = null, val note: String = "")

@Serializable
data class SpiralPack(
    val version: Int,
    val origin: GeoPoint,
    val rings: List<RingDef>,
    val regions: List<Region>,
    val places: List<SpiralPlace>,
    val superlatives: List<Superlative> = emptyList(),
)

@Serializable
data class Destination(
    val id: String,
    val name: String,
    /** "округ Москвы" | "город" | "рабочий посёлок" | "дачный посёлок" | "посёлок" | "село" | "деревня" | "усадьба" | "монастырь" | "природа" | "памятное место" */
    val settlementType: String,
    val mustHave: Int,
    val regionId: String,
    val distanceFromKremlinKm: Double,
    val coords: GeoPoint,
    val boundaryGeoJson: GeoPolygon? = null,
    /** "osm" — граница из OpenStreetMap, "approx" — приближённая оболочка вокруг точек маршрута. */
    val boundarySource: String = "approx",
    val intro: List<String>,
    val startPoint: RoutePoint,
    val endPoint: RoutePoint,
    val route: RouteInfo,
    val stops: List<Stop>,
    val dish: Dish,
    val title: Title,
    val sources: List<Source>,
    val lastChecked: String = "",
    /** 1 — старый короткий маршрут, 2 — полноценный велодень: 40–70 км, 8–12 точек. */
    val routeVersion: Int = 1,
)

@Serializable
data class RoutePoint(val name: String, val coords: GeoPoint, val transport: String)

@Serializable
data class RouteInfo(
    val distanceKm: Double,
    val durationH: Double,
    /** "лёгкий" | "средний" | "сложный" */
    val difficulty: String,
    /** "линейный" | "петля" */
    val shape: String = "линейный",
    val surfaceNotes: String,
    val seasonNotes: String,
    val safetyNotes: String,
    val googleMapsBlocks: List<String> = emptyList(),
    val polyline: String? = null,
    /** Железные дороги и магистрали: оптимизатор штрафует порядок, при котором их пересекают лишний раз. */
    val barriers: List<Barrier> = emptyList(),
)

@Serializable
data class Barrier(val name: String, val kind: String, val line: List<GeoPoint>)

@Serializable
data class Stop(
    val id: String,
    val order: Int,
    val name: String,
    val coords: GeoPoint,
    /** Координаты сверены по OSM. Пока нет — зона отметки шире (см. Geofence). */
    val coordsVerified: Boolean = false,
    val address: String? = null,
    val zoneRadiusM: Int? = null,
    val zonePolygon: GeoPolygon? = null,
    /** "смотреть снаружи" | "заходим внутрь" | "проезжаем" */
    val kind: String,
    /** Необязательные точки не нужны для звания места, но дают бонусное звание. */
    val optional: Boolean = false,
    val visitMinutes: Int,
    val openingHoursNote: String = "",
    val schedule: Schedule? = null,
    /** Природа и лес — только в светлое время. */
    val daylightOnly: Boolean = false,
    val ticketNote: String = "",
    val bikeParkingNote: String = "",
    val guideStory: List<String>,
    val facts: List<Fact>,
    val warnings: List<String> = emptyList(),
)

/**
 * Часы работы по дням: ключи mon..sun, значение "10:00-18:00" или "closed".
 * Отсутствующий день считается закрытым; null вместо расписания — доступно всегда.
 */
@Serializable
data class Schedule(
    val days: Map<String, String>,
    val byAppointment: Boolean = false,
)

@Serializable
data class Fact(val text: String, val sourceUrl: String, val verified: Boolean)

@Serializable
data class Dish(
    val name: String,
    /** "блюдо" | "продукт" | "тематическая замена" */
    val kind: String,
    val honestyNote: String? = null,
    val story: List<String>,
    val whereToBuy: List<Shop>,
)

@Serializable
data class Shop(
    val name: String,
    val address: String,
    val coords: GeoPoint? = null,
    val verified: Boolean,
    val note: String = "",
)

@Serializable
data class Title(
    val id: String,
    val name: String,
    val legend: String,
    val ritual: String,
    val pairVariant: String,
    val bonusVariant: String? = null,
)

@Serializable
data class Source(val title: String, val url: String)
