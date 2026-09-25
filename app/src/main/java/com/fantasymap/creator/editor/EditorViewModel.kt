package com.fantasymap.creator.editor

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fantasymap.creator.data.ProjectStore
import com.fantasymap.creator.geom.CityGenerator
import com.fantasymap.creator.geom.FragmentCopy
import com.fantasymap.creator.geom.AlignResult
import com.fantasymap.creator.geom.PolygonOps
import com.fantasymap.creator.geom.ShoreGenerator
import com.fantasymap.creator.geom.WorldGenerator
import com.fantasymap.creator.export.Exporter
import com.fantasymap.creator.data.AssetStore
import com.fantasymap.creator.model.BBox
import com.fantasymap.creator.model.CustomAsset
import com.fantasymap.creator.model.CustomKind
import com.fantasymap.creator.model.BiomeGroup
import com.fantasymap.creator.model.BiomeRegion
import com.fantasymap.creator.model.BiomeType
import com.fantasymap.creator.model.BuildingGroup
import com.fantasymap.creator.model.BuildingType
import com.fantasymap.creator.model.BattleStage
import com.fantasymap.creator.model.CityStage
import com.fantasymap.creator.model.Condition
import com.fantasymap.creator.model.FogArea
import com.fantasymap.creator.model.GridKind
import com.fantasymap.creator.model.SceneInfo
import com.fantasymap.creator.model.Token
import com.fantasymap.creator.model.TokenFaction
import com.fantasymap.creator.model.TokenGroup
import com.fantasymap.creator.model.TokenSize
import com.fantasymap.creator.model.TokenType
import com.fantasymap.creator.model.DistrictType
import com.fantasymap.creator.model.Country
import com.fantasymap.creator.model.CountryInfo
import com.fantasymap.creator.model.Geometry
import com.fantasymap.creator.model.LabelStyle
import com.fantasymap.creator.model.LandKind
import com.fantasymap.creator.model.Landmass
import com.fantasymap.creator.model.LineFeature
import com.fantasymap.creator.model.LineFeatureType
import com.fantasymap.creator.model.MapLabel
import com.fantasymap.creator.model.MapKind
import com.fantasymap.creator.model.MapLayer
import com.fantasymap.creator.model.MapStage
import com.fantasymap.creator.model.MapProject
import com.fantasymap.creator.model.MapStyle
import com.fantasymap.creator.model.Marker
import com.fantasymap.creator.model.MarkerGroup
import com.fantasymap.creator.model.MarkerType
import com.fantasymap.creator.model.ProjectSummary
import com.fantasymap.creator.model.Road
import com.fantasymap.creator.model.RoadType
import com.fantasymap.creator.model.Selection
import com.fantasymap.creator.model.Building
import com.fantasymap.creator.model.District
import com.fantasymap.creator.model.Stage
import com.fantasymap.creator.model.stageFor
import com.fantasymap.creator.model.stagesFor
import com.fantasymap.creator.model.StylePreset
import com.fantasymap.creator.model.Tool
import com.fantasymap.creator.model.Vec
import com.fantasymap.creator.model.WaterBody
import com.fantasymap.creator.model.WaterKind
import com.fantasymap.creator.render.Camera
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Состояние редактора карты: проект, инструменты, камера, история изменений. */
class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val store = ProjectStore(application)
    private val exporter = Exporter(application)

    /** Авторский контент: свои постройки, зоны и объекты с картинками. */
    val assetStore = AssetStore(application)

    var projects by mutableStateOf<List<ProjectSummary>>(emptyList())
        private set

    var project by mutableStateOf<MapProject?>(null)
        private set

    var stage by mutableStateOf<MapStage>(Stage.CONTINENTS)
        private set

    var tool by mutableStateOf(Tool.LAND)
    var biome by mutableStateOf(BiomeType.MIXED_FOREST)
    var markerType by mutableStateOf(MarkerType.CITY)
    var markerGroup by mutableStateOf(MarkerGroup.SETTLEMENT)
    var lineType by mutableStateOf(LineFeatureType.RIVER)
    var roadType by mutableStateOf(RoadType.ROAD)
    var labelStyle by mutableStateOf(LabelStyle.REGION)
    var waterKind by mutableStateOf(WaterKind.LAKE)
    var buildingType by mutableStateOf(BuildingType.HOUSE)
    var buildingGroup by mutableStateOf(BuildingGroup.HOME)
    var districtType by mutableStateOf(DistrictType.OLD_TOWN)

    /** Фишка, которую ставит инструмент «Фишка». */
    var tokenType by mutableStateOf(TokenType.GOBLIN)
    var tokenGroup by mutableStateOf(TokenGroup.GREENSKINS)

    /** Подпись линейки, пока палец ведёт замер. */
    var rulerText by mutableStateOf<String?>(null)
        private set

    /** Последние броски кубиков, свежие сверху. */
    val diceLog = mutableStateListOf<String>()

    /** Вся авторская библиотека. Обновляется после каждого изменения. */
    var customAssets by mutableStateOf<List<CustomAsset>>(emptyList())
        private set

    /** Выбранные авторские заготовки: зона, постройка и объект. */
    var customZone by mutableStateOf<CustomAsset?>(null)
        private set
    var customBuilding by mutableStateOf<CustomAsset?>(null)
        private set
    var customObject by mutableStateOf<CustomAsset?>(null)
        private set
    var customToken by mutableStateOf<CustomAsset?>(null)
        private set

    /** Плотность застройки квартала: 0 — просторно, 1 — тесно. */
    var buildDensity by mutableStateOf(0.5f)
    var activeCountryId by mutableStateOf<String?>(null)
    var selection by mutableStateOf<Selection?>(null)
    var camera by mutableStateOf(Camera())
    var message by mutableStateOf<String?>(null)
    var busy by mutableStateOf(false)
        private set

    /** Развёрнута ли нижняя панель. В свёрнутом виде карте достаётся весь экран. */
    var panelExpanded by mutableStateOf(true)

    /** Рисовать подпись вдоль кривой, а не в точке. */
    var labelCurved by mutableStateOf(false)

    /** Точки текущего, ещё не завершённого штриха. */
    val draft = mutableStateListOf<Vec>()

    /** Выделенный прямоугольник фрагмента, ждущий подтверждения. */
    var fragmentRect by mutableStateOf<BBox?>(null)
        private set

    private var fragmentStart: Vec? = null

    var canUndo by mutableStateOf(false)
        private set
    var canRedo by mutableStateOf(false)
        private set

    private val undoStack = ArrayDeque<MapProject>()
    private val redoStack = ArrayDeque<MapProject>()
    private var saveJob: Job? = null
    private var draggingSelection: Selection? = null

    /** Подпись, только что нарисованная вдоль кривой: её карточку надо открыть. */
    var pendingLabelEdit by mutableStateOf<String?>(null)
    private var viewWidth = 0f
    private var viewHeight = 0f

    init {
        refreshProjects()
        refreshAssets()
    }

    // --------------------------------------------------- авторский контент

    fun refreshAssets() {
        viewModelScope.launch {
            customAssets = withContext(Dispatchers.IO) { assetStore.list() }
        }
    }

    /** Заготовки, уместные на открытой карте. */
    fun assetsOf(kind: CustomKind): List<CustomAsset> =
        customAssets.filter { it.kind == kind && it.fits(mapKind) }

    /**
     * Внести свою картинку в библиотеку.
     * Картинка переносится во внутреннюю память, карта хранит только ссылку на заготовку.
     */
    fun addAsset(uri: Uri, draft: CustomAsset) {
        viewModelScope.launch {
            busy = true
            val saved = withContext(Dispatchers.IO) {
                if (!assetStore.importImage(uri, draft.id)) return@withContext null
                val color = assetStore.averageColor(draft.id) ?: draft.color
                val asset = draft.copy(color = color, createdAt = System.currentTimeMillis())
                assetStore.save(asset)
                asset
            }
            customAssets = withContext(Dispatchers.IO) { assetStore.list() }
            busy = false
            if (saved == null) {
                message = "Не удалось прочитать картинку"
            } else {
                message = "«${saved.title}» добавлена в вашу библиотеку"
                selectAsset(saved)
            }
        }
    }

    fun updateAsset(asset: CustomAsset) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { assetStore.save(asset) }
            customAssets = withContext(Dispatchers.IO) { assetStore.list() }
            if (customZone?.id == asset.id) customZone = asset
            if (customBuilding?.id == asset.id) customBuilding = asset
            if (customObject?.id == asset.id) customObject = asset
            if (customToken?.id == asset.id) customToken = asset
        }
    }

    fun deleteAsset(id: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { assetStore.delete(id) }
            customAssets = withContext(Dispatchers.IO) { assetStore.list() }
            if (customZone?.id == id) customZone = null
            if (customBuilding?.id == id) customBuilding = null
            if (customObject?.id == id) customObject = null
            if (customToken?.id == id) customToken = null
        }
    }

    /** Выбрать авторскую заготовку — ею и будет рисовать выбранный инструмент. */
    fun selectAsset(asset: CustomAsset) {
        when (asset.kind) {
            CustomKind.ZONE -> {
                customZone = asset
                tool = Tool.BIOME
            }
            CustomKind.BUILDING -> {
                customBuilding = asset
                tool = Tool.BUILDING
            }
            CustomKind.OBJECT -> {
                customObject = asset
                tool = Tool.MARKER
            }
            CustomKind.TOKEN -> {
                customToken = asset
                tool = Tool.TOKEN
            }
        }
    }

    fun clearAsset(kind: CustomKind) {
        when (kind) {
            CustomKind.ZONE -> customZone = null
            CustomKind.BUILDING -> customBuilding = null
            CustomKind.OBJECT -> customObject = null
            CustomKind.TOKEN -> customToken = null
        }
    }

    /** Выбор обычной зоны отменяет авторскую. */
    fun selectBiome(type: BiomeType) {
        biome = type
        customZone = null
    }

    fun selectMarkerType(type: MarkerType) {
        markerType = type
        customObject = null
    }

    fun selectBuildingType(type: BuildingType) {
        buildingType = type
        customBuilding = null
    }

    // ------------------------------------------------------------- проекты

    fun refreshProjects() {
        viewModelScope.launch {
            projects = withContext(Dispatchers.IO) { store.list() }
        }
    }

    fun createProject(
        name: String,
        width: Float,
        height: Float,
        kind: MapKind = MapKind.WORLD,
        ground: BiomeType? = null,
        landBase: Boolean = false
    ) {
        val base = MapProject(
            name = name.ifBlank {
                when (kind) {
                    MapKind.CITY -> "Новый город"
                    MapKind.BATTLE -> "Новая локация"
                    MapKind.WORLD -> "Новый мир"
                }
            },
            worldWidth = width,
            worldHeight = height,
            kind = kind,
            landBase = landBase && kind != MapKind.BATTLE
        )
        // Боевая локация: тёмный стол, основа-пол, фото-текстуры, без компаса и рамки.
        val fresh = if (kind == MapKind.BATTLE) {
            base.copy(
                groundBiome = ground ?: BiomeType.STONE_FLOOR,
                style = base.style.copy(
                    oceanColor = 0xFF24211E.toInt(),
                    deskColor = 0xFF161412.toInt(),
                    showCompass = false,
                    showFrame = false,
                    photoTextures = true
                )
            )
        } else {
            base
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) { store.save(fresh) }
            openProject(fresh)
            refreshProjects()
        }
    }

    fun openProject(id: String) {
        viewModelScope.launch {
            val loaded = withContext(Dispatchers.IO) { store.load(id) }
            if (loaded == null) {
                message = "Не удалось открыть карту"
            } else {
                openProject(loaded)
            }
        }
    }

    private fun openProject(loaded: MapProject) {
        project = loaded
        stage = stageFor(loaded.kind, loaded.stage)
        tool = defaultToolFor(stage)
        selection = null
        draft.clear()
        undoStack.clear()
        redoStack.clear()
        canUndo = false
        canRedo = false
        activeCountryId = loaded.countries.firstOrNull()?.id
        syncMarkerPickerToKind()
        if (viewWidth > 0f) fitToView(viewWidth, viewHeight)
    }

    fun closeProject() {
        saveNow()
        project = null
        selection = null
        draft.clear()
        refreshProjects()
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { store.delete(id) }
            if (project?.id == id) project = null
            refreshProjects()
            message = "Карта удалена"
        }
    }

    fun duplicateProject(id: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { store.duplicate(id) }
            refreshProjects()
            message = "Создана копия карты"
        }
    }

    fun renameProject(name: String) {
        edit { it.copy(name = name.ifBlank { it.name }) }
    }

    fun saveNow() {
        val current = project ?: return
        saveJob?.cancel()
        viewModelScope.launch {
            withContext(Dispatchers.IO) { store.save(current) }
            projects = withContext(Dispatchers.IO) { store.list() }
        }
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(900)
            val current = project ?: return@launch
            withContext(Dispatchers.IO) { store.save(current) }
        }
    }

    // ------------------------------------------------------------- история

    private fun edit(block: (MapProject) -> MapProject) {
        val current = project ?: return
        undoStack.addLast(current)
        while (undoStack.size > HISTORY_LIMIT) undoStack.removeFirst()
        redoStack.clear()
        project = block(current).copy(updatedAt = System.currentTimeMillis())
        canUndo = true
        canRedo = false
        scheduleSave()
    }

    /** Изменение без записи в историю (перетаскивание, правка анкет — иначе история переполнится). */
    private fun editQuiet(block: (MapProject) -> MapProject) {
        val current = project ?: return
        project = block(current).copy(updatedAt = System.currentTimeMillis())
    }

    fun undo() {
        val current = project ?: return
        val previous = undoStack.removeLastOrNull() ?: return
        redoStack.addLast(current)
        project = previous
        canUndo = undoStack.isNotEmpty()
        canRedo = true
        selection = null
        scheduleSave()
    }

    fun redo() {
        val current = project ?: return
        val next = redoStack.removeLastOrNull() ?: return
        undoStack.addLast(current)
        project = next
        canUndo = true
        canRedo = redoStack.isNotEmpty()
        selection = null
        scheduleSave()
    }

    // ------------------------------------------------------------- этапы

    /** Вид открытой карты: мир или город. */
    val mapKind: MapKind get() = project?.kind ?: MapKind.WORLD

    /** Шаги для открытой карты. */
    fun stages(): List<MapStage> = stagesFor(mapKind)

    /** Группы зон: на карте города городские идут первыми. */
    fun biomeGroups(): List<BiomeGroup> {
        val world = BiomeGroup.entries.filter { !it.battle && it != BiomeGroup.CITY }
        val battle = BiomeGroup.entries.filter { it.battle }
        return when (mapKind) {
            MapKind.CITY -> listOf(BiomeGroup.CITY) + world
            MapKind.BATTLE -> battle + BiomeGroup.SHORE + BiomeGroup.CITY
            MapKind.WORLD -> world + BiomeGroup.CITY
        }
    }

    /** Виды линий, уместные на открытой карте. */
    fun lineTypes(): List<LineFeatureType> = LineFeatureType.entries.filter { it.fits(mapKind) }

    /** Группы объектов, уместные на открытой карте. */
    fun markerGroups(): List<MarkerGroup> = MarkerType.groupsFor(mapKind)

    /** Объекты выбранной группы, уместные на открытой карте. */
    fun markerTypes(): List<MarkerType> = MarkerType.byGroup(markerGroup, mapKind)

    /** Подменяет группу, если на такой карте в ней нет ни одного объекта. */
    private fun ensureGroupFits(group: MarkerGroup): MarkerGroup =
        if (MarkerType.byGroup(group, mapKind).isNotEmpty()) group
        else MarkerType.groupsFor(mapKind).firstOrNull() ?: group

    /** Приводит выбранную группу и объект в соответствие виду карты. */
    fun syncMarkerPickerToKind() {
        val group = ensureGroupFits(markerGroup)
        if (group != markerGroup) markerGroup = group
        if (!markerType.fits(mapKind) || markerType.group != markerGroup) {
            markerType = MarkerType.byGroup(markerGroup, mapKind).firstOrNull() ?: markerType
        }
    }

    fun selectMarkerGroup(group: MarkerGroup) {
        markerGroup = group
        MarkerType.byGroup(group, mapKind).firstOrNull()?.let { markerType = it }
    }

    fun selectStage(newStage: MapStage) {
        stage = newStage
        tool = defaultToolFor(newStage)
        selection = null
        draft.clear()
        markerGroup = when (newStage) {
            Stage.NATURE -> MarkerGroup.NATURE
            Stage.SETTLEMENTS -> MarkerGroup.SETTLEMENT
            Stage.SPECIAL -> MarkerGroup.MAGIC
            CityStage.WALLS -> MarkerGroup.CITY_WALLS
            CityStage.DETAILS -> MarkerGroup.CITY_STREET
            CityStage.GREEN -> MarkerGroup.CITY_SERVICE
            CityStage.BUILDINGS -> MarkerGroup.CITY_SPECIAL
            else -> markerGroup
        }
        markerGroup = ensureGroupFits(markerGroup)
        markerType = MarkerType.byGroup(markerGroup, mapKind).firstOrNull() ?: markerType
        if (newStage == Stage.SPECIAL) markerType = MarkerType.WIZARD_TOWER
        if (newStage == Stage.SETTLEMENTS) markerType = MarkerType.CITY
        if (newStage == Stage.NATURE) markerType = MarkerType.MOUNTAIN_PEAK
        when (newStage) {
            CityStage.GROUND -> lineType = LineFeatureType.RIVER
            CityStage.WALLS -> {
                lineType = LineFeatureType.CITY_WALL
                markerType = MarkerType.CITY_MAIN_GATE
            }
            CityStage.STREETS -> roadType = RoadType.MAIN_STREET
            CityStage.GREEN -> biome = BiomeType.CITY_PARK
            CityStage.DETAILS -> markerType = MarkerType.CITY_FOUNTAIN
            BattleStage.GROUND -> biome = BiomeType.GRASS_GROUND
            BattleStage.WALLS -> {
                lineType = LineFeatureType.DUNGEON_WALL
                markerGroup = MarkerGroup.BATTLE_DOORS
                markerType = MarkerType.B_DOOR
            }
            BattleStage.PROPS -> {
                markerGroup = MarkerGroup.BATTLE_FURNITURE
                markerType = MarkerType.B_TABLE
            }
            BattleStage.TRAPS -> {
                markerGroup = MarkerGroup.BATTLE_TRAPS
                markerType = MarkerType.B_SPIKE_TRAP
            }
            BattleStage.ENEMIES -> {
                tokenGroup = TokenGroup.GREENSKINS
                tokenType = TokenType.GOBLIN
            }
            BattleStage.HEROES -> {
                tokenGroup = TokenGroup.HEROES
                tokenType = TokenType.HERO_FIGHTER
            }
            BattleStage.FOG -> {
                markerGroup = MarkerGroup.BATTLE_TACTICS
                markerType = MarkerType.B_LIGHT
            }
            else -> Unit
        }
        editQuiet { it.copy(stage = newStage.number) }
        scheduleSave()
    }

    fun toolsFor(currentStage: MapStage): List<Tool> = when (currentStage) {
        is CityStage -> cityToolsFor(currentStage)
        is BattleStage -> battleToolsFor(currentStage)
        else -> worldToolsFor(currentStage)
    }

    private fun battleToolsFor(currentStage: BattleStage): List<Tool> = when (currentStage) {
        BattleStage.GROUND -> listOf(Tool.PAN, Tool.BIOME, Tool.SELECT, Tool.ERASER, Tool.RULER, Tool.FRAGMENT)
        BattleStage.WALLS -> listOf(Tool.PAN, Tool.LINE, Tool.MARKER, Tool.SELECT, Tool.ERASER, Tool.RULER)
        BattleStage.PROPS -> listOf(Tool.PAN, Tool.MARKER, Tool.SELECT, Tool.ERASER, Tool.RULER)
        BattleStage.TRAPS -> listOf(Tool.PAN, Tool.MARKER, Tool.SELECT, Tool.ERASER)
        BattleStage.ENEMIES -> listOf(Tool.PAN, Tool.TOKEN, Tool.SELECT, Tool.ERASER, Tool.RULER)
        BattleStage.HEROES -> listOf(Tool.PAN, Tool.TOKEN, Tool.SELECT, Tool.ERASER, Tool.RULER)
        BattleStage.FOG -> listOf(Tool.PAN, Tool.FOG, Tool.MARKER, Tool.SELECT, Tool.ERASER)
        BattleStage.SCENE -> listOf(Tool.SELECT, Tool.PAN, Tool.RULER, Tool.TOKEN, Tool.LABEL, Tool.ERASER)
    }

    private fun cityToolsFor(currentStage: CityStage): List<Tool> = when (currentStage) {
        CityStage.GROUND -> listOf(Tool.PAN, Tool.LAND, Tool.WATER, Tool.LINE, Tool.SELECT, Tool.ERASER, Tool.FRAGMENT)
        CityStage.WALLS -> listOf(Tool.PAN, Tool.LINE, Tool.MARKER, Tool.SELECT, Tool.ERASER)
        CityStage.DISTRICTS -> listOf(Tool.PAN, Tool.DISTRICT, Tool.SELECT, Tool.ERASER, Tool.FRAGMENT)
        CityStage.STREETS -> listOf(Tool.PAN, Tool.ROAD, Tool.LABEL, Tool.SELECT, Tool.ERASER)
        CityStage.BUILDINGS -> listOf(Tool.PAN, Tool.BUILDING, Tool.SELECT, Tool.ERASER, Tool.FRAGMENT)
        CityStage.GREEN -> listOf(Tool.PAN, Tool.BIOME, Tool.SELECT, Tool.ERASER)
        CityStage.DETAILS -> listOf(Tool.PAN, Tool.MARKER, Tool.LABEL, Tool.SELECT, Tool.ERASER)
        CityStage.CITY_INFO -> listOf(Tool.PAN, Tool.LABEL, Tool.SELECT)
    }

    private fun worldToolsFor(currentStage: MapStage): List<Tool> = when (currentStage) {
        Stage.CONTINENTS -> listOf(Tool.PAN, Tool.LAND, Tool.ISLAND, Tool.WATER, Tool.SELECT, Tool.ERASER, Tool.FRAGMENT)
        Stage.BIOMES -> listOf(Tool.PAN, Tool.BIOME, Tool.SELECT, Tool.ERASER, Tool.FRAGMENT)
        Stage.NATURE -> listOf(Tool.PAN, Tool.LINE, Tool.MARKER, Tool.LABEL, Tool.SELECT, Tool.ERASER, Tool.FRAGMENT)
        Stage.SETTLEMENTS -> listOf(Tool.PAN, Tool.MARKER, Tool.ROAD, Tool.LABEL, Tool.SELECT, Tool.ERASER, Tool.FRAGMENT)
        Stage.CAPITALS -> listOf(Tool.PAN, Tool.SELECT, Tool.MARKER)
        Stage.SPECIAL -> listOf(Tool.PAN, Tool.MARKER, Tool.LABEL, Tool.SELECT, Tool.ERASER, Tool.FRAGMENT)
        Stage.BORDERS -> listOf(Tool.PAN, Tool.COUNTRY, Tool.SELECT, Tool.ERASER, Tool.FRAGMENT)
        else -> listOf(Tool.PAN, Tool.SELECT)
    }

    private fun defaultToolFor(currentStage: MapStage): Tool = when (currentStage) {
        BattleStage.GROUND -> Tool.BIOME
        BattleStage.WALLS -> Tool.LINE
        BattleStage.PROPS -> Tool.MARKER
        BattleStage.TRAPS -> Tool.MARKER
        BattleStage.ENEMIES -> Tool.TOKEN
        BattleStage.HEROES -> Tool.TOKEN
        BattleStage.FOG -> Tool.FOG
        BattleStage.SCENE -> Tool.SELECT
        CityStage.GROUND -> Tool.LAND
        CityStage.WALLS -> Tool.LINE
        CityStage.DISTRICTS -> Tool.DISTRICT
        CityStage.STREETS -> Tool.ROAD
        CityStage.BUILDINGS -> Tool.BUILDING
        CityStage.GREEN -> Tool.BIOME
        CityStage.DETAILS -> Tool.MARKER
        CityStage.CITY_INFO -> Tool.LABEL
        Stage.CONTINENTS -> Tool.LAND
        Stage.BIOMES -> Tool.BIOME
        Stage.NATURE -> Tool.LINE
        Stage.SETTLEMENTS -> Tool.MARKER
        Stage.CAPITALS -> Tool.SELECT
        Stage.SPECIAL -> Tool.MARKER
        Stage.BORDERS -> Tool.COUNTRY
        else -> Tool.PAN
    }

    /** Рисуется ли текущим инструментом замкнутая область. */
    fun toolDrawsArea(): Boolean =
        tool == Tool.LAND || tool == Tool.ISLAND || tool == Tool.WATER ||
            tool == Tool.BIOME || tool == Tool.COUNTRY || tool == Tool.DISTRICT || tool == Tool.FOG

    fun toolDrawsLine(): Boolean =
        tool == Tool.LINE || tool == Tool.ROAD || (tool == Tool.LABEL && labelCurved) || tool == Tool.RULER

    /** Замкнут ли рисуемый сейчас контур (область или рамка фрагмента). */
    fun draftClosed(): Boolean = toolDrawsArea() || tool == Tool.FRAGMENT || tool == Tool.BUILDING

    fun draftColor(): Int = when (tool) {
        Tool.LAND, Tool.ISLAND -> 0xFF8A6A3A.toInt()
        Tool.WATER -> 0xFF2E6E93.toInt()
        Tool.BIOME -> biome.color
        Tool.COUNTRY -> project?.countryById(activeCountryId)?.color ?: 0xFFB03A2E.toInt()
        Tool.LINE -> lineType.color
        Tool.ROAD -> roadType.color
        Tool.FRAGMENT -> 0xFF2E6E93.toInt()
        Tool.BUILDING -> buildingType.color
        Tool.DISTRICT -> districtType.color
        Tool.FOG -> 0xFF2A2F3A.toInt()
        Tool.RULER -> 0xFFFFC400.toInt()
        else -> 0xFF9B2C2C.toInt()
    }

    // ------------------------------------------------------------- камера

    fun onViewSize(width: Float, height: Float) {
        val first = viewWidth == 0f
        viewWidth = width
        viewHeight = height
        if (first) project?.let { fitToView(width, height) }
    }

    fun fitToView(width: Float = viewWidth, height: Float = viewHeight) {
        val current = project ?: return
        if (width <= 0f || height <= 0f) return
        camera = Camera.fit(current.worldWidth, current.worldHeight, width, height)
    }

    fun pan(dx: Float, dy: Float) {
        camera = camera.panned(dx, dy)
    }

    fun zoom(factor: Float, focusX: Float, focusY: Float) {
        camera = camera.zoomed(factor, focusX, focusY)
    }

    fun zoomBy(factor: Float) {
        camera = camera.zoomed(factor, viewWidth / 2f, viewHeight / 2f)
    }

    // ------------------------------------------------------------- рисование

    fun startStroke(world: Vec) {
        if (tool == Tool.SELECT) {
            val hit = hitTest(world)
            selection = hit
            draggingSelection = if (
                hit is Selection.MarkerSel || hit is Selection.LabelSel || hit is Selection.TokenSel
            ) hit else null
            if (draggingSelection != null) pushHistoryForDrag()
            return
        }
        if (tool == Tool.FRAGMENT || tool == Tool.BUILDING) {
            fragmentStart = world
            draft.clear()
            draft.add(world)
            return
        }
        if (!toolDrawsArea() && !toolDrawsLine()) return
        draft.clear()
        draft.add(world)
    }

    fun extendStroke(world: Vec) {
        val dragging = draggingSelection
        if (dragging != null) {
            moveSelected(dragging, world)
            return
        }
        if (tool == Tool.RULER) {
            if (draft.isEmpty()) return
            val first = draft.first()
            draft.clear()
            draft.add(first)
            draft.add(world)
            rulerText = measure(first, world)
            return
        }
        val start = fragmentStart
        if (start != null) {
            draft.clear()
            draft.add(Vec(start.x, start.y))
            draft.add(Vec(world.x, start.y))
            draft.add(Vec(world.x, world.y))
            draft.add(Vec(start.x, world.y))
            return
        }
        if (draft.isEmpty()) return
        val minDistance = max(1.5f, 3.5f / camera.scale)
        if (draft.last().distanceTo(world) >= minDistance) draft.add(world)
    }

    fun finishStroke() {
        val dragged = draggingSelection
        if (dragged != null) {
            if (dragged is Selection.TokenSel) snapToken(dragged.id)
            draggingSelection = null
            scheduleSave()
            return
        }
        if (tool == Tool.RULER) {
            val text = rulerText
            draft.clear()
            rulerText = null
            if (text != null) message = "Расстояние: $text"
            return
        }
        val start = fragmentStart
        if (start != null) {
            val corners = draft.toList()
            fragmentStart = null
            draft.clear()
            if (tool == Tool.BUILDING) {
                if (corners.size >= 4) {
                    val bounds = Geometry.bounds(corners)
                    if (bounds.width > 2f && bounds.height > 2f) {
                        addBuilding(rectangle(bounds))
                        return
                    }
                }
                addBuilding(defaultFootprint(start))
                return
            }
            if (corners.size < 4) {
                message = "Обведите прямоугольник вокруг нужного куска карты"
                return
            }
            val bounds = Geometry.bounds(corners)
            val current = project
            val minSide = if (current == null) 40f else min(current.worldWidth, current.worldHeight) * 0.03f
            if (bounds.width < minSide || bounds.height < minSide) {
                message = "Слишком маленький кусок — выделите область побольше"
                return
            }
            fragmentRect = bounds
            return
        }
        if (draft.isEmpty()) return
        val points = draft.toList()
        draft.clear()
        commitStroke(points)
    }

    fun cancelStroke() {
        draft.clear()
        rulerText = null
        draggingSelection = null
        fragmentStart = null
    }

    /** Отменить выделение фрагмента. */
    fun cancelFragment() {
        fragmentRect = null
        fragmentStart = null
        draft.clear()
    }

    /**
     * Создать отдельную карту из выделенного фрагмента.
     * Исходная карта не меняется — фрагмент именно копируется.
     */
    fun createMapFromFragment(name: String, targetLongSide: Float, placeLink: Boolean) {
        val current = project ?: return
        val rect = fragmentRect ?: return
        fragmentRect = null
        viewModelScope.launch {
            busy = true
            val fragment = withContext(Dispatchers.Default) {
                FragmentCopy.create(current, rect, name, targetLongSide)
            }
            // На исходной карте можно оставить метку, ведущую на новую карту.
            val source = if (placeLink) {
                current.copy(
                    markers = current.markers + Marker(
                        type = MarkerType.MAP_LINK,
                        name = fragment.name,
                        pos = Vec(rect.centerX, rect.centerY),
                        linkedProjectId = fragment.id
                    ),
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                current
            }
            withContext(Dispatchers.IO) {
                store.save(source)
                store.save(fragment)
            }
            busy = false
            refreshProjects()
            openProject(fragment)
            message = "Карта «${fragment.name}» создана из фрагмента"
        }
    }

    /** Вставить другую карту в выделенную область этой карты. */
    fun insertMapIntoArea(sourceId: String) {
        val current = project ?: return
        val rect = fragmentRect ?: return
        fragmentRect = null
        viewModelScope.launch {
            busy = true
            val source = withContext(Dispatchers.IO) { store.load(sourceId) }
            if (source == null) {
                busy = false
                message = "Не удалось открыть карту для вставки"
                return@launch
            }
            val merged = withContext(Dispatchers.Default) {
                FragmentCopy.insertInto(current, source, rect)
            }
            busy = false
            edit { merged }
            message = "Карта «${source.name}» вставлена в область"
        }
    }

    /** Привязать к объекту подробную карту или снять привязку. */
    fun setMarkerLink(markerId: String, projectId: String?) {
        edit { state ->
            state.copy(
                markers = state.markers.map {
                    if (it.id == markerId) it.copy(linkedProjectId = projectId) else it
                }
            )
        }
    }

    /** Перейти на карту, связанную с объектом. */
    fun openLinkedMap(markerId: String) {
        val current = project ?: return
        val marker = current.markers.firstOrNull { it.id == markerId } ?: return
        val target = marker.linkedProjectId
        if (target == null) {
            message = "К объекту не привязана карта"
            return
        }
        saveNow()
        openProject(target)
    }

    private fun commitStroke(rawPoints: List<Vec>) {
        project ?: return
        val tolerance = max(1.5f, 2.5f / camera.scale)

        if (toolDrawsArea()) {
            if (rawPoints.size < 4) {
                if (rawPoints.size >= 2) message = "Обведите область целиком, не отрывая палец"
                return
            }
            val simplified = Geometry.simplify(rawPoints, tolerance)
            if (simplified.size < 3) return
            val smooth = Geometry.smoothClosed(simplified, 2)
            if (Geometry.area(smooth) < 40f) {
                message = "Область слишком мала"
                return
            }
            when (tool) {
                Tool.LAND -> edit { it.copy(landmasses = it.landmasses + Landmass(kind = LandKind.CONTINENT, points = smooth)) }
                Tool.ISLAND -> edit { it.copy(landmasses = it.landmasses + Landmass(kind = LandKind.ISLAND, points = smooth)) }
                Tool.WATER -> edit { it.copy(waters = it.waters + WaterBody(kind = waterKind, points = smooth)) }
                Tool.BIOME -> edit {
                    val asset = customZone
                    it.copy(
                        biomes = it.biomes + BiomeRegion(
                            biome = biome,
                            name = if (asset != null) asset.title else "",
                            points = smooth,
                            assetId = asset?.id
                        )
                    )
                }
                Tool.COUNTRY -> addCountryArea(smooth)
                Tool.DISTRICT -> edit {
                    it.copy(districts = it.districts + District(type = districtType, points = smooth))
                }
                Tool.FOG -> edit { it.copy(fog = it.fog + FogArea(points = smooth)) }
                else -> Unit
            }
            return
        }

        if (toolDrawsLine()) {
            if (rawPoints.size < 3) {
                if (rawPoints.size == 2) message = "Проведите линию, не отрывая палец"
                return
            }
            val simplified = Geometry.simplify(rawPoints, tolerance)
            if (simplified.size < 2) return
            val smooth = Geometry.smoothOpen(simplified, 2)
            when (tool) {
                Tool.LINE -> edit {
                    // Стены боевой локации — ровные отрезки по узлам сетки.
                    val points = if (lineType.battle) wallPoints(rawPoints) else smooth
                    if (points.size < 2) it else it.copy(lines = it.lines + LineFeature(type = lineType, points = points))
                }
                Tool.ROAD -> edit { it.copy(roads = it.roads + Road(type = roadType, points = smooth)) }
                Tool.LABEL -> {
                    val middle = smooth[smooth.size / 2]
                    val label = MapLabel(text = "Название", pos = middle, style = labelStyle, path = smooth)
                    edit { it.copy(labels = it.labels + label) }
                    selection = Selection.LabelSel(label.id)
                    pendingLabelEdit = label.id
                }
                else -> Unit
            }
            return
        }
    }

    private fun addCountryArea(area: List<Vec>) {
        val current = project ?: return
        val existing = current.countries.firstOrNull { it.id == activeCountryId }
        if (existing == null) {
            val created = createCountryInternal("Новое королевство").copy(areas = listOf(area))
            edit { it.copy(countries = it.countries + created) }
            activeCountryId = created.id
        } else {
            edit { state ->
                state.copy(
                    countries = state.countries.map {
                        if (it.id == existing.id) it.copy(areas = it.areas + listOf(area)) else it
                    }
                )
            }
        }
    }

    private fun pushHistoryForDrag() {
        val current = project ?: return
        undoStack.addLast(current)
        while (undoStack.size > HISTORY_LIMIT) undoStack.removeFirst()
        redoStack.clear()
        canUndo = true
        canRedo = false
    }

    private fun moveSelected(target: Selection, world: Vec) {
        when (target) {
            is Selection.MarkerSel -> editQuiet { state ->
                state.copy(markers = state.markers.map { if (it.id == target.id) it.copy(pos = world) else it })
            }
            is Selection.LabelSel -> editQuiet { state ->
                state.copy(labels = state.labels.map { if (it.id == target.id) it.copy(pos = world) else it })
            }
            is Selection.TokenSel -> editQuiet { state ->
                state.copy(tokens = state.tokens.map { if (it.id == target.id) it.copy(pos = world) else it })
            }
            else -> Unit
        }
    }

    // ------------------------------------------------------------- касание

    /** Одиночное касание карты. Возвращает true, если нужно открыть карточку объекта. */
    fun tap(world: Vec): Boolean {
        when (tool) {
            Tool.MARKER -> {
                val asset = customObject
                val marker = Marker(
                    type = markerType,
                    pos = world,
                    countryId = activeCountryId,
                    name = if (asset != null) asset.title else "",
                    assetId = asset?.id
                )
                edit { it.copy(markers = it.markers + marker) }
                selection = Selection.MarkerSel(marker.id)
                return true
            }
            Tool.LABEL -> {
                val label = MapLabel(text = "Название", pos = world, style = labelStyle)
                edit { it.copy(labels = it.labels + label) }
                selection = Selection.LabelSel(label.id)
                return true
            }
            Tool.BUILDING -> {
                addBuilding(defaultFootprint(world))
                return false
            }
            Tool.TOKEN -> {
                placeToken(world)
                return false
            }
            Tool.ERASER -> {
                val hit = hitTest(world)
                if (hit != null) {
                    deleteObject(hit)
                    selection = null
                } else {
                    message = "Здесь нечего стирать"
                }
                return false
            }
            Tool.SELECT -> {
                val hit = hitTest(world)
                selection = hit
                if (stage == Stage.CAPITALS && hit is Selection.MarkerSel) {
                    promoteToCapital(hit.id)
                    return false
                }
                return hit != null
            }
            else -> return false
        }
    }

    /** Поиск объекта под пальцем: сверху вниз по слоям. */
    fun hitTest(world: Vec): Selection? {
        val current = project ?: return null
        val style = current.style
        val tolerance = max(6f, 16f / camera.scale)

        fun open(layer: MapLayer) = layer.visible(style) && !layer.locked(style)

        fun fogAt(): Selection? {
            if (!open(MapLayer.FOG)) return null
            return current.fog.asReversed()
                .firstOrNull { Geometry.pointInPolygon(world, it.points) }
                ?.let { Selection.FogSel(it.id) }
        }

        if (stage == BattleStage.FOG) fogAt()?.let { return it }

        if (open(MapLayer.TOKENS)) current.tokens
            .filter { it.pos.distanceTo(world) <= max(tolerance, it.size.cells * current.gridCell * 0.5f) }
            .minByOrNull { it.pos.distanceTo(world) }
            ?.let { return Selection.TokenSel(it.id) }

        if (open(MapLayer.MARKERS)) current.markers
            .filter { it.pos.distanceTo(world) <= tolerance * 1.3f }
            .minByOrNull { it.pos.distanceTo(world) }
            ?.let { return Selection.MarkerSel(it.id) }

        if (open(MapLayer.LABELS)) current.labels
            .filter { it.pos.distanceTo(world) <= tolerance * 1.6f }
            .minByOrNull { it.pos.distanceTo(world) }
            ?.let { return Selection.LabelSel(it.id) }

        if (open(MapLayer.ROADS)) current.roads
            .filter { Geometry.distanceToPolyline(world, it.points) <= tolerance }
            .minByOrNull { Geometry.distanceToPolyline(world, it.points) }
            ?.let { return Selection.RoadSel(it.id) }

        if (open(MapLayer.BUILDINGS)) current.buildings.asReversed()
            .firstOrNull { Geometry.pointInPolygon(world, it.points) }
            ?.let { return Selection.BuildingSel(it.id) }

        if (open(MapLayer.LINES)) current.lines
            .filter { Geometry.distanceToPolyline(world, it.points) <= tolerance }
            .minByOrNull { Geometry.distanceToPolyline(world, it.points) }
            ?.let { return Selection.Line(it.id) }

        if (open(MapLayer.COUNTRIES)) for (country in current.countries.asReversed()) {
            for ((index, area) in country.areas.withIndex()) {
                if (Geometry.distanceToPolygonOutline(world, area) <= tolerance) {
                    return Selection.CountryArea(country.id, index)
                }
            }
        }

        if (open(MapLayer.DISTRICTS)) current.districts.asReversed()
            .firstOrNull { Geometry.pointInContours(world, it.contours()) }
            ?.let { return Selection.DistrictSel(it.id) }

        if (open(MapLayer.BIOMES)) current.biomes.asReversed()
            .firstOrNull { Geometry.pointInContours(world, it.contours()) }
            ?.let { return Selection.Biome(it.id) }

        if (open(MapLayer.WATER)) current.waters.asReversed()
            .firstOrNull { Geometry.pointInPolygon(world, it.points) }
            ?.let { return Selection.Water(it.id) }

        if (open(MapLayer.LAND)) current.landmasses.asReversed()
            .firstOrNull { Geometry.pointInPolygon(world, it.points) }
            ?.let { return Selection.Land(it.id) }

        if (current.kind == MapKind.BATTLE) fogAt()?.let { return it }

        if (open(MapLayer.COUNTRIES)) for (country in current.countries.asReversed()) {
            for ((index, area) in country.areas.withIndex()) {
                if (Geometry.pointInPolygon(world, area)) return Selection.CountryArea(country.id, index)
            }
        }
        return null
    }

    // ------------------------------------------------------------- изменение объектов

    fun deleteSelection() {
        val target = selection ?: return
        deleteObject(target)
        selection = null
    }

    private fun deleteObject(target: Selection) {
        edit { state ->
            when (target) {
                is Selection.Land -> state.copy(landmasses = state.landmasses.filterNot { it.id == target.id })
                is Selection.Water -> state.copy(waters = state.waters.filterNot { it.id == target.id })
                is Selection.Biome -> state.copy(biomes = state.biomes.filterNot { it.id == target.id })
                is Selection.Line -> state.copy(lines = state.lines.filterNot { it.id == target.id })
                is Selection.RoadSel -> state.copy(roads = state.roads.filterNot { it.id == target.id })
                is Selection.MarkerSel -> state.copy(markers = state.markers.filterNot { it.id == target.id })
                is Selection.LabelSel -> state.copy(labels = state.labels.filterNot { it.id == target.id })
                is Selection.BuildingSel -> state.copy(buildings = state.buildings.filterNot { it.id == target.id })
                is Selection.DistrictSel -> state.copy(districts = state.districts.filterNot { it.id == target.id })
                is Selection.TokenSel -> state.copy(tokens = state.tokens.filterNot { it.id == target.id })
                is Selection.FogSel -> state.copy(fog = state.fog.filterNot { it.id == target.id })
                is Selection.CountryArea -> state.copy(
                    countries = state.countries.map { country ->
                        if (country.id == target.id) {
                            country.copy(areas = country.areas.filterIndexed { i, _ -> i != target.index })
                        } else {
                            country
                        }
                    }
                )
            }
        }
    }

    fun updateMarker(marker: Marker) {
        edit { state -> state.copy(markers = state.markers.map { if (it.id == marker.id) marker else it }) }
    }

    fun updateLabel(label: MapLabel) {
        edit { state -> state.copy(labels = state.labels.map { if (it.id == label.id) label else it }) }
    }

    fun renameSelected(name: String) {
        val target = selection ?: return
        edit { state ->
            when (target) {
                is Selection.Land -> state.copy(landmasses = state.landmasses.map { if (it.id == target.id) it.copy(name = name) else it })
                is Selection.Water -> state.copy(waters = state.waters.map { if (it.id == target.id) it.copy(name = name) else it })
                is Selection.Biome -> state.copy(biomes = state.biomes.map { if (it.id == target.id) it.copy(name = name) else it })
                is Selection.Line -> state.copy(lines = state.lines.map { if (it.id == target.id) it.copy(name = name) else it })
                is Selection.RoadSel -> state.copy(roads = state.roads.map { if (it.id == target.id) it.copy(name = name) else it })
                is Selection.MarkerSel -> state.copy(markers = state.markers.map { if (it.id == target.id) it.copy(name = name) else it })
                is Selection.LabelSel -> state.copy(labels = state.labels.map { if (it.id == target.id) it.copy(text = name) else it })
                is Selection.BuildingSel -> state.copy(
                    buildings = state.buildings.map { if (it.id == target.id) it.copy(name = name) else it }
                )
                is Selection.DistrictSel -> state.copy(
                    districts = state.districts.map { if (it.id == target.id) it.copy(name = name) else it }
                )
                is Selection.TokenSel -> state.copy(
                    tokens = state.tokens.map { if (it.id == target.id) it.copy(name = name) else it }
                )
                is Selection.FogSel -> state
                is Selection.CountryArea -> state.copy(
                    countries = state.countries.map { if (it.id == target.id) it.copy(name = name) else it }
                )
            }
        }
    }

    // ------------------------------------------------------------- боевая локация

    /** Центр клетки (или узел сетки для фишек чётного размера). */
    fun snap(world: Vec, cells: Float = 1f): Vec {
        val current = project ?: return world
        if (current.kind != MapKind.BATTLE || !current.style.snapToGrid) return world
        val cell = current.gridCell
        if (current.gridKind == GridKind.HEX) {
            val r = cell / kotlin.math.sqrt(3f)
            val rowStep = r * 1.5f
            var best = world
            var bestDistance = Float.MAX_VALUE
            val baseRow = kotlin.math.floor((world.y - r) / rowStep).toInt()
            for (row in baseRow - 1..baseRow + 1) {
                val offset = if (row % 2 != 0) cell / 2f else 0f
                val col = kotlin.math.round((world.x - offset - cell / 2f) / cell).toInt()
                for (c in col - 1..col + 1) {
                    val center = Vec(c * cell + offset + cell / 2f, row * rowStep + r)
                    val distance = center.distanceTo(world)
                    if (distance < bestDistance) {
                        bestDistance = distance
                        best = center
                    }
                }
            }
            return best
        }
        val even = cells >= 2f && cells.toInt() % 2 == 0
        return if (even) {
            Vec(kotlin.math.round(world.x / cell) * cell, kotlin.math.round(world.y / cell) * cell)
        } else {
            Vec(
                kotlin.math.floor(world.x / cell) * cell + cell / 2f,
                kotlin.math.floor(world.y / cell) * cell + cell / 2f
            )
        }
    }

    /** Узел сетки — к нему липнут концы стен. */
    private fun snapCorner(world: Vec): Vec {
        val current = project ?: return world
        if (!current.style.snapToGrid || current.gridKind == GridKind.NONE) return world
        val half = current.gridCell / 2f
        return Vec(kotlin.math.round(world.x / half) * half, kotlin.math.round(world.y / half) * half)
    }

    /** Штрих стены превращается в ломаную по узлам сетки. */
    private fun wallPoints(raw: List<Vec>): List<Vec> {
        val current = project ?: return raw
        val simplified = Geometry.simplify(raw, current.gridCell * 0.35f)
        val snapped = ArrayList<Vec>()
        for (point in simplified) {
            val p = snapCorner(point)
            if (snapped.isEmpty() || snapped.last().distanceTo(p) > 0.5f) snapped.add(p)
        }
        return snapped
    }

    /** Расстояние по правилам настолки: диагональ считается как одна клетка. */
    private fun measure(from: Vec, to: Vec): String {
        val current = project ?: return ""
        val cell = current.gridCell
        val cells = if (current.gridKind == GridKind.SQUARE) {
            max(kotlin.math.abs(to.x - from.x), kotlin.math.abs(to.y - from.y)) / cell
        } else {
            from.distanceTo(to) / cell
        }
        val rounded = kotlin.math.round(cells).toInt()
        return "${rounded * current.feetPerCell} фт · $rounded кл."
    }

    private fun placeToken(world: Vec) {
        val current = project ?: return
        val type = tokenType
        val asset = customToken
        val sameType = current.tokens.count { it.type == type && it.assetId == asset?.id }
        val baseName = asset?.title ?: type.title
        val token = Token(
            type = type,
            name = if (sameType == 0) baseName else "$baseName ${sameType + 1}",
            pos = snap(world, type.size.cells),
            size = type.size,
            faction = type.faction,
            hp = type.hp,
            maxHp = type.hp,
            ac = type.ac,
            assetId = asset?.id
        )
        edit { it.copy(tokens = it.tokens + token) }
        selection = Selection.TokenSel(token.id)
    }

    private fun snapToken(id: String) {
        editQuiet { state ->
            state.copy(tokens = state.tokens.map { if (it.id == id) it.copy(pos = snap(it.pos, it.size.cells)) else it })
        }
    }

    fun selectTokenType(type: TokenType) {
        tokenType = type
        customToken = null
    }

    fun updateToken(token: Token) {
        edit { state -> state.copy(tokens = state.tokens.map { if (it.id == token.id) token else it }) }
    }

    /** Урон или лечение: delta < 0 — урон. */
    fun changeHp(id: String, delta: Int) {
        edit { state ->
            state.copy(tokens = state.tokens.map {
                if (it.id == id) it.copy(hp = (it.hp + delta).coerceIn(0, max(it.maxHp, 0))) else it
            })
        }
    }

    fun toggleCondition(id: String, condition: Condition) {
        edit { state ->
            state.copy(tokens = state.tokens.map {
                if (it.id != id) it
                else if (condition in it.conditions) it.copy(conditions = it.conditions - condition)
                else it.copy(conditions = it.conditions + condition)
            })
        }
    }

    fun duplicateToken(id: String) {
        val current = project ?: return
        val original = current.tokens.firstOrNull { it.id == id } ?: return
        val same = current.tokens.count { it.type == original.type }
        val copy = original.copy(
            id = java.util.UUID.randomUUID().toString(),
            name = "${original.type.title} ${same + 1}",
            pos = snap(Vec(original.pos.x + current.gridCell, original.pos.y), original.size.cells),
            hp = original.maxHp,
            conditions = emptyList(),
            initiative = null
        )
        edit { it.copy(tokens = it.tokens + copy) }
        selection = Selection.TokenSel(copy.id)
    }

    /** Порядок ходов: по убыванию инициативы, у кого её нет — в конце. */
    fun initiativeOrder(): List<Token> {
        val current = project ?: return emptyList()
        return current.tokens
            .filter { it.initiative != null && !it.dead }
            .sortedWith(compareByDescending<Token> { it.initiative }.thenBy { it.title })
    }

    /** Фишка, чей сейчас ход. */
    fun activeTokenId(): String? {
        val current = project ?: return null
        val order = initiativeOrder()
        if (order.isEmpty()) return null
        return order[current.scene.turn.mod(order.size)].id
    }

    /** Бросить инициативу (к20) всем, у кого её ещё нет. */
    fun rollInitiative(onlyMissing: Boolean) {
        val random = kotlin.random.Random(System.nanoTime())
        edit { state ->
            state.copy(
                tokens = state.tokens.map {
                    if (onlyMissing && it.initiative != null) it
                    else it.copy(initiative = random.nextInt(1, 21))
                },
                scene = state.scene.copy(round = 1, turn = 0)
            )
        }
        message = "Инициатива брошена"
    }

    fun setInitiative(id: String, value: Int?) {
        edit { state -> state.copy(tokens = state.tokens.map { if (it.id == id) it.copy(initiative = value) else it }) }
    }

    fun nextTurn() {
        val current = project ?: return
        val order = initiativeOrder()
        if (order.isEmpty()) {
            message = "Сначала бросьте инициативу"
            return
        }
        var turn = current.scene.turn + 1
        var round = current.scene.round
        if (turn >= order.size) {
            turn = 0
            round++
        }
        edit { it.copy(scene = it.scene.copy(turn = turn, round = round)) }
        val active = order[turn]
        selection = Selection.TokenSel(active.id)
        message = "Раунд $round · ходит ${active.title}"
    }

    fun endCombat() {
        edit { state ->
            state.copy(
                tokens = state.tokens.map { it.copy(initiative = null) },
                scene = state.scene.copy(round = 1, turn = 0)
            )
        }
    }

    fun updateScene(scene: SceneInfo) {
        edit { it.copy(scene = scene) }
    }

    fun setGround(ground: BiomeType) {
        edit { it.copy(groundBiome = ground) }
    }

    fun setGrid(kind: GridKind, feet: Int, opacity: Float) {
        edit {
            it.copy(
                gridKind = kind,
                feetPerCell = feet.coerceIn(1, 100),
                style = it.style.copy(gridOpacity = opacity.coerceIn(0f, 1f))
            )
        }
    }

    /** Вся карта — суша или океан. На суше моря и озёра рисуются инструментом воды. */
    fun toggleLandBase() {
        edit { it.copy(landBase = !it.landBase) }
        message = if (project?.landBase == true) {
            "Вся карта теперь суша — моря и озёра рисуйте инструментом «Озеро / море»"
        } else {
            "Карта снова океан — материки рисуются инструментом «Континент»"
        }
    }

    fun togglePlayerView() {
        edit { it.copy(style = it.style.copy(playerView = !it.style.playerView)) }
        message = if (project?.style?.playerView == true) "Вид для игроков: туман сплошной, тайное скрыто"
        else "Вид мастера"
    }

    fun toggleSnap() {
        edit { it.copy(style = it.style.copy(snapToGrid = !it.style.snapToGrid)) }
    }

    fun togglePhotoTextures() {
        edit { it.copy(style = it.style.copy(photoTextures = !it.style.photoTextures)) }
    }

    /** Убрать весь туман разом. */
    fun clearFog() {
        edit { it.copy(fog = emptyList()) }
    }

    /** Закрыть туманом всю карту — дальше открывать по частям ластиком. */
    fun coverWithFog() {
        val current = project ?: return
        val all = FogArea(
            points = listOf(
                Vec(0f, 0f), Vec(current.worldWidth, 0f),
                Vec(current.worldWidth, current.worldHeight), Vec(0f, current.worldHeight)
            )
        )
        edit { it.copy(fog = it.fog + all) }
    }

    /**
     * Бросок кубиков: count × кD sides + modifier.
     * advantage: 1 — с преимуществом, -1 — с помехой (для одиночного к20).
     */
    fun rollDice(count: Int, sides: Int, modifier: Int, advantage: Int = 0): String {
        val random = kotlin.random.Random(System.nanoTime())
        val safeCount = count.coerceIn(1, 20)
        val text = if (sides == 20 && safeCount == 1 && advantage != 0) {
            val first = random.nextInt(1, 21)
            val second = random.nextInt(1, 21)
            val kept = if (advantage > 0) max(first, second) else min(first, second)
            val label = if (advantage > 0) "преимущество" else "помеха"
            val total = kept + modifier
            "к20 ($label): $first и $second → $kept${modText(modifier)} = $total" +
                (if (kept == 20) " · КРИТ!" else if (kept == 1) " · провал" else "")
        } else {
            val rolls = List(safeCount) { random.nextInt(1, sides + 1) }
            val total = rolls.sum() + modifier
            val crit = if (sides == 20 && safeCount == 1) {
                when (rolls[0]) {
                    20 -> " · КРИТ!"
                    1 -> " · провал"
                    else -> ""
                }
            } else {
                ""
            }
            "${safeCount}к$sides${modText(modifier)}: ${rolls.joinToString(" + ")}${modText(modifier)} = $total$crit"
        }
        diceLog.add(0, text)
        while (diceLog.size > 30) diceLog.removeAt(diceLog.lastIndex)
        return text
    }

    private fun modText(modifier: Int): String = when {
        modifier > 0 -> " + $modifier"
        modifier < 0 -> " − ${-modifier}"
        else -> ""
    }

    /**
     * Выровнять границы природных зон.
     * Соседние области одного ландшафта сливаются в одну; у разных ландшафтов
     * убирается наложение — лишнее вырезается у той, что нарисована раньше,
     * так что общая граница становится одной линией без двойной закраски.
     */
    fun alignBiomeBorders() {
        val current = project ?: return
        val alignBiomes = current.biomes.size >= 2
        val alignDistricts = current.districts.size >= 2
        if (!alignBiomes && !alignDistricts) {
            message = "Нужно хотя бы две зоны или два квартала"
            return
        }
        viewModelScope.launch {
            busy = true
            val minArea = max(30f, current.worldWidth * current.worldHeight * 0.00002f)
            val touchTolerance = max(3f, min(current.worldWidth, current.worldHeight) * 0.004f)
            val (zones, quarters) = withContext(Dispatchers.Default) {
                val zones = if (alignBiomes) PolygonOps.alignZones(current.biomes, minArea, touchTolerance) else null
                val quarters = if (alignDistricts) alignDistrictsOf(current.districts, minArea, touchTolerance) else null
                zones to quarters
            }
            busy = false
            if (project?.biomes !== current.biomes || project?.districts !== current.districts) {
                message = "Карта изменилась, повторите выравнивание"
                return@launch
            }
            val zonesChanged = zones?.changed == true
            val quartersChanged = quarters?.second?.changed == true
            if (!zonesChanged && !quartersChanged) {
                message = "Наложений не найдено — границы уже совпадают"
                return@launch
            }
            selection = null
            edit { state ->
                var next = state
                if (zones != null && zonesChanged) next = next.copy(biomes = zones.regions)
                if (quarters != null && quartersChanged) next = next.copy(districts = quarters.first)
                next
            }
            message = buildString {
                append("Границы выровнены")
                val merged = (zones?.merged ?: 0) + (quarters?.second?.merged ?: 0)
                val trimmed = (zones?.trimmed ?: 0) + (quarters?.second?.trimmed ?: 0)
                val removed = (zones?.removed ?: 0) + (quarters?.second?.removed ?: 0)
                if (merged > 0) append(", слито одинаковых: $merged")
                if (trimmed > 0) append(", подрезано: $trimmed")
                if (removed > 0) append(", убрано перекрытых: $removed")
                if (quartersChanged) append(" (кварталы тоже)")
            }
        }
    }

    /**
     * Выровнять кварталы теми же правилами, что и природные зоны: последний
     * нарисованный затирает прежние, соседние кварталы одного рода сливаются.
     */
    private fun alignDistrictsOf(
        districts: List<District>,
        minArea: Float,
        touchTolerance: Float
    ): Pair<List<District>, AlignResult> {
        val byId = districts.associateBy { it.id }
        // Род квартала прячется в метку заготовки — так слияние сравнит его, как вид зоны.
        val asZones = districts.map {
            BiomeRegion(
                id = it.id,
                biome = BiomeType.CITY_YARD,
                name = it.name,
                points = it.points,
                extraContours = it.extraContours,
                assetId = DISTRICT_KEY + it.type.name
            )
        }
        val result = PolygonOps.alignZones(asZones, minArea, touchTolerance)
        val back = result.regions.mapNotNull { region ->
            val original = byId[region.id] ?: return@mapNotNull null
            original.copy(name = region.name, points = region.points, extraContours = region.extraContours)
        }
        return back to result
    }

    /**
     * Поднять выбранную зону наверх: при выравнивании границ побеждает та,
     * что нанесена последней, поэтому наверху зона затирает все остальные.
     */
    fun raiseSelectedZone() {
        val quarter = selection as? Selection.DistrictSel
        if (quarter != null) {
            val district = project?.districts?.firstOrNull { it.id == quarter.id } ?: return
            edit { state -> state.copy(districts = state.districts.filterNot { it.id == quarter.id } + district) }
            message = "Квартал наверху — при выравнивании он затирает соседей"
            return
        }
        val target = selection as? Selection.Biome ?: return
        val region = project?.biomes?.firstOrNull { it.id == target.id } ?: return
        edit { state -> state.copy(biomes = state.biomes.filterNot { it.id == target.id } + region) }
        message = "Зона наверху — при выравнивании она затирает остальные"
    }

    /** Опустить выбранную зону вниз: её затрут все остальные. */
    fun lowerSelectedZone() {
        val quarter = selection as? Selection.DistrictSel
        if (quarter != null) {
            val district = project?.districts?.firstOrNull { it.id == quarter.id } ?: return
            edit { state -> state.copy(districts = listOf(district) + state.districts.filterNot { it.id == quarter.id }) }
            message = "Квартал внизу — при выравнивании его затирают соседи"
            return
        }
        val target = selection as? Selection.Biome ?: return
        val region = project?.biomes?.firstOrNull { it.id == target.id } ?: return
        edit { state -> state.copy(biomes = listOf(region) + state.biomes.filterNot { it.id == target.id }) }
        message = "Зона внизу — при выравнивании её затирают остальные"
    }

    /** Поставить здание выбранного вида. */
    private fun addBuilding(footprint: List<Vec>) {
        if (footprint.size < 3) return
        val asset = customBuilding
        val building = Building(
            type = buildingType,
            name = if (asset != null) asset.title else "",
            points = footprint,
            assetId = asset?.id
        )
        edit { it.copy(buildings = it.buildings + building) }
        selection = Selection.BuildingSel(building.id)
    }

    /** След здания по умолчанию: небольшой дом со слегка случайным поворотом. */
    private fun defaultFootprint(center: Vec): List<Vec> {
        val current = project ?: return emptyList()
        val base = min(current.worldWidth, current.worldHeight)
        val asset = customBuilding
        val scale = if (asset != null) asset.size else if (buildingType.big) 1.7f else 1f
        val unit = base * 0.016f * scale
        val width = if (asset != null) unit else unit * buildingType.shape.widthScale
        val height = if (asset != null) unit * 0.78f else unit * buildingType.shape.depthScale
        val noise = Geometry.hashNoise(center.x.toInt(), center.y.toInt(), current.style.seed)
        val angle = (noise - 0.5f) * 0.5f
        return rotatedRect(center, width, height, angle)
    }

    private fun rotatedRect(center: Vec, width: Float, height: Float, angle: Float): List<Vec> {
        val halfWidth = width / 2f
        val halfHeight = height / 2f
        val cosA = cos(angle)
        val sinA = sin(angle)
        fun corner(dx: Float, dy: Float) = Vec(
            center.x + dx * cosA - dy * sinA,
            center.y + dx * sinA + dy * cosA
        )
        return listOf(
            corner(-halfWidth, -halfHeight),
            corner(halfWidth, -halfHeight),
            corner(halfWidth, halfHeight),
            corner(-halfWidth, halfHeight)
        )
    }

    private fun rectangle(bounds: BBox): List<Vec> = listOf(
        Vec(bounds.minX, bounds.minY),
        Vec(bounds.maxX, bounds.minY),
        Vec(bounds.maxX, bounds.maxY),
        Vec(bounds.minX, bounds.maxY)
    )

    /**
     * Застроить выбранный квартал домами: ряды вдоль ближайшей улицы,
     * с оглядкой на улицы, воду и уже стоящие дома.
     */
    /** Открыто ли окно «Берега». */
    var showShoreDialog by mutableStateOf(false)

    /** Прокладывать ли улицы при застройке квартала. */
    var fillWithStreets by mutableStateOf(true)

    fun fillDistrictWithHouses() {
        val current = project ?: return
        val target = selection as? Selection.DistrictSel
        val district = target?.let { sel -> current.districts.firstOrNull { it.id == sel.id } }
        if (district == null) {
            message = "Выберите квартал инструментом «☝ Выбрать» и нажмите ещё раз"
            return
        }
        fillDistricts(current, listOf(district))
    }

    /** Застроить разом все кварталы города. */
    fun fillAllDistricts() {
        val current = project ?: return
        if (current.districts.isEmpty()) {
            message = "Сначала разметьте кварталы"
            return
        }
        fillDistricts(current, current.districts)
    }

    private fun fillDistricts(current: MapProject, targets: List<District>) {
        viewModelScope.launch {
            busy = true
            val streets = fillWithStreets
            val result = withContext(Dispatchers.Default) {
                var state = current
                var houses = 0
                var streetCount = 0
                for ((index, district) in targets.withIndex()) {
                    val seed = (System.currentTimeMillis() and 0xFFFF).toInt() + state.buildings.size + index * 131
                    val fill = CityGenerator.fillDistrict(state, district, buildDensity, seed, streets)
                    houses += fill.buildings.size
                    streetCount += fill.roads.size
                    // Сады и площади кладутся под старые зоны, чтобы ничего не затереть.
                    state = state.copy(
                        buildings = state.buildings + fill.buildings,
                        roads = state.roads + fill.roads,
                        biomes = fill.gardens + state.biomes
                    )
                }
                Triple(state, houses, streetCount)
            }
            busy = false
            val (state, houses, streetCount) = result
            if (houses == 0 && streetCount == 0) {
                message = "Дома не поместились — квартал мал или весь занят улицами"
                return@launch
            }
            edit { it.copy(buildings = state.buildings, roads = state.roads, biomes = state.biomes) }
            message = buildString {
                append("Поставлено домов: $houses")
                if (streetCount > 0) append(", улиц: $streetCount")
            }
        }
    }

    /**
     * Соединить улицы соседних кварталов на их границе. Трогаются только
     * оборванные концы у границы; дома прямо на новой связке сносятся.
     */
    fun connectDistrictStreets() {
        val current = project ?: return
        if (current.roads.size < 2) {
            message = "Сначала застройте кварталы с улицами или проведите улицы"
            return
        }
        viewModelScope.launch {
            busy = true
            val result = withContext(Dispatchers.Default) { CityGenerator.connectStreets(current) }
            busy = false
            if (result.links == 0) {
                message = "Нечего соединять: у границ кварталов нет оборванных улиц"
                return@launch
            }
            edit { state ->
                state.copy(
                    roads = result.roads,
                    buildings = state.buildings.filterNot { it.id in result.removedBuildings }
                )
            }
            message = buildString {
                append("Соединено улиц: ${result.links}")
                if (result.removedBuildings.isNotEmpty()) append(", снесено домов на пути: ${result.removedBuildings.size}")
            }
        }
    }

    /** Нарисовать берега вдоль всей воды на карте. */
    fun generateShores(type: BiomeType, widthScale: Float, replace: Boolean) {
        val current = project ?: return
        viewModelScope.launch {
            busy = true
            val seed = (System.currentTimeMillis() and 0xFFFF).toInt()
            val shores = withContext(Dispatchers.Default) {
                runCatching { ShoreGenerator.generate(current, type, widthScale, seed) }.getOrDefault(emptyList())
            }
            busy = false
            if (shores.isEmpty()) {
                message = "Воды не найдено — нарисуйте озеро, море, реку или материк в океане"
                return@launch
            }
            edit { state ->
                val kept = if (replace) state.biomes.filterNot { ShoreGenerator.isShore(it.biome) } else state.biomes
                state.copy(biomes = kept + shores)
            }
            message = "Берега готовы: ${type.title.lowercase()}"
        }
    }

    fun updateBuilding(building: Building) {
        edit { state ->
            state.copy(buildings = state.buildings.map { if (it.id == building.id) building else it })
        }
    }

    fun updateDistrict(district: District) {
        edit { state ->
            state.copy(districts = state.districts.map { if (it.id == district.id) district else it })
        }
    }

    fun changeBiomeOfSelection(newBiome: BiomeType) {
        val target = selection as? Selection.Biome ?: return
        edit { state ->
            state.copy(biomes = state.biomes.map { if (it.id == target.id) it.copy(biome = newBiome) else it })
        }
    }

    fun changeLineTypeOfSelection(newType: LineFeatureType) {
        val target = selection as? Selection.Line ?: return
        edit { state ->
            state.copy(lines = state.lines.map { if (it.id == target.id) it.copy(type = newType) else it })
        }
    }

    fun changeRoadTypeOfSelection(newType: RoadType) {
        val target = selection as? Selection.RoadSel ?: return
        edit { state ->
            state.copy(roads = state.roads.map { if (it.id == target.id) it.copy(type = newType) else it })
        }
    }

    /** Сделать выбранный объект столицей активной страны. */
    fun promoteToCapital(markerId: String) {
        val current = project ?: return
        val marker = current.markers.firstOrNull { it.id == markerId } ?: return
        val countryId = marker.countryId ?: activeCountryId
        edit { state ->
            val markers = state.markers.map {
                when {
                    it.id == markerId -> it.copy(type = MarkerType.CAPITAL, countryId = countryId)
                    countryId != null && it.countryId == countryId && it.type == MarkerType.CAPITAL ->
                        it.copy(type = MarkerType.CITY)
                    else -> it
                }
            }
            val countries = if (countryId == null) {
                state.countries
            } else {
                state.countries.map { country ->
                    if (country.id == countryId) {
                        country.copy(info = country.info.copy(capital = marker.name.ifBlank { country.info.capital }))
                    } else {
                        country
                    }
                }
            }
            state.copy(markers = markers, countries = countries)
        }
        message = if (marker.name.isBlank()) "Столица отмечена — дайте ей имя" else "«${marker.name}» — столица"
    }

    fun assignSelectedMarkerToCountry(countryId: String?) {
        val target = selection as? Selection.MarkerSel ?: return
        edit { state ->
            state.copy(markers = state.markers.map { if (it.id == target.id) it.copy(countryId = countryId) else it })
        }
    }

    // ------------------------------------------------------------- страны

    private fun createCountryInternal(name: String): Country {
        val used = project?.countries?.size ?: 0
        return Country(name = name, color = COUNTRY_COLORS[used % COUNTRY_COLORS.size])
    }

    fun addCountry(name: String): String {
        val country = createCountryInternal(name.ifBlank { "Новая страна" })
        edit { it.copy(countries = it.countries + country) }
        activeCountryId = country.id
        return country.id
    }

    fun updateCountry(country: Country) {
        // Анкета правится посимвольно, поэтому не засоряем историю отмены.
        editQuiet { state -> state.copy(countries = state.countries.map { if (it.id == country.id) country else it }) }
        scheduleSave()
    }

    fun deleteCountry(id: String) {
        edit { state ->
            state.copy(
                countries = state.countries.filterNot { it.id == id },
                markers = state.markers.map { if (it.countryId == id) it.copy(countryId = null) else it }
            )
        }
        if (activeCountryId == id) activeCountryId = project?.countries?.firstOrNull()?.id
    }

    fun updateCountryInfo(id: String, info: CountryInfo) {
        editQuiet { state -> state.copy(countries = state.countries.map { if (it.id == id) it.copy(info = info) else it }) }
        scheduleSave()
    }

    // ------------------------------------------------------------- стиль

    fun updateStyle(style: MapStyle) {
        edit { it.copy(style = style) }
    }

    /** Показать или спрятать слой. */
    fun setLayerVisible(layer: MapLayer, visible: Boolean) {
        editQuiet { it.copy(style = layer.withVisible(it.style, visible)) }
        scheduleSave()
    }

    /** Запереть слой от правки: его объекты нельзя выбрать и стереть. */
    fun setLayerLocked(layer: MapLayer, locked: Boolean) {
        editQuiet { it.copy(style = layer.withLocked(it.style, locked)) }
        scheduleSave()
        if (locked) selection = null
    }

    // ------------------------------------------------------------- помощники по области

    /** Нарисовать в выделенной области рваное побережье. */
    fun generateCoastline(islands: Boolean, roughness: Float) {
        val current = project ?: return
        val rect = fragmentRect ?: return
        fragmentRect = null
        viewModelScope.launch {
            busy = true
            val seed = (System.currentTimeMillis() and 0xFFFF).toInt() + current.landmasses.size * 17
            val shapes = withContext(Dispatchers.Default) {
                if (islands) {
                    WorldGenerator.islands(rect, 4, roughness, seed)
                } else {
                    listOf(WorldGenerator.coastline(rect, roughness, seed))
                }
            }
            busy = false
            if (shapes.isEmpty()) {
                message = "Не получилось нарисовать берег"
                return@launch
            }
            edit { state ->
                state.copy(
                    landmasses = state.landmasses + shapes.map {
                        Landmass(kind = WorldGenerator.kindFor(it, state), points = it)
                    }
                )
            }
            message = if (islands) "Острова созданы — правьте как обычную сушу" else "Материк создан"
        }
    }

    /** Провести реки от гор к ближайшей воде внутри области. */
    fun generateRivers(count: Int) {
        val current = project ?: return
        val rect = fragmentRect ?: return
        fragmentRect = null
        viewModelScope.launch {
            busy = true
            val seed = (System.currentTimeMillis() and 0xFFFF).toInt() + current.lines.size * 7
            val rivers = withContext(Dispatchers.Default) {
                WorldGenerator.rivers(current, rect, count, seed)
            }
            busy = false
            if (rivers.isEmpty()) {
                message = "Не нашлось истоков: нарисуйте в области горный хребет или вершины"
                return@launch
            }
            edit { it.copy(lines = it.lines + rivers) }
            message = "Проведено рек: ${rivers.size}"
        }
    }

    /** Разложить природные зоны по широте внутри области. */
    fun generateBiomeBands(replaceExisting: Boolean) {
        val current = project ?: return
        val rect = fragmentRect ?: return
        fragmentRect = null
        viewModelScope.launch {
            busy = true
            val seed = current.style.seed
            val bands = withContext(Dispatchers.Default) {
                WorldGenerator.biomeBands(current, rect, seed)
            }
            busy = false
            if (bands.isEmpty()) {
                message = "В области нет суши — сначала нарисуйте материк"
                return@launch
            }
            edit { state ->
                val kept = if (replaceExisting) {
                    state.biomes.filterNot { WorldGenerator.insideRect(it.points, rect) }
                } else {
                    state.biomes
                }
                state.copy(biomes = kept + bands)
            }
            message = "Разложено зон: ${bands.size}"
        }
    }

    /** Применить готовый вид карты. Отменяется стрелкой отмены. */
    fun applyStylePreset(preset: StylePreset) {
        edit { it.copy(style = preset.apply(it.style)) }
        message = "Вид карты: ${preset.title}"
    }

    // ------------------------------------------------------------- экспорт

    fun exportPng(uri: Uri, longSide: Int, withLegend: Boolean) {
        val current = project ?: return
        viewModelScope.launch {
            busy = true
            val ok = withContext(Dispatchers.IO) {
                exporter.exportPng(current, uri, longSide, withLegend)
            }
            busy = false
            message = if (ok) "Карта сохранена в PNG" else "Не удалось сохранить PNG"
        }
    }

    /** Карта в PDF: одним листом или разрезанной на листы A4 для печати. */
    fun exportPdf(uri: Uri, tilesAcross: Int, withLegend: Boolean) {
        val current = project ?: return
        viewModelScope.launch {
            busy = true
            val ok = withContext(Dispatchers.IO) {
                exporter.exportPdf(current, uri, tilesAcross, withLegend)
            }
            busy = false
            message = when {
                !ok -> "Не удалось сохранить PDF"
                tilesAcross <= 1 -> "Карта сохранена в PDF"
                else -> "PDF готов: карта разрезана на листы A4"
            }
        }
    }

    fun exportJson(uri: Uri) {
        val current = project ?: return
        viewModelScope.launch {
            busy = true
            val ok = withContext(Dispatchers.IO) { exporter.writeText(uri, store.toJsonText(current)) }
            busy = false
            message = if (ok) "Проект сохранён в файл" else "Не удалось сохранить файл"
        }
    }

    fun exportCountriesText(uri: Uri) {
        val current = project ?: return
        viewModelScope.launch {
            busy = true
            val ok = withContext(Dispatchers.IO) { exporter.writeText(uri, buildWorldReport(current)) }
            busy = false
            message = if (ok) "Описание мира сохранено" else "Не удалось сохранить файл"
        }
    }

    fun importProject(uri: Uri) {
        viewModelScope.launch {
            busy = true
            val text = withContext(Dispatchers.IO) { exporter.readText(uri) }
            val imported = if (text == null) null else withContext(Dispatchers.IO) { store.importFromText(text) }
            busy = false
            if (imported == null) {
                message = "Файл не распознан"
            } else {
                refreshProjects()
                openProject(imported)
                message = "Карта загружена"
            }
        }
    }

    private fun buildWorldReport(current: MapProject): String {
        val builder = StringBuilder()
        builder.append("═══════════════════════════════\n")
        builder.append("МИР: ${current.name}\n")
        builder.append("═══════════════════════════════\n")
        builder.append("Размер карты: ${current.worldWidth.toInt()} × ${current.worldHeight.toInt()}\n")
        builder.append("Пройден шаг: ${current.stage} из 8\n")
        builder.append("Всего объектов на карте: ${current.objectCount()}\n\n")

        val continents = current.landmasses.filter { it.kind == LandKind.CONTINENT }
        val islands = current.landmasses.filter { it.kind != LandKind.CONTINENT }
        if (current.landmasses.isNotEmpty()) {
            builder.append("─── СУША ───\n")
            builder.append("Материков: ${continents.size}, островов и полуостровов: ${islands.size}\n")
            for (land in current.landmasses) {
                if (land.name.isNotBlank()) builder.append("  • ${land.kind.title}: ${land.name}\n")
            }
            builder.append("\n")
        }

        if (current.waters.isNotEmpty()) {
            builder.append("─── ВОДЫ ───\n")
            for ((kind, group) in current.waters.groupBy { it.kind }) {
                val named = group.filter { it.name.isNotBlank() }.joinToString(", ") { it.name }
                builder.append("  • ${kind.title}: ${group.size}")
                if (named.isNotBlank()) builder.append(" — $named")
                builder.append("\n")
            }
            builder.append("\n")
        }

        if (current.biomes.isNotEmpty()) {
            builder.append("─── ПРИРОДНЫЕ ЗОНЫ ───\n")
            for ((biome, group) in current.biomes.groupBy { it.biome }.entries.sortedByDescending { it.value.size }) {
                builder.append("  • ${biome.title}: областей ${group.size}\n")
            }
            builder.append("\n")
        }

        if (current.lines.isNotEmpty()) {
            builder.append("─── РЕКИ, ХРЕБТЫ И СТЕНЫ ───\n")
            for ((type, group) in current.lines.groupBy { it.type }) {
                val named = group.filter { it.name.isNotBlank() }.joinToString(", ") { it.name }
                builder.append("  • ${type.title}: ${group.size}")
                if (named.isNotBlank()) builder.append(" — $named")
                builder.append("\n")
            }
            builder.append("\n")
        }

        if (current.roads.isNotEmpty()) {
            builder.append("─── ПУТИ ───\n")
            for ((type, group) in current.roads.groupBy { it.type }) {
                val named = group.filter { it.name.isNotBlank() }.joinToString(", ") { it.name }
                builder.append("  • ${type.title}: ${group.size}")
                if (named.isNotBlank()) builder.append(" — $named")
                builder.append("\n")
            }
            builder.append("\n")
        }

        if (current.kind == MapKind.BATTLE) {
            val scene = current.scene
            builder.append("─── СЦЕНА ───\n")
            if (scene.goal.isNotBlank()) builder.append("  Цель: ${scene.goal}\n")
            if (scene.enemyTactics.isNotBlank()) builder.append("  Тактика врагов: ${scene.enemyTactics}\n")
            if (scene.reward.isNotBlank()) builder.append("  Награда: ${scene.reward}\n")
            if (scene.notes.isNotBlank()) builder.append("  Заметки: ${scene.notes}\n")
            builder.append("  Сетка: клетка ${current.feetPerCell} фт, раунд ${scene.round}\n\n")
            if (current.tokens.isNotEmpty()) {
                builder.append("─── СУЩЕСТВА ───\n")
                for ((faction, group) in current.tokens.groupBy { it.faction }) {
                    builder.append("\n  ${faction.title} (${group.size}):\n")
                    for (token in group.sortedBy { it.title }) {
                        builder.append("    • ${token.title} — ${token.type.title}, ${token.size.title.lowercase()}")
                        if (token.maxHp > 0) builder.append(", здоровье ${token.hp}/${token.maxHp}")
                        builder.append(", защита ${token.ac}")
                        if (token.conditions.isNotEmpty()) builder.append(", ${token.conditions.joinToString { it.title.lowercase() }}")
                        builder.append("\n")
                        if (token.notes.isNotBlank()) builder.append("      ${token.notes}\n")
                    }
                }
                builder.append("\n")
            }
        }

        if (current.markers.isNotEmpty()) {
            builder.append("─── ОБЪЕКТЫ МИРА ───\n")
            for (group in MarkerGroup.entries) {
                val inGroup = current.markers.filter { it.type.group == group }
                if (inGroup.isEmpty()) continue
                builder.append("\n  ${group.title} (${inGroup.size}):\n")
                for (marker in inGroup.sortedBy { it.type.title }) {
                    builder.append("    • ${marker.type.title}")
                    if (marker.name.isNotBlank()) builder.append(" «${marker.name}»")
                    val country = current.countryById(marker.countryId)
                    if (country != null && country.name.isNotBlank()) builder.append(", ${country.name}")
                    if (marker.population.isNotBlank()) builder.append(", население: ${marker.population}")
                    builder.append("\n")
                    if (marker.description.isNotBlank()) {
                        builder.append("      ${marker.description}\n")
                    }
                }
            }
            builder.append("\n")
        }

        if (current.labels.isNotEmpty()) {
            builder.append("─── ПОДПИСИ НА КАРТЕ ───\n")
            for (label in current.labels) {
                if (label.text.isNotBlank()) builder.append("  • ${label.text}\n")
            }
            builder.append("\n")
        }

        builder.append("─── ГОСУДАРСТВА ───\n")
        builder.append("Стран: ${current.countries.size}\n\n")
        for (country in current.countries) {
            val info = country.info
            builder.append("═══ ${country.name.ifBlank { "Без названия" }} ═══\n")
            fun line(title: String, value: String) {
                if (value.isNotBlank()) builder.append("$title: $value\n")
            }
            line("Столица", info.capital)
            line("Правитель", info.ruler)
            line("Государственный строй", info.government)
            line("Население", info.population)
            line("Народы", info.peoples)
            line("Вера", info.religion)
            line("Язык", info.language)
            line("Монета", info.currency)
            line("Войско", info.army)
            line("Хозяйство", info.economy)
            line("Культура", info.culture)
            line("Отношения с соседями", info.relations)
            line("История", info.history)
            line("Описание", info.description)
            val cities = current.markersOf(country.id)
            if (cities.isNotEmpty()) {
                builder.append("Объекты (${cities.size}):\n")
                for (marker in cities) {
                    builder.append("  • ${marker.type.title}")
                    if (marker.name.isNotBlank()) builder.append(" «${marker.name}»")
                    if (marker.population.isNotBlank()) builder.append(", население: ${marker.population}")
                    builder.append("\n")
                }
            }
            builder.append("\n")
        }
        return builder.toString()
    }

    fun consumeMessage() {
        message = null
    }

    companion object {
        private const val HISTORY_LIMIT = 40
        private const val DISTRICT_KEY = "district:"

        val COUNTRY_COLORS = listOf(
            0xFFB03A2E.toInt(), 0xFF1F618D.toInt(), 0xFF117A65.toInt(), 0xFF7D6608.toInt(),
            0xFF6C3483.toInt(), 0xFFA04000.toInt(), 0xFF196F3D.toInt(), 0xFF884EA0.toInt(),
            0xFF2E86C1.toInt(), 0xFF922B21.toInt(), 0xFF4D5656.toInt(), 0xFFB9770E.toInt()
        )
    }
}
