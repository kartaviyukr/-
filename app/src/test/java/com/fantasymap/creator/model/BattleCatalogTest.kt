package com.fantasymap.creator.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Справочник боевой локации: существа, объекты и зоны на месте. */
class BattleCatalogTest {

    @Test
    fun `в каждой группе существ есть хотя бы одно`() {
        for (group in TokenGroup.entries) {
            assertTrue("Пустая группа: ${group.title}", TokenType.byGroup(group).isNotEmpty())
        }
    }

    @Test
    fun `названия существ не повторяются`() {
        val titles = TokenType.entries.map { it.title }
        assertEquals(titles.size, titles.toSet().size)
    }

    @Test
    fun `существ больше сотни`() {
        assertTrue(TokenType.entries.size >= 100)
    }

    @Test
    fun `у боевой локации свои объекты, а у мира их нет`() {
        val battle = MarkerType.groupsFor(MapKind.BATTLE)
        assertTrue(battle.isNotEmpty())
        assertTrue(battle.all { it.name.startsWith("BATTLE") || MarkerType.byGroup(it, MapKind.BATTLE).isNotEmpty() })
        assertTrue(MarkerType.groupsFor(MapKind.WORLD).none { it.name.startsWith("BATTLE") })
        assertTrue(MarkerType.groupsFor(MapKind.CITY).none { it.name.startsWith("BATTLE") })
    }

    @Test
    fun `в каждой группе зон боевой локации есть зоны`() {
        for (group in BiomeGroup.entries.filter { it.battle }) {
            assertTrue("Пустая группа: ${group.title}", BiomeType.byGroup(group).isNotEmpty())
        }
    }

    @Test
    fun `стены боевой локации только на боевой локации`() {
        val walls = LineFeatureType.entries.filter { it.battle }
        assertTrue(walls.isNotEmpty())
        assertTrue(walls.none { it.fits(MapKind.WORLD) })
        assertTrue(walls.all { it.fits(MapKind.BATTLE) })
    }

    @Test
    fun `шаги боевой локации идут по порядку`() {
        val stages = stagesFor(MapKind.BATTLE)
        assertEquals(8, stages.size)
        assertEquals((1..8).toList(), stages.map { it.number })
    }

    @Test
    fun `заготовки боевых локаций кратны клетке`() {
        for (preset in MapProject.BATTLE_PRESETS) {
            assertTrue(preset.ground != null)
            assertEquals(0f, preset.width % 50f, 0.001f)
            assertEquals(0f, preset.height % 50f, 0.001f)
            assertTrue(preset.width >= MapProject.MIN_WORLD_SIZE || preset.height >= MapProject.MIN_WORLD_SIZE)
        }
    }

    @Test
    fun `мёртвая фишка — с нулём здоровья`() {
        assertTrue(Token(hp = 0, maxHp = 7).dead)
        assertTrue(!Token(hp = 3, maxHp = 7).dead)
        assertTrue(!Token(hp = 0, maxHp = 0).dead)
    }
}
