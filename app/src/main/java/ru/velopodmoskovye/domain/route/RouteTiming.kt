package ru.velopodmoskovye.domain.route

import ru.velopodmoskovye.domain.geo.Geo
import ru.velopodmoskovye.domain.model.Destination
import ru.velopodmoskovye.domain.model.GeoPoint
import ru.velopodmoskovye.domain.model.Schedule
import ru.velopodmoskovye.domain.model.Stop
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/** Окно работы на конкретный день, минуты от полуночи. */
data class TimeWindow(val openMin: Int, val closeMin: Int)

object Schedules {
    private val KEYS = mapOf(
        DayOfWeek.MONDAY to "mon", DayOfWeek.TUESDAY to "tue", DayOfWeek.WEDNESDAY to "wed",
        DayOfWeek.THURSDAY to "thu", DayOfWeek.FRIDAY to "fri", DayOfWeek.SATURDAY to "sat",
        DayOfWeek.SUNDAY to "sun",
    )

    /** null — ограничений нет; пустой список — в этот день закрыто. */
    fun windowsFor(schedule: Schedule?, date: LocalDate): List<TimeWindow>? {
        if (schedule == null) return null
        val raw = schedule.days[KEYS.getValue(date.dayOfWeek)] ?: return emptyList()
        if (raw.isBlank() || raw == "closed") return emptyList()
        return raw.split(",").map { part ->
            val (a, b) = part.trim().split("-")
            TimeWindow(parse(a), parse(b))
        }
    }

    fun parse(hhmm: String): Int {
        val (h, m) = hhmm.trim().split(":").map { it.toInt() }
        return h * 60 + m
    }
}

/** Проверка «успеваю до заката» и предупреждения о часах работы на выбранную дату. */
object RouteTiming {
    const val DEFAULT_SPEED_KMH = 12.0
    /** Внутри города дорога длиннее прямой. */
    const val DETOUR = 1.35

    enum class Issue { CLOSED_THAT_DAY, WAIT_FOR_OPENING, CLOSES_BEFORE_ARRIVAL, AFTER_SUNSET, BY_APPOINTMENT }

    data class StopEta(val stop: Stop, val arriveMin: Int, val leaveMin: Int, val issues: List<Issue>)

    data class Result(
        val date: LocalDate,
        val startMin: Int,
        val finishMin: Int,
        val sunset: LocalTime?,
        val stops: List<StopEta>,
        val rideKm: Double,
    ) {
        val sunsetMin: Int? get() = sunset?.let { it.hour * 60 + it.minute }
        /** Сколько минут остаётся до заката на финише (отрицательное — не успеваем). */
        val marginMin: Int? get() = sunsetMin?.let { it - finishMin }
        val beforeSunset: Boolean get() = (marginMin ?: 1) >= 0
    }

    fun check(
        destination: Destination,
        stopsInOrder: List<Stop> = destination.stops.sortedBy { it.order },
        date: LocalDate,
        start: LocalTime = LocalTime.of(10, 0),
        speedKmh: Double = DEFAULT_SPEED_KMH,
    ): Result {
        val sun = Sun.times(date, destination.coords.lat, destination.coords.lon)
        val sunsetMin = sun.sunset?.let { it.hour * 60 + it.minute }
        var t = start.hour * 60 + start.minute
        var pos: GeoPoint = destination.startPoint.coords
        var km = 0.0
        val etas = mutableListOf<StopEta>()
        for (s in stopsInOrder) {
            val legKm = Geo.distanceKm(pos, s.coords) * DETOUR
            km += legKm
            t += (legKm / speedKmh * 60).toInt()
            val issues = mutableListOf<Issue>()
            val arrive = t
            val windows = Schedules.windowsFor(s.schedule, date)
            var visit = s.visitMinutes
            if (windows != null && s.kind == "заходим внутрь") {
                if (s.schedule?.byAppointment == true) issues += Issue.BY_APPOINTMENT
                if (windows.isEmpty()) {
                    issues += Issue.CLOSED_THAT_DAY
                    visit = minOf(visit, 10) // смотрим снаружи
                } else {
                    val w = windows.firstOrNull { t < it.closeMin }
                    when {
                        w == null -> { issues += Issue.CLOSES_BEFORE_ARRIVAL; visit = minOf(visit, 10) }
                        t < w.openMin -> { issues += Issue.WAIT_FOR_OPENING; t = w.openMin }
                    }
                }
            }
            t += visit
            if (s.daylightOnly && sunsetMin != null && t > sunsetMin) issues += Issue.AFTER_SUNSET
            etas += StopEta(s, arrive, t, issues)
            pos = s.coords
        }
        val lastKm = Geo.distanceKm(pos, destination.endPoint.coords) * DETOUR
        km += lastKm
        t += (lastKm / speedKmh * 60).toInt()
        return Result(date, start.hour * 60 + start.minute, t, sun.sunset, etas, km)
    }

    fun fmt(min: Int): String = "%02d:%02d".format((min / 60) % 24, min % 60)
}
