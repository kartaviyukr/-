package ru.velopodmoskovye.domain.route

import ru.velopodmoskovye.domain.model.GeoPoint
import java.util.Locale

/**
 * Ссылка «Построить в Яндекс Картах»: все точки маршрута по порядку в параметре rtext
 * (через «~»), тип маршрута — rtt (bc — велосипед, pd — пешком). Если на телефоне
 * стоят Яндекс Карты, ссылка откроется в приложении, иначе — в браузере.
 */
object YandexMapsLinks {
    fun url(points: List<GeoPoint>, mode: GoogleMapsLinks.Mode = GoogleMapsLinks.Mode.BICYCLING): String {
        require(points.size >= 2) { "Нужны хотя бы старт и финиш" }
        val rtt = when (mode) {
            GoogleMapsLinks.Mode.BICYCLING -> "bc"
            GoogleMapsLinks.Mode.WALKING -> "pd"
        }
        return "https://yandex.ru/maps/?rtext=" + points.joinToString("~") { fmt(it) } + "&rtt=" + rtt
    }

    private fun fmt(p: GeoPoint) = String.format(Locale.US, "%.6f%%2C%.6f", p.lat, p.lon)
}
