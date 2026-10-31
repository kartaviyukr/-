package ru.velopodmoskovye.domain.edit

import kotlinx.serialization.Serializable
import ru.velopodmoskovye.domain.geo.Geo
import ru.velopodmoskovye.domain.model.Destination
import ru.velopodmoskovye.domain.model.GeoPoint
import ru.velopodmoskovye.domain.model.RoutePoint
import ru.velopodmoskovye.domain.model.Stop

/**
 * Правки, которые пользователь сделал сам: поправленные координаты точек и свои новые точки.
 * Хранятся на телефоне отдельно от контент-пака и накладываются на него при каждом чтении,
 * поэтому обновление приложения их не стирает.
 */
@Serializable
data class UserEdits(
    val version: Int = 1,
    val moves: List<StopMove> = emptyList(),
    val added: List<UserStop> = emptyList(),
) {
    val isEmpty: Boolean get() = moves.isEmpty() && added.isEmpty()
    val count: Int get() = moves.size + added.size

    fun moveFor(destinationId: String, stopId: String): StopMove? =
        moves.firstOrNull { it.destinationId == destinationId && it.stopId == stopId }

    fun withMove(m: StopMove): UserEdits {
        // Свою точку правим прямо в списке добавленных.
        if (added.any { it.destinationId == m.destinationId && it.id == m.stopId }) {
            return copy(added = added.map { if (it.destinationId == m.destinationId && it.id == m.stopId) it.copy(coords = m.coords) else it })
        }
        return copy(moves = moves.filterNot { it.destinationId == m.destinationId && it.stopId == m.stopId } + m)
    }

    fun withoutMove(destinationId: String, stopId: String): UserEdits =
        copy(moves = moves.filterNot { it.destinationId == destinationId && it.stopId == stopId })

    fun withAdded(s: UserStop): UserEdits = copy(added = added.filterNot { it.destinationId == s.destinationId && it.id == s.id } + s)

    fun withoutAdded(destinationId: String, id: String): UserEdits =
        copy(added = added.filterNot { it.destinationId == destinationId && it.id == id })
}

/** Новые координаты точки маршрута; stopId "start" и "end" — старт и финиш. */
@Serializable
data class StopMove(
    val destinationId: String,
    val stopId: String,
    val coords: GeoPoint,
    val editedAt: Long,
    /** Название на момент правки — чтобы выгрузка была понятна человеку. */
    val name: String = "",
)

@Serializable
data class UserStop(
    val destinationId: String,
    val id: String,
    val name: String,
    val coords: GeoPoint,
    val note: String = "",
    val createdAt: Long,
)

object EditApplier {
    const val START = "start"
    const val END = "end"
    const val USER_PREFIX = "user-"

    fun isUserStop(stopId: String) = stopId.startsWith(USER_PREFIX)

    fun apply(dest: Destination, edits: UserEdits): Destination {
        val moves = edits.moves.filter { it.destinationId == dest.id }.associateBy { it.stopId }
        val added = edits.added.filter { it.destinationId == dest.id }
        if (moves.isEmpty() && added.isEmpty()) return dest

        fun RoutePoint.moved(key: String) = moves[key]?.let { copy(coords = it.coords) } ?: this
        val start = dest.startPoint.moved(START)
        val end = dest.endPoint.moved(END)
        var stops = dest.stops.sortedBy { it.order }.map { s ->
            moves[s.id]?.let { s.copy(coords = it.coords, coordsVerified = true) } ?: s
        }
        for (u in added.sortedBy { it.createdAt }) {
            stops = insertCheapest(start.coords, end.coords, stops, toStop(u))
        }
        return dest.copy(
            startPoint = start,
            endPoint = end,
            stops = stops.mapIndexed { i, s -> s.copy(order = i + 1) },
        )
    }

    fun toStop(u: UserStop) = Stop(
        id = u.id,
        order = 0,
        name = u.name,
        coords = u.coords,
        coordsVerified = true,
        kind = "смотреть снаружи",
        // Своя точка не мешает получить звание места.
        optional = true,
        visitMinutes = 15,
        guideStory = listOf(u.note.ifBlank { "Эту точку вы добавили сами." }),
        facts = emptyList(),
    )

    /** Вставляем точку туда, где она меньше всего удлиняет маршрут. */
    fun insertCheapest(start: GeoPoint, end: GeoPoint, stops: List<Stop>, stop: Stop): List<Stop> {
        val pts = listOf(start) + stops.map { it.coords } + end
        var best = 0
        var bestCost = Double.MAX_VALUE
        for (i in 0 until pts.size - 1) {
            val cost = Geo.distanceKm(pts[i], stop.coords) + Geo.distanceKm(stop.coords, pts[i + 1]) - Geo.distanceKm(pts[i], pts[i + 1])
            if (cost < bestCost) {
                bestCost = cost
                best = i
            }
        }
        return stops.take(best) + stop + stops.drop(best)
    }
}

/**
 * Координаты из того, что пользователь скопировал: «55.75, 37.61», «55,75 37,61», «55.75° N, 37.61° E»,
 * ссылка Яндекс Карт (ll= / pt= / whatshere — там сначала долгота) или Google Maps (@lat,lon / q=lat,lon).
 */
object CoordParser {
    private const val NUM = """-?\d{1,3}(?:[.,]\d+)?"""
    private const val SEP = """(?:,|%2C)"""
    /** В ссылках Яндекса сначала долгота; метка (pt, whatshere) важнее центра карты (ll). */
    private val yandex = listOf("""whatshere(?:%5B|\[)point(?:%5D|])""", "pt", "ll")
        .map { Regex("""[?&]$it=($NUM)$SEP($NUM)""", RegexOption.IGNORE_CASE) }
    private val googleAt = Regex("""@($NUM),($NUM)""")
    private val googleQ = Regex("""[?&](?:q|query|ll|destination)=($NUM)$SEP($NUM)""")
    private val plainDot = Regex("""(?<![\d.,])(-?\d{1,3}\.\d+)\s*°?\s*[NnСс]?\s*[,;\s]\s*(-?\d{1,3}\.\d+)(?![\d.])""")
    private val plainComma = Regex("""(?<![\d.,])(-?\d{1,3},\d+)\s*°?\s*[NnСс]?\s*[;\s]\s*(-?\d{1,3},\d+)(?![\d,])""")

    fun parse(text: String): GeoPoint? {
        val t = text.trim()
        if (t.isEmpty()) return null
        if (t.contains("yandex", ignoreCase = true) || t.contains("ya.ru")) {
            for (r in yandex) r.find(t)?.let { m -> point(m.groupValues[2], m.groupValues[1])?.let { return it } }
        }
        for (r in listOf(googleAt, googleQ, plainDot, plainComma)) {
            r.find(t)?.let { m -> point(m.groupValues[1], m.groupValues[2])?.let { return it } }
        }
        return null
    }

    private fun point(lat: String, lon: String): GeoPoint? {
        val la = lat.replace(',', '.').toDoubleOrNull() ?: return null
        val lo = lon.replace(',', '.').toDoubleOrNull() ?: return null
        if (la !in -90.0..90.0 || lo !in -180.0..180.0) return null
        return GeoPoint(la, lo)
    }
}
