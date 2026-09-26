package com.fantasymap.creator.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fantasymap.creator.editor.EditorViewModel
import com.fantasymap.creator.model.AreaShape
import com.fantasymap.creator.model.MapKind
import com.fantasymap.creator.model.Selection
import com.fantasymap.creator.model.Tool

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(viewModel: EditorViewModel) {
    val project = viewModel.project ?: return

    var menuOpen by remember { mutableStateOf(false) }
    var showStyle by remember { mutableStateOf(false) }
    var showLayers by remember { mutableStateOf(false) }
    var showExport by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var showRenameProject by remember { mutableStateOf(false) }
    var showCountries by remember { mutableStateOf(false) }
    var showObjectDialog by remember { mutableStateOf(false) }
    var showAssets by remember { mutableStateOf(false) }
    var battleDialog by remember { mutableStateOf<BattleDialog?>(null) }
    var pendingImage by remember { mutableStateOf<Uri?>(null) }
    var renameTarget by remember { mutableStateOf<Selection?>(null) }
    var pngSize by remember { mutableIntStateOf(2048) }
    var pdfTiles by remember { mutableIntStateOf(1) }
    var exportLegend by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel.message) {
        val text = viewModel.message
        if (text != null) {
            snackbarHostState.showSnackbar(text)
            viewModel.consumeMessage()
        }
    }

    val pngLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("image/png")
    ) { uri -> if (uri != null) viewModel.exportPng(uri, pngSize, exportLegend) }

    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri -> if (uri != null) viewModel.exportPdf(uri, pdfTiles, exportLegend) }

    val jsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> if (uri != null) viewModel.exportJson(uri) }

    val textLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri -> if (uri != null) viewModel.exportCountriesText(uri) }

    // Своя картинка для авторской заготовки: постройки, зоны или объекта.
    val imageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            showAssets = false
            pendingImage = uri
        }
    }

    BackHandler {
        when {
            pendingImage != null -> pendingImage = null
            showAssets -> showAssets = false
            showCountries -> showCountries = false
            viewModel.selection != null -> viewModel.selection = null
            else -> viewModel.closeProject()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(project.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Шаг ${viewModel.stage.number} из 8 · ${viewModel.stage.title}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.closeProject() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "К списку карт")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.undo() }, enabled = viewModel.canUndo) {
                        Text("↶", style = MaterialTheme.typography.titleLarge)
                    }
                    IconButton(onClick = { viewModel.redo() }, enabled = viewModel.canRedo) {
                        Text("↷", style = MaterialTheme.typography.titleLarge)
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Меню")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Сохранить результат") },
                                onClick = { menuOpen = false; showExport = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Выделить область") },
                                onClick = {
                                    menuOpen = false
                                    viewModel.tool = Tool.FRAGMENT
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Выровнять границы зон") },
                                onClick = { menuOpen = false; viewModel.alignBiomeBorders() }
                            )
                            if (project.kind == MapKind.BATTLE) {
                                DropdownMenuItem(
                                    text = {
                                        Text(if (project.style.playerView) "Вид мастера" else "Вид для игроков")
                                    },
                                    onClick = { menuOpen = false; viewModel.togglePlayerView() }
                                )
                                DropdownMenuItem(
                                    text = { Text("Сетка и текстуры") },
                                    onClick = { menuOpen = false; battleDialog = BattleDialog.GRID }
                                )
                                DropdownMenuItem(
                                    text = { Text("Кубики") },
                                    onClick = { menuOpen = false; battleDialog = BattleDialog.DICE }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text(if (project.landBase) "Основа: океан" else "Вся карта — суша") },
                                    onClick = { menuOpen = false; viewModel.toggleLandBase() }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Берега вдоль воды") },
                                onClick = { menuOpen = false; viewModel.showShoreDialog = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Авторский контент") },
                                onClick = { menuOpen = false; showAssets = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Слои карты") },
                                onClick = { menuOpen = false; showLayers = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Вид карты") },
                                onClick = { menuOpen = false; showStyle = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Страны мира") },
                                onClick = { menuOpen = false; showCountries = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Переименовать мир") },
                                onClick = { menuOpen = false; showRenameProject = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Сохранить сейчас") },
                                onClick = { menuOpen = false; viewModel.saveNow() }
                            )
                            DropdownMenuItem(
                                text = { Text("Как рисовать мир") },
                                onClick = { menuOpen = false; showHelp = true }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            EditorBottomPanel(
                viewModel = viewModel,
                onOpenCountries = { showCountries = true },
                onOpenAssets = { showAssets = true },
                onBattleDialog = { battleDialog = it }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MapCanvas(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize(),
                onEditRequest = { showObjectDialog = true }
            )

            Column(
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MapControlButton("＋") { viewModel.zoomBy(1.35f) }
                MapControlButton("－") { viewModel.zoomBy(1f / 1.35f) }
                MapControlButton("⤢") { viewModel.fitToView() }
            }

            if (viewModel.busy) {
                LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }

            val selection = viewModel.selection
            if (selection != null) {
                SelectionCard(
                    viewModel = viewModel,
                    selection = selection,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(10.dp),
                    onEdit = {
                        if (selection is Selection.MarkerSel || selection is Selection.LabelSel ||
                            selection is Selection.BuildingSel || selection is Selection.DistrictSel ||
                            selection is Selection.TokenSel
                        ) {
                            showObjectDialog = true
                        } else {
                            renameTarget = selection
                        }
                    }
                )
            }
        }
    }

    // ---- диалоги ----

    if (viewModel.showShoreDialog) {
        ShoreDialog(viewModel) { viewModel.showShoreDialog = false }
    }

    if (showAssets) {
        CustomAssetsDialog(
            viewModel = viewModel,
            onAddImage = { imageLauncher.launch(arrayOf("image/*")) },
            onDismiss = { showAssets = false }
        )
    }
    val image = pendingImage
    if (image != null) {
        NewAssetDialog(
            viewModel = viewModel,
            onSave = { asset ->
                viewModel.addAsset(image, asset)
                pendingImage = null
            },
            onDismiss = { pendingImage = null }
        )
    }

    val selection = viewModel.selection
    if (showObjectDialog && selection is Selection.TokenSel) {
        val token = project.tokens.firstOrNull { it.id == selection.id }
        if (token == null) {
            showObjectDialog = false
        } else {
            TokenEditDialog(viewModel, token) { showObjectDialog = false }
        }
    }
    when (battleDialog) {
        BattleDialog.INITIATIVE -> InitiativeDialog(viewModel) { battleDialog = null }
        BattleDialog.DICE -> DiceDialog(viewModel) { battleDialog = null }
        BattleDialog.SCENE -> SceneDialog(viewModel) { battleDialog = null }
        BattleDialog.GRID -> GridDialog(viewModel) { battleDialog = null }
        null -> Unit
    }

    if (showObjectDialog && selection is Selection.MarkerSel) {
        val marker = project.markers.firstOrNull { it.id == selection.id }
        if (marker == null) {
            showObjectDialog = false
        } else {
            MarkerEditDialog(viewModel, marker) { showObjectDialog = false }
        }
    }
    if (showObjectDialog && selection is Selection.BuildingSel) {
        val building = project.buildings.firstOrNull { it.id == selection.id }
        if (building == null) {
            showObjectDialog = false
        } else {
            BuildingEditDialog(viewModel, building) { showObjectDialog = false }
        }
    }
    if (showObjectDialog && selection is Selection.DistrictSel) {
        val district = project.districts.firstOrNull { it.id == selection.id }
        if (district == null) {
            showObjectDialog = false
        } else {
            DistrictEditDialog(viewModel, district) { showObjectDialog = false }
        }
    }
    if (showObjectDialog && selection is Selection.LabelSel) {
        val label = project.labels.firstOrNull { it.id == selection.id }
        if (label == null) {
            showObjectDialog = false
        } else {
            LabelEditDialog(viewModel, label) { showObjectDialog = false }
        }
    }

    val rename = renameTarget
    if (rename != null) {
        SimpleTextDialog(
            title = "Название",
            initial = currentNameOf(viewModel, rename),
            onDismiss = { renameTarget = null },
            onConfirm = {
                viewModel.renameSelected(it)
                renameTarget = null
            }
        )
    }

    if (showRenameProject) {
        SimpleTextDialog(
            title = "Название мира",
            initial = project.name,
            onDismiss = { showRenameProject = false },
            onConfirm = {
                viewModel.renameProject(it)
                showRenameProject = false
            }
        )
    }

    LaunchedEffect(viewModel.pendingLabelEdit) {
        if (viewModel.pendingLabelEdit != null) {
            showObjectDialog = true
            viewModel.pendingLabelEdit = null
        }
    }

    val fragment = viewModel.fragmentRect
    if (fragment != null) {
        AreaDialog(
            viewModel = viewModel,
            sourceName = project.name,
            fragmentWidth = fragment.width,
            fragmentHeight = fragment.height,
            onDismiss = { viewModel.cancelFragment() }
        )
    }

    if (showStyle) StyleDialog(viewModel) { showStyle = false }
    if (showLayers) LayersDialog(viewModel) { showLayers = false }
    if (showHelp) HelpDialog { showHelp = false }

    if (showExport) {
        ExportDialog(
            onPng = { size, legend ->
                pngSize = size
                exportLegend = legend
                showExport = false
                pngLauncher.launch(fileBaseName(project.name) + ".png")
            },
            onPdf = { tiles, legend ->
                pdfTiles = tiles
                exportLegend = legend
                showExport = false
                pdfLauncher.launch(fileBaseName(project.name) + ".pdf")
            },
            onJson = {
                showExport = false
                jsonLauncher.launch(fileBaseName(project.name) + ".json")
            },
            onText = {
                showExport = false
                textLauncher.launch(fileBaseName(project.name) + "-мир.txt")
            },
            onDismiss = { showExport = false }
        )
    }

    if (showCountries) {
        CountriesOverlay(
            viewModel = viewModel,
            onExportText = { textLauncher.launch(fileBaseName(project.name) + "-мир.txt") },
            onClose = { showCountries = false }
        )
    }
}

@Composable
private fun MapControlButton(label: String, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun SelectionCard(
    viewModel: EditorViewModel,
    selection: Selection,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit
) {
    val project = viewModel.project ?: return
    val (title, subtitle) = when (selection) {
        is Selection.MarkerSel -> {
            val marker = project.markers.firstOrNull { it.id == selection.id }
            if (marker == null) "Объект" to "" else marker.name.ifBlank { marker.type.title } to marker.type.title
        }
        is Selection.LabelSel -> {
            val label = project.labels.firstOrNull { it.id == selection.id }
            (label?.text?.takeIf { it.isNotBlank() } ?: "Подпись") to "Подпись на карте"
        }
        is Selection.Land -> {
            val land = project.landmasses.firstOrNull { it.id == selection.id }
            if (land == null) "Суша" to "" else land.name.ifBlank { land.kind.title } to land.kind.title
        }
        is Selection.Water -> {
            val water = project.waters.firstOrNull { it.id == selection.id }
            if (water == null) "Водоём" to "" else water.name.ifBlank { water.kind.title } to water.kind.title
        }
        is Selection.Biome -> {
            val region = project.biomes.firstOrNull { it.id == selection.id }
            if (region == null) "Зона" to "" else region.name.ifBlank { region.biome.title } to region.biome.title
        }
        is Selection.Line -> {
            val line = project.lines.firstOrNull { it.id == selection.id }
            if (line == null) "Линия" to "" else line.name.ifBlank { line.type.title } to line.type.title
        }
        is Selection.RoadSel -> {
            val road = project.roads.firstOrNull { it.id == selection.id }
            if (road == null) "Дорога" to "" else road.name.ifBlank { road.type.title } to road.type.title
        }
        is Selection.CountryArea -> {
            val country = project.countryById(selection.id)
            (country?.name?.takeIf { it.isNotBlank() } ?: "Страна") to "Территория страны"
        }
        is Selection.BuildingSel -> {
            val building = project.buildings.firstOrNull { it.id == selection.id }
            if (building == null) {
                "Здание" to ""
            } else {
                building.name.ifBlank { building.type.title } to building.type.title
            }
        }
        is Selection.DistrictSel -> {
            val district = project.districts.firstOrNull { it.id == selection.id }
            if (district == null) {
                "Квартал" to ""
            } else {
                district.name.ifBlank { district.type.title } to district.type.title
            }
        }
        is Selection.TokenSel -> {
            val token = project.tokens.firstOrNull { it.id == selection.id }
            if (token == null) {
                "Фишка" to ""
            } else {
                val health = if (token.maxHp > 0) "здоровье ${token.hp}/${token.maxHp} · " else ""
                token.title to "$health защита ${token.ac} · ${token.faction.title}"
            }
        }
        is Selection.FogSel -> "Туман войны" to "Удалите, чтобы открыть этот кусок"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                if (subtitle.isNotBlank()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            val linked = (selection as? Selection.MarkerSel)?.let { sel ->
                project.markers.firstOrNull { it.id == sel.id }?.linkedProjectId
            }
            if (linked != null) {
                TextButton(
                    onClick = { viewModel.openLinkedMap((selection as Selection.MarkerSel).id) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) { Text("→ карта") }
            }
            if (selection is Selection.TokenSel) {
                TextButton(
                    onClick = { viewModel.changeHp(selection.id, -1) },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) { Text("−1") }
                TextButton(
                    onClick = { viewModel.changeHp(selection.id, -5) },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) { Text("−5") }
                TextButton(
                    onClick = { viewModel.changeHp(selection.id, 1) },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) { Text("+1") }
            }
            if (selection is Selection.Biome || selection is Selection.DistrictSel ||
                selection is Selection.Land || selection is Selection.Water || selection is Selection.FogSel
            ) {
                Box {
                    var shapeMenu by remember { mutableStateOf(false) }
                    TextButton(
                        onClick = { shapeMenu = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) { Text("◇") }
                    DropdownMenu(expanded = shapeMenu, onDismissRequest = { shapeMenu = false }) {
                        for (shape in AreaShape.entries) {
                            if (shape == AreaShape.FREE) continue
                            DropdownMenuItem(
                                text = { Text("${shape.icon} ${shape.title}") },
                                onClick = {
                                    shapeMenu = false
                                    viewModel.reshapeSelection(shape)
                                }
                            )
                        }
                    }
                }
            }
            if (selection is Selection.Biome || selection is Selection.DistrictSel) {
                TextButton(
                    onClick = { viewModel.raiseSelectedZone() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) { Text("⬆") }
                TextButton(
                    onClick = { viewModel.lowerSelectedZone() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) { Text("⬇") }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Изменить")
            }
            IconButton(onClick = { viewModel.deleteSelection() }) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
            }
            TextButton(onClick = { viewModel.selection = null }) { Text("Снять") }
        }
    }
}

@Composable
private fun SimpleTextDialog(
    title: String,
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(value.trim()) }) { Text("Готово") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

private fun currentNameOf(viewModel: EditorViewModel, selection: Selection): String {
    val project = viewModel.project ?: return ""
    return when (selection) {
        is Selection.Land -> project.landmasses.firstOrNull { it.id == selection.id }?.name.orEmpty()
        is Selection.Water -> project.waters.firstOrNull { it.id == selection.id }?.name.orEmpty()
        is Selection.Biome -> project.biomes.firstOrNull { it.id == selection.id }?.name.orEmpty()
        is Selection.Line -> project.lines.firstOrNull { it.id == selection.id }?.name.orEmpty()
        is Selection.RoadSel -> project.roads.firstOrNull { it.id == selection.id }?.name.orEmpty()
        is Selection.MarkerSel -> project.markers.firstOrNull { it.id == selection.id }?.name.orEmpty()
        is Selection.LabelSel -> project.labels.firstOrNull { it.id == selection.id }?.text.orEmpty()
        is Selection.CountryArea -> project.countryById(selection.id)?.name.orEmpty()
        is Selection.BuildingSel -> project.buildings.firstOrNull { it.id == selection.id }?.name.orEmpty()
        is Selection.DistrictSel -> project.districts.firstOrNull { it.id == selection.id }?.name.orEmpty()
        is Selection.TokenSel -> project.tokens.firstOrNull { it.id == selection.id }?.name.orEmpty()
        is Selection.FogSel -> ""
    }
}

private fun fileBaseName(name: String): String {
    val cleaned = name.trim().replace(Regex("[\\\\/:*?\"<>|]"), "-")
    return cleaned.ifBlank { "карта" }
}
