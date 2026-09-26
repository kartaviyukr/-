package com.fantasymap.creator.model

import com.fantasymap.creator.geom.WallJoiner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Шаблоны зданий и соединение стен. */
class BattleTemplatesTest {

    @Test
    fun `каждый шаблон — пол, стены и обстановка в своих границах`() {
        for (template in RoomTemplate.entries) {
            val plan = BattleTemplates.plan(template)
            assertTrue("$template: нет пола", plan.floors.isNotEmpty())
            assertTrue("$template: нет стен", plan.walls.isNotEmpty())
            assertTrue("$template: нет обстановки", plan.props.size >= 4)
            val all = plan.floors.flatMap { it.second } + plan.walls.flatMap { it.second } + plan.props.map { it.second }
            for (p in all) {
                assertTrue("$template: $p за границей", p.x >= -0.01f && p.y >= -0.01f &&
                    p.x <= plan.cols + 0.01f && p.y <= plan.rows + 0.01f)
            }
        }
    }

    @Test
    fun `проём разрезает стену, без проёмов стена замкнута`() {
        val square = listOf(Vec(0f, 0f), Vec(4f, 0f), Vec(4f, 4f), Vec(0f, 4f))
        val whole = BattleTemplates.cut(square, emptyList())
        assertEquals(1, whole.size)
        assertEquals(whole[0].first(), whole[0].last())
        val cut = BattleTemplates.cut(square, listOf(BattleTemplates.gap(2f, 4f)))
        assertEquals(1, cut.size)
        val piece = cut[0]
        assertEquals(1.5f, piece.first().x, 0.01f)
        assertEquals(2.5f, piece.last().x, 0.01f)
        assertEquals(6, piece.size)
    }

    @Test
    fun `шаблон растягивается по рамке и ложится на клетки`() {
        val placed = BattleTemplates.place(RoomTemplate.HOUSE, Vec(100f, 100f), 50f, 800f, 700f)
        val xs = placed.lines.flatMap { it.points }.map { it.x }
        assertEquals(100f, xs.minOrNull()!!, 0.1f)
        assertEquals(900f, xs.maxOrNull()!!, 0.1f)
    }

    @Test
    fun `концы стен рядом сливаются, почти замкнутая стена замыкается`() {
        val a = LineFeature(type = LineFeatureType.DUNGEON_WALL, points = listOf(Vec(0f, 0f), Vec(100f, 0f)))
        val b = LineFeature(type = LineFeatureType.DUNGEON_WALL, points = listOf(Vec(104f, 2f), Vec(100f, 100f), Vec(3f, 98f), Vec(2f, 4f)))
        val result = WallJoiner.join(listOf(a, b), 10f)
        assertEquals(1, result.lines.size)
        val pts = result.lines[0].points
        assertEquals(pts.first(), pts.last())
    }

    @Test
    fun `конец у середины другой стены упирается в неё`() {
        val a = LineFeature(type = LineFeatureType.DUNGEON_WALL, points = listOf(Vec(0f, 0f), Vec(100f, 0f)))
        val b = LineFeature(type = LineFeatureType.TIMBER_WALL, points = listOf(Vec(50f, 80f), Vec(50f, 6f)))
        val result = WallJoiner.join(listOf(a, b), 10f)
        assertEquals(0f, result.lines[1].points.last().y, 0.01f)
    }

    @Test
    fun `дверной проём не заращивается`() {
        val a = LineFeature(type = LineFeatureType.DUNGEON_WALL, points = listOf(Vec(0f, 0f), Vec(100f, 0f)))
        val b = LineFeature(type = LineFeatureType.DUNGEON_WALL, points = listOf(Vec(106f, 0f), Vec(200f, 0f)))
        val open = WallJoiner.join(listOf(a, b), 10f, listOf(Vec(103f, 0f)))
        assertEquals(0, open.joins)
        assertEquals(1, WallJoiner.join(listOf(a, b), 10f).lines.size)
    }

    @Test
    fun `шаблонов не меньше тридцати`() {
        assertTrue(RoomTemplate.entries.size >= 30)
    }

    @Test
    fun `реки не трогаются`() {
        val a = LineFeature(type = LineFeatureType.RIVER, points = listOf(Vec(0f, 0f), Vec(100f, 0f)))
        val b = LineFeature(type = LineFeatureType.RIVER, points = listOf(Vec(102f, 0f), Vec(200f, 0f)))
        assertEquals(0, WallJoiner.join(listOf(a, b), 10f).joins)
    }
}
