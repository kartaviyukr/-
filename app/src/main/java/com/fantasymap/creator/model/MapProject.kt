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
        const val MIN_WORLD_SIZE = 600f
        const val MAX_WORLD_SIZE = 10000f

        /** Пресеты размера мира: от карманного острова до эпического материка. */
        val PRESETS: List<WorldPreset> = listOf(
            // Небольшие — целиком видны на экране, рисуются быстро
            WorldPreset("Островок", "Небольшие", 1000f, 1000f, "1:1"),
            WorldPreset("Долина", "Небольшие", 1200f, 800f, "3:2"),
            WorldPreset("Побережье", "Небольшие", 800f, 1200f, "2:3"),
            WorldPreset("Уезд", "Небольшие", 1400f, 1050f, "4:3"),
            WorldPreset("Узкий пролив", "Небольшие", 1500f, 750f, "2:1"),

            // Средние — обычный размер для одной страны или материка
            WorldPreset("Королевство", "Средние", 2000f, 1500f, "4:3"),
            WorldPreset("Один континент", "Средние", 2400f, 1600f, "3:2"),
            WorldPreset("Архипелаг", "Средние", 2000f, 2000f, "1:1"),
            WorldPreset("Вертикальный мир", "Средние", 1600f, 2400f, "2:3"),
            WorldPreset("Широкий берег", "Средние", 2800f, 1400f, "2:1"),

            // Большие — есть куда приближаться и добавлять подробности
            WorldPreset("Целый мир", "Большие", 3200f, 1600f, "2:1"),
            WorldPreset("Большой материк", "Большие", 4200f, 2800f, "3:2"),
            WorldPreset("Свиток", "Большие", 4500f, 1500f, "3:1"),
            WorldPreset("Круглый мир", "Большие", 4000f, 4000f, "1:1"),
            WorldPreset("Высокий мир", "Большие", 2800f, 4200f, "2:3"),

            // Огромные — для очень подробных карт, двигаемся двумя пальцами
            WorldPreset("Огромный мир", "Огромные", 6400f, 3200f, "2:1"),
            WorldPreset("Эпический материк", "Огромные", 7200f, 4800f, "3:2"),
            WorldPreset("Бескрайние земли", "Огромные", 8000f, 8000f, "1:1")
        )

        val PRESET_GROUPS: List<String> = PRESETS.map { it.group }.distinct()
    }
}

/** Готовый размер карты для выбора при создании мира. */
data class WorldPreset(
    val title: String,
    val group: String,
    val width: Float,
    val height: Float,
    val ratio: String
) {
    /** Подпись вида «2400 × 1600 · 3:2». */
    val caption: String
        get() = "${width.toInt()} × ${height.toInt()} · $ratio"
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
