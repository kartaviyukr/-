package ru.velopodmoskovye.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.velopodmoskovye.domain.geo.Geo
import ru.velopodmoskovye.domain.spiral.DayPlanner
import ru.velopodmoskovye.domain.spiral.Leg
import ru.velopodmoskovye.domain.spiral.SpiralBuilder
import ru.velopodmoskovye.domain.spiral.Transfers

class SpiralBuilderTest {
    private val rows = TestData.appendixA()

    @Test
    fun reproducesAppendixAOrderFromCoordinates() {
        assertEquals(183, rows.size)
        // Перемешиваем вход, чтобы порядок определялся только алгоритмом.
        val shuffled = rows.shuffled(java.util.Random(42))
        val nodes = SpiralBuilder.build(shuffled, coords = { it.point }, isFinish = { it.num == 183 })
        assertEquals((1..183).toList(), nodes.map { it.item.num })
    }

    @Test
    fun coordinatesMatchStatedDistanceAndAzimuth() {
        for (r in rows.dropLast(1)) {
            val d = Geo.distanceKm(Geo.KREMLIN, r.point)
            val az = Geo.bearingDeg(Geo.KREMLIN, r.point)
            assertEquals(r.name, r.distKm, d, 1.0)
            val dAz = SpiralBuilder.clockwiseFrom(r.az, az).let { minOf(it, 360 - it) }
            assertTrue("${r.name}: азимут $az вместо ${r.az}", dAz < 2.0)
        }
    }

    @Test
    fun ringsAreMonotonicFromOuterToInner() {
        val nodes = SpiralBuilder.build(rows, coords = { it.point }, isFinish = { it.num == 183 })
        nodes.zipWithNext().forEach { (a, b) -> assertTrue(a.ring <= b.ring) }
        assertEquals(0, nodes.first().ring)
        assertEquals("Серебряные Пруды", nodes.first().item.name)
        assertEquals("Кремль", nodes.last().item.name)
    }

    @Test
    fun ringBoundsLowerInclusive() {
        assertEquals(0, SpiralBuilder.ringOf(120.0))
        assertEquals(1, SpiralBuilder.ringOf(119.9))
        assertEquals(5, SpiralBuilder.ringOf(20.0))
        assertEquals(6, SpiralBuilder.ringOf(19.99))
        assertEquals(6, SpiralBuilder.ringOf(0.0))
    }

    @Test
    fun withinRingAzimuthGoesClockwiseWithAtMostOneWrap() {
        val nodes = SpiralBuilder.build(rows, coords = { it.point }, isFinish = { it.num == 183 })
        for ((_, ring) in nodes.dropLast(1).groupBy { it.ring }) {
            val wraps = ring.zipWithNext().count { (a, b) -> b.azimuthDeg < a.azimuthDeg }
            assertTrue("кольцо ${ring.first().ring}: $wraps переходов через север", wraps <= 1)
        }
    }

    @Test
    fun azimuthBlocksGlueNeighbours() {
        val nodes = SpiralBuilder.build(rows, coords = { it.point }, isFinish = { it.num == 183 })
        val blocks = SpiralBuilder.azimuthBlocks(nodes)
        val byName = nodes.map { it.item.name }.zip(blocks).toMap()
        // Рошаль и Святое озеро — одинаковый азимут 93°.
        assertEquals(byName.getValue("Рошаль"), byName.getValue("Святое озеро (Мещера)"))
        // Коломна и Бобренев монастырь — оба 135°.
        assertEquals(byName.getValue("Коломна"), byName.getValue("Бобренев монастырь"))
        assertTrue(byName.getValue("Серебряные Пруды") != byName.getValue("Уваровка"))
    }

    @Test
    fun transferSuggestsTrainWhenLong() {
        val a = rows.first { it.name == "Серебряные Пруды" }.point
        val b = rows.first { it.name == "Уваровка" }.point
        val t = Transfers.between("a", a, "b", b)
        assertTrue(t.rideKm > t.straightKm)
        assertTrue(t.suggestTrain)
        val near = Transfers.between("a", a, "a2", Geo.destination(a, 90.0, 5_000.0))
        assertTrue(!near.suggestTrain)
    }

    @Test
    fun dayPlannerRespectsTarget() {
        val legs = (1..10).map { Leg("p$it", transferKm = 20.0, placeKm = 15.0, block = it) }
        val days = DayPlanner.plan(legs, targetKmPerDay = 80.0)
        assertEquals(legs.map { it.placeId }, days.flatMap { it.placeIds })
        days.forEach { assertTrue("${it.km}", it.km <= 80.0 * 1.15) }
        assertEquals(5, days.size)
        assertEquals("p2", days.first().overnightAt)
    }

    @Test
    fun dayPlannerKeepsAzimuthBlockTogether() {
        val legs = listOf(
            Leg("a", 30.0, 15.0, 0),
            Leg("b1", 10.0, 10.0, 1), Leg("b2", 5.0, 10.0, 1), Leg("b3", 5.0, 10.0, 1),
        )
        val days = DayPlanner.plan(legs, targetKmPerDay = 60.0)
        assertEquals(listOf(listOf("a"), listOf("b1", "b2", "b3")), days.map { it.placeIds })
    }
}
