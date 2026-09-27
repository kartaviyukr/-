package com.fantasymap.creator.model

import com.fantasymap.creator.geom.MapConvert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Фрагмент можно переделать в карту другого вида. */
class MapConvertTest {

    private val world = MapProject(
        kind = MapKind.WORLD,
        worldWidth = 1000f,
        worldHeight = 800f,
        biomes = listOf(BiomeRegion(biome = BiomeType.TAIGA, points = listOf(Vec(0f, 0f), Vec(100f, 0f), Vec(100f, 100f)))),
        roads = listOf(Road(type = RoadType.HIGHWAY, points = listOf(Vec(0f, 400f), Vec(1000f, 400f)))),
        markers = listOf(Marker(type = MarkerType.CITY, name = "Гавань", pos = Vec(500f, 400f)))
    )

    @Test
    fun `мир в город — поселение становится кварталом, тракт главной улицей`() {
        val city = MapConvert.convert(world, MapKind.CITY)
        assertEquals(MapKind.CITY, city.kind)
        assertEquals(1, city.districts.size)
        assertEquals("Гавань", city.districts[0].name)
        assertEquals(RoadType.MAIN_STREET, city.roads[0].type)
        assertTrue(city.markers.none { it.type == MarkerType.CITY })
    }

    @Test
    fun `город в мир — кварталы становятся значком города`() {
        val city = MapConvert.convert(world, MapKind.CITY)
        val back = MapConvert.convert(city.copy(name = "Гавань — фрагмент"), MapKind.WORLD)
        assertEquals(MapKind.WORLD, back.kind)
        assertTrue(back.districts.isEmpty())
        assertTrue(back.markers.any { it.name == "Гавань" })
        assertEquals(RoadType.HIGHWAY, back.roads[0].type)
    }

    @Test
    fun `в боевую локацию — зоны становятся землёй, дома стенами`() {
        val city = MapConvert.convert(world, MapKind.CITY).copy(
            buildings = listOf(Building(points = listOf(Vec(10f, 10f), Vec(40f, 10f), Vec(40f, 30f), Vec(10f, 30f))))
        )
        val battle = MapConvert.convert(city, MapKind.BATTLE)
        assertEquals(MapKind.BATTLE, battle.kind)
        assertTrue(battle.biomes.all { it.biome.group.battle })
        assertTrue(battle.lines.any { it.type == LineFeatureType.BRICK_WALL })
        assertTrue(battle.roads.isEmpty())
        assertTrue(battle.gridCell > 0f)
    }
}
