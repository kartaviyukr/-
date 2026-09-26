package com.fantasymap.creator.model

import com.fantasymap.creator.geom.StreetNetwork
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Улицы сводятся в одну сеть: концы дотягиваются, хвостики обрезаются, куски связываются. */
class StreetNetworkTest {

    private fun road(vararg xy: Float) =
        Road(type = RoadType.STREET, points = xy.toList().chunked(2).map { Vec(it[0], it[1]) })

    @Test
    fun `недотянутый конец упирается в соседнюю улицу`() {
        val main = road(0f, 0f, 100f, 0f)
        val side = road(50f, 40f, 50f, 6f)
        val result = StreetNetwork.weld(listOf(main, side), emptyList(), stub = 3f, snap = 10f, ray = 20f, link = 0f)
        val end = result[1].points.last()
        assertEquals(0f, Geometry.distanceToPolyline(end, main.points), 0.01f)
    }

    @Test
    fun `хвостик за перекрёстком обрезается`() {
        val main = road(0f, 0f, 100f, 0f)
        val side = road(50f, 40f, 50f, -2f)
        val result = StreetNetwork.weld(listOf(main, side), emptyList(), stub = 5f, snap = 10f, ray = 20f, link = 0f)
        assertEquals(0f, result[1].points.last().y, 0.01f)
    }

    @Test
    fun `оторванный кусок связывается с сетью`() {
        val main = road(0f, 0f, 100f, 0f)
        val far = road(0f, 30f, 100f, 30f)
        val result = StreetNetwork.weld(listOf(main, far), emptyList(), stub = 3f, snap = 5f, ray = 5f, link = 40f)
        assertEquals(3, result.size)
        val link = result[2].points
        assertTrue(Geometry.distanceToPolyline(link.first(), far.points) < 0.01f || Geometry.distanceToPolyline(link.first(), main.points) < 0.01f)
    }

    @Test
    fun `далёкие улицы не трогаются`() {
        val main = road(0f, 0f, 100f, 0f)
        val far = road(0f, 300f, 100f, 300f)
        val result = StreetNetwork.weld(listOf(main, far), emptyList(), stub = 3f, snap = 5f, ray = 5f, link = 40f)
        assertEquals(2, result.size)
        assertEquals(far.points, result[1].points)
    }
}
