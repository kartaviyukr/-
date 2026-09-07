package com.fantasymap.creator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fantasymap.creator.editor.EditorViewModel
import com.fantasymap.creator.model.BiomeGroup
import com.fantasymap.creator.model.BiomeType
import com.fantasymap.creator.model.LabelStyle
import com.fantasymap.creator.model.LineFeatureType
import com.fantasymap.creator.model.MarkerGroup
import com.fantasymap.creator.model.MarkerType
import com.fantasymap.creator.model.RoadType
import com.fantasymap.creator.model.Stage
import com.fantasymap.creator.model.Tool
import com.fantasymap.creator.model.WaterKind

/** Нижняя панель редактора: этапы, инструменты и выбор вида объекта. */
@Composable
fun EditorBottomPanel(
    viewModel: EditorViewModel,
    onOpenCountries: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Column(Modifier.padding(bottom = 6.dp)) {
            StageBar(viewModel.stage) { viewModel.setStage(it) }
            Text(
                text = viewModel.stage.hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
            )
            ToolBar(viewModel)
            ContextPicker(viewModel, onOpenCountries)
        }
    }
}

@Composable
private fun StageBar(current: Stage, onSelect: (Stage) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(Stage.entries.toList()) { stage ->
            val selected = stage == current
            FilterChip(
                selected = selected,
                onClick = { onSelect(stage) },
                label = {
                    Text(
                        "${stage.number}. ${stage.title}",
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
private fun ToolBar(viewModel: EditorViewModel) {
    val tools = viewModel.toolsFor(viewModel.stage)
    LazyRow(
        contentPadding = PaddingValues(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(tools) { tool ->
            FilterChip(
                selected = viewModel.tool == tool,
                onClick = { viewModel.tool = tool },
                label = { Text("${tool.icon}  ${tool.title}") }
            )
        }
    }
}

@Composable
private fun ContextPicker(viewModel: EditorViewModel, onOpenCountries: () -> Unit) {
    when {
        viewModel.stage == Stage.COUNTRIES -> {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Заполните анкеты государств",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onOpenCountries) { Text("Открыть список стран") }
            }
        }

        viewModel.tool == Tool.BIOME -> BiomePicker(viewModel)
        viewModel.tool == Tool.MARKER -> MarkerPicker(viewModel)
        viewModel.tool == Tool.LINE -> LinePicker(viewModel)
        viewModel.tool == Tool.ROAD -> RoadPicker(viewModel)
        viewModel.tool == Tool.WATER -> WaterPicker(viewModel)
        viewModel.tool == Tool.LABEL -> LabelPicker(viewModel)
        viewModel.tool == Tool.COUNTRY -> CountryPicker(viewModel, onOpenCountries)
        viewModel.stage == Stage.CAPITALS -> Text(
            "Коснитесь города на карте — он станет столицей выбранной страны.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
        else -> Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun BiomePicker(viewModel: EditorViewModel) {
    val group = viewModel.biome.group
    Column {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(BiomeGroup.entries.toList()) { item ->
                FilterChip(
                    selected = group == item,
                    onClick = {
                        BiomeType.byGroup(item).firstOrNull()?.let { viewModel.biome = it }
                    },
                    label = { Text(item.title) }
                )
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(BiomeType.byGroup(group)) { item ->
                FilterChip(
                    selected = viewModel.biome == item,
                    onClick = { viewModel.biome = item },
                    label = { Text(item.title) },
                    leadingIcon = { ColorDot(Color(item.color)) }
                )
            }
        }
    }
}

@Composable
private fun MarkerPicker(viewModel: EditorViewModel) {
    Column {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(MarkerGroup.entries.toList()) { item ->
                FilterChip(
                    selected = viewModel.markerGroup == item,
                    onClick = {
                        viewModel.markerGroup = item
                        MarkerType.byGroup(item).firstOrNull()?.let { viewModel.markerType = it }
                    },
                    label = { Text(item.title) }
                )
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(MarkerType.byGroup(viewModel.markerGroup)) { item ->
                FilterChip(
                    selected = viewModel.markerType == item,
                    onClick = { viewModel.markerType = item },
                    label = { Text(item.title) }
                )
            }
        }
    }
}

@Composable
private fun LinePicker(viewModel: EditorViewModel) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(LineFeatureType.entries.toList()) { item ->
            FilterChip(
                selected = viewModel.lineType == item,
                onClick = { viewModel.lineType = item },
                label = { Text(item.title) },
                leadingIcon = { ColorDot(Color(item.color)) }
            )
        }
    }
}

@Composable
private fun RoadPicker(viewModel: EditorViewModel) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(RoadType.entries.toList()) { item ->
            FilterChip(
                selected = viewModel.roadType == item,
                onClick = { viewModel.roadType = item },
                label = { Text(item.title) },
                leadingIcon = { ColorDot(Color(item.color)) }
            )
        }
    }
}

@Composable
private fun WaterPicker(viewModel: EditorViewModel) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(WaterKind.entries.toList()) { item ->
            FilterChip(
                selected = viewModel.waterKind == item,
                onClick = { viewModel.waterKind = item },
                label = { Text(item.title) },
                leadingIcon = { ColorDot(Color(item.color)) }
            )
        }
    }
}

@Composable
private fun LabelPicker(viewModel: EditorViewModel) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(LabelStyle.entries.toList()) { item ->
            FilterChip(
                selected = viewModel.labelStyle == item,
                onClick = { viewModel.labelStyle = item },
                label = { Text(item.title) }
            )
        }
    }
}

@Composable
private fun CountryPicker(viewModel: EditorViewModel, onOpenCountries: () -> Unit) {
    val countries = viewModel.project?.countries.orEmpty()
    Column {
        if (countries.isEmpty()) {
            Text(
                "Обведите территорию — страна будет создана автоматически.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(countries) { country ->
                FilterChip(
                    selected = viewModel.activeCountryId == country.id,
                    onClick = { viewModel.activeCountryId = country.id },
                    label = { Text(country.name.ifBlank { "Без названия" }) },
                    leadingIcon = { ColorDot(Color(country.color)) }
                )
            }
            item {
                FilterChip(
                    selected = false,
                    onClick = { viewModel.addCountry("Новая страна") },
                    label = { Text("+ страна") },
                    colors = FilterChipDefaults.filterChipColors()
                )
            }
            item {
                FilterChip(
                    selected = false,
                    onClick = onOpenCountries,
                    label = { Text("Список стран") }
                )
            }
        }
    }
}

@Composable
fun ColorDot(color: Color, size: Int = 14) {
    Box(
        Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
    )
}

/** Небольшая цветная карточка-подпись (используется в списках стран). */
@Composable
fun CountryBadge(name: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ColorDot(color)
        Spacer(Modifier.width(6.dp))
        Text(name, style = MaterialTheme.typography.labelLarge)
    }
}
