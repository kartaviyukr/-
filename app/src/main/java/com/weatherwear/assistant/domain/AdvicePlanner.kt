package com.weatherwear.assistant.domain

import com.weatherwear.assistant.data.HourlyPoint
import com.weatherwear.assistant.data.WeatherBundle
import java.time.LocalDateTime

/**
 * Собирает [AdviceInput] из прогноза: берём ближайшие часы, а не только «сейчас»,
 * чтобы совет учитывал вечернее похолодание и дождь через три часа.
 */
object AdvicePlanner {

    const val DEFAULT_HORIZON_HOURS = 12

    /** Часы, по которым строится совет: от текущего часа и на [horizonHours] вперёд. */
    fun planningWindow(
        hourly: List<HourlyPoint>,
        now: LocalDateTime,
        horizonHours: Int = DEFAULT_HORIZON_HOURS,
    ): List<HourlyPoint> {
        val startOfHour = now.withMinute(0).withSecond(0).withNano(0)
        val end = startOfHour.plusHours(horizonHours.toLong())
        val window = hourly.filter { !it.time.isBefore(startOfHour) && it.time.isBefore(end) }
        return window.ifEmpty { hourly.take(horizonHours) }
    }

    fun buildInput(
        bundle: WeatherBundle,
        horizonHours: Int = DEFAULT_HORIZON_HOURS,
    ): AdviceInput {
        val window = planningWindow(bundle.hourly, bundle.localNow, horizonHours)
        val current = bundle.current
        val today = bundle.daily.firstOrNull()

        if (window.isEmpty()) {
            // Прогноза по часам нет — работаем по текущим значениям и суткам.
            return AdviceInput(
                temperatureNow = current.temperature,
                feelsLikeNow = current.apparentTemperature,
                feelsLikeMin = today?.apparentMin ?: current.apparentTemperature,
                feelsLikeMax = today?.apparentMax ?: current.apparentTemperature,
                windSpeedMax = maxOf(current.windSpeed, today?.windSpeedMax ?: 0.0),
                windGustsMax = maxOf(current.windGusts, today?.windGustsMax ?: 0.0),
                precipitationProbabilityMax = today?.precipitationProbabilityMax ?: 0,
                precipitationSum = today?.precipitationSum ?: current.precipitation,
                snowfallSum = today?.snowfallSum ?: 0.0,
                uvIndexMax = today?.uvIndexMax ?: 0.0,
                humidityNow = current.humidity,
                isDay = current.isDay,
                weatherCodes = listOfNotNull(current.weatherCode, today?.weatherCode),
            )
        }

        return AdviceInput(
            temperatureNow = current.temperature,
            feelsLikeNow = current.apparentTemperature,
            feelsLikeMin = minOf(window.minOf { it.apparentTemperature }, current.apparentTemperature),
            feelsLikeMax = maxOf(window.maxOf { it.apparentTemperature }, current.apparentTemperature),
            windSpeedMax = maxOf(window.maxOf { it.windSpeed }, current.windSpeed),
            windGustsMax = maxOf(window.maxOf { it.windGusts }, current.windGusts),
            precipitationProbabilityMax = window.maxOf { it.precipitationProbability },
            precipitationSum = window.sumOf { it.precipitation },
            snowfallSum = window.sumOf { it.snowfall },
            uvIndexMax = window.maxOf { it.uvIndex },
            humidityNow = current.humidity,
            isDay = current.isDay,
            weatherCodes = (window.map { it.weatherCode } + current.weatherCode).distinct(),
        )
    }
}
