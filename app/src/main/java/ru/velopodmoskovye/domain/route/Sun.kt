package ru.velopodmoskovye.domain.route

import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Восход и закат по упрощённому алгоритму NOAA (точность — пара минут, для планирования хватает).
 * Время — местное, по умолчанию московское UTC+3 (в России нет перевода часов с 2014 года).
 */
object Sun {
    data class Day(val sunrise: LocalTime?, val sunset: LocalTime?, val polar: Boolean = false)

    fun times(date: LocalDate, lat: Double, lon: Double, utcOffsetH: Double = 3.0): Day {
        val n = date.dayOfYear
        val gamma = 2 * PI / 365.0 * (n - 1)
        val eqTime = 229.18 * (0.000075 + 0.001868 * cos(gamma) - 0.032077 * sin(gamma) -
            0.014615 * cos(2 * gamma) - 0.040849 * sin(2 * gamma))
        val decl = 0.006918 - 0.399912 * cos(gamma) + 0.070257 * sin(gamma) -
            0.006758 * cos(2 * gamma) + 0.000907 * sin(2 * gamma) -
            0.002697 * cos(3 * gamma) + 0.00148 * sin(3 * gamma)
        val latR = lat * PI / 180.0
        val zenith = 90.833 * PI / 180.0
        val cosHa = cos(zenith) / (cos(latR) * cos(decl)) - tan(latR) * tan(decl)
        if (cosHa < -1 || cosHa > 1) return Day(null, null, polar = true)
        val ha = acos(cosHa) * 180.0 / PI
        val sunriseMin = 720 - 4 * (lon + ha) - eqTime + utcOffsetH * 60
        val sunsetMin = 720 - 4 * (lon - ha) - eqTime + utcOffsetH * 60
        return Day(toTime(sunriseMin), toTime(sunsetMin))
    }

    private fun toTime(minutes: Double): LocalTime {
        val m = ((minutes.toInt() % 1440) + 1440) % 1440
        return LocalTime.of(m / 60, m % 60)
    }
}
