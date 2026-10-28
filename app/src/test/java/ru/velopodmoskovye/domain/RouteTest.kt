package ru.velopodmoskovye.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.velopodmoskovye.domain.model.Barrier
import ru.velopodmoskovye.domain.model.GeoPoint
import ru.velopodmoskovye.domain.model.Schedule
import ru.velopodmoskovye.domain.route.GoogleMapsLinks
import ru.velopodmoskovye.domain.route.YandexMapsLinks
import ru.velopodmoskovye.domain.route.RouteOptimizer
import ru.velopodmoskovye.domain.route.RouteTiming
import ru.velopodmoskovye.domain.route.Sun
import java.time.LocalDate
import java.time.LocalTime

class RouteTest {
    @Test
    fun googleBlocksHaveAtMostNineWaypoints() {
        val pts = (0 until 25).map { GeoPoint(55.7 + it * 0.001, 37.6) }
        val blocks = GoogleMapsLinks.blocks(pts)
        assertEquals(3, blocks.size)
        assertEquals(0, blocks[0].fromIndex)
        assertEquals(10, blocks[0].toIndex)
        assertEquals(10, blocks[1].fromIndex)
        assertEquals(24, blocks.last().toIndex)
        for (b in blocks) {
            val wp = Regex("waypoints=([^&]*)").find(b.url)?.groupValues?.get(1)
            val count = wp?.split("%7C")?.size ?: 0
            assertTrue(count <= 9)
            assertTrue(b.url.contains("travelmode=bicycling"))
            assertTrue(b.url.startsWith("https://www.google.com/maps/dir/?api=1&origin="))
        }
        assertTrue(GoogleMapsLinks.blocks(pts.take(2), GoogleMapsLinks.Mode.WALKING).single().url.endsWith("travelmode=walking"))
    }

    @Test
    fun yandexLinkHasAllPointsInOrder() {
        val pts = (0 until 14).map { GeoPoint(55.7 + it * 0.001, 37.6) }
        val url = YandexMapsLinks.url(pts)
        assertTrue(url.startsWith("https://yandex.ru/maps/?rtext=55.700000%2C37.600000~"))
        assertTrue(url.endsWith("&rtt=bc"))
        val rtext = Regex("rtext=([^&]*)").find(url)!!.groupValues[1].split("~")
        assertEquals(14, rtext.size)
        assertEquals("55.713000%2C37.600000", rtext.last())
        assertTrue(YandexMapsLinks.url(pts.take(2), GoogleMapsLinks.Mode.WALKING).endsWith("&rtt=pd"))
    }

    @Test
    fun sunsetInMoscowIsPlausible() {
        val june = Sun.times(LocalDate.of(2026, 6, 21), 55.75, 37.62)
        val dec = Sun.times(LocalDate.of(2026, 12, 21), 55.75, 37.62)
        // Справочно: 21 июня закат около 21:18, 21 декабря около 15:58.
        assertTrue(june.sunset!! in LocalTime.of(21, 5)..LocalTime.of(21, 30))
        assertTrue(dec.sunset!! in LocalTime.of(15, 45)..LocalTime.of(16, 10))
        assertTrue(june.sunrise!! in LocalTime.of(3, 35)..LocalTime.of(3, 55))
    }

    @Test
    fun timingDetectsClosedMuseumAndSunset() {
        val museum = TestData.stop(
            "museum", 55.76, 37.86, kind = "заходим внутрь", visit = 60,
            schedule = Schedule(mapOf("mon" to "10:00-18:00", "tue" to "10:00-18:00", "sat" to "10:00-18:00")),
        )
        val forest = TestData.stop("forest", 55.77, 37.87, daylight = true, visit = 30)
        val dest = TestData.destination("d", listOf(museum, forest))
        val sunday = LocalDate.of(2026, 10, 25)
        val r = RouteTiming.check(dest, date = sunday)
        assertTrue(RouteTiming.Issue.CLOSED_THAT_DAY in r.stops[0].issues)
        assertTrue(r.beforeSunset)
        val late = RouteTiming.check(dest, date = LocalDate.of(2026, 12, 21), start = LocalTime.of(15, 40))
        assertTrue(RouteTiming.Issue.AFTER_SUNSET in late.stops[1].issues)
        assertTrue(!late.beforeSunset)
        val early = RouteTiming.check(dest, date = LocalDate.of(2026, 10, 26), start = LocalTime.of(8, 0))
        assertTrue(RouteTiming.Issue.WAIT_FOR_OPENING in early.stops[0].issues)
        assertEquals(10 * 60 + 60, early.stops[0].leaveMin)
    }

    @Test
    fun optimizerFindsObviousOrderAndKeepsGoodManualOrder() {
        // Точки на прямой с запада на восток, старт на западе, финиш на востоке.
        val a = TestData.stop("a", 55.75, 37.60)
        val b = TestData.stop("b", 55.75, 37.62)
        val c = TestData.stop("c", 55.75, 37.64)
        val d = TestData.stop("d", 55.75, 37.66)
        val start = GeoPoint(55.75, 37.58)
        val end = GeoPoint(55.75, 37.68)
        val best = RouteOptimizer.optimize(start, end, listOf(c, a, d, b), emptyList(), null)
        assertEquals(listOf("a", "b", "c", "d"), best.order.map { it.id })
        assertNull(RouteOptimizer.suggest(start, end, listOf(a, b, c, d), emptyList(), null))
        assertNotNull(RouteOptimizer.suggest(start, end, listOf(d, a, c, b), emptyList(), null))
    }

    @Test
    fun optimizerPenalizesExtraBarrierCrossings() {
        // Ж/д идёт с севера на юг по долготе 37.63. Две точки западнее, две восточнее.
        val rail = Barrier("ж/д", "railway", listOf(GeoPoint(55.70, 37.63), GeoPoint(55.80, 37.63)))
        val w1 = TestData.stop("w1", 55.74, 37.61)
        val w2 = TestData.stop("w2", 55.76, 37.61)
        val e1 = TestData.stop("e1", 55.74, 37.65)
        val e2 = TestData.stop("e2", 55.76, 37.65)
        val start = GeoPoint(55.75, 37.60)
        val end = GeoPoint(55.75, 37.66)
        val best = RouteOptimizer.optimize(start, end, listOf(e1, w1, e2, w2), listOf(rail), null)
        assertEquals(1, best.barrierCrossings)
        assertEquals(setOf("w1", "w2"), best.order.take(2).map { it.id }.toSet())
    }

    @Test
    fun optimizerHandlesManyStops() {
        val stops = (0 until 11).map { TestData.stop("s$it", 55.75, 37.60 + it * 0.005) }.shuffled(java.util.Random(1))
        val best = RouteOptimizer.optimize(GeoPoint(55.75, 37.59), GeoPoint(55.75, 37.66), stops, emptyList(), null)
        assertEquals((0 until 11).map { "s$it" }, best.order.map { it.id })
    }
}
