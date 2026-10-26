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
 * Большой маршрут к цели: спираль от дальних мест к Кремлю.
 *
 * 1. Места делятся на кольца по расстоянию от Кремля (нижняя граница включительно).
 * 2. Первое кольцо начинается с самого дальнего места.
 * 3. Внутри кольца места идут по часовой стрелке по азимуту, начиная с азимута,
 *    на котором закончилось предыдущее кольцо, — так переход в следующее кольцо
 *    ведёт к ближайшему по ходу места, а не через всю область.
 * 4. Финиш (Кремль) всегда последний.
 */
object SpiralBuilder {
    /** Нижние границы колец, км: >120, 100–120, 80–100, 60–80, 40–60, 20–40, <20. */
    val DEFAULT_RING_BOUNDS = listOf(120.0, 100.0, 80.0, 60.0, 40.0, 20.0, 0.0)

    val RING_NAMES = listOf(
        "дальше 120 км", "100–120 км", "80–100 км", "60–80 км", "40–60 км", "20–40 км", "ближе 20 км",
    )

    fun ringOf(distanceKm: Double, bounds: List<Double> = DEFAULT_RING_BOUNDS): Int {
        val i = bounds.indexOfFirst { distanceKm >= it }
        return if (i < 0) bounds.lastIndex else i
    }

    fun <T> build(
        items: List<T>,
        coords: (T) -> GeoPoint,
        isFinish: (T) -> Boolean = { false },
        origin: GeoPoint = Geo.KREMLIN,
        bounds: List<Double> = DEFAULT_RING_BOUNDS,
    ): List<SpiralNode<T>> {
        data class Tmp<T>(val item: T, val index: Int, val d: Double, val az: Double, val ring: Int)

        val (finish, rest) = items.withIndex().partition { isFinish(it.value) }
        val all = rest.map { (i, t) ->
            val c = coords(t)
            val d = Geo.distanceKm(origin, c)
            Tmp(t, i, d, Geo.bearingDeg(origin, c), ringOf(d, bounds))
        }
        val result = mutableListOf<Tmp<T>>()
        var startAz: Double? = null
        for (ring in bounds.indices) {
            val inRing = all.filter { it.ring == ring }
            if (inRing.isEmpty()) continue
            val start = startAz ?: inRing.maxBy { it.d }.az
            val sorted = inRing.sortedWith(compareBy<Tmp<T>>({ clockwiseFrom(start, it.az) }, { it.index }))
            result += sorted
            startAz = sorted.last().az
        }
        val nodes = result.mapIndexed { i, t -> SpiralNode(t.item, i + 1, t.ring, t.d, t.az) }.toMutableList()
        finish.forEach { (_, t) ->
            val c = coords(t)
            nodes += SpiralNode(t, nodes.size + 1, bounds.lastIndex, Geo.distanceKm(origin, c), 0.0)
        }
        return nodes
    }

    /** На сколько градусов надо повернуть по часовой стрелке от from до to (0..360). */
    fun clockwiseFrom(from: Double, to: Double): Double = ((to - from) % 360.0 + 360.0) % 360.0

    /**
     * Блоки внутри кольца: соседние места с почти одинаковым азимутом склеиваются,
     * даже если по расстоянию они в разных концах кольца.
     * Возвращает номер блока для каждого узла (в порядке списка).
     */
    fun <T> azimuthBlocks(nodes: List<SpiralNode<T>>, toleranceDeg: Double = 6.0): List<Int> {
        var block = 0
        return nodes.mapIndexed { i, n ->
            if (i > 0) {
                val prev = nodes[i - 1]
                val delta = clockwiseFrom(prev.azimuthDeg, n.azimuthDeg).let { minOf(it, 360 - it) }
                if (prev.ring != n.ring || delta > toleranceDeg) block++
            }
            block
        }
    }
}

/** Перегон между финишем одного места и стартом следующего. */
data class Transfer(
    val fromId: String,
    val toId: String,
    val straightKm: Double,
    val rideKm: Double,
    val durationH: Double,
    /** Перегон длинный — стоит предложить сократить электричкой или МЦД. */
    val suggestTrain: Boolean,
)

object Transfers {
    /** Дорога длиннее прямой примерно на четверть-треть. */
    const val DETOUR = 1.3
    const val SPEED_KMH = 15.0
    const val TRAIN_THRESHOLD_KM = 35.0

    fun between(fromId: String, from: GeoPoint, toId: String, to: GeoPoint, speedKmh: Double = SPEED_KMH): Transfer {
        val straight = Geo.distanceKm(from, to)
        val ride = straight * DETOUR
        return Transfer(fromId, toId, straight, ride, ride / speedKmh, ride > TRAIN_THRESHOLD_KM)
    }
}

/** Один пункт большого маршрута для разбивки на дни. */
data class Leg(val placeId: String, val transferKm: Double, val placeKm: Double, val block: Int) {
    val totalKm get() = transferKm + placeKm
}

data class Day(val index: Int, val placeIds: List<String>, val km: Double) {
    /** Ночёвка — в последнем месте дня. */
    val overnightAt: String get() = placeIds.last()
}

object DayPlanner {
    /**
     * Жадная разбивка на дни по целевому километражу (40–120 км).
     * Блок мест с одинаковым азимутом стараемся не рвать: если он целиком
     * влезает в день с запасом 30 %, оставляем его вместе.
     */
    fun plan(legs: List<Leg>, targetKmPerDay: Double): List<Day> {
        require(targetKmPerDay > 0)
        val days = mutableListOf<Day>()
        var cur = mutableListOf<String>()
        var km = 0.0
        fun close() {
            if (cur.isNotEmpty()) days += Day(days.size + 1, cur, km)
            cur = mutableListOf()
            km = 0.0
        }
        var i = 0
        while (i < legs.size) {
            // Остаток текущего блока, начиная с i.
            val blockEnd = (i until legs.size).takeWhile { legs[it].block == legs[i].block }.last()
            val blockStartsHere = i == 0 || legs[i - 1].block != legs[i].block
            val blockKm = (i..blockEnd).sumOf { legs[it].totalKm }
            if (blockStartsHere && cur.isNotEmpty() && km + blockKm > targetKmPerDay * 1.3 &&
                blockKm <= targetKmPerDay * 1.3 && km >= targetKmPerDay * 0.5
            ) {
                close()
            }
            val leg = legs[i]
            if (cur.isNotEmpty() && km + leg.totalKm > targetKmPerDay * 1.15) close()
            cur += leg.placeId
            km += leg.totalKm
            i++
        }
        close()
        return days
    }
}
