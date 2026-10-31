package ru.velopodmoskovye.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.velopodmoskovye.domain.geo.Geo
import ru.velopodmoskovye.domain.spiral.SpiralBuilder

class SpiralBuilderTest {
    private val rows = TestData.places()
    private fun spiral(input: List<TestData.PlaceRow> = rows) =
        SpiralBuilder.build(input, coords = { it.point }, isStart = { it.isStart }, insideMkad = { it.insideMkad })

    @Test
    fun startsAtTsaoAndCoversAllPlaces() {
        assertEquals(365, rows.size)
        val nodes = spiral()
        assertEquals("ЦАО", nodes.first().item.name)
        assertEquals(rows.map { it.num }.toSet(), nodes.map { it.item.num }.toSet())
        assertEquals((1..rows.size).toList(), nodes.map { it.order })
    }

    @Test
    fun moscowOkrugsInsideMkadComeFirst() {
        val nodes = spiral()
        val okrugs = listOf("ЦАО", "САО", "СВАО", "ВАО", "ЮВАО", "ЮАО", "ЮЗАО", "ЗАО", "СЗАО")
        assertEquals(okrugs.toSet(), nodes.take(9).map { it.item.name }.toSet())
        nodes.take(9).forEach { assertEquals(SpiralBuilder.INSIDE_MKAD_RING, it.ring) }
        nodes.drop(9).forEach { assertTrue(it.item.name, it.ring > SpiralBuilder.INSIDE_MKAD_RING) }
        // Отдельных пунктов внутри МКАД (Кремль, Коломенское…) больше нет — они внутри маршрутов округов.
        assertTrue(rows.none { it.name in setOf("Кремль", "Коломенское", "Царицыно", "Останкино", "Перово") })
    }

    @Test
    fun orderDoesNotDependOnInputOrder() {
        val shuffled = rows.shuffled(java.util.Random(42))
        assertEquals(spiral().map { it.item.num }, spiral(shuffled).map { it.item.num })
    }

    @Test
    fun goesOutwardRingByRing() {
        val nodes = spiral().drop(1)
        nodes.zipWithNext().forEach { (a, b) -> assertTrue("${a.item.name} → ${b.item.name}", a.ring <= b.ring) }
        assertEquals(SpiralBuilder.INSIDE_MKAD_RING, nodes.first().ring)
        assertEquals(SpiralBuilder.RING_NAMES.lastIndex, nodes.last().ring)
        // Последним идёт место внешнего кольца, до которого дальше всех по часовой стрелке от входа в кольцо.
        val outer = nodes.filter { it.ring == nodes.last().ring }
        assertEquals(outer.last().item.name, nodes.last().item.name)
        // Первым после ЦАО — ближайший к Кремлю округ.
        assertEquals(rows.filter { it.insideMkad && !it.isStart }.minBy { Geo.distanceKm(Geo.KREMLIN, it.point) }.name, nodes.first().item.name)
    }

    @Test
    fun withinRingAzimuthGoesClockwiseWithAtMostOneWrap() {
        val nodes = spiral().drop(1)
        for ((_, ring) in nodes.groupBy { it.ring }) {
            val wraps = ring.zipWithNext().count { (a, b) -> b.azimuthDeg < a.azimuthDeg }
            assertTrue("кольцо ${ring.first().ring}: $wraps переходов через север", wraps <= 1)
        }
    }

    @Test
    fun nextRingContinuesFromLastAzimuth() {
        val nodes = spiral().drop(1)
        for ((a, b) in nodes.zipWithNext().filter { (a, b) -> a.ring != b.ring }) {
            val next = nodes.filter { it.ring == b.ring }
            val minTurn = next.minOf { SpiralBuilder.clockwiseFrom(a.azimuthDeg, it.azimuthDeg) }
            assertEquals(minTurn, SpiralBuilder.clockwiseFrom(a.azimuthDeg, b.azimuthDeg), 1e-9)
        }
    }

    @Test
    fun coordinatesMatchStatedDistanceAndAzimuth() {
        for (r in rows.filter { !it.isStart }) {
            val d = Geo.distanceKm(Geo.KREMLIN, r.point)
            val az = Geo.bearingDeg(Geo.KREMLIN, r.point)
            assertEquals(r.name, r.distKm, d, 1.0)
            val dAz = SpiralBuilder.clockwiseFrom(r.az, az).let { minOf(it, 360 - it) }
            assertTrue("${r.name}: азимут $az вместо ${r.az}", dAz < 2.0)
        }
    }

    @Test
    fun ringBoundsLowerInclusive() {
        assertEquals(1, SpiralBuilder.ringOf(0.0))
        assertEquals(1, SpiralBuilder.ringOf(19.99))
        assertEquals(2, SpiralBuilder.ringOf(20.0))
        assertEquals(6, SpiralBuilder.ringOf(119.9))
        assertEquals(7, SpiralBuilder.ringOf(120.0))
        assertEquals(7, SpiralBuilder.ringOf(149.9))
        assertEquals(8, SpiralBuilder.ringOf(160.0))
        assertEquals(9, SpiralBuilder.ringOf(200.0))
        assertEquals(9, SpiralBuilder.ringOf(266.0))
    }
}
