package ru.velopodmoskovye.domain.spiral

import ru.velopodmoskovye.domain.geo.Geo
import ru.velopodmoskovye.domain.model.GeoPoint

/** Место в спирали после расчёта: кольцо, расстояние и азимут от Кремля, порядковый номер. */
data class SpiralNode<T>(
    val item: T,
    val order: Int,
    val ring: Int,
    val distanceKm: Double,
    val azimuthDeg: Double,
)

/**
 * Большой маршрут: спираль от Кремля наружу.
 *
 * 1. Старт — ЦАО, округ с Кремлём.
 * 2. Кольцо 0 — административные округа Москвы внутри МКАД. Остальные места делятся
 *    на кольца по расстоянию от Кремля (нижняя граница включительно); кольца идут изнутри наружу.
 * 3. Первое кольцо начинается с ближайшего к Кремлю места.
 * 4. Внутри кольца места идут по часовой стрелке по азимуту, начиная с азимута,
 *    на котором закончилось предыдущее кольцо, — переход наружу ведёт к ближайшему
 *    по ходу месту, а не через всю область.
 */
object SpiralBuilder {
    /** Нижние границы колец за МКАД, км: <20, 20–40, 40–60, 60–80, 80–100, 100–120, >120. */
    val RING_LOWER_KM = listOf(0.0, 20.0, 40.0, 60.0, 80.0, 100.0, 120.0)

    val RING_NAMES = listOf(
        "Москва внутри МКАД",
        "ближе 20 км", "20–40 км", "40–60 км", "60–80 км", "80–100 км", "100–120 км", "дальше 120 км",
    )

    const val INSIDE_MKAD_RING = 0

    /** Кольцо места за МКАД (1..7). */
    fun ringOf(distanceKm: Double, lower: List<Double> = RING_LOWER_KM): Int =
        1 + lower.indexOfLast { distanceKm >= it }.coerceAtLeast(0)

    fun <T> build(
        items: List<T>,
        coords: (T) -> GeoPoint,
        isStart: (T) -> Boolean = { false },
        insideMkad: (T) -> Boolean = { false },
        origin: GeoPoint = Geo.KREMLIN,
        lower: List<Double> = RING_LOWER_KM,
    ): List<SpiralNode<T>> {
        data class Tmp<T>(val item: T, val index: Int, val d: Double, val az: Double, val ring: Int)

        val (start, rest) = items.withIndex().partition { isStart(it.value) }
        val all = rest.map { (i, t) ->
            val c = coords(t)
            val d = Geo.distanceKm(origin, c)
            Tmp(t, i, d, Geo.bearingDeg(origin, c), if (insideMkad(t)) INSIDE_MKAD_RING else ringOf(d, lower))
        }
        val nodes = mutableListOf<SpiralNode<T>>()
        start.forEach { (_, t) -> nodes += SpiralNode(t, nodes.size + 1, 0, Geo.distanceKm(origin, coords(t)), 0.0) }
        var startAz: Double? = null
        for (ring in 0..lower.size) {
            val inRing = all.filter { it.ring == ring }
            if (inRing.isEmpty()) continue
            val from = startAz ?: inRing.minBy { it.d }.az
            val sorted = inRing.sortedWith(compareBy<Tmp<T>>({ clockwiseFrom(from, it.az) }, { it.d }, { it.index }))
            sorted.forEach { nodes += SpiralNode(it.item, nodes.size + 1, it.ring, it.d, it.az) }
            startAz = sorted.last().az
        }
        return nodes
    }

    /** На сколько градусов надо повернуть по часовой стрелке от from до to (0..360). */
    fun clockwiseFrom(from: Double, to: Double): Double = ((to - from) % 360.0 + 360.0) % 360.0
}
