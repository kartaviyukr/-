package com.weatherwear.assistant.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Работа с Open-Meteo (https://open-meteo.com) — бесплатный API прогноза
 * погоды, не требующий ключа и регистрации.
 *
 * Если захочется перейти на Яндекс.Погоду, достаточно заменить реализацию
 * [loadWeather]: формат [WeatherBundle] от источника не зависит.
 */
class WeatherRepository {

    private companion object {
        const val FORECAST_URL = "https://api.open-meteo.com/v1/forecast"
        const val GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search"

        const val CURRENT_FIELDS =
            "temperature_2m,apparent_temperature,relative_humidity_2m,precipitation," +
                "weather_code,wind_speed_10m,wind_gusts_10m,is_day"
        const val HOURLY_FIELDS =
            "temperature_2m,apparent_temperature,precipitation_probability,precipitation," +
                "snowfall,weather_code,wind_speed_10m,wind_gusts_10m,uv_index"
        const val DAILY_FIELDS =
            "weather_code,temperature_2m_max,temperature_2m_min,apparent_temperature_max," +
                "apparent_temperature_min,precipitation_sum,precipitation_probability_max," +
                "snowfall_sum,wind_speed_10m_max,wind_gusts_10m_max,uv_index_max"
    }

    suspend fun loadWeather(place: Place): WeatherBundle = withContext(Dispatchers.IO) {
        val url = buildString {
            append(FORECAST_URL)
            append("?latitude=").append(place.latitude)
            append("&longitude=").append(place.longitude)
            append("&current=").append(CURRENT_FIELDS)
            append("&hourly=").append(HOURLY_FIELDS)
            append("&daily=").append(DAILY_FIELDS)
            append("&timezone=auto")
            append("&wind_speed_unit=ms")
            append("&forecast_days=6")
        }
        val json = JSONObject(Http.getString(url))
        parseForecast(json, place)
    }

    /** Поиск города по названию. Возвращает пустой список, если ничего не найдено. */
    suspend fun searchPlaces(query: String): List<Place> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return@withContext emptyList()

        val url = GEOCODING_URL +
            "?name=" + URLEncoder.encode(trimmed, "UTF-8") +
            "&count=10&language=ru&format=json"
        val json = JSONObject(Http.getString(url))
        val results = json.optJSONArray("results") ?: return@withContext emptyList()

        (0 until results.length()).mapNotNull { index ->
            val item = results.optJSONObject(index) ?: return@mapNotNull null
            Place(
                name = item.optString("name"),
                admin1 = item.optString("admin1").takeIf { it.isNotBlank() },
                country = item.optString("country").takeIf { it.isNotBlank() },
                latitude = item.optDouble("latitude", Double.NaN),
                longitude = item.optDouble("longitude", Double.NaN),
            ).takeIf { !it.latitude.isNaN() && !it.longitude.isNaN() && it.name.isNotBlank() }
        }
    }

    private fun parseForecast(json: JSONObject, requestedPlace: Place): WeatherBundle {
        val utcOffsetSeconds = json.optInt("utc_offset_seconds", 0)
        val localNow = Instant.now()
            .atOffset(ZoneOffset.ofTotalSeconds(utcOffsetSeconds))
            .toLocalDateTime()

        val currentJson = json.optJSONObject("current")
            ?: throw IOException("Ответ сервера без текущей погоды")

        val current = CurrentWeather(
            temperature = currentJson.optDouble("temperature_2m", 0.0),
            apparentTemperature = currentJson.optDouble(
                "apparent_temperature",
                currentJson.optDouble("temperature_2m", 0.0),
            ),
            humidity = currentJson.optInt("relative_humidity_2m", 0),
            precipitation = currentJson.optDouble("precipitation", 0.0),
            weatherCode = currentJson.optInt("weather_code", 0),
            windSpeed = currentJson.optDouble("wind_speed_10m", 0.0),
            windGusts = currentJson.optDouble("wind_gusts_10m", 0.0),
            isDay = currentJson.optInt("is_day", 1) == 1,
        )

        val hourly = parseHourly(json.optJSONObject("hourly"))
        val daily = parseDaily(json.optJSONObject("daily"))

        val place = requestedPlace.copy(
            latitude = json.optDouble("latitude", requestedPlace.latitude),
            longitude = json.optDouble("longitude", requestedPlace.longitude),
        )

        return WeatherBundle(
            place = place,
            current = current,
            hourly = hourly,
            daily = daily,
            localNow = localNow,
            utcOffsetSeconds = utcOffsetSeconds,
        )
    }

    private fun parseHourly(hourlyJson: JSONObject?): List<HourlyPoint> {
        if (hourlyJson == null) return emptyList()
        val times = hourlyJson.optJSONArray("time") ?: return emptyList()

        val temperature = hourlyJson.optJSONArray("temperature_2m")
        val apparent = hourlyJson.optJSONArray("apparent_temperature")
        val probability = hourlyJson.optJSONArray("precipitation_probability")
        val precipitation = hourlyJson.optJSONArray("precipitation")
        val snowfall = hourlyJson.optJSONArray("snowfall")
        val codes = hourlyJson.optJSONArray("weather_code")
        val wind = hourlyJson.optJSONArray("wind_speed_10m")
        val gusts = hourlyJson.optJSONArray("wind_gusts_10m")
        val uv = hourlyJson.optJSONArray("uv_index")

        return (0 until times.length()).mapNotNull { i ->
            val time = parseDateTime(times.optString(i)) ?: return@mapNotNull null
            val temp = temperature.optDoubleAt(i, 0.0)
            HourlyPoint(
                time = time,
                temperature = temp,
                apparentTemperature = apparent.optDoubleAt(i, temp),
                precipitationProbability = probability.optIntAt(i, 0),
                precipitation = precipitation.optDoubleAt(i, 0.0),
                snowfall = snowfall.optDoubleAt(i, 0.0),
                weatherCode = codes.optIntAt(i, 0),
                windSpeed = wind.optDoubleAt(i, 0.0),
                windGusts = gusts.optDoubleAt(i, wind.optDoubleAt(i, 0.0)),
                uvIndex = uv.optDoubleAt(i, 0.0),
            )
        }
    }

    private fun parseDaily(dailyJson: JSONObject?): List<DailyPoint> {
        if (dailyJson == null) return emptyList()
        val dates = dailyJson.optJSONArray("time") ?: return emptyList()

        val codes = dailyJson.optJSONArray("weather_code")
        val tempMax = dailyJson.optJSONArray("temperature_2m_max")
        val tempMin = dailyJson.optJSONArray("temperature_2m_min")
        val apparentMax = dailyJson.optJSONArray("apparent_temperature_max")
        val apparentMin = dailyJson.optJSONArray("apparent_temperature_min")
        val precipitationSum = dailyJson.optJSONArray("precipitation_sum")
        val probabilityMax = dailyJson.optJSONArray("precipitation_probability_max")
        val snowfallSum = dailyJson.optJSONArray("snowfall_sum")
        val windMax = dailyJson.optJSONArray("wind_speed_10m_max")
        val gustsMax = dailyJson.optJSONArray("wind_gusts_10m_max")
        val uvMax = dailyJson.optJSONArray("uv_index_max")

        return (0 until dates.length()).mapNotNull { i ->
            val date = parseDate(dates.optString(i)) ?: return@mapNotNull null
            val max = tempMax.optDoubleAt(i, 0.0)
            val min = tempMin.optDoubleAt(i, 0.0)
            DailyPoint(
                date = date,
                weatherCode = codes.optIntAt(i, 0),
                temperatureMax = max,
                temperatureMin = min,
                apparentMax = apparentMax.optDoubleAt(i, max),
                apparentMin = apparentMin.optDoubleAt(i, min),
                precipitationSum = precipitationSum.optDoubleAt(i, 0.0),
                precipitationProbabilityMax = probabilityMax.optIntAt(i, 0),
                snowfallSum = snowfallSum.optDoubleAt(i, 0.0),
                windSpeedMax = windMax.optDoubleAt(i, 0.0),
                windGustsMax = gustsMax.optDoubleAt(i, 0.0),
                uvIndexMax = uvMax.optDoubleAt(i, 0.0),
            )
        }
    }

    private fun parseDateTime(value: String?): LocalDateTime? =
        runCatching { LocalDateTime.parse(value) }.getOrNull()

    private fun parseDate(value: String?): LocalDate? =
        runCatching { LocalDate.parse(value) }.getOrNull()

    /** Open-Meteo присылает null в массивах, если значение недоступно. */
    private fun JSONArray?.optDoubleAt(index: Int, fallback: Double): Double {
        if (this == null || index >= length() || isNull(index)) return fallback
        val value = optDouble(index, fallback)
        return if (value.isNaN()) fallback else value
    }

    private fun JSONArray?.optIntAt(index: Int, fallback: Int): Int {
        if (this == null || index >= length() || isNull(index)) return fallback
        return optInt(index, fallback)
    }
}
