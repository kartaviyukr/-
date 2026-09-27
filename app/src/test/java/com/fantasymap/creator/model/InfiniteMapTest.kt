package com.fantasymap.creator.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Бесконечная карта мира: печатается всё нарисованное, даже за начальной областью. */
class InfiniteMapTest {

    @Test
    fun `область печати захватывает сушу за краем`() {
        val project = MapProject(
            worldWidth = 1000f,
            worldHeight = 800f,
            infinite = true,
            landmasses = listOf(Landmass(points = listOf(Vec(-500f, 100f), Vec(-200f, 100f), Vec(-300f, 1400f))))
        )
        val box = project.contentBounds()
        assertTrue(box.minX < -500f)
        assertTrue(box.maxY > 1400f)
        assertTrue(box.maxX > 1000f)
    }

    @Test
    fun `без краёв бывает только карта мира`() {
        assertTrue(MapProject(kind = MapKind.WORLD, infinite = true).boundless)
        assertFalse(MapProject(kind = MapKind.CITY, infinite = true).boundless)
        assertFalse(MapProject(kind = MapKind.WORLD).boundless)
    }
}
