package ru.velopodmoskovye.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.velopodmoskovye.domain.fog.Fog
import ru.velopodmoskovye.domain.geo.Geo
import ru.velopodmoskovye.domain.model.GeoPoint
import ru.velopodmoskovye.domain.model.GeoPolygon
import ru.velopodmoskovye.domain.model.Region
import ru.velopodmoskovye.domain.model.RingDef
import ru.velopodmoskovye.domain.model.SpiralPack
import ru.velopodmoskovye.domain.model.SpiralPlace
import ru.velopodmoskovye.domain.progress.Fix
import ru.velopodmoskovye.domain.progress.Geofence
import ru.velopodmoskovye.domain.progress.ProgressCalculator
import ru.velopodmoskovye.domain.progress.TitleKind

class ProgressTest {
    private val center = GeoPoint(55.76, 37.86)
    private fun fixAt(bearing: Double, dist: Double, acc: Double = 10.0) =
        Fix(Geo.destination(center, bearing, dist), acc, 0)

    @Test
    fun geofenceDefaultRadius() {
        val s = TestData.stop("s", center.lat, center.lon)
        assertTrue(Geofence.isInside(s, fixAt(0.0, 140.0)))
        assertTrue(Geofence.isInside(s, fixAt(90.0, 155.0)))
        assertFalse(Geofence.isInside(s, fixAt(180.0, 200.0)))
        // Плохая точность — автоматическую отметку не ставим.
        assertFalse(Geofence.isInside(s, fixAt(0.0, 10.0, acc = 300.0)))
        // Радиус из настроек.
        assertTrue(Geofence.isInside(s, fixAt(0.0, 280.0), defaultRadiusM = 300))
    }

    @Test
    fun geofenceCustomRadiusAndPolygon() {
        val field = TestData.stop("borodino", center.lat, center.lon, radius = 2_000)
        assertTrue(Geofence.isInside(field, fixAt(45.0, 1_900.0)))
        val poly = GeoPolygon.of(Geo.circle(center, 600.0, 8))
        val s = TestData.stop("p", center.lat, center.lon).copy(zonePolygon = poly)
        assertTrue(Geofence.isInside(s, fixAt(10.0, 500.0)))
    }

    @Test
    fun manualCheckBlocksFarAway() {
        val s = TestData.stop("s", center.lat, center.lon)
        assertTrue(Geofence.manualCheck(s, fixAt(0.0, 400.0)) is Geofence.ManualResult.Allowed)
        assertTrue(Geofence.manualCheck(s, fixAt(0.0, 5_000.0)) is Geofence.ManualResult.TooFar)
        assertEquals(Geofence.ManualResult.NoFix, Geofence.manualCheck(s, null))
    }

    private fun pack(): Pair<SpiralPack, Map<String, ru.velopodmoskovye.domain.model.Destination>> {
        val d1 = TestData.destination("d1", listOf(
            TestData.stop("d1a", 55.76, 37.86), TestData.stop("d1b", 55.77, 37.86),
            TestData.stop("d1c", 55.78, 37.86, optional = true),
        ))
        val d2 = TestData.destination("d2", listOf(TestData.stop("d2a", 55.65, 37.86)))
        val places = listOf(
            SpiralPlace("p1", 1, "П1", "г.", "r1", 3, d1.coords, 15.0, 86.0, 6, "d1"),
            SpiralPlace("p2", 2, "П2", "г.", "r2", 1, d2.coords, 19.0, 126.0, 6, "d2"),
            SpiralPlace("kremlin", 3, "Кремль", "финиш", "msk", 3, Geo.KREMLIN, 0.0, 0.0, 6, null, finish = true),
        )
        val sp = SpiralPack(1, Geo.KREMLIN, listOf(RingDef(6, "ближе 20 км", 0.0, 20.0)),
            listOf(Region("r1", "Реутов", "МО_городской_округ"), Region("r2", "Котельники", "МО_городской_округ")),
            places)
        return sp to mapOf("d1" to d1, "d2" to d2)
    }

    @Test
    fun placeTitleNeedsAllRequiredStops() {
        val (sp, dests) = pack()
        val d1 = dests.getValue("d1")
        assertFalse(ProgressCalculator.destination(d1, setOf("d1a")).complete)
        val p = ProgressCalculator.destination(d1, setOf("d1a", "d1b"))
        assertTrue(p.complete)
        assertFalse(p.bonus)
        assertEquals(2f / 3f, p.fraction, 1e-4f)
        val titles = ProgressCalculator.earnedTitles(sp, dests, setOf("d1a", "d1b"))
        assertTrue(titles.any { it.kind == TitleKind.PLACE && it.id == "place:d1" })
        assertTrue(titles.any { it.kind == TitleKind.REGION && it.id == "region:r1" })
        assertTrue(titles.any { it.kind == TitleKind.STARS })
        assertFalse(titles.any { it.kind == TitleKind.RING })
    }

    @Test
    fun bonusRingAndSpiralTitles() {
        val (sp, dests) = pack()
        val all = setOf("d1a", "d1b", "d1c", "d2a")
        val titles = ProgressCalculator.earnedTitles(sp, dests, all)
        assertTrue(titles.any { it.kind == TitleKind.PLACE_BONUS })
        assertTrue(titles.any { it.kind == TitleKind.RING })
        assertFalse(titles.any { it.kind == TitleKind.SPIRAL })
        val withKremlin = ProgressCalculator.earnedTitles(sp, dests, all + ProgressCalculator.KREMLIN_STOP_ID)
        assertTrue(withKremlin.any { it.kind == TitleKind.SPIRAL })
    }

    @Test
    fun fogMergesOverlappingHoles() {
        val a = Geo.circle(center, 150.0)
        val b = Geo.circle(Geo.destination(center, 90.0, 200.0), 150.0)
        val far = Geo.circle(Geo.destination(center, 0.0, 5_000.0), 150.0)
        val merged = Fog.mergeOverlapping(listOf(a, b, far))
        assertEquals(2, merged.size)
        val json = Fog.fogGeoJson(listOf(a, b, far))
        assertTrue(json.startsWith("{\"type\":\"Feature\""))
        // Внешнее кольцо + 2 дыры.
        assertEquals(2, Regex(Regex.escape("]],[[")).findAll(json).count())
    }

    @Test
    fun areaOfCircleIsClose() {
        val area = Fog.areaKm2(Geo.circle(center, 1_000.0, 64))
        assertEquals(Math.PI, area, 0.05)
    }
}
