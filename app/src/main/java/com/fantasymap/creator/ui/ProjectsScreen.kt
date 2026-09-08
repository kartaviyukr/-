package com.fantasymap.creator.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fantasymap.creator.editor.EditorViewModel
import com.fantasymap.creator.model.MapProject
import com.fantasymap.creator.model.ProjectSummary
import com.fantasymap.creator.model.Stage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(viewModel: EditorViewModel) {
    var showCreate by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<ProjectSummary?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) viewModel.importProject(uri) }

    LaunchedEffect(viewModel.message) {
        val text = viewModel.message
        if (text != null) {
            snackbarHostState.showSnackbar(text)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Карты фэнтезийных миров", fontWeight = FontWeight.Bold)
                        Text(
                            "рисуйте свой мир от берегов до столиц",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) }) {
                        Text("Загрузить")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreate = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Новый мир") }
            )
        }
    ) { padding ->
        if (viewModel.projects.isEmpty()) {
            EmptyProjects(Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(viewModel.projects, key = { it.id }) { summary ->
                    ProjectCard(
                        summary = summary,
                        onOpen = { viewModel.openProject(summary.id) },
                        onDuplicate = { viewModel.duplicateProject(summary.id) },
                        onDelete = { deleteTarget = summary }
                    )
                }
            }
        }
    }

    if (showCreate) {
        CreateProjectDialog(
            onDismiss = { showCreate = false },
            onCreate = { name, width, height ->
                showCreate = false
                viewModel.createProject(name, width, height)
            }
        )
    }

    val target = deleteTarget
    if (target != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Удалить карту?") },
            text = { Text("«${target.name}» будет удалена без возможности восстановления.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProject(target.id)
                    deleteTarget = null
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun EmptyProjects(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("🗺", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(12.dp))
            Text(
                "Здесь пока нет ни одного мира",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Нажмите «Новый мир» и пройдите восемь шагов: " +
                    "континенты → природные зоны → реки и горы → города и дороги → " +
                    "столицы → особые строения → границы стран → описания стран.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProjectCard(
    summary: ProjectSummary,
    onOpen: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru")) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(summary.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Шаг ${summary.stage} из 8 · ${Stage.byNumber(summary.stage).title}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Суша: ${summary.landCount} · объектов: ${summary.markerCount} · стран: ${summary.countryCount}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Изменено: ${formatter.format(Date(summary.updatedAt))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDuplicate) { Text("Копия") }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить")
                }
                TextButton(onClick = onOpen) { Text("Открыть") }
            }
        }
    }
}

@Composable
private fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Float, Float) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(MapProject.PRESETS.firstOrNull { it.title == "Один континент" } ?: MapProject.PRESETS.first()) }
    var custom by remember { mutableStateOf(false) }
    var customWidth by remember { mutableFloatStateOf(2400f) }
    var customHeight by remember { mutableFloatStateOf(1600f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый мир") },
        text = {
            Column(
                Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название мира") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Карта любого размера сразу видна целиком: двумя пальцами её можно " +
                        "двигать и приближать. Чем больше размер, тем больше подробностей поместится.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Свой размер", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                    Switch(checked = custom, onCheckedChange = { custom = it })
                }

                if (custom) {
                    Text(
                        "${customWidth.toInt()} × ${customHeight.toInt()}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Ширина", style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = customWidth,
                        onValueChange = { customWidth = it },
                        valueRange = MapProject.MIN_WORLD_SIZE..MapProject.MAX_WORLD_SIZE
                    )
                    Text("Высота", style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = customHeight,
                        onValueChange = { customHeight = it },
                        valueRange = MapProject.MIN_WORLD_SIZE..MapProject.MAX_WORLD_SIZE
                    )
                } else {
                    MapProject.PRESET_GROUPS.forEach { group ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            group,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        MapProject.PRESETS.filter { it.group == group }.forEach { preset ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = selected == preset,
                                        onClick = { selected = preset }
                                    )
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selected == preset,
                                    onClick = { selected = preset }
                                )
                                Spacer(Modifier.width(4.dp))
                                Column {
                                    Text(preset.title, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        preset.caption,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (custom) {
                    onCreate(name.trim(), customWidth, customHeight)
                } else {
                    onCreate(name.trim(), selected.width, selected.height)
                }
            }) { Text("Создать") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
