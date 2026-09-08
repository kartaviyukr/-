package com.fantasymap.creator.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeometryTest {

    private val square = listOf(
        Vec(0f, 0f), Vec(100f, 0f), Vec(100f, 100f), Vec(0f, 100f)
    )

    @Test
    fun `точка внутри многоугольника определяется верно`() {
        assertTrue(Geometry.pointInPolygon(Vec(50f, 50f), square))
        assertFalse(Geometry.pointInPolygon(Vec(150f, 50f), square))
        assertFalse(Geometry.pointInPolygon(Vec(-1f, -1f), square))
    }

    @Test
    fun `площадь квадрата считается верно`() {
        assertEquals(10000f, Geometry.area(square), 0.5f)
    }

    @Test
    fun `центроид квадрата в его середине`() {
        val centroid = Geometry.centroid(square)
        assertEquals(50f, centroid.x, 0.5f)
        assertEquals(50f, centroid.y, 0.5f)
    }

    @Test
    fun `упрощение убирает промежуточные точки прямой`() {
        val line = listOf(Vec(0f, 0f), Vec(10f, 0.1f), Vec(20f, 0f), Vec(30f, 0.05f), Vec(40f, 0f))
        val simplified = Geometry.simplify(line, 1f)
        assertEquals(2, simplified.size)
    }

    @Test
    fun `упрощение сохраняет излом`() {
        val line = listOf(Vec(0f, 0f), Vec(10f, 0f), Vec(20f, 40f), Vec(30f, 0f), Vec(40f, 0f))
        val simplified = Geometry.simplify(line, 1f)
        assertTrue(simplified.size >= 3)
        assertTrue(simplified.any { it.y > 30f })
    }

    @Test
    fun `расстояние до отрезка не отрицательно и корректно`() {
        val distance = Geometry.distanceToSegment(Vec(50f, 30f), Vec(0f, 0f), Vec(100f, 0f))
        assertEquals(30f, distance, 0.01f)
    }

    @Test
    fun `расстояние до ломаной берёт ближайшее звено`() {
        val polyline = listOf(Vec(0f, 0f), Vec(100f, 0f), Vec(100f, 100f))
        assertEquals(5f, Geometry.distanceToPolyline(Vec(105f, 50f), polyline), 0.01f)
    }

    @Test
    fun `сглаживание замкнутого контура не теряет точки`() {
        val smooth = Geometry.smoothClosed(square, 2)
        assertTrue(smooth.size > square.size)
    }

    @Test
    fun `передискретизация даёт равномерный шаг`() {
        val resampled = Geometry.resample(listOf(Vec(0f, 0f), Vec(100f, 0f)), 25f)
        assertTrue(resampled.size >= 4)
    }

    @Test
    fun `граница страны может лежать в море и не зависит от суши`() {
        // Территория страны — самостоятельный многоугольник: он может накрывать воду.
        val seaArea = listOf(Vec(200f, 200f), Vec(400f, 200f), Vec(400f, 400f), Vec(200f, 400f))
        val country = Country(name = "Морская держава", areas = listOf(seaArea))
        assertTrue(Geometry.pointInPolygon(Vec(300f, 300f), country.areas.first()))
        assertEquals(1, country.areas.size)
    }

    @Test
    fun `контур внутри контура даёт дыру`() {
        val outer = listOf(Vec(0f, 0f), Vec(100f, 0f), Vec(100f, 100f), Vec(0f, 100f))
        val hole = listOf(Vec(40f, 40f), Vec(60f, 40f), Vec(60f, 60f), Vec(40f, 60f))
        val contours = listOf(outer, hole)
        assertTrue(Geometry.pointInContours(Vec(10f, 10f), contours))
        assertFalse(Geometry.pointInContours(Vec(50f, 50f), contours))
        assertFalse(Geometry.pointInContours(Vec(150f, 50f), contours))
    }

    @Test
    fun `разрезанная зона из двух кусков остаётся цельной областью`() {
        val left = listOf(Vec(0f, 0f), Vec(40f, 0f), Vec(40f, 100f), Vec(0f, 100f))
        val right = listOf(Vec(60f, 0f), Vec(100f, 0f), Vec(100f, 100f), Vec(60f, 100f))
        val region = BiomeRegion(points = left, extraContours = listOf(right))
        assertEquals(2, region.contours().size)
        assertTrue(Geometry.pointInContours(Vec(20f, 50f), region.contours()))
        assertTrue(Geometry.pointInContours(Vec(80f, 50f), region.contours()))
        assertFalse(Geometry.pointInContours(Vec(50f, 50f), region.contours()))
    }

    @Test
    fun `зона без дополнительных контуров ведёт себя как обычный многоугольник`() {
        val region = BiomeRegion(points = square)
        assertEquals(1, region.contours().size)
        assertTrue(Geometry.pointInContours(Vec(50f, 50f), region.contours()))
    }

    @Test
    fun `анкета страны считает заполненные поля`() {
        val info = CountryInfo(capital = "Аргос", ruler = "Король Эйн", religion = "Culture of Dawn")
        assertEquals(3, info.filledCount())
        assertEquals(14, CountryInfo.FIELD_COUNT)
    }
}
