package com.fantasymap.creator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.FilledTonalButton
import com.fantasymap.creator.model.Condition
import com.fantasymap.creator.model.GridKind
import com.fantasymap.creator.model.Token
import com.fantasymap.creator.model.TokenFaction
import com.fantasymap.creator.model.TokenGroup
import com.fantasymap.creator.model.TokenSize
import com.fantasymap.creator.model.TokenType
import com.fantasymap.creator.editor.EditorViewModel
import com.fantasymap.creator.model.Building
import com.fantasymap.creator.model.BuildingGroup
import com.fantasymap.creator.model.BuildingType
import com.fantasymap.creator.model.Country
import com.fantasymap.creator.model.CustomAsset
import com.fantasymap.creator.model.CustomKind
import com.fantasymap.creator.model.MarkerScope
import com.fantasymap.creator.model.District
import com.fantasymap.creator.model.DistrictType
import com.fantasymap.creator.model.CountryInfo
import com.fantasymap.creator.model.MapLabel
import com.fantasymap.creator.model.MapLayer
import com.fantasymap.creator.model.MapStyle
import com.fantasymap.creator.model.StylePreset
import com.fantasymap.creator.model.Marker
import com.fantasymap.creator.model.MarkerType

/** Карточка объекта: имя, вид, принадлежность стране, описание. */
@Composable
fun MarkerEditDialog(
    viewModel: EditorViewModel,
    marker: Marker,
    onDismiss: () -> Unit
) {
    var draft by remember(marker.id) { mutableStateOf(marker) }
    val countries = viewModel.project?.countries.orEmpty()
    var group by remember(marker.id) { mutableStateOf(marker.type.group) }

    AlertDialog(
        onDismissRequest = {
            viewModel.updateMarker(draft)
            onDismiss()
        },
        title = { Text(draft.type.title) },
        text = {
            Column(
                Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { draft = draft.copy(name = it) },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))

                Text("Вид объекта", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(viewModel.markerGroups()) { item ->
                        FilterChip(
                            selected = group == item,
                            onClick = { group = item },
                            label = { Text(item.title) }
                        )
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(MarkerType.byGroup(group, viewModel.mapKind)) { item ->
                        FilterChip(
                            selected = draft.type == item,
                            onClick = { draft = draft.copy(type = item) },
                            label = { Text(item.title) }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text("Страна", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = draft.countryId == null,
                            onClick = { draft = draft.copy(countryId = null) },
                            label = { Text("Ничья земля") }
                        )
                    }
                    items(countries) { country ->
                        FilterChip(
                            selected = draft.countryId == country.id,
                            onClick = { draft = draft.copy(countryId = country.id) },
                            label = { Text(country.name.ifBlank { "Без названия" }) },
                            leadingIcon = { ColorDot(Color(country.color)) }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = draft.population,
                    onValueChange = { draft = draft.copy(population = it) },
                    label = { Text("Население") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = draft.ruler,
                    onValueChange = { draft = draft.copy(ruler = it) },
                    label = { Text("Кто правит / кто владеет") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = draft.description,
                    onValueChange = { draft = draft.copy(description = it) },
                    label = { Text("Описание, легенды, слухи") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))
                Text("Подробная карта", style = MaterialTheme.typography.labelLarge)
                Text(
                    "С объекта можно перейти на отдельную карту — получится атлас.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = draft.linkedProjectId == null,
                            onClick = { draft = draft.copy(linkedProjectId = null) },
                            label = { Text("нет") }
                        )
                    }
                    items(viewModel.projects.filter { it.id != viewModel.project?.id }) { summary ->
                        FilterChip(
                            selected = draft.linkedProjectId == summary.id,
                            onClick = { draft = draft.copy(linkedProjectId = summary.id) },
                            label = { Text(summary.name) }
                        )
                    }
                }
                if (draft.linkedProjectId != null) {
                    TextButton(onClick = {
                        viewModel.updateMarker(draft)
                        viewModel.openLinkedMap(draft.id)
                        onDismiss()
                    }) { Text("→ Открыть связанную карту") }
                }

                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Подпись на карте", Modifier.weight(1f))
                    Switch(
                        checked = draft.showLabel,
                        onCheckedChange = { draft = draft.copy(showLabel = it) }
                    )
                }
                Text("Размер значка", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = draft.scale,
                    onValueChange = { draft = draft.copy(scale = it) },
                    valueRange = 0.5f..2.5f
                )

                if (draft.type.isSettlement && draft.type != MarkerType.CAPITAL) {
                    TextButton(onClick = {
                        viewModel.updateMarker(draft)
                        viewModel.promoteToCapital(draft.id)
                        onDismiss()
                    }) { Text("★ Сделать столицей") }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.updateMarker(draft)
                onDismiss()
            }) { Text("Готово") }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.deleteSelection()
                onDismiss()
            }) { Text("Удалить") }
        }
    )
}

/** Правка свободной подписи на карте. */
@Composable
fun LabelEditDialog(
    viewModel: EditorViewModel,
    label: MapLabel,
    onDismiss: () -> Unit
) {
    var draft by remember(label.id) { mutableStateOf(label) }
    AlertDialog(
        onDismissRequest = {
            viewModel.updateLabel(draft)
            onDismiss()
        },
        title = { Text("Подпись на карте") },
        text = {
            Column {
                OutlinedTextField(
                    value = draft.text,
                    onValueChange = { draft = draft.copy(text = it) },
                    label = { Text("Текст") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                if (draft.curved) {
                    Text(
                        "Подпись идёт вдоль нарисованной кривой.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { draft = draft.copy(path = emptyList()) }) {
                        Text("Сделать обычной подписью")
                    }
                } else {
                    Text("Поворот: ${draft.rotation.toInt()}°", style = MaterialTheme.typography.labelLarge)
                    Slider(
                        value = draft.rotation,
                        onValueChange = { draft = draft.copy(rotation = it) },
                        valueRange = -90f..90f
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.updateLabel(draft)
                onDismiss()
            }) { Text("Готово") }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.deleteSelection()
                onDismiss()
            }) { Text("Удалить") }
        }
    )
}

/** Карточка здания: вид, название, этажи, хозяин, описание. */
@Composable
fun BuildingEditDialog(
    viewModel: EditorViewModel,
    building: Building,
    onDismiss: () -> Unit
) {
    var draft by remember(building.id) { mutableStateOf(building) }
    var group by remember(building.id) { mutableStateOf(building.type.group) }

    AlertDialog(
        onDismissRequest = {
            viewModel.updateBuilding(draft)
            onDismiss()
        },
        title = { Text(draft.type.title) },
        text = {
            Column(
                Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { draft = draft.copy(name = it) },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Text("Вид постройки", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(BuildingGroup.entries.toList()) { item ->
                        FilterChip(
                            selected = group == item,
                            onClick = { group = item },
                            label = { Text(item.title) }
                        )
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(BuildingType.byGroup(group)) { item ->
                        FilterChip(
                            selected = draft.type == item,
                            onClick = { draft = draft.copy(type = item) },
                            label = { Text(item.title) },
                            leadingIcon = { ColorDot(Color(item.color)) }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = draft.owner,
                    onValueChange = { draft = draft.copy(owner = it) },
                    label = { Text("Хозяин, кто здесь живёт или работает") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = draft.description,
                    onValueChange = { draft = draft.copy(description = it) },
                    label = { Text("Описание, слухи, что внутри") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Text("Этажей: ${draft.floors}", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = draft.floors.toFloat(),
                    onValueChange = { draft = draft.copy(floors = it.toInt().coerceIn(1, 9)) },
                    valueRange = 1f..9f,
                    steps = 7
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Подпись на карте", Modifier.weight(1f))
                    Switch(
                        checked = draft.showLabel,
                        onCheckedChange = { draft = draft.copy(showLabel = it) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.updateBuilding(draft)
                onDismiss()
            }) { Text("Готово") }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.deleteSelection()
                onDismiss()
            }) { Text("Удалить") }
        }
    )
}

/** Карточка квартала: вид, название, описание, застройка домами. */
@Composable
fun DistrictEditDialog(
    viewModel: EditorViewModel,
    district: District,
    onDismiss: () -> Unit
) {
    var draft by remember(district.id) { mutableStateOf(district) }

    AlertDialog(
        onDismissRequest = {
            viewModel.updateDistrict(draft)
            onDismiss()
        },
        title = { Text(draft.type.title) },
        text = {
            Column(
                Modifier
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { draft = draft.copy(name = it) },
                    label = { Text("Название квартала") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Text("Какой это квартал", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(DistrictType.entries.toList()) { item ->
                        FilterChip(
                            selected = draft.type == item,
                            onClick = { draft = draft.copy(type = item) },
                            label = { Text(item.title) },
                            leadingIcon = { ColorDot(Color(item.color)) }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = draft.description,
                    onValueChange = { draft = draft.copy(description = it) },
                    label = { Text("Чем живёт квартал") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Плотность застройки: ${(viewModel.buildDensity * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge
                )
                Slider(
                    value = viewModel.buildDensity,
                    onValueChange = { viewModel.buildDensity = it },
                    valueRange = 0f..1f
                )
                TextButton(onClick = {
                    viewModel.updateDistrict(draft)
                    viewModel.fillDistrictWithHouses()
                    onDismiss()
                }) { Text("🏘 Застроить квартал домами") }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.updateDistrict(draft)
                onDismiss()
            }) { Text("Готово") }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.deleteSelection()
                onDismiss()
            }) { Text("Удалить") }
        }
    )
}

/** Слои карты: что показывать и что запереть от правки. */
@Composable
fun LayersDialog(viewModel: EditorViewModel, onDismiss: () -> Unit) {
    val project = viewModel.project ?: return
    val style = project.style
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Слои карты") },
        text = {
            Column(
                Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Глаз прячет слой, замок защищает его от выбора и стирания.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                MapLayer.entries.forEach { layer ->
                    val visible = layer.visible(style)
                    val locked = layer.locked(style)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            layer.title,
                            Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (visible) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        TextButton(
                            onClick = { viewModel.setLayerVisible(layer, !visible) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) { Text(if (visible) "👁 видно" else "🚫 скрыт") }
                        TextButton(
                            onClick = { viewModel.setLayerLocked(layer, !locked) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) { Text(if (locked) "🔒 заперт" else "🔓 правится") }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Готово") } }
    )
}

/** Настройки отображения карты. */
@Composable
fun StyleDialog(viewModel: EditorViewModel, onDismiss: () -> Unit) {
    val project = viewModel.project ?: return
    val initial = remember { project.style }
    var style by remember { mutableStateOf(project.style) }

    fun apply(new: MapStyle) {
        style = new
        viewModel.updateStyle(new)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Вид карты") },
        text = {
            Column(
                Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Готовый вид", style = MaterialTheme.typography.labelLarge)
                StylePreset.ALL.forEach { preset ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.applyStylePreset(preset)
                                style = preset.apply(style)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(preset.title, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                preset.hint,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        ColorDot(Color(preset.apply(style).oceanColor), size = 18)
                        Spacer(Modifier.width(6.dp))
                        ColorDot(Color(preset.apply(style).landColor), size = 18)
                    }
                }
                TextButton(onClick = {
                    viewModel.updateStyle(initial)
                    style = initial
                }) { Text("↺ Вернуть как было") }
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                ToggleRow("Природные зоны", style.showBiomes) { apply(style.copy(showBiomes = it)) }
                ToggleRow("Текстуры ландшафта", style.showPatterns) { apply(style.copy(showPatterns = it)) }
                ToggleRow("Дороги", style.showRoads) { apply(style.copy(showRoads = it)) }
                ToggleRow("Объекты", style.showMarkers) { apply(style.copy(showMarkers = it)) }
                ToggleRow("Подписи", style.showLabels) { apply(style.copy(showLabels = it)) }
                ToggleRow("Границы стран", style.showBorders) { apply(style.copy(showBorders = it)) }
                ToggleRow("Заливка стран", style.bordersFilled) { apply(style.copy(bordersFilled = it)) }
                ToggleRow("Сетка", style.showGrid) { apply(style.copy(showGrid = it)) }
                ToggleRow("Рамка", style.showFrame) { apply(style.copy(showFrame = it)) }
                ToggleRow("Роза ветров", style.showCompass) { apply(style.copy(showCompass = it)) }
                Spacer(Modifier.height(8.dp))
                Text("Размер подписей", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = style.labelScale,
                    onValueChange = { apply(style.copy(labelScale = it)) },
                    valueRange = 0.5f..2f
                )
                Spacer(Modifier.height(8.dp))
                Text("Цвет океана", style = MaterialTheme.typography.labelLarge)
                ColorRow(OCEAN_COLORS, style.oceanColor) { apply(style.copy(oceanColor = it)) }
                Spacer(Modifier.height(8.dp))
                Text("Цвет суши", style = MaterialTheme.typography.labelLarge)
                ColorRow(LAND_COLORS, style.landColor) { apply(style.copy(landColor = it)) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Готово") } }
    )
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun ColorRow(colors: List<Int>, selected: Int, onSelect: (Int) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(colors) { value ->
            Box(
                Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(value))
                    .border(
                        width = if (value == selected) 3.dp else 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                    .clickable { onSelect(value) }
            )
        }
    }
}

/** Выбор формата сохранения результата. */
@Composable
fun ExportDialog(
    onPng: (Int, Boolean) -> Unit,
    onPdf: (Int, Boolean) -> Unit,
    onJson: () -> Unit,
    onText: () -> Unit,
    onDismiss: () -> Unit
) {
    var withLegend by remember { mutableStateOf(true) }
    var tiles by remember { mutableFloatStateOf(1f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Сохранить результат") },
        text = {
            Column(
                Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Условные обозначения")
                        Text(
                            "список зон, объектов и стран рядом с картой",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = withLegend, onCheckedChange = { withLegend = it })
                }
                HorizontalDivider(Modifier.padding(vertical = 6.dp))

                Text("Картинка (PNG)", fontWeight = FontWeight.Bold)
                TextButton(onClick = { onPng(2048, withLegend) }) { Text("PNG — обычный размер (2048 px)") }
                TextButton(onClick = { onPng(4096, withLegend) }) { Text("PNG — большой размер (4096 px)") }
                TextButton(onClick = { onPng(6144, withLegend) }) { Text("PNG — для печати (6144 px)") }

                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                Text("Печать (PDF)", fontWeight = FontWeight.Bold)
                Text(
                    if (tiles < 1.5f) {
                        "Один лист A4 целиком"
                    } else {
                        "Плитками: ${tiles.toInt()} листа A4 по ширине — склеить в большую карту"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = tiles,
                    onValueChange = { tiles = it },
                    valueRange = 1f..6f,
                    steps = 4
                )
                TextButton(onClick = { onPdf(tiles.toInt(), withLegend) }) {
                    Text(if (tiles < 1.5f) "Сохранить PDF" else "Сохранить PDF плитками")
                }

                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                Text("Текст и проект", fontWeight = FontWeight.Bold)
                TextButton(onClick = onText) { Text("Описание мира (.txt)") }
                TextButton(onClick = onJson) { Text("Файл проекта (.json) — можно открыть снова") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}

/** Что сделать с выделенной областью карты. */
@Composable
fun AreaDialog(
    viewModel: EditorViewModel,
    sourceName: String,
    fragmentWidth: Float,
    fragmentHeight: Float,
    onDismiss: () -> Unit
) {
    var mode by remember { mutableIntStateOf(0) }
    var islands by remember { mutableStateOf(false) }
    var roughness by remember { mutableFloatStateOf(0.55f) }
    var riverCount by remember { mutableFloatStateOf(5f) }
    var replaceZones by remember { mutableStateOf(true) }

    if (mode == 1) {
        FragmentDialog(
            sourceName = sourceName,
            fragmentWidth = fragmentWidth,
            fragmentHeight = fragmentHeight,
            onCreate = { name, longSide, placeLink ->
                viewModel.createMapFromFragment(name, longSide, placeLink)
            },
            onDismiss = onDismiss
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выделенная область") },
        text = {
            Column(
                Modifier
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Кусок ${fragmentWidth.toInt()} × ${fragmentHeight.toInt()}. Что с ним сделать?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                when (mode) {
                    2 -> {
                        Text("Побережье", style = MaterialTheme.typography.labelLarge)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = !islands, onClick = { islands = false })
                            Text("Один материк", Modifier.weight(1f))
                            RadioButton(selected = islands, onClick = { islands = true })
                            Text("Острова")
                        }
                        Text(
                            "Изрезанность берега: ${(roughness * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Slider(
                            value = roughness,
                            onValueChange = { roughness = it },
                            valueRange = 0.1f..1f
                        )
                        Text(
                            "Берег рисуется случайно — не понравится, отмените стрелкой ↶ и нажмите ещё раз.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { viewModel.generateCoastline(islands, roughness) }) {
                            Text("Нарисовать берег")
                        }
                    }

                    3 -> {
                        Text("Реки", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "Истоки берутся у горных хребтов и вершин внутри области, " +
                                "реки текут к ближайшей воде.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Сколько рек: ${riverCount.toInt()}",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Slider(
                            value = riverCount,
                            onValueChange = { riverCount = it },
                            valueRange = 1f..12f
                        )
                        TextButton(onClick = { viewModel.generateRivers(riverCount.toInt()) }) {
                            Text("Провести реки")
                        }
                    }

                    4 -> {
                        Text("Природные зоны", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "Зоны раскладываются по широте: у полюсов льды и тундра, " +
                                "в средних широтах леса и степи, у тропиков пустыни и саванна, " +
                                "у экватора джунгли. Вдоль хребтов ложатся горы.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Заменить зоны в области", Modifier.weight(1f))
                            Switch(checked = replaceZones, onCheckedChange = { replaceZones = it })
                        }
                        TextButton(onClick = { viewModel.generateBiomeBands(replaceZones) }) {
                            Text("Разложить зоны")
                        }
                    }

                    5 -> {
                        Text("Вставить другую карту", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "Выбранная карта впишется в выделенную область целиком — " +
                                "со своей сушей, зонами, объектами и странами.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        val others = viewModel.projects.filter { it.id != viewModel.project?.id }
                        if (others.isEmpty()) {
                            Text("Других карт пока нет.", style = MaterialTheme.typography.bodyMedium)
                        }
                        others.forEach { summary ->
                            TextButton(onClick = { viewModel.insertMapIntoArea(summary.id) }) {
                                Text("${summary.name} · объектов ${summary.markerCount}")
                            }
                        }
                    }

                    else -> {
                        TextButton(onClick = { mode = 1 }) { Text("⧉  Скопировать в новую карту") }
                        TextButton(onClick = { mode = 5 }) { Text("⊞  Вставить сюда другую карту") }
                        TextButton(onClick = { mode = 2 }) { Text("🗺  Сгенерировать побережье") }
                        TextButton(onClick = { mode = 4 }) { Text("🖌  Разложить природные зоны") }
                        TextButton(onClick = { mode = 3 }) { Text("〰  Провести реки от гор к морю") }
                    }
                }
            }
        },
        confirmButton = {
            if (mode == 0) {
                TextButton(onClick = onDismiss) { Text("Закрыть") }
            } else {
                TextButton(onClick = { mode = 0 }) { Text("Назад") }
            }
        },
        dismissButton = {
            if (mode != 0) TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

/**
 * Создание отдельной карты из выделенного куска.
 * Исходная карта остаётся нетронутой — фрагмент копируется.
 */
@Composable
fun FragmentDialog(
    sourceName: String,
    fragmentWidth: Float,
    fragmentHeight: Float,
    onCreate: (String, Float, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("$sourceName — фрагмент") }
    var longSide by remember { mutableFloatStateOf(2400f) }
    var placeLink by remember { mutableStateOf(true) }
    val currentLongSide = maxOf(fragmentWidth, fragmentHeight, 1f)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая карта из фрагмента") },
        text = {
            Column(
                Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Выделенный кусок ${fragmentWidth.toInt()} × ${fragmentHeight.toInt()} " +
                        "будет скопирован в отдельную карту и растянут на весь её размер. " +
                        "Эта карта останется без изменений.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название новой карты") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Оставить здесь метку-переход")
                        Text(
                            "на этой карте появится значок, с которого можно перейти на новую",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = placeLink, onCheckedChange = { placeLink = it })
                }
                Spacer(Modifier.height(8.dp))
                Text("Размер новой карты", style = MaterialTheme.typography.labelLarge)
                FRAGMENT_SIZES.forEach { size ->
                    val zoom = size / currentLongSide
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { longSide = size }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = longSide == size, onClick = { longSide = size })
                        Spacer(Modifier.width(4.dp))
                        Column {
                            Text(
                                "${size.toInt()} по длинной стороне",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "подробнее в ${formatZoom(zoom)} раза",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onCreate(name.trim(), longSide, placeLink) }) {
                Text("Создать карту")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

private fun formatZoom(zoom: Float): String {
    val rounded = (zoom * 10f).toInt() / 10f
    return if (rounded >= 10f) rounded.toInt().toString() else rounded.toString()
}

private val FRAGMENT_SIZES = listOf(1600f, 2400f, 3200f, 4800f, 6400f)

/** Краткая справка по работе с картой. */
@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Как рисовать мир") },
        text = {
            Column(
                Modifier
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Управление", fontWeight = FontWeight.Bold)
                Text("• Один палец — рисование выбранным инструментом.")
                Text("• Два пальца — перемещение и масштаб карты.")
                Text("• Инструмент «✋ Перемещение» — двигать карту одним пальцем.")
                Text("• Инструмент «☝ Выбрать» — выделить объект, перетащить город или подпись.")
                Text("• «🧽 Стереть» — коснуться объекта, чтобы удалить.")
                Spacer(Modifier.height(10.dp))
                Text("Порядок работы", fontWeight = FontWeight.Bold)
                Text("1. Континенты — обведите сушу; остальное останется океаном.")
                Text("2. Природные зоны — закрасьте леса, степи, пустыни, горы, льды. " +
                    "Кнопка «Выровнять границы зон» наводит порядок: у разных ландшафтов " +
                    "убирается наложение, а соседние области одного ландшафта сливаются в одну.")
                Text("Кто нанесён последним, тот и затирает предыдущего. Если нужно, чтобы " +
                    "победила зона, нарисованная раньше, выберите её и нажмите ⬆ — она " +
                    "поднимется наверх; ⬇ опускает зону вниз.")
                Text("3. Природные объекты — реки, хребты, вершины, пещеры, водопады.")
                Text("4. Города и дороги — поселения, порты, крепости и пути между ними.")
                Text("5. Столицы — коснитесь города, чтобы сделать его столицей.")
                Text("6. Особые строения — храмы, башни магов, руины, шахты, порталы.")
                Text("7. Границы стран — обведите территорию; граница может идти и по воде.")
                Text("8. Информация о странах — заполните анкету каждого государства.")
                Spacer(Modifier.height(10.dp))
                Text("Выделенная область «⧉»", fontWeight = FontWeight.Bold)
                Text("Обведите прямоугольником кусок карты — откроется меню из пяти действий:")
                Text("• скопировать кусок в отдельную карту и заполнять её подробнее;")
                Text("• вставить сюда другую карту целиком;")
                Text("• сгенерировать рваное побережье — материк или острова;")
                Text("• разложить природные зоны по широте, с горами вдоль хребтов;")
                Text("• провести реки от гор к ближайшей воде.")
                Text("Любую генерацию можно отменить стрелкой ↶ и повторить.")
                Spacer(Modifier.height(10.dp))
                Text("Атлас: переходы между картами", fontWeight = FontWeight.Bold)
                Text("В карточке объекта можно выбрать «Подробная карта» — и с этого города " +
                    "или области получится перейти на её отдельную карту. У связанного " +
                    "объекта в углу появляется закладка, а в карточке выбора — кнопка «→ карта».")
                Spacer(Modifier.height(10.dp))
                Text("Вся карта — суша", fontWeight = FontWeight.Bold)
                Text("На первом шаге (или в меню ⋮) выберите «Вся карта — суша»: океана не будет, " +
                    "а моря и озёра рисуются инструментом «Озеро / море».")
                Spacer(Modifier.height(10.dp))
                Text("Боевая локация", fontWeight = FontWeight.Bold)
                Text("Третий вид карты — для боя: подземелье, таверна, поляна. Сетка по 5 футов, " +
                    "пол и земля с фото-текстурами, стены липнут к узлам сетки.")
                Text("• Фишки врагов и героев: у каждой здоровье, защита, размер, сторона " +
                    "(кольцо своего цвета), состояния и аура. Свою картинку можно вставить в фишку как портрет.")
                Text("• Выбранная фишка: кнопки −1, −5, +1 меняют здоровье, карандаш открывает карточку.")
                Text("• Туман войны и «Вид для игроков»: туман сплошной, спрятанное мастером не видно.")
                Text("• Линейка меряет расстояние в футах; на шаге «Бой»: инициатива, раунды, кубики, сцена.")
                Text("Фото-текстуры пола и земли — Poly Haven (polyhaven.com), общественное достояние CC0.",
                    style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(10.dp))
                Text("Авторский контент", fontWeight = FontWeight.Bold)
                Text("Меню ⋮ → «Авторский контент»: загрузите свою картинку и укажите, " +
                    "чем она станет — постройкой, зоной или объектом. Заготовка появится " +
                    "первой строкой в выборе зон, зданий и объектов (кнопка «＋ своё»), " +
                    "годится и для карты мира, и для карты города и остаётся в " +
                    "приложении для всех ваших карт.")
                Spacer(Modifier.height(10.dp))
                Text("Слои и вид карты", fontWeight = FontWeight.Bold)
                Text("Меню ⋮ → «Слои карты»: глаз прячет слой, замок защищает его от " +
                    "выбора и стирания. Меню ⋮ → «Вид карты»: готовые стили — пергамент, " +
                    "гравюра, цветная, тёмное фэнтези, набросок, с кнопкой «вернуть как было».")
                Spacer(Modifier.height(10.dp))
                Text("Сохранение", fontWeight = FontWeight.Bold)
                Text("Карта сохраняется автоматически. Через меню «⋮» её можно выгрузить как картинку PNG, файл проекта .json или текстовое описание стран.")
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Понятно") } }
    )
}

/** Полноэкранный список стран и анкеты государств (шаг 8). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountriesOverlay(
    viewModel: EditorViewModel,
    onExportText: () -> Unit,
    onClose: () -> Unit
) {
    val project = viewModel.project ?: return
    var editingId by remember { mutableStateOf<String?>(null) }
    val editing = project.countries.firstOrNull { it.id == editingId }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (editing == null) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Страны мира") },
                        navigationIcon = {
                            IconButton(onClick = onClose) {
                                Icon(Icons.Default.Close, contentDescription = "Закрыть")
                            }
                        },
                        actions = {
                            IconButton(onClick = { editingId = viewModel.addCountry("Новая страна") }) {
                                Icon(Icons.Default.Add, contentDescription = "Добавить страну")
                            }
                        }
                    )
                }
            ) { padding ->
                if (project.countries.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Стран пока нет. Вернитесь к шагу 7 и обведите территорию " +
                                "или нажмите «+», чтобы создать страну вручную.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(project.countries, key = { it.id }) { country ->
                            CountryRow(
                                country = country,
                                markerCount = project.markersOf(country.id).size,
                                onClick = { editingId = country.id }
                            )
                        }
                        item {
                            TextButton(onClick = onExportText, modifier = Modifier.fillMaxWidth()) {
                                Text("Сохранить описания стран в файл")
                            }
                        }
                    }
                }
            }
        } else {
            CountryEditor(
                viewModel = viewModel,
                country = editing,
                onBack = { editingId = null }
            )
        }
    }
}

@Composable
private fun CountryRow(country: Country, markerCount: Int, onClick: () -> Unit) {
    val filled = country.info.filledCount()
    ElevatedCard(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorDot(Color(country.color), size = 18)
                Spacer(Modifier.width(10.dp))
                Text(
                    country.name.ifBlank { "Без названия" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Областей: ${country.areas.size} · объектов: $markerCount",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (country.info.capital.isNotBlank()) {
                Text("Столица: ${country.info.capital}", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { filled.toFloat() / CountryInfo.FIELD_COUNT },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Анкета заполнена: $filled из ${CountryInfo.FIELD_COUNT}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryEditor(
    viewModel: EditorViewModel,
    country: Country,
    onBack: () -> Unit
) {
    var draft by remember(country.id) { mutableStateOf(country) }
    var confirmDelete by remember { mutableStateOf(false) }
    val project = viewModel.project

    fun push(updated: Country) {
        draft = updated
        viewModel.updateCountry(updated)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(draft.name.ifBlank { "Страна" }) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.updateCountry(draft)
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить страну")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = draft.name,
                onValueChange = { push(draft.copy(name = it)) },
                label = { Text("Название государства") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Text("Цвет на карте", style = MaterialTheme.typography.labelLarge)
            ColorRow(EditorViewModel.COUNTRY_COLORS, draft.color) { push(draft.copy(color = it)) }

            Spacer(Modifier.height(16.dp))
            Text("Анкета государства", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            val info = draft.info
            InfoField("Столица", info.capital) { push(draft.copy(info = info.copy(capital = it))) }
            InfoField("Правитель, династия", info.ruler) { push(draft.copy(info = info.copy(ruler = it))) }
            InfoField("Государственный строй", info.government) { push(draft.copy(info = info.copy(government = it))) }
            InfoField("Население", info.population) { push(draft.copy(info = info.copy(population = it))) }
            InfoField("Народы и расы", info.peoples) { push(draft.copy(info = info.copy(peoples = it))) }
            InfoField("Вера, боги", info.religion) { push(draft.copy(info = info.copy(religion = it))) }
            InfoField("Язык и письменность", info.language) { push(draft.copy(info = info.copy(language = it))) }
            InfoField("Монета, казна", info.currency) { push(draft.copy(info = info.copy(currency = it))) }
            InfoField("Войско и флот", info.army, lines = 2) { push(draft.copy(info = info.copy(army = it))) }
            InfoField("Хозяйство и ремёсла", info.economy, lines = 2) { push(draft.copy(info = info.copy(economy = it))) }
            InfoField("Культура и обычаи", info.culture, lines = 2) { push(draft.copy(info = info.copy(culture = it))) }
            InfoField("Отношения с соседями", info.relations, lines = 2) { push(draft.copy(info = info.copy(relations = it))) }
            InfoField("История", info.history, lines = 4) { push(draft.copy(info = info.copy(history = it))) }
            InfoField("Общее описание", info.description, lines = 4) { push(draft.copy(info = info.copy(description = it))) }

            val markers = project?.markersOf(draft.id).orEmpty()
            if (markers.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("Объекты страны (${markers.size})", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                markers.forEach { marker ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                        Column(Modifier.padding(10.dp)) {
                            Text(
                                marker.name.ifBlank { "Без названия" },
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                marker.type.title +
                                    if (marker.population.isNotBlank()) " · ${marker.population}" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Удалить страну?") },
            text = { Text("Территории и анкета «${draft.name}» будут удалены. Города останутся на карте.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.deleteCountry(draft.id)
                    onBack()
                }) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun InfoField(label: String, value: String, lines: Int = 1, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = lines == 1,
        minLines = lines,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

private val OCEAN_COLORS = listOf(
    0xFF6E9EBF.toInt(), 0xFF5A8CB0.toInt(), 0xFF43769B.toInt(), 0xFF2F5D7E.toInt(),
    0xFF7FB2C9.toInt(), 0xFF88A9B5.toInt(), 0xFF4E7F8E.toInt(), 0xFF9BC0D4.toInt()
)

private val LAND_COLORS = listOf(
    0xFFE8DCBE.toInt(), 0xFFEFE3C6.toInt(), 0xFFDDCFAA.toInt(), 0xFFD8C9A3.toInt(),
    0xFFE6D9B8.toInt(), 0xFFCFC29C.toInt(), 0xFFF2E8CF.toInt(), 0xFFC8BB94.toInt()
)

/**
 * Библиотека авторского контента: свои постройки, зоны и объекты.
 * Каждая заготовка — картинка плюс несколько настроек.
 */
@Composable
fun CustomAssetsDialog(
    viewModel: EditorViewModel,
    onAddImage: () -> Unit,
    onDismiss: () -> Unit
) {
    var editing by remember { mutableStateOf<CustomAsset?>(null) }
    val assets = viewModel.customAssets

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Авторский контент") },
        text = {
            Column(
                Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Загрузите свою картинку — и ставьте её на карту как постройку, " +
                        "зону или объект. Заготовки хранятся в приложении и годятся " +
                        "для всех ваших карт.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))

                if (assets.isEmpty()) {
                    Text("Пока пусто.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    for (asset in assets) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { editing = asset }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssetThumb(viewModel, asset, 40.dp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    asset.title.ifBlank { "Без названия" },
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "${asset.kind.title} · ${scopeTitle(asset.scope)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { viewModel.selectAsset(asset) }) { Text("Взять") }
                            IconButton(onClick = { viewModel.deleteAsset(asset.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Удалить заготовку")
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onAddImage) { Text("Загрузить картинку") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )

    val target = editing
    if (target != null) {
        AssetSettingsDialog(
            viewModel = viewModel,
            asset = target,
            title = "Заготовка",
            onSave = { viewModel.updateAsset(it); editing = null },
            onDismiss = { editing = null }
        )
    }
}

/** Что заполняется сразу после загрузки картинки. */
@Composable
fun NewAssetDialog(
    viewModel: EditorViewModel,
    onSave: (CustomAsset) -> Unit,
    onDismiss: () -> Unit
) {
    val draft = remember { CustomAsset(title = "Моя заготовка") }
    AssetSettingsDialog(
        viewModel = viewModel,
        asset = draft,
        title = "Новая заготовка",
        preview = null,
        onSave = onSave,
        onDismiss = onDismiss
    )
}

private fun scopeTitle(scope: MarkerScope): String = when (scope) {
    MarkerScope.BOTH -> "мир и город"
    MarkerScope.WORLD -> "только карта мира"
    MarkerScope.CITY -> "только карта города"
    MarkerScope.BATTLE -> "только боевая локация"
    MarkerScope.ALL -> "любая карта"
}

/** Общие настройки заготовки: название, вид, где применять, размер. */
@Composable
private fun AssetSettingsDialog(
    viewModel: EditorViewModel,
    asset: CustomAsset,
    title: String,
    preview: CustomAsset? = asset,
    onSave: (CustomAsset) -> Unit,
    onDismiss: () -> Unit
) {
    var draft by remember(asset.id) { mutableStateOf(asset) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (preview != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AssetThumb(viewModel, preview, 56.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Картинка уже в библиотеке.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = draft.title,
                    onValueChange = { draft = draft.copy(title = it) },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))

                Text("Чем ставить на карту", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(CustomKind.entries.toList()) { item ->
                        FilterChip(
                            selected = draft.kind == item,
                            onClick = { draft = draft.copy(kind = item) },
                            label = { Text(item.title) }
                        )
                    }
                }
                Text(
                    draft.kind.hint,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(10.dp))
                Text("Где пригодится", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(MarkerScope.entries.toList()) { item ->
                        FilterChip(
                            selected = draft.scope == item,
                            onClick = { draft = draft.copy(scope = item) },
                            label = { Text(scopeTitle(item)) }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                if (draft.kind == CustomKind.ZONE) {
                    Text(
                        "Размер плитки: ${draft.tile.toInt()}",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Slider(
                        value = draft.tile,
                        onValueChange = { draft = draft.copy(tile = it) },
                        valueRange = 20f..400f
                    )
                } else {
                    Text(
                        "Размер: ${"%.1f".format(draft.size)}×",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Slider(
                        value = draft.size,
                        onValueChange = { draft = draft.copy(size = it) },
                        valueRange = 0.3f..4f
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = draft.outlined,
                        onCheckedChange = { draft = draft.copy(outlined = it) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Обводить контур", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(draft.copy(title = draft.title.ifBlank { "Моя заготовка" })) }
            ) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

// ------------------------------------------------------------------ боевая локация

private fun numberOrNull(text: String): Int? = text.trim().replace("−", "-").toIntOrNull()

@Composable
private fun NumberField(label: String, value: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { text -> onChange(text.filter { it.isDigit() || it == '-' }.take(5)) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}

/** Карточка фишки: имя, сторона, размер, здоровье, защита, состояния, аура. */
@Composable
fun TokenEditDialog(viewModel: EditorViewModel, token: Token, onDismiss: () -> Unit) {
    var draft by remember(token.id) { mutableStateOf(token) }
    var hpText by remember(token.id) { mutableStateOf(token.hp.toString()) }
    var maxHpText by remember(token.id) { mutableStateOf(token.maxHp.toString()) }
    var acText by remember(token.id) { mutableStateOf(token.ac.toString()) }
    var initText by remember(token.id) { mutableStateOf(token.initiative?.toString().orEmpty()) }
    var group by remember(token.id) { mutableStateOf(token.type.group) }
    val portraits = viewModel.assetsOf(CustomKind.TOKEN)

    fun result(): Token = draft.copy(
        hp = numberOrNull(hpText) ?: draft.hp,
        maxHp = numberOrNull(maxHpText) ?: draft.maxHp,
        ac = numberOrNull(acText) ?: draft.ac,
        initiative = numberOrNull(initText)
    )

    AlertDialog(
        onDismissRequest = {
            viewModel.updateToken(result())
            onDismiss()
        },
        title = { Text(draft.title) },
        text = {
            Column(
                Modifier
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { draft = draft.copy(name = it) },
                    label = { Text("Имя") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NumberField("Здоровье", hpText, Modifier.weight(1f)) { hpText = it }
                    NumberField("Из", maxHpText, Modifier.weight(1f)) { maxHpText = it }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NumberField("Защита", acText, Modifier.weight(1f)) { acText = it }
                    NumberField("Инициатива", initText, Modifier.weight(1f)) { initText = it }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (delta in listOf(-10, -5, -1, 1, 5)) {
                        TextButton(
                            onClick = {
                                val max = numberOrNull(maxHpText) ?: draft.maxHp
                                val current = numberOrNull(hpText) ?: draft.hp
                                hpText = (current + delta).coerceIn(0, maxOf(max, 0)).toString()
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) { Text(if (delta > 0) "+$delta" else "$delta") }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text("Сторона", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(TokenFaction.entries.toList()) { item ->
                        FilterChip(
                            selected = draft.faction == item,
                            onClick = { draft = draft.copy(faction = item) },
                            label = { Text(item.title) },
                            leadingIcon = { ColorDot(Color(item.color)) }
                        )
                    }
                }
                Text("Размер", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(TokenSize.entries.toList()) { item ->
                        FilterChip(
                            selected = draft.size == item,
                            onClick = { draft = draft.copy(size = item) },
                            label = { Text(item.title) }
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))
                Text("Состояния", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(Condition.entries.toList()) { item ->
                        val on = item in draft.conditions
                        FilterChip(
                            selected = on,
                            onClick = {
                                draft = draft.copy(
                                    conditions = if (on) draft.conditions - item else draft.conditions + item
                                )
                            },
                            label = { Text(item.title) },
                            leadingIcon = { ColorDot(Color(item.color)) }
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    if (draft.aura > 0) "Аура: ${draft.aura} фт" else "Аура: нет",
                    style = MaterialTheme.typography.labelLarge
                )
                Slider(
                    value = draft.aura.toFloat(),
                    onValueChange = { draft = draft.copy(aura = (it / 5f).toInt() * 5) },
                    valueRange = 0f..60f
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = draft.showLabel, onCheckedChange = { draft = draft.copy(showLabel = it) })
                    Spacer(Modifier.width(8.dp))
                    Text("Подписывать имя")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = draft.hidden, onCheckedChange = { draft = draft.copy(hidden = it) })
                    Spacer(Modifier.width(8.dp))
                    Text("Спрятать от игроков")
                }

                Spacer(Modifier.height(6.dp))
                Text("Кто это", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(TokenGroup.entries.toList()) { item ->
                        FilterChip(selected = group == item, onClick = { group = item }, label = { Text(item.title) })
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(TokenType.byGroup(group)) { item ->
                        FilterChip(
                            selected = draft.type == item,
                            onClick = { draft = draft.copy(type = item) },
                            label = { Text(item.title) }
                        )
                    }
                }

                if (portraits.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text("Портрет", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = draft.assetId == null,
                                onClick = { draft = draft.copy(assetId = null) },
                                label = { Text("Значок") }
                            )
                        }
                        items(portraits) { asset ->
                            FilterChip(
                                selected = draft.assetId == asset.id,
                                onClick = { draft = draft.copy(assetId = asset.id) },
                                label = { Text(asset.title) },
                                leadingIcon = { AssetThumb(viewModel, asset) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = draft.notes,
                    onValueChange = { draft = draft.copy(notes = it) },
                    label = { Text("Заметки мастера: атаки, тактика, добыча") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.updateToken(result())
                onDismiss()
            }) { Text("Готово") }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.duplicateToken(token.id)
                onDismiss()
            }) { Text("Копия") }
        }
    )
}

/** Список инициативы: кто за кем ходит, раунд, быстрые правки здоровья. */
@Composable
fun InitiativeDialog(viewModel: EditorViewModel, onDismiss: () -> Unit) {
    val project = viewModel.project ?: return
    val order = viewModel.initiativeOrder()
    val activeId = viewModel.activeTokenId()
    val waiting = project.tokens.filter { it.initiative == null && !it.dead }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Инициатива · раунд ${project.scene.round}") },
        text = {
            Column(
                Modifier
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(selected = false, onClick = { viewModel.rollInitiative(onlyMissing = false) }, label = { Text("🎲 Всем заново") })
                    }
                    item {
                        FilterChip(selected = false, onClick = { viewModel.rollInitiative(onlyMissing = true) }, label = { Text("🎲 Кто без") })
                    }
                    item {
                        FilterChip(selected = false, onClick = { viewModel.endCombat() }, label = { Text("Конец боя") })
                    }
                }
                Spacer(Modifier.height(6.dp))
                if (order.isEmpty()) {
                    Text(
                        "Никто ещё не бросал инициативу. Нажмите «Всем заново» или впишите число в карточке фишки.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                for (token in order) {
                    val active = token.id == activeId
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(
                                if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                            .clickable { viewModel.selection = com.fantasymap.creator.model.Selection.TokenSel(token.id) }
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${token.initiative}",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(34.dp)
                        )
                        ColorDot(Color(token.faction.color))
                        Spacer(Modifier.width(6.dp))
                        Column(Modifier.weight(1f)) {
                            Text(token.title, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                            if (token.maxHp > 0) {
                                Text(
                                    "здоровье ${token.hp}/${token.maxHp} · защита ${token.ac}" +
                                        if (token.conditions.isNotEmpty()) " · " + token.conditions.joinToString { it.title } else "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        TextButton(
                            onClick = { viewModel.changeHp(token.id, -1) },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) { Text("−1") }
                        TextButton(
                            onClick = { viewModel.changeHp(token.id, -5) },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) { Text("−5") }
                        TextButton(
                            onClick = { viewModel.changeHp(token.id, 1) },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) { Text("+1") }
                    }
                    HorizontalDivider()
                }
                if (waiting.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Без инициативы: " + waiting.joinToString { it.title },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = { viewModel.nextTurn() }) { Text("▶ Следующий ход") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}

/** Кубики: к4…к100, количество, модификатор, преимущество и помеха. */
@Composable
fun DiceDialog(viewModel: EditorViewModel, onDismiss: () -> Unit) {
    var count by remember { mutableIntStateOf(1) }
    var bonus by remember { mutableIntStateOf(0) }
    val log = viewModel.diceLog

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Кубики") },
        text = {
            Column(
                Modifier
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Сколько: $count", Modifier.weight(1f))
                    TextButton(onClick = { if (count > 1) count-- }) { Text("−") }
                    TextButton(onClick = { if (count < 20) count++ }) { Text("+") }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Модификатор: " + if (bonus >= 0) "+$bonus" else "$bonus",
                        Modifier.weight(1f)
                    )
                    TextButton(onClick = { if (bonus > -20) bonus-- }) { Text("−") }
                    TextButton(onClick = { if (bonus < 30) bonus++ }) { Text("+") }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf(4, 6, 8, 10, 12, 20, 100)) { sides ->
                        FilledTonalButton(
                            onClick = { viewModel.rollDice(count, sides, bonus) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                        ) { Text("к$sides") }
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.rollDice(1, 20, bonus, advantage = 1) },
                            label = { Text("к20 с преимуществом") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.rollDice(1, 20, bonus, advantage = -1) },
                            label = { Text("к20 с помехой") }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                for ((index, line) in log.withIndex()) {
                    Text(
                        line,
                        style = if (index == 0) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodySmall,
                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}

/** Описание сцены для мастера: цель, тактика врагов, награда, заметки. */
@Composable
fun SceneDialog(viewModel: EditorViewModel, onDismiss: () -> Unit) {
    val project = viewModel.project ?: return
    var draft by remember { mutableStateOf(project.scene) }
    AlertDialog(
        onDismissRequest = {
            viewModel.updateScene(draft)
            onDismiss()
        },
        title = { Text("Сцена") },
        text = {
            Column(
                Modifier
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = draft.goal,
                    onValueChange = { draft = draft.copy(goal = it) },
                    label = { Text("Цель героев") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = draft.enemyTactics,
                    onValueChange = { draft = draft.copy(enemyTactics = it) },
                    label = { Text("Тактика врагов") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = draft.reward,
                    onValueChange = { draft = draft.copy(reward = it) },
                    label = { Text("Награда и добыча") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = draft.notes,
                    onValueChange = { draft = draft.copy(notes = it) },
                    label = { Text("Заметки мастера") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.updateScene(draft)
                onDismiss()
            }) { Text("Готово") }
        }
    )
}

/** Сетка: вид, футы в клетке, заметность, прилипание фишек. */
@Composable
fun GridDialog(viewModel: EditorViewModel, onDismiss: () -> Unit) {
    val project = viewModel.project ?: return
    var kind by remember { mutableStateOf(project.gridKind) }
    var feet by remember { mutableIntStateOf(project.feetPerCell) }
    var opacity by remember { mutableFloatStateOf(project.style.gridOpacity) }
    AlertDialog(
        onDismissRequest = {
            viewModel.setGrid(kind, feet, opacity)
            onDismiss()
        },
        title = { Text("Сетка") },
        text = {
            Column {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(GridKind.entries.toList()) { item ->
                        FilterChip(selected = kind == item, onClick = { kind = item }, label = { Text(item.title) })
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Одна клетка: $feet фт", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf(5, 10, 15, 30, 50, 100)) { item ->
                        FilterChip(selected = feet == item, onClick = { feet = item }, label = { Text("$item фт") })
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Заметность сетки", style = MaterialTheme.typography.labelLarge)
                Slider(value = opacity, onValueChange = { opacity = it }, valueRange = 0.05f..1f)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = project.style.snapToGrid, onCheckedChange = { viewModel.toggleSnap() })
                    Spacer(Modifier.width(8.dp))
                    Text("Фишки и стены липнут к клеткам")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = project.style.photoTextures, onCheckedChange = { viewModel.togglePhotoTextures() })
                    Spacer(Modifier.width(8.dp))
                    Text("Фото-текстуры пола и земли")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.setGrid(kind, feet, opacity)
                onDismiss()
            }) { Text("Готово") }
        }
    )
}
