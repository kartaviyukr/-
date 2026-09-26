package com.fantasymap.creator.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** У каждой постройки своя крыша: материал, форма и деталь не повторяются. */
class BuildingLookTest {

    @Test
    fun `крыши всех построек разные`() {
        val looks = BuildingType.entries.map { BuildingLook.of(it) }
        val shared = BuildingType.entries.groupBy { BuildingLook.of(it) }.filterValues { it.size > 1 }
        assertTrue("Одинаковые крыши: $shared", shared.isEmpty())
        assertEquals(BuildingType.entries.size, looks.toSet().size)
    }

    @Test
    fun `развалины выглядят развалинами`() {
        for (type in BuildingType.byGroup(BuildingGroup.RUIN_HOUSE)) {
            assertEquals(type.name, RoofForm.RUIN, BuildingLook.of(type).form)
        }
    }
}
