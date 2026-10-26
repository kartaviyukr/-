package ru.velopodmoskovye.domain.route

import ru.velopodmoskovye.domain.model.GeoPoint
import java.util.Locale

/**
 * Ссылки «Вести в Google Maps». Google принимает не больше 9 промежуточных точек,
 * поэтому длинный маршрут режется на блоки: конец одного блока — начало следующего.
 */
object GoogleMapsLinks {
    const val MAX_WAYPOINTS = 9

    enum class Mode(val param: String) { BICYCLING("bicycling"), WALKING("walking") }

    data class Block(val index: Int, val fromIndex: Int, val toIndex: Int, val url: String)

    fun blocks(points: List<GeoPoint>, mode: Mode = Mode.BICYCLING): List<Block> {
        if (points.size < 2) return emptyList()
        val perBlock = MAX_WAYPOINTS + 2
        val result = mutableListOf<Block>()
        var start = 0
        while (start < points.lastIndex) {
            val end = minOf(start + perBlock - 1, points.lastIndex)
            result += Block(result.size + 1, start, end, url(points.subList(start, end + 1), mode))
            start = end
        }
        return result
    }

    fun url(points: List<GeoPoint>, mode: Mode): String {
        require(points.size >= 2) { "Нужны хотя бы старт и финиш" }
        require(points.size - 2 <= MAX_WAYPOINTS) { "Слишком много промежуточных точек" }
        val sb = StringBuilder("https://www.google.com/maps/dir/?api=1")
        sb.append("&origin=").append(fmt(points.first()))
        sb.append("&destination=").append(fmt(points.last()))
        val mid = points.subList(1, points.lastIndex)
        if (mid.isNotEmpty()) sb.append("&waypoints=").append(mid.joinToString("%7C") { fmt(it) })
        sb.append("&travelmode=").append(mode.param)
        return sb.toString()
    }

    private fun fmt(p: GeoPoint) = String.format(Locale.US, "%.6f%%2C%.6f", p.lat, p.lon)
}
