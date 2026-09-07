package com.weatherwear.assistant.domain

import com.weatherwear.assistant.data.CurrentWeather
import com.weatherwear.assistant.data.HourlyPoint
import com.weatherwear.assistant.data.Place
import com.weatherwear.assistant.data.WeatherBundle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class AdvicePlannerTest {

    private val now: LocalDateTime = LocalDateTime.of(2026, 1, 15, 9, 40)

    private fun hour(offset: Long, temp: Double, probability: Int = 0, precipitation: Double = 0.0) =
        HourlyPoint(
            time = now.withMinute(0).plusHours(offset),
            temperature = temp,
            apparentTemperature = temp - 2,
            precipitationProbability = probability,
            precipitation = precipitation,
            snowfall = 0.0,
            weatherCode = if (probability > 50) 61 else 1,
            windSpeed = 3.0,
            windGusts = 6.0,
            uvIndex = 2.0,
        )

    private fun bundle(hourly: List<HourlyPoint>) = WeatherBundle(
        place = Place(name = "Тест", latitude = 55.0, longitude = 37.0),
        current = CurrentWeather(
            temperature = 5.0,
            apparentTemperature = 3.0,
            humidity = 60,
            precipitation = 0.0,
            weatherCode = 1,
            windSpeed = 3.0,
            windGusts = 5.0,
            isDay = true,
        ),
        hourly = hourly,
        daily = emptyList(),
        localNow = now,
        utcOffsetSeconds = 10800,
    )

    @Test
    fun `окно планирования начинается с текущего часа`() {
        val hourly = (-3L..20L).map { hour(it, 5.0) }
        val window = AdvicePlanner.planningWindow(hourly, now, horizonHours = 12)

        assertEquals(12, window.size)
        assertEquals(9, window.first().time.hour)
        assertEquals(20, window.last().time.hour)
    }

    @Test
    fun `агрегаты берутся по всему окну а не только по текущему часу`() {
        val hourly = listOf(
            hour(0, 8.0),
            hour(1, 6.0),
            hour(2, 2.0, probability = 90, precipitation = 1.5),
            hour(3, 0.0, probability = 60, precipitation = 1.0),
        ) + (4L..12L).map { hour(it, 1.0) }

        val advice = AdvicePlanner.buildInput(bundle(hourly))

        assertEquals(90, advice.precipitationProbabilityMax)
        assertEquals(2.5, advice.precipitationSum, 0.001)
        assertEquals(-2.0, advice.feelsLikeMin, 0.001)
        assertTrue(advice.weatherCodes.contains(61))
    }

    @Test
    fun `без почасового прогноза используются текущие данные`() {
        val advice = AdvicePlanner.buildInput(bundle(emptyList()))

        assertEquals(5.0, advice.temperatureNow, 0.001)
        assertEquals(3.0, advice.feelsLikeMin, 0.001)
        assertEquals(0, advice.precipitationProbabilityMax)
    }
}
