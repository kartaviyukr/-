package com.fantasymap.creator.model

import com.fantasymap.creator.geom.Visibility
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Правила D&D и зрение героев в режиме игры. */
class PlayModeTest {

    @Test
    fun `модификаторы характеристик как в книге`() {
        assertEquals(-1, Dnd.modifier(8))
        assertEquals(-1, Dnd.modifier(9))
        assertEquals(0, Dnd.modifier(10))
        assertEquals(2, Dnd.modifier(14))
        assertEquals(5, Dnd.modifier(20))
    }

    @Test
    fun `кубики разбираются по-русски и по-английски`() {
        assertEquals(Dnd.DiceSpec(2, 6, 3), Dnd.parse("2к6+3"))
        assertEquals(Dnd.DiceSpec(1, 8, -1), Dnd.parse("1d8-1"))
        assertEquals(Dnd.DiceSpec(1, 20, 0), Dnd.parse("к20"))
        assertEquals(Dnd.DiceSpec(0, 1, 4), Dnd.parse("4"))
        assertNull(Dnd.parse("меч"))
    }

    @Test
    fun `натуральная 20 — крит, натуральная 1 — промах`() {
        var sawCrit = false
        var sawMiss = false
        for (seed in 0 until 400) {
            val r = Dnd.attack("A", Attack("Меч", 5, "1к8+3"), "B", 30, Random(seed), 0)
            if (r.d20.kept == 20) {
                sawCrit = true
                assertTrue(r.hit && r.crit && r.damage >= 5)
            }
            if (r.d20.kept == 1) {
                sawMiss = true
                assertFalse(r.hit)
            }
            if (r.d20.kept in 2..19) assertFalse("КД 30 не пробить без крита", r.hit)
        }
        assertTrue(sawCrit && sawMiss)
    }

    @Test
    fun `бонус навыка — модификатор плюс мастерство`() {
        val sheet = CharacterSheet(wis = 14, proficiency = 3, skills = listOf(CheckSkill.PERCEPTION))
        assertEquals(5, sheet.skillBonus(CheckSkill.PERCEPTION))
        assertEquals(2, sheet.skillBonus(CheckSkill.INSIGHT))
    }

    @Test
    fun `стена закрывает обзор, открытая дверь нет`() {
        val wall = listOf(Visibility.Segment(Vec(100f, 0f), Vec(100f, 300f)))
        val poly = Visibility.polygon(Vec(50f, 150f), 200f, wall)
        assertTrue(Visibility.sees(poly, Vec(80f, 150f)))
        assertFalse(Visibility.sees(poly, Vec(150f, 150f)))
        val open = Visibility.polygon(Vec(50f, 150f), 200f, emptyList())
        assertTrue(Visibility.sees(open, Vec(150f, 150f)))
    }

    @Test
    fun `закрытая дверь в проёме загораживает обзор`() {
        val project = MapProject(
            kind = MapKind.BATTLE,
            gridCell = 50f,
            lines = listOf(
                LineFeature(type = LineFeatureType.DUNGEON_WALL, points = listOf(Vec(200f, 0f), Vec(200f, 175f))),
                LineFeature(type = LineFeatureType.DUNGEON_WALL, points = listOf(Vec(200f, 225f), Vec(200f, 400f)))
            ),
            markers = listOf(Marker(type = MarkerType.B_DOOR, pos = Vec(200f, 200f))),
            tokens = listOf(Token(faction = TokenFaction.HERO, pos = Vec(100f, 200f)))
        )
        val closed = Visibility.heroViews(project).first()
        assertFalse(Visibility.sees(closed, Vec(260f, 200f)))
        val opened = project.copy(markers = project.markers.map { it.copy(open = true) })
        assertTrue(Visibility.sees(Visibility.heroViews(opened).first(), Vec(260f, 200f)))
    }

    @Test
    fun `тайна по умолчанию не спрятана`() {
        assertFalse(Marker().secret.hidden)
        assertNotNull(Dnd.sheetOf(Token()).attacks.firstOrNull())
    }
}
