package com.fantasymap.creator.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Проверки справочника ландшафтов и объектов: списки не должны разъезжаться. */
class CatalogTest {

    @Test
    fun `в каждой группе объектов есть хотя бы один вид`() {
        for (group in MarkerGroup.entries) {
            assertTrue("Пустая группа: ${group.title}", MarkerType.byGroup(group).isNotEmpty())
        }
    }

    @Test
    fun `в каждой группе ландшафтов есть хотя бы одна зона`() {
        for (group in BiomeGroup.entries) {
            assertTrue("Пустая группа: ${group.title}", BiomeType.byGroup(group).isNotEmpty())
        }
    }

    @Test
    fun `названия объектов не повторяются`() {
        val titles = MarkerType.entries.map { it.title }
        assertEquals(titles.size, titles.toSet().size)
    }

    @Test
    fun `названия ландшафтов не повторяются`() {
        val titles = BiomeType.entries.map { it.title }
        assertEquals(titles.size, titles.toSet().size)
    }

    @Test
    fun `каждый вид объекта разложен по группам без потерь`() {
        val fromGroups = MarkerGroup.entries.flatMap { MarkerType.byGroup(it) }
        assertEquals(MarkerType.entries.size, fromGroups.size)
    }

    @Test
    fun `каждая зона разложена по группам без потерь`() {
        val fromGroups = BiomeGroup.entries.flatMap { BiomeType.byGroup(it) }
        assertEquals(BiomeType.entries.count { !it.legacy }, fromGroups.size)
    }

    @Test
    fun `справочник заметно больше сотни объектов и полусотни зон`() {
        assertTrue(MarkerType.entries.size >= 150)
        assertTrue(BiomeType.entries.size >= 80)
    }

    @Test
    fun `прямоугольник фрагмента отбирает то, что внутри`() {
        val rect = BBox(100f, 100f, 500f, 400f)
        assertTrue(rect.contains(Vec(300f, 200f)))
        assertFalse(rect.contains(Vec(50f, 200f)))
        assertFalse(rect.contains(Vec(300f, 900f)))
        assertEquals(400f, rect.width, 0.01f)
        assertEquals(300f, rect.height, 0.01f)
    }

    @Test
    fun `прямоугольник фрагмента пересекается с областью карты`() {
        val rect = BBox(100f, 100f, 500f, 400f)
        assertTrue(rect.intersects(BBox(400f, 300f, 900f, 900f)))
        assertFalse(rect.intersects(BBox(600f, 600f, 900f, 900f)))
    }
}
