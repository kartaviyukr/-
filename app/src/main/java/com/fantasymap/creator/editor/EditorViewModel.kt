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
import com.fantasymap.creator.geom.PolygonOps
import com.fantasymap.creator.export.Exporter
import com.fantasymap.creator.model.BiomeRegion
import com.fantasymap.creator.model.BiomeType
import com.fantasymap.creator.model.Country
import com.fantasymap.creator.model.CountryInfo
import com.fantasymap.creator.model.Geometry
import com.fantasymap.creator.model.LabelStyle
import com.fantasymap.creator.model.LandKind
import com.fantasymap.creator.model.Landmass
import com.fantasymap.creator.model.LineFeature
import com.fantasymap.creator.model.LineFeatureType
import com.fantasymap.creator.model.MapLabel
import com.fantasymap.creator.model.MapProject
import com.fantasymap.creator.model.MapStyle
import com.fantasymap.creator.model.Marker
import com.fantasymap.creator.model.MarkerGroup
import com.fantasymap.creator.model.MarkerType
import com.fantasymap.creator.model.ProjectSummary
import com.fantasymap.creator.model.Road
import com.fantasymap.creator.model.RoadType
import com.fantasymap.creator.model.Selection
import com.fantasymap.creator.model.Stage
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

/** Состояние редактора карты: проект, инструменты, камера, история изменений. */
class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val store = ProjectStore(application)
    private val exporter = Exporter(application)

    var projects by mutableStateOf<List<ProjectSummary>>(emptyList())
        private set

    var project by mutableStateOf<MapProject?>(null)
        private set

    var stage by mutableStateOf(Stage.CONTINENTS)
        private set

    var tool by mutableStateOf(Tool.LAND)
    var biome by mutableStateOf(BiomeType.MIXED_FOREST)
    var markerType by mutableStateOf(MarkerType.CITY)
    var markerGroup by mutableStateOf(MarkerGroup.SETTLEMENT)
    var lineType by mutableStateOf(LineFeatureType.RIVER)
    var roadType by mutableStateOf(RoadType.ROAD)
    var labelStyle by mutableStateOf(LabelStyle.REGION)
    var waterKind by mutableStateOf(WaterKind.LAKE)
    var activeCountryId by mutableStateOf<String?>(null)
    var selection by mutableStateOf<Selection?>(null)
    var camera by mutableStateOf(Camera())
    var message by mutableStateOf<String?>(null)
    var busy by mutableStateOf(false)
        private set

    /** Точки текущего, ещё не завершённого штриха. */
    val draft = mutableStateListOf<Vec>()

    var canUndo by mutableStateOf(false)
        private set
    var canRedo by mutableStateOf(false)
        private set

    private val undoStack = ArrayDeque<MapProject>()
    private val redoStack = ArrayDeque<MapProject>()
    private var saveJob: Job? = null
    private var draggingSelection: Selection? = null
    private var viewWidth = 0f
    private var viewHeight = 0f

    init {
        refreshProjects()
    }

    // ------------------------------------------------------------- проекты

    fun refreshProjects() {
        viewModelScope.launch {
            projects = withContext(Dispatchers.IO) { store.list() }
        }
    }

    fun createProject(name: String, width: Float, height: Float) {
        val fresh = MapProject(
            name = name.ifBlank { "Новый мир" },
            worldWidth = width,
            worldHeight = height
        )
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
        stage = Stage.byNumber(loaded.stage)
        tool = defaultToolFor(stage)
        selection = null
        draft.clear()
        undoStack.clear()
        redoStack.clear()
        canUndo = false
        canRedo = false
        activeCountryId = loaded.countries.firstOrNull()?.id
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

    fun selectStage(newStage: Stage) {
        stage = newStage
        tool = defaultToolFor(newStage)
        selection = null
        draft.clear()
        markerGroup = when (newStage) {
            Stage.NATURE -> MarkerGroup.NATURE
            Stage.SETTLEMENTS -> MarkerGroup.SETTLEMENT
            Stage.SPECIAL -> MarkerGroup.MAGIC
            else -> markerGroup
        }
        markerType = MarkerType.byGroup(markerGroup).firstOrNull() ?: markerType
        if (newStage == Stage.SPECIAL) markerType = MarkerType.WIZARD_TOWER
        if (newStage == Stage.SETTLEMENTS) markerType = MarkerType.CITY
        if (newStage == Stage.NATURE) markerType = MarkerType.MOUNTAIN_PEAK
        editQuiet { it.copy(stage = newStage.number) }
        scheduleSave()
    }

    fun toolsFor(currentStage: Stage): List<Tool> = when (currentStage) {
        Stage.CONTINENTS -> listOf(Tool.PAN, Tool.LAND, Tool.ISLAND, Tool.WATER, Tool.SELECT, Tool.ERASER)
        Stage.BIOMES -> listOf(Tool.PAN, Tool.BIOME, Tool.SELECT, Tool.ERASER)
        Stage.NATURE -> listOf(Tool.PAN, Tool.LINE, Tool.MARKER, Tool.LABEL, Tool.SELECT, Tool.ERASER)
        Stage.SETTLEMENTS -> listOf(Tool.PAN, Tool.MARKER, Tool.ROAD, Tool.LABEL, Tool.SELECT, Tool.ERASER)
        Stage.CAPITALS -> listOf(Tool.PAN, Tool.SELECT, Tool.MARKER)
        Stage.SPECIAL -> listOf(Tool.PAN, Tool.MARKER, Tool.LABEL, Tool.SELECT, Tool.ERASER)
        Stage.BORDERS -> listOf(Tool.PAN, Tool.COUNTRY, Tool.SELECT, Tool.ERASER)
        Stage.COUNTRIES -> listOf(Tool.PAN, Tool.SELECT)
    }

    private fun defaultToolFor(currentStage: Stage): Tool = when (currentStage) {
        Stage.CONTINENTS -> Tool.LAND
        Stage.BIOMES -> Tool.BIOME
        Stage.NATURE -> Tool.LINE
        Stage.SETTLEMENTS -> Tool.MARKER
        Stage.CAPITALS -> Tool.SELECT
        Stage.SPECIAL -> Tool.MARKER
        Stage.BORDERS -> Tool.COUNTRY
        Stage.COUNTRIES -> Tool.PAN
    }

    /** Рисуется ли текущим инструментом замкнутая область. */
    fun toolDrawsArea(): Boolean =
        tool == Tool.LAND || tool == Tool.ISLAND || tool == Tool.WATER ||
            tool == Tool.BIOME || tool == Tool.COUNTRY

    fun toolDrawsLine(): Boolean = tool == Tool.LINE || tool == Tool.ROAD

    fun draftColor(): Int = when (tool) {
        Tool.LAND, Tool.ISLAND -> 0xFF8A6A3A.toInt()
        Tool.WATER -> 0xFF2E6E93.toInt()
        Tool.BIOME -> biome.color
        Tool.COUNTRY -> project?.countryById(activeCountryId)?.color ?: 0xFFB03A2E.toInt()
        Tool.LINE -> lineType.color
        Tool.ROAD -> roadType.color
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
            draggingSelection = if (hit is Selection.MarkerSel || hit is Selection.LabelSel) hit else null
            if (draggingSelection != null) pushHistoryForDrag()
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
        if (draft.isEmpty()) return
        val minDistance = max(1.5f, 3.5f / camera.scale)
        if (draft.last().distanceTo(world) >= minDistance) draft.add(world)
    }

    fun finishStroke() {
        if (draggingSelection != null) {
            draggingSelection = null
            scheduleSave()
            return
        }
        if (draft.isEmpty()) return
        val points = draft.toList()
        draft.clear()
        commitStroke(points)
    }

    fun cancelStroke() {
        draft.clear()
        draggingSelection = null
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
                Tool.BIOME -> edit { it.copy(biomes = it.biomes + BiomeRegion(biome = biome, points = smooth)) }
                Tool.COUNTRY -> addCountryArea(smooth)
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
                Tool.LINE -> edit { it.copy(lines = it.lines + LineFeature(type = lineType, points = smooth)) }
                Tool.ROAD -> edit { it.copy(roads = it.roads + Road(type = roadType, points = smooth)) }
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
            else -> Unit
        }
    }

    // ------------------------------------------------------------- касание

    /** Одиночное касание карты. Возвращает true, если нужно открыть карточку объекта. */
    fun tap(world: Vec): Boolean {
        when (tool) {
            Tool.MARKER -> {
                val marker = Marker(
                    type = markerType,
                    pos = world,
                    countryId = activeCountryId,
                    name = ""
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
        val tolerance = max(6f, 16f / camera.scale)

        current.markers
            .filter { it.pos.distanceTo(world) <= tolerance * 1.3f }
            .minByOrNull { it.pos.distanceTo(world) }
            ?.let { return Selection.MarkerSel(it.id) }

        current.labels
            .filter { it.pos.distanceTo(world) <= tolerance * 1.6f }
            .minByOrNull { it.pos.distanceTo(world) }
            ?.let { return Selection.LabelSel(it.id) }

        current.roads
            .filter { Geometry.distanceToPolyline(world, it.points) <= tolerance }
            .minByOrNull { Geometry.distanceToPolyline(world, it.points) }
            ?.let { return Selection.RoadSel(it.id) }

        current.lines
            .filter { Geometry.distanceToPolyline(world, it.points) <= tolerance }
            .minByOrNull { Geometry.distanceToPolyline(world, it.points) }
            ?.let { return Selection.Line(it.id) }

        for (country in current.countries.asReversed()) {
            for ((index, area) in country.areas.withIndex()) {
                if (Geometry.distanceToPolygonOutline(world, area) <= tolerance) {
                    return Selection.CountryArea(country.id, index)
                }
            }
        }

        current.biomes.asReversed()
            .firstOrNull { Geometry.pointInContours(world, it.contours()) }
            ?.let { return Selection.Biome(it.id) }

        current.waters.asReversed()
            .firstOrNull { Geometry.pointInPolygon(world, it.points) }
            ?.let { return Selection.Water(it.id) }

        current.landmasses.asReversed()
            .firstOrNull { Geometry.pointInPolygon(world, it.points) }
            ?.let { return Selection.Land(it.id) }

        for (country in current.countries.asReversed()) {
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
                is Selection.CountryArea -> state.copy(
                    countries = state.countries.map { if (it.id == target.id) it.copy(name = name) else it }
                )
            }
        }
    }

    /**
     * Выровнять границы природных зон: там, где области наложились друг на друга,
     * лишнее вырезается у той, что нарисована раньше. Общая граница становится
     * одной линией, без двойной закраски.
     */
    fun alignBiomeBorders() {
        val current = project ?: return
        if (current.biomes.size < 2) {
            message = "Нужно хотя бы две зоны"
            return
        }
        viewModelScope.launch {
            busy = true
            val minArea = max(30f, current.worldWidth * current.worldHeight * 0.00002f)
            val result = withContext(Dispatchers.Default) {
                PolygonOps.resolveOverlaps(current.biomes, minArea)
            }
            busy = false
            if (project?.biomes !== current.biomes) {
                message = "Карта изменилась, повторите выравнивание"
                return@launch
            }
            if (!result.changed) {
                message = "Наложений не найдено — границы уже совпадают"
                return@launch
            }
            selection = null
            edit { it.copy(biomes = result.regions) }
            message = buildString {
                append("Границы выровнены")
                if (result.trimmed > 0) append(", подрезано зон: ${result.trimmed}")
                if (result.removed > 0) append(", убрано перекрытых: ${result.removed}")
            }
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

    // ------------------------------------------------------------- экспорт

    fun exportPng(uri: Uri, longSide: Int) {
        val current = project ?: return
        viewModelScope.launch {
            busy = true
            val ok = withContext(Dispatchers.IO) { exporter.exportPng(current, uri, longSide) }
            busy = false
            message = if (ok) "Карта сохранена в PNG" else "Не удалось сохранить PNG"
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
            val ok = withContext(Dispatchers.IO) { exporter.writeText(uri, buildCountriesReport(current)) }
            busy = false
            message = if (ok) "Описание стран сохранено" else "Не удалось сохранить файл"
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

    private fun buildCountriesReport(current: MapProject): String {
        val builder = StringBuilder()
        builder.append("МИР: ${current.name}\n")
        builder.append("Стран: ${current.countries.size}, объектов на карте: ${current.objectCount()}\n\n")
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

        val COUNTRY_COLORS = listOf(
            0xFFB03A2E.toInt(), 0xFF1F618D.toInt(), 0xFF117A65.toInt(), 0xFF7D6608.toInt(),
            0xFF6C3483.toInt(), 0xFFA04000.toInt(), 0xFF196F3D.toInt(), 0xFF884EA0.toInt(),
            0xFF2E86C1.toInt(), 0xFF922B21.toInt(), 0xFF4D5656.toInt(), 0xFFB9770E.toInt()
        )
    }
}
