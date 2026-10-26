package ru.velopodmoskovye.domain.route

import ru.velopodmoskovye.domain.geo.Geo
import ru.velopodmoskovye.domain.model.Barrier
import ru.velopodmoskovye.domain.model.GeoPoint
import ru.velopodmoskovye.domain.model.Stop
import java.time.LocalDate

/**
 * Порядок точек внутри места: TSP с фиксированными стартом и финишем,
 * окнами работы и штрафом за пересечение барьеров (ж/д, магистрали).
 *
 * Ручной порядок из контента главный — оптимизатор только сообщает,
 * если нашёл заметно лучший вариант.
 */
object RouteOptimizer {
    data class Params(
        val speedKmh: Double = RouteTiming.DEFAULT_SPEED_KMH,
        val startMin: Int = 10 * 60,
        /** Штраф за одно пересечение барьера, в минутах — переезд ищем, ждём, переводим велосипед. */
        val barrierPenaltyMin: Double = 15.0,
        /** Штраф за точку, которая окажется закрыта к приезду. */
        val closedPenaltyMin: Double = 120.0,
        /** «Заметно лучше» — экономия не меньше этой доли и не меньше minGainMin минут. */
        val minGainShare: Double = 0.10,
        val minGainMin: Double = 10.0,
    )

    data class Evaluation(val order: List<Stop>, val costMin: Double, val rideKm: Double, val barrierCrossings: Int)

    data class Suggestion(val manual: Evaluation, val best: Evaluation) {
        val gainMin get() = manual.costMin - best.costMin
    }

    fun evaluate(
        start: GeoPoint, end: GeoPoint, order: List<Stop>, barriers: List<Barrier>,
        date: LocalDate?, p: Params = Params(),
    ): Evaluation {
        var t = p.startMin.toDouble()
        var cost = 0.0
        var km = 0.0
        var crossings = 0
        var pos = start
        fun ride(to: GeoPoint) {
            val legKm = Geo.distanceKm(pos, to) * RouteTiming.DETOUR
            km += legKm
            t += legKm / p.speedKmh * 60
            val c = barriers.sumOf { Geo.crossings(pos, to, it.line) }
            crossings += c
            cost += c * p.barrierPenaltyMin
            pos = to
        }
        for (s in order) {
            ride(s.coords)
            val windows = date?.let { Schedules.windowsFor(s.schedule, it) }
            if (windows != null && s.kind == "заходим внутрь") {
                val w = windows.firstOrNull { t < it.closeMin }
                when {
                    w == null -> cost += p.closedPenaltyMin
                    t < w.openMin -> t = w.openMin.toDouble()
                }
            }
            t += s.visitMinutes
        }
        ride(end)
        return Evaluation(order, cost + (t - p.startMin), km, crossings)
    }

    /** Лучший порядок: перебор для ≤ 8 точек, иначе Хелд–Карп по расстоянию + локальное улучшение 2-opt. */
    fun optimize(
        start: GeoPoint, end: GeoPoint, stops: List<Stop>, barriers: List<Barrier>,
        date: LocalDate?, p: Params = Params(),
    ): Evaluation {
        if (stops.size <= 1) return evaluate(start, end, stops, barriers, date, p)
        val seed = if (stops.size <= 8) {
            var best: Evaluation? = null
            permutations(stops) { perm ->
                val e = evaluate(start, end, perm, barriers, date, p)
                if (best == null || e.costMin < best!!.costMin) best = e
            }
            return best!!
        } else heldKarp(start, end, stops, barriers, p)
        return twoOpt(start, end, seed, barriers, date, p)
    }

    fun suggest(
        start: GeoPoint, end: GeoPoint, manualOrder: List<Stop>, barriers: List<Barrier>,
        date: LocalDate?, p: Params = Params(),
    ): Suggestion? {
        val manual = evaluate(start, end, manualOrder, barriers, date, p)
        val best = optimize(start, end, manualOrder, barriers, date, p)
        val gain = manual.costMin - best.costMin
        return if (gain >= p.minGainMin && gain >= manual.costMin * p.minGainShare) Suggestion(manual, best) else null
    }

    private fun heldKarp(start: GeoPoint, end: GeoPoint, stops: List<Stop>, barriers: List<Barrier>, p: Params): List<Stop> {
        val n = stops.size
        fun w(a: GeoPoint, b: GeoPoint) =
            Geo.distanceKm(a, b) * RouteTiming.DETOUR / p.speedKmh * 60 +
                barriers.sumOf { Geo.crossings(a, b, it.line) } * p.barrierPenaltyMin
        val full = (1 shl n) - 1
        val dp = Array(1 shl n) { DoubleArray(n) { Double.MAX_VALUE } }
        val parent = Array(1 shl n) { IntArray(n) { -1 } }
        for (i in 0 until n) dp[1 shl i][i] = w(start, stops[i].coords)
        for (mask in 1..full) for (last in 0 until n) {
            val cur = dp[mask][last]
            if (cur == Double.MAX_VALUE || mask and (1 shl last) == 0) continue
            for (next in 0 until n) {
                if (mask and (1 shl next) != 0) continue
                val nm = mask or (1 shl next)
                val c = cur + w(stops[last].coords, stops[next].coords)
                if (c < dp[nm][next]) { dp[nm][next] = c; parent[nm][next] = last }
            }
        }
        var last = (0 until n).minBy { dp[full][it] + w(stops[it].coords, end) }
        var mask = full
        val order = ArrayDeque<Stop>()
        while (last >= 0) {
            order.addFirst(stops[last])
            val prev = parent[mask][last]
            mask = mask xor (1 shl last)
            last = prev
        }
        return order.toList()
    }

    private fun twoOpt(
        start: GeoPoint, end: GeoPoint, seed: List<Stop>, barriers: List<Barrier>, date: LocalDate?, p: Params,
    ): Evaluation {
        var best = evaluate(start, end, seed, barriers, date, p)
        var improved = true
        while (improved) {
            improved = false
            for (i in 0 until best.order.size - 1) for (j in i + 1 until best.order.size) {
                val cand = best.order.toMutableList().also { it.subList(i, j + 1).reverse() }
                val e = evaluate(start, end, cand, barriers, date, p)
                if (e.costMin + 1e-6 < best.costMin) { best = e; improved = true }
            }
        }
        return best
    }

    private fun <T> permutations(items: List<T>, visit: (List<T>) -> Unit) {
        val a = items.toMutableList()
        fun rec(k: Int) {
            if (k == a.size) { visit(a.toList()); return }
            for (i in k until a.size) {
                a[k] = a[i].also { a[i] = a[k] }
                rec(k + 1)
                a[k] = a[i].also { a[i] = a[k] }
            }
        }
        rec(0)
    }
}
