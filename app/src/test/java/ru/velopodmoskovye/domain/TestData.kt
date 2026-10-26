package ru.velopodmoskovye.domain

import ru.velopodmoskovye.domain.model.Dish
import ru.velopodmoskovye.domain.model.Destination
import ru.velopodmoskovye.domain.model.GeoPoint
import ru.velopodmoskovye.domain.model.RouteInfo
import ru.velopodmoskovye.domain.model.RoutePoint
import ru.velopodmoskovye.domain.model.Schedule
import ru.velopodmoskovye.domain.model.Stop
import ru.velopodmoskovye.domain.model.Title
import java.io.File

object TestData {
    val repoRoot: File = File(System.getProperty("repoRoot") ?: "..").absoluteFile

    data class AppendixRow(val num: Int, val name: String, val distKm: Double, val az: Double, val point: GeoPoint)

    /** Приложение А из ТЗ: 183 места в эталонном порядке спирали. */
    fun appendixA(): List<AppendixRow> =
        File(repoRoot, "content/tools/appendix_a.tsv").readLines(Charsets.UTF_8).filter { it.isNotBlank() }.map {
            val c = it.split('\t')
            AppendixRow(c[0].toInt(), c[2], c[5].toDouble(), c[6].toDouble(), GeoPoint(c[7].toDouble(), c[8].toDouble()))
        }

    fun stop(
        id: String, lat: Double, lon: Double, order: Int = 0, optional: Boolean = false,
        kind: String = "смотреть снаружи", visit: Int = 10, schedule: Schedule? = null,
        radius: Int? = null, daylight: Boolean = false,
    ) = Stop(
        id = id, order = order, name = id, coords = GeoPoint(lat, lon), zoneRadiusM = radius, kind = kind,
        optional = optional, visitMinutes = visit, schedule = schedule, daylightOnly = daylight,
        guideStory = listOf("…"), facts = emptyList(),
    )

    fun destination(id: String, stops: List<Stop>, start: GeoPoint = stops.first().coords, end: GeoPoint = stops.last().coords) =
        Destination(
            id = id, name = id, settlementType = "город", mustHave = 1, regionId = "r-$id",
            distanceFromKremlinKm = 20.0, coords = stops.first().coords, intro = listOf("…"),
            startPoint = RoutePoint("старт", start, "МЦД"), endPoint = RoutePoint("финиш", end, "метро"),
            route = RouteInfo(10.0, 3.0, "лёгкий", surfaceNotes = "", seasonNotes = "", safetyNotes = ""),
            stops = stops, dish = Dish("хлеб", "тематическая замена", null, listOf("…"), emptyList()),
            title = Title("t-$id", "Звание $id", "легенда", "ритуал", "Пара $id", "Бонус $id"),
            sources = emptyList(),
        )
}
