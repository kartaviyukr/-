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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fantasymap.creator.editor.EditorViewModel
import com.fantasymap.creator.model.Country
import com.fantasymap.creator.model.CountryInfo
import com.fantasymap.creator.model.MapLabel
import com.fantasymap.creator.model.MapStyle
import com.fantasymap.creator.model.Marker
import com.fantasymap.creator.model.MarkerGroup
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
                    items(MarkerGroup.entries.toList()) { item ->
                        FilterChip(
                            selected = group == item,
                            onClick = { group = item },
                            label = { Text(item.title) }
                        )
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(MarkerType.byGroup(group)) { item ->
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
                Text("Поворот: ${draft.rotation.toInt()}°", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = draft.rotation,
                    onValueChange = { draft = draft.copy(rotation = it) },
                    valueRange = -90f..90f
                )
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

/** Настройки отображения карты. */
@Composable
fun StyleDialog(viewModel: EditorViewModel, onDismiss: () -> Unit) {
    val project = viewModel.project ?: return
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
    onPng: (Int) -> Unit,
    onJson: () -> Unit,
    onText: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Сохранить результат") },
        text = {
            Column {
                Text("Картинка карты (PNG)", fontWeight = FontWeight.Bold)
                TextButton(onClick = { onPng(2048) }) { Text("PNG — обычный размер (2048 px)") }
                TextButton(onClick = { onPng(4096) }) { Text("PNG — большой размер (4096 px)") }
                TextButton(onClick = { onPng(6144) }) { Text("PNG — для печати (6144 px)") }
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                Text("Проект и описания", fontWeight = FontWeight.Bold)
                TextButton(onClick = onJson) { Text("Файл проекта (.json) — можно открыть снова") }
                TextButton(onClick = onText) { Text("Описания стран (.txt)") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}

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
                    "Кнопка «Выровнять границы зон» наводит порядок: соседние области " +
                    "одного ландшафта сливаются в одну, а у разных ландшафтов убирается " +
                    "наложение — общая граница становится одной линией.")
                Text("3. Природные объекты — реки, хребты, вершины, пещеры, водопады.")
                Text("4. Города и дороги — поселения, порты, крепости и пути между ними.")
                Text("5. Столицы — коснитесь города, чтобы сделать его столицей.")
                Text("6. Особые строения — храмы, башни магов, руины, шахты, порталы.")
                Text("7. Границы стран — обведите территорию; граница может идти и по воде.")
                Text("8. Информация о странах — заполните анкету каждого государства.")
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
