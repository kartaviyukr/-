package ru.velopodmoskovye.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.velopodmoskovye.data.content.ContentParser
import ru.velopodmoskovye.domain.progress.ProgressCalculator
import ru.velopodmoskovye.domain.route.GoogleMapsLinks
import ru.velopodmoskovye.domain.route.RouteOptimizer
import ru.velopodmoskovye.domain.route.RouteTiming
import ru.velopodmoskovye.domain.spiral.SpiralBuilder
import ru.velopodmoskovye.ui.map.MapData
import ru.velopodmoskovye.ui.spiral.SpiralSort
import ru.velopodmoskovye.ui.spiral.SpiralViewModel
import java.io.File
import java.time.LocalDate

/** Контент-пак, который едет в APK, читается приложением и согласован со спиралью. */
class ContentPackTest {
    private val dir = File(System.getProperty("contentDir") ?: "../content/pack")
    private val pack = ContentParser.parse { File(dir, it).readText(Charsets.UTF_8) }

    @Test
    fun spiralStartsAtTsaoWithOkrugsInsideMkad() {
        assertEquals(200, pack.spiral.places.size)
        assertTrue(pack.spiral.places.first().start)
        assertEquals("ЦАО", pack.spiral.places.first().name)
        assertEquals(9, pack.spiral.places.count { it.insideMkad })
        assertTrue(pack.spiral.places.take(9).all { it.insideMkad })
    }

    @Test
    fun spiralOrderMatchesAlgorithm() {
        val nodes = SpiralBuilder.build(pack.spiral.places, coords = { it.coords }, isStart = { it.start }, insideMkad = { it.insideMkad })
        assertEquals(pack.spiral.places.map { it.id }, nodes.map { it.item.id })
        nodes.forEach { assertEquals(it.item.name, it.item.ring, it.ring) }
    }

    @Test
    fun everyLinkedDestinationIsLoaded() {
        pack.spiral.places.mapNotNull { it.destinationId }.forEach { id ->
            assertTrue("нет маршрута $id", id in pack.destinations)
        }
    }

    @Test
    fun destinationsAreUsable() {
        for (d in pack.destinations.values) {
            // Версия 3 — только свои точки места: 3–12 точек (цель 6–8), без заездов к соседям.
            assertEquals(d.id, 3, d.routeVersion)
            assertTrue("${d.id}: ${d.stops.size} точек", d.stops.size in 3..12)
            assertTrue(d.id, d.stops.zipWithNext().all { (a, b) -> a.order < b.order })
            d.stops.forEach { s ->
                assertTrue(s.id, s.facts.size in 2..12)
                assertTrue(s.id, s.facts.all { it.sourceUrl.startsWith("http") })
                assertTrue(s.id, s.guideStory.size in 2..6)
            }
            assertTrue(d.id, d.intro.size in 3..6)
            val blocks = GoogleMapsLinks.blocks(MapData.routePoints(d))
            assertTrue(blocks.isNotEmpty())
            val timing = RouteTiming.check(d, date = LocalDate.of(2026, 6, 13))
            assertTrue("${d.id}: летом маршрут должен укладываться в световой день", timing.beforeSunset)
            // Оптимизатор отрабатывает на реальных данных и не падает.
            RouteOptimizer.optimize(d.startPoint.coords, d.endPoint.coords, d.stops, d.route.barriers, LocalDate.of(2026, 6, 13))
            val titles = ProgressCalculator.earnedTitles(pack.spiral, pack.destinations, d.stops.filter { !it.optional }.map { it.id }.toSet())
            assertTrue(titles.any { it.id == "place:${d.id}" })
        }
    }

    @Test
    fun sortingByRouteLengthAndDistance() {
        val rows = SpiralViewModel.buildRows(pack, emptySet(), me = pack.spiral.places.last().coords)
        val long = SpiralViewModel.sorted(rows, SpiralSort.ROUTE_LONG).mapNotNull { it.routeKm }
        assertEquals(long.sortedDescending(), long)
        val short = SpiralViewModel.sorted(rows, SpiralSort.ROUTE_SHORT).mapNotNull { it.routeKm }
        assertEquals(short.sorted(), short)
        val near = SpiralViewModel.sorted(rows, SpiralSort.NEAR_ME)
        assertEquals(pack.spiral.places.last().id, near.first().place.id)
        val far = SpiralViewModel.sorted(rows, SpiralSort.KREMLIN_FAR).map { it.place.distanceFromKremlinKm }
        assertEquals(far.sortedDescending(), far)
        assertEquals(pack.spiral.places.map { it.id }, SpiralViewModel.sorted(rows, SpiralSort.SPIRAL).map { it.place.id })
    }

    @Test
    fun searchFindsPlacesAndStops() {
        val rows = SpiralViewModel.buildRows(pack, emptySet())
        assertEquals(rows.size, SpiralViewModel.search(pack, rows, "  ").rows.size)
        val byPlace = SpiralViewModel.search(pack, rows, "коломна")
        assertTrue(byPlace.rows.any { it.place.name == "Коломна" })
        // Поиск по точке: «ё» и «е» не различаются, регистр не важен.
        val byStop = SpiralViewModel.search(pack, rows, "малевич")
        assertTrue(byStop.stops.isNotEmpty())
        byStop.stops.forEach { hit ->
            val dest = pack.destinations.getValue(hit.destinationId)
            assertTrue(dest.stops.any { it.id == hit.stopId })
        }
        assertTrue(byStop.rows.all { r -> byStop.stops.any { it.placeName == r.place.name } || r.place.name.lowercase().contains("малевич") })
        assertTrue(SpiralViewModel.search(pack, rows, "зъщщхыжь").rows.isEmpty())
    }
}
