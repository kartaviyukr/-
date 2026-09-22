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

/**
 * Область природной зоны / ландшафта.
 * После выравнивания границ область может состоять из нескольких контуров:
 * отрезанных кусков и дыр. Они рисуются по правилу чётности, поэтому контур
 * внутри другого контура даёт дыру.
 */
@Serializable
data class BiomeRegion(
    val id: String = newId(),
    val biome: BiomeType = BiomeType.MIXED_FOREST,
    val name: String = "",
    val points: List<Vec> = emptyList(),
    val extraContours: List<List<Vec>> = emptyList(),
    /** Авторская заготовка: область замащивается своей картинкой. */
    val assetId: String? = null
) {
    /** Все контуры области: основной и дополнительные. */
    fun contours(): List<List<Vec>> =
        if (extraContours.isEmpty()) {
            listOf(points)
        } else {
            (listOf(points) + extraContours).filter { it.size >= 3 }
        }
}

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
    val showLabel: Boolean = true,
    /** Карта, на которую ведёт этот объект: тапнул — перешёл к подробной карте. */
    val linkedProjectId: String? = null,
    /** Авторская заготовка: вместо знака рисуется своя картинка. */
    val assetId: String? = null
)

/**
 * Отдельное здание на карте города: дом, церковь, ратуша, лавка.
 * points — след здания на земле, обычно четырёхугольник.
 */
@Serializable
data class Building(
    val id: String = newId(),
    val type: BuildingType = BuildingType.HOUSE,
    val name: String = "",
    val description: String = "",
    val points: List<Vec> = emptyList(),
    val floors: Int = 1,
    val owner: String = "",
    val showLabel: Boolean = false,
    /** Авторская заготовка: след здания закрывается своей картинкой. */
    val assetId: String? = null
)

/** Квартал города — район со своим характером. */
@Serializable
data class District(
    val id: String = newId(),
    val type: DistrictType = DistrictType.OLD_TOWN,
    val name: String = "",
    val description: String = "",
    val points: List<Vec> = emptyList()
)

/**
 * Свободная подпись на карте.
 * Если задан path, подпись идёт вдоль кривой — так подписывают реки и хребты.
 */
@Serializable
data class MapLabel(
    val id: String = newId(),
    val text: String = "",
    val pos: Vec = Vec(0f, 0f),
    val style: LabelStyle = LabelStyle.REGION,
    val rotation: Float = 0f,
    val path: List<Vec> = emptyList()
) {
    val curved: Boolean get() = path.size >= 2
}

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

/** Настройки отображения карты: что видно, что заперто от правки и какими красками. */
@Serializable
data class MapStyle(
    val oceanColor: Int = 0xFF6E9EBF.toInt(),
    val landColor: Int = 0xFFE8DCBE.toInt(),
    val coastColor: Int = 0xFF4A6B80.toInt(),
    val inkColor: Int = 0xFF3A2E22.toInt(),
    val deskColor: Int = 0xFF2B2925.toInt(),
    val labelColor: Int = 0,
    val parchment: Boolean = true,
    val showGrid: Boolean = false,
    val showBorders: Boolean = true,
    val bordersFilled: Boolean = true,
    val showLand: Boolean = true,
    val showWater: Boolean = true,
    val showBiomes: Boolean = true,
    val showLines: Boolean = true,
    val showPatterns: Boolean = true,
    val showRoads: Boolean = true,
    val showMarkers: Boolean = true,
    val showLabels: Boolean = true,
    val showFrame: Boolean = true,
    val showCompass: Boolean = true,
    val lockLand: Boolean = false,
    val lockWater: Boolean = false,
    val lockBiomes: Boolean = false,
    val lockLines: Boolean = false,
    val lockRoads: Boolean = false,
    val lockMarkers: Boolean = false,
    val lockLabels: Boolean = false,
    val lockCountries: Boolean = false,
    val showDistricts: Boolean = true,
    val showBuildings: Boolean = true,
    val lockDistricts: Boolean = false,
    val lockBuildings: Boolean = false,
    val labelScale: Float = 1f,
    val seed: Int = 1337
)

/** Слой карты — строка в списке слоёв. */
enum class MapLayer(val title: String) {
    LAND("Суша и берега"),
    WATER("Озёра и моря"),
    BIOMES("Природные зоны"),
    LINES("Реки, хребты, стены"),
    ROADS("Дороги и пути"),
    MARKERS("Объекты"),
    LABELS("Подписи"),
    COUNTRIES("Границы стран"),
    DISTRICTS("Кварталы города"),
    BUILDINGS("Здания");

    fun visible(style: MapStyle): Boolean = when (this) {
        LAND -> style.showLand
        WATER -> style.showWater
        BIOMES -> style.showBiomes
        LINES -> style.showLines
        ROADS -> style.showRoads
        MARKERS -> style.showMarkers
        LABELS -> style.showLabels
        COUNTRIES -> style.showBorders
        DISTRICTS -> style.showDistricts
        BUILDINGS -> style.showBuildings
    }

    fun locked(style: MapStyle): Boolean = when (this) {
        LAND -> style.lockLand
        WATER -> style.lockWater
        BIOMES -> style.lockBiomes
        LINES -> style.lockLines
        ROADS -> style.lockRoads
        MARKERS -> style.lockMarkers
        LABELS -> style.lockLabels
        COUNTRIES -> style.lockCountries
        DISTRICTS -> style.lockDistricts
        BUILDINGS -> style.lockBuildings
    }

    fun withVisible(style: MapStyle, value: Boolean): MapStyle = when (this) {
        LAND -> style.copy(showLand = value)
        WATER -> style.copy(showWater = value)
        BIOMES -> style.copy(showBiomes = value)
        LINES -> style.copy(showLines = value)
        ROADS -> style.copy(showRoads = value)
        MARKERS -> style.copy(showMarkers = value)
        LABELS -> style.copy(showLabels = value)
        COUNTRIES -> style.copy(showBorders = value)
        DISTRICTS -> style.copy(showDistricts = value)
        BUILDINGS -> style.copy(showBuildings = value)
    }

    fun withLocked(style: MapStyle, value: Boolean): MapStyle = when (this) {
        LAND -> style.copy(lockLand = value)
        WATER -> style.copy(lockWater = value)
        BIOMES -> style.copy(lockBiomes = value)
        LINES -> style.copy(lockLines = value)
        ROADS -> style.copy(lockRoads = value)
        MARKERS -> style.copy(lockMarkers = value)
        LABELS -> style.copy(lockLabels = value)
        COUNTRIES -> style.copy(lockCountries = value)
        DISTRICTS -> style.copy(lockDistricts = value)
        BUILDINGS -> style.copy(lockBuildings = value)
    }
}

/** Готовый вид карты, который применяется одним нажатием. */
data class StylePreset(val title: String, val hint: String, val apply: (MapStyle) -> MapStyle) {
    companion object {
        val ALL: List<StylePreset> = listOf(
            StylePreset("Старый пергамент", "тёплая бумага, синее море") { base ->
                base.copy(
                    oceanColor = 0xFF6E9EBF.toInt(),
                    landColor = 0xFFE8DCBE.toInt(),
                    coastColor = 0xFF4A6B80.toInt(),
                    inkColor = 0xFF3A2E22.toInt(),
                    deskColor = 0xFF2B2925.toInt(),
                    labelColor = 0,
                    showBiomes = true,
                    showPatterns = true,
                    showFrame = true,
                    showCompass = true
                )
            },
            StylePreset("Чернильная гравюра", "светлая бумага, только линии") { base ->
                base.copy(
                    oceanColor = 0xFFE3DCCA.toInt(),
                    landColor = 0xFFF6F1E3.toInt(),
                    coastColor = 0xFF2E2A24.toInt(),
                    inkColor = 0xFF23201B.toInt(),
                    deskColor = 0xFF4A453C.toInt(),
                    labelColor = 0xFF23201B.toInt(),
                    showBiomes = false,
                    showPatterns = true,
                    showFrame = true,
                    showCompass = true
                )
            },
            StylePreset("Цветная карта", "яркие краски, всё видно") { base ->
                base.copy(
                    oceanColor = 0xFF4FA3D1.toInt(),
                    landColor = 0xFFE2EFC8.toInt(),
                    coastColor = 0xFF2E6E93.toInt(),
                    inkColor = 0xFF33302A.toInt(),
                    deskColor = 0xFF33414A.toInt(),
                    labelColor = 0,
                    showBiomes = true,
                    showPatterns = true,
                    showFrame = true,
                    showCompass = true
                )
            },
            StylePreset("Тёмное фэнтези", "ночная карта, светлые чернила") { base ->
                base.copy(
                    oceanColor = 0xFF1E2A33.toInt(),
                    landColor = 0xFF3B3C34.toInt(),
                    coastColor = 0xFF8FA9B8.toInt(),
                    inkColor = 0xFFE8DEC6.toInt(),
                    deskColor = 0xFF121417.toInt(),
                    labelColor = 0xFFEDE3CC.toInt(),
                    showBiomes = true,
                    showPatterns = true,
                    showFrame = true,
                    showCompass = true
                )
            },
            StylePreset("Чистый набросок", "без рамки и текстур") { base ->
                base.copy(
                    oceanColor = 0xFFD9E4EA.toInt(),
                    landColor = 0xFFFAF6EC.toInt(),
                    coastColor = 0xFF6E7B84.toInt(),
                    inkColor = 0xFF44403A.toInt(),
                    deskColor = 0xFF9EA6AB.toInt(),
                    labelColor = 0,
                    showBiomes = true,
                    showPatterns = false,
                    showFrame = false,
                    showCompass = false
                )
            }
        )
    }
}

/** Полный проект карты — всё, что сохраняется в файл. */
@Serializable
data class MapProject(
    val id: String = newId(),
    val name: String = "Новый мир",
    val worldWidth: Float = 2400f,
    val worldHeight: Float = 1600f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val kind: MapKind = MapKind.WORLD,
    val stage: Int = 1,
    val landmasses: List<Landmass> = emptyList(),
    val waters: List<WaterBody> = emptyList(),
    val biomes: List<BiomeRegion> = emptyList(),
    val lines: List<LineFeature> = emptyList(),
    val roads: List<Road> = emptyList(),
    val markers: List<Marker> = emptyList(),
    val countries: List<Country> = emptyList(),
    val labels: List<MapLabel> = emptyList(),
    val districts: List<District> = emptyList(),
    val buildings: List<Building> = emptyList(),
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
        countries.isEmpty() && labels.isEmpty() && buildings.isEmpty() && districts.isEmpty()

    fun objectCount(): Int = landmasses.size + waters.size + biomes.size + lines.size +
        roads.size + markers.size + labels.size + buildings.size + districts.size

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

        /** Размеры карты города: от деревни до столицы. */
        val CITY_PRESETS: List<WorldPreset> = listOf(
            WorldPreset("Деревня", "Небольшие", 600f, 450f, "4:3"),
            WorldPreset("Городок", "Небольшие", 900f, 700f, "9:7"),
            WorldPreset("Город у реки", "Средние", 1400f, 900f, "3:2"),
            WorldPreset("Крепостной город", "Средние", 1200f, 1200f, "1:1"),
            WorldPreset("Портовый город", "Средние", 1600f, 1000f, "8:5"),
            WorldPreset("Большой город", "Большие", 2200f, 1600f, "11:8"),
            WorldPreset("Столица", "Большие", 3000f, 2200f, "15:11"),
            WorldPreset("Великий город", "Огромные", 4000f, 3000f, "4:3")
        )
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
    data class BuildingSel(override val id: String) : Selection
    data class DistrictSel(override val id: String) : Selection
    data class LabelSel(override val id: String) : Selection
}

/** Краткая карточка проекта для списка. */
data class ProjectSummary(
    val id: String,
    val name: String,
    val kind: MapKind = MapKind.WORLD,
    val updatedAt: Long,
    val stage: Int,
    val landCount: Int,
    val markerCount: Int,
    val countryCount: Int
)
