package com.fantasymap.creator.model

import com.fantasymap.creator.geom.CityGenerator
import com.fantasymap.creator.geom.StreetPattern
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.atan2

/** Застройка кварталов: у каждого рода свой характер, улицы и границы соблюдены. */
class CityGeneratorTest {

    private val project = MapProject(kind = MapKind.CITY, worldWidth = 1400f, worldHeight = 1000f)

    private fun square(size: Float) = listOf(
        Vec(100f, 100f), Vec(100f + size, 100f), Vec(100f + size, 100f + size), Vec(100f, 100f + size)
    )

    private fun fill(type: DistrictType, seed: Int = 7) =
        CityGenerator.fillDistrict(project, District(id = "test-district", type = type, points = square(420f)), 0.5f, seed)

    private fun averageArea(buildings: List<Building>): Float =
        buildings.map { Geometry.area(it.points) }.average().toFloat()

    /** Разброс поворота домов: насколько они стоят вкривь. */
    private fun tiltSpread(buildings: List<Building>): Float {
        val angles = buildings.map {
            val a = it.points[0]
            val b = it.points[1]
            atan2(b.y - a.y, b.x - a.x)
        }
        val mean = angles.average().toFloat()
        return angles.map { abs(it - mean) }.average().toFloat()
    }

    @Test
    fun `каждый квартал застраивается, а где положено — с улицами`() {
        for (type in DistrictType.entries) {
            val result = fill(type)
            assertTrue("$type: нет домов", result.buildings.isNotEmpty())
            if (CityGenerator.planFor(type).streets != StreetPattern.NONE) {
                assertTrue("$type: нет улиц", result.roads.isNotEmpty())
            }
        }
    }

    @Test
    fun `дома не выходят за границу квартала`() {
        val outline = square(420f)
        for (type in DistrictType.entries) {
            for (building in fill(type).buildings) {
                for (point in building.points) {
                    assertTrue("$type: дом снаружи", Geometry.pointInPolygon(point, outline))
                }
            }
        }
    }

    @Test
    fun `трущобы мельче и кривее бедного квартала`() {
        val slums = fill(DistrictType.SLUMS).buildings
        val poor = fill(DistrictType.POOR_QUARTER).buildings
        assertTrue(averageArea(slums) < averageArea(poor))
        assertTrue(tiltSpread(slums) > tiltSpread(poor) * 3f)
    }

    @Test
    fun `дворцовый квартал не похож на знатный`() {
        val palace = fill(DistrictType.PALACE_QUARTER)
        val noble = fill(DistrictType.NOBLE_QUARTER)
        assertTrue(palace.buildings.any { it.type == BuildingType.PALACE })
        assertFalse(noble.buildings.any { it.type == BuildingType.PALACE })
        assertTrue(noble.buildings.any { it.type == BuildingType.MANOR || it.type == BuildingType.RICH_HOUSE })
        assertFalse(palace.buildings.any { it.type == BuildingType.MANOR || it.type == BuildingType.RICH_HOUSE })
        assertTrue(palace.gardens.any { it.biome == BiomeType.CITY_ROYAL_GARDEN || it.biome == BiomeType.CITY_TILED_PLAZA })
    }

    @Test
    fun `без улиц — только дома`() {
        val result = CityGenerator.fillDistrict(
            project, District(id = "test-district", type = DistrictType.NEW_TOWN, points = square(420f)), 0.5f, 3, withStreets = false
        )
        assertTrue(result.roads.isEmpty())
        assertTrue(result.buildings.isNotEmpty())
    }
}

/** Густота бедного квартала и соединение улиц соседних кварталов. */
class CityDensityAndLinksTest {

    private val project = MapProject(kind = MapKind.CITY, worldWidth = 1400f, worldHeight = 1000f)

    private fun rect(x: Float, y: Float, w: Float, h: Float) =
        listOf(Vec(x, y), Vec(x + w, y), Vec(x + w, y + h), Vec(x, y + h))

    @Test
    fun `бедный квартал застроен плотно`() {
        val fill = CityGenerator.fillDistrict(
            project, District(id = "test-district", type = DistrictType.POOR_QUARTER, points = rect(100f, 100f, 420f, 420f)), 0.5f, 5
        )
        assertTrue("домов: ${fill.buildings.size}", fill.buildings.size >= 100)
    }

    @Test
    fun `ползунок густоты добавляет дома`() {
        for (type in listOf(DistrictType.POOR_QUARTER, DistrictType.SLUMS, DistrictType.OLD_TOWN)) {
            val district = District(id = "test-district", type = type, points = rect(100f, 100f, 420f, 420f))
            val sparse = CityGenerator.fillDistrict(project, district, 0.1f, 9).buildings.size
            val dense = CityGenerator.fillDistrict(project, district, 0.9f, 9).buildings.size
            assertTrue("$type: $sparse → $dense", dense > sparse)
        }
    }

    @Test
    fun `улицы соседних кварталов соединяются на границе`() {
        val left = District(id = "test-district", type = DistrictType.NEW_TOWN, points = rect(100f, 100f, 420f, 420f))
        val right = District(id = "test-district", type = DistrictType.CRAFT_QUARTER, points = rect(520f, 100f, 420f, 420f))
        var state = project.copy(districts = listOf(left, right))
        for (district in listOf(left, right)) {
            val fill = CityGenerator.fillDistrict(state, district, 0.5f, 11)
            state = state.copy(buildings = state.buildings + fill.buildings, roads = state.roads + fill.roads)
        }
        val links = CityGenerator.connectStreets(state)
        assertTrue("связок: ${links.links}", links.links > 0)
        // Далёкие улицы не перекраиваются: каждая связка — короткая.
        for ((before, after) in state.roads.zip(links.roads)) {
            assertTrue(after.points.size - before.points.size <= 2)
        }
    }
}
