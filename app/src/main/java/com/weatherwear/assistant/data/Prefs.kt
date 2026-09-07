package com.weatherwear.assistant.data

import android.content.Context

/** Запоминаем последний выбранный город, чтобы приложение открывалось сразу с прогнозом. */
class Prefs(context: Context) {

    private val prefs = context.getSharedPreferences("chto_nadet", Context.MODE_PRIVATE)

    var useGeolocation: Boolean
        get() = prefs.getBoolean(KEY_USE_GEO, true)
        set(value) = prefs.edit().putBoolean(KEY_USE_GEO, value).apply()

    fun savePlace(place: Place) {
        prefs.edit()
            .putString(KEY_NAME, place.name)
            .putString(KEY_ADMIN, place.admin1)
            .putString(KEY_COUNTRY, place.country)
            .putFloat(KEY_LAT, place.latitude.toFloat())
            .putFloat(KEY_LON, place.longitude.toFloat())
            .apply()
    }

    fun loadPlace(): Place? {
        val name = prefs.getString(KEY_NAME, null) ?: return null
        if (!prefs.contains(KEY_LAT) || !prefs.contains(KEY_LON)) return null
        return Place(
            name = name,
            admin1 = prefs.getString(KEY_ADMIN, null),
            country = prefs.getString(KEY_COUNTRY, null),
            latitude = prefs.getFloat(KEY_LAT, 0f).toDouble(),
            longitude = prefs.getFloat(KEY_LON, 0f).toDouble(),
        )
    }

    private companion object {
        const val KEY_USE_GEO = "use_geolocation"
        const val KEY_NAME = "place_name"
        const val KEY_ADMIN = "place_admin"
        const val KEY_COUNTRY = "place_country"
        const val KEY_LAT = "place_lat"
        const val KEY_LON = "place_lon"
    }
}

/** Москва — запасной вариант, если геолокация недоступна и город ещё не выбран. */
val DEFAULT_PLACE = Place(
    name = "Москва",
    admin1 = "Москва",
    country = "Россия",
    latitude = 55.7558,
    longitude = 37.6173,
)
