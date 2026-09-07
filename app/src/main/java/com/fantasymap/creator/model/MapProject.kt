package com.fantasymap.creator.model

import kotlinx.serialization.Serializable
import java.util.UUID

private fun newId(): String = UUID.randomUUID().toString()

/** Континент, остров или полуостров — контур суши в океане. */
@Serializable
data class Landmass(
    val id: String = newId(),
    val name: String = "",
    val kind: LandKind = LandKind.CONTINENT,
    val points: List<Vec> = emptyList()
)

/** Внутренний водоём: озеро, внутреннее море, залив. */
@Serializable
data class WaterBody(
    val id: String = newId(),
    val name: String = "",
    val kind: WaterKind = WaterKind.LAKE,
    val points: List<Vec> = emptyList()
)

/** Область природной зоны / ландшафта. */
@Serializable
data class BiomeRegion(
    val id: String = newId(),
    val biome: BiomeType = BiomeType.MIXED_FOREST,
    val name: String = "",
    val points: List<Vec> = emptyList()
)

/** Линейный природный объект: река, хребет, обрыв, стена. */
@Serializable
data class LineFeature(
    val id: String = newId(),
    val type: LineFeatureType = LineFeatureType.RIVER,
    val name: String = "",
    val points: List<Vec> = emptyList(),
    val width: Float = 0f
) {
    val effectiveWidth: Float get() = if (width > 0f) width else type.defaultWidth
}

/** Дорога, тропа или морской путь. */
@Serializable
data class Road(
    val id: String = newId(),
    val type: RoadType = RoadType.ROAD,
    val name: String = "",
    val points: List<Vec> = emptyList()
)

/** Точечный объект мира: город, крепость, порт, храм, руины и т.д. */
@Serializable
data class Marker(
    val id: String = newId(),
    val type: MarkerType = MarkerType.CITY,
    val name: String = "",
    val description: String = "",
    val pos: Vec = Vec(0f, 0f),
    val countryId: String? = null,
    val population: String = "",
    val ruler: String = "",
    val scale: Float = 1f,
    val showLabel: Boolean = true
)

/** Свободная подпись на карте. */
@Serializable
data class MapLabel(
    val id: String = newId(),
    val text: String = "",
    val pos: Vec = Vec(0f, 0f),
    val style: LabelStyle = LabelStyle.REGION,
    val rotation: Float = 0f
)

/** Подробная анкета государства. */
@Serializable
data class CountryInfo(
    val capital: String = "",
    val ruler: String = "",
    val government: String = "",
    val population: String = "",
    val peoples: String = "",
    val religion: String = "",
    val language: String = "",
    val currency: String = "",
    val army: String = "",
    val economy: String = "",
    val culture: String = "",
    val relations: String = "",
    val history: String = "",
    val description: String = ""
) {
    /** Сколько полей анкеты заполнено — для индикатора прогресса. */
    fun filledCount(): Int = listOf(
        capital, ruler, government, population, peoples, religion, language,
        currency, army, economy, culture, relations, history, description
    ).count { it.isNotBlank() }

    companion object {
        const val FIELD_COUNT = 14
    }
}

/**
 * Государство. Территория задаётся отдельными областями (areas):
 * граница может проходить и по воде, поэтому области не привязаны к суше.
 */
@Serializable
data class Country(
    val id: String = newId(),
    val name: String = "",
    val color: Int = 0xFFB03A2E.toInt(),
    val areas: List<List<Vec>> = emptyList(),
    val info: CountryInfo = CountryInfo()
)

/** Настройки отображения карты. */
@Serializable
data class MapStyle(
    val oceanColor: Int = 0xFF6E9EBF.toInt(),
    val landColor: Int = 0xFFE8DCBE.toInt(),
    val coastColor: Int = 0xFF4A6B80.toInt(),
    val parchment: Boolean = true,
    val showGrid: Boolean = false,
    val showBorders: Boolean = true,
    val bordersFilled: Boolean = true,
    val showBiomes: Boolean = true,
    val showPatterns: Boolean = true,
    val showRoads: Boolean = true,
    val showMarkers: Boolean = true,
    val showLabels: Boolean = true,
    val showFrame: Boolean = true,
    val showCompass: Boolean = true,
    val labelScale: Float = 1f,
    val seed: Int = 1337
)

/** Полный проект карты — всё, что сохраняется в файл. */
@Serializable
data class MapProject(
    val id: String = newId(),
    val name: String = "Новый мир",
    val worldWidth: Float = 2400f,
    val worldHeight: Float = 1600f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val stage: Int = 1,
    val landmasses: List<Landmass> = emptyList(),
    val waters: List<WaterBody> = emptyList(),
    val biomes: List<BiomeRegion> = emptyList(),
    val lines: List<LineFeature> = emptyList(),
    val roads: List<Road> = emptyList(),
    val markers: List<Marker> = emptyList(),
    val countries: List<Country> = emptyList(),
    val labels: List<MapLabel> = emptyList(),
    val style: MapStyle = MapStyle()
) {
    fun countryById(id: String?): Country? =
        if (id == null) null else countries.firstOrNull { it.id == id }

    /** Столицы государства. */
    fun capitalsOf(countryId: String): List<Marker> =
        markers.filter { it.countryId == countryId && it.type == MarkerType.CAPITAL }

    fun markersOf(countryId: String): List<Marker> =
        markers.filter { it.countryId == countryId }

    fun isEmpty(): Boolean = landmasses.isEmpty() && waters.isEmpty() && biomes.isEmpty() &&
        lines.isEmpty() && roads.isEmpty() && markers.isEmpty() &&
        countries.isEmpty() && labels.isEmpty()

    fun objectCount(): Int = landmasses.size + waters.size + biomes.size + lines.size +
        roads.size + markers.size + labels.size

    companion object {
        /** Пресеты размера мира. */
        val PRESETS: List<Triple<String, Float, Float>> = listOf(
            Triple("Один континент (3:2)", 2400f, 1600f),
            Triple("Целый мир (2:1)", 3200f, 1600f),
            Triple("Архипелаг (1:1)", 2000f, 2000f),
            Triple("Королевство (4:3)", 2000f, 1500f),
            Triple("Вертикальный (2:3)", 1600f, 2400f)
        )
    }
}

/** Что сейчас выделено в редакторе (только состояние UI, не сохраняется). */
sealed interface Selection {
    val id: String

    data class Land(override val id: String) : Selection
    data class Water(override val id: String) : Selection
    data class Biome(override val id: String) : Selection
    data class Line(override val id: String) : Selection
    data class RoadSel(override val id: String) : Selection
    data class MarkerSel(override val id: String) : Selection
    data class CountryArea(override val id: String, val index: Int) : Selection
    data class LabelSel(override val id: String) : Selection
}

/** Краткая карточка проекта для списка. */
data class ProjectSummary(
    val id: String,
    val name: String,
    val updatedAt: Long,
    val stage: Int,
    val landCount: Int,
    val markerCount: Int,
    val countryCount: Int
)
