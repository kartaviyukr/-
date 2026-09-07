package com.weatherwear.assistant.data

import java.time.LocalDate
import java.time.LocalDateTime

/** Населённый пункт, для которого показывается прогноз. */
data class Place(
    val name: String,
    val admin1: String? = null,
    val country: String? = null,
    val latitude: Double,
    val longitude: Double,
) {
    /** Короткая подпись под названием города: «Московская область, Россия». */
    val subtitle: String
        get() = listOfNotNull(admin1?.takeIf { it.isNotBlank() && it != name }, country?.takeIf { it.isNotBlank() })
            .joinToString(", ")
}

data class CurrentWeather(
    val temperature: Double,
    val apparentTemperature: Double,
    val humidity: Int,
    val precipitation: Double,
    val weatherCode: Int,
    val windSpeed: Double,
    val windGusts: Double,
    val isDay: Boolean,
)

data class HourlyPoint(
    val time: LocalDateTime,
    val temperature: Double,
    val apparentTemperature: Double,
    val precipitationProbability: Int,
    val precipitation: Double,
    val snowfall: Double,
    val weatherCode: Int,
    val windSpeed: Double,
    val windGusts: Double,
    val uvIndex: Double,
)

data class DailyPoint(
    val date: LocalDate,
    val weatherCode: Int,
    val temperatureMax: Double,
    val temperatureMin: Double,
    val apparentMax: Double,
    val apparentMin: Double,
    val precipitationSum: Double,
    val precipitationProbabilityMax: Int,
    val snowfallSum: Double,
    val windSpeedMax: Double,
    val windGustsMax: Double,
    val uvIndexMax: Double,
)

/** Всё, что приложение знает о погоде в выбранной точке. */
data class WeatherBundle(
    val place: Place,
    val current: CurrentWeather,
    val hourly: List<HourlyPoint>,
    val daily: List<DailyPoint>,
    val localNow: LocalDateTime,
    val utcOffsetSeconds: Int,
)
