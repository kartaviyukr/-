package com.fantasymap.creator.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fantasymap.creator.editor.EditorViewModel
import com.fantasymap.creator.model.BiomeType
import com.fantasymap.creator.model.BuildingGroup
import com.fantasymap.creator.model.BuildingType
import com.fantasymap.creator.model.CustomAsset
import com.fantasymap.creator.model.CustomKind
import com.fantasymap.creator.model.DistrictType
import com.fantasymap.creator.model.MapStage
import com.fantasymap.creator.model.LabelStyle
import com.fantasymap.creator.model.LineFeatureType
import com.fantasymap.creator.model.RoadType
import com.fantasymap.creator.model.Stage
import com.fantasymap.creator.model.Tool
import com.fantasymap.creator.model.WaterKind

/** Нижняя панель редактора: этапы, инструменты и выбор вида объекта. */
@Composable
fun EditorBottomPanel(
    viewModel: EditorViewModel,
    onOpenCountries: () -> Unit,
    onOpenAssets: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Column(Modifier.navigationBarsPadding()) {
            val expanded = viewModel.panelExpanded
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.panelExpanded = !expanded }
                    .padding(start = 14.dp, end = 6.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${viewModel.stage.number}. ${viewModel.stage.title}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { viewModel.panelExpanded = !expanded },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Text(if (expanded) "▾ свернуть" else "▴ развернуть")
                }
            }
            if (expanded) {
                StageBar(viewModel.stages(), viewModel.stage) { viewModel.selectStage(it) }
                Text(
                    text = viewModel.stage.hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }
            ToolBar(viewModel)
            if (expanded) {
                ContextPicker(viewModel, onOpenCountries, onOpenAssets)
                // Пустое место под последней строкой: до неё легко дотянуться,
                // и она не прячется за системной панелью навигации.
                Spacer(Modifier.height(44.dp))
            } else {
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun StageBar(stages: List<MapStage>, current: MapStage, onSelect: (MapStage) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(stages) { stage ->
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
private fun ContextPicker(
    viewModel: EditorViewModel,
    onOpenCountries: () -> Unit,
    onOpenAssets: () -> Unit
) {
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

        viewModel.tool == Tool.BUILDING -> BuildingPicker(viewModel, onOpenAssets)
        viewModel.tool == Tool.DISTRICT -> DistrictPicker(viewModel)
        viewModel.tool == Tool.BIOME -> BiomePicker(viewModel, onOpenAssets)
        viewModel.tool == Tool.MARKER -> MarkerPicker(viewModel, onOpenAssets)
        viewModel.tool == Tool.LINE -> LinePicker(viewModel)
        viewModel.tool == Tool.ROAD -> RoadPicker(viewModel)
        viewModel.tool == Tool.WATER -> WaterPicker(viewModel)
        viewModel.tool == Tool.LABEL -> LabelPicker(viewModel)
        viewModel.tool == Tool.COUNTRY -> CountryPicker(viewModel, onOpenCountries)
        viewModel.tool == Tool.FRAGMENT -> Text(
            "Обведите прямоугольником кусок карты — можно скопировать его в отдельную " +
                "карту, сгенерировать там побережье, разложить природные зоны или провести реки.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
        viewModel.stage == Stage.CAPITALS -> Text(
            "Коснитесь города на карте — он станет столицей выбранной страны.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
        else -> Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun BiomePicker(viewModel: EditorViewModel, onOpenAssets: () -> Unit) {
    val group = viewModel.biome.group
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { viewModel.alignBiomeBorders() },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
            ) {
                Text("⇲ Выровнять границы зон")
            }
            Text(
                "сливает одинаковые, убирает наложения",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        CustomAssetRow(viewModel, CustomKind.ZONE, viewModel.customZone, onOpenAssets)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(viewModel.biomeGroups()) { item ->
                FilterChip(
                    selected = group == item && viewModel.customZone == null,
                    onClick = {
                        BiomeType.byGroup(item).firstOrNull()?.let { viewModel.selectBiome(it) }
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
                    selected = viewModel.biome == item && viewModel.customZone == null,
                    onClick = { viewModel.selectBiome(item) },
                    label = { Text(item.title) },
                    leadingIcon = { ColorDot(Color(item.color)) }
                )
            }
        }
    }
}

@Composable
private fun BuildingPicker(viewModel: EditorViewModel, onOpenAssets: () -> Unit) {
    Column {
        Text(
            "Касание ставит дом, протяжка задаёт его размер.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp)
        )
        CustomAssetRow(viewModel, CustomKind.BUILDING, viewModel.customBuilding, onOpenAssets)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(BuildingGroup.entries.toList()) { item ->
                FilterChip(
                    selected = viewModel.buildingGroup == item && viewModel.customBuilding == null,
                    onClick = {
                        viewModel.buildingGroup = item
                        BuildingType.byGroup(item).firstOrNull()?.let { viewModel.selectBuildingType(it) }
                    },
                    label = { Text(item.title) }
                )
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(BuildingType.byGroup(viewModel.buildingGroup)) { item ->
                FilterChip(
                    selected = viewModel.buildingType == item && viewModel.customBuilding == null,
                    onClick = { viewModel.selectBuildingType(item) },
                    label = { Text(item.title) },
                    leadingIcon = { ColorDot(Color(item.color)) }
                )
            }
        }
    }
}

@Composable
private fun DistrictPicker(viewModel: EditorViewModel) {
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { viewModel.fillDistrictWithHouses() },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
            ) {
                Text("🏘 Застроить квартал домами")
            }
            Text(
                "выберите квартал",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(DistrictType.entries.toList()) { item ->
                FilterChip(
                    selected = viewModel.districtType == item,
                    onClick = { viewModel.districtType = item },
                    label = { Text(item.title) },
                    leadingIcon = { ColorDot(Color(item.color)) }
                )
            }
        }
    }
}

@Composable
private fun MarkerPicker(viewModel: EditorViewModel, onOpenAssets: () -> Unit) {
    Column {
        CustomAssetRow(viewModel, CustomKind.OBJECT, viewModel.customObject, onOpenAssets)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(viewModel.markerGroups()) { item ->
                FilterChip(
                    selected = viewModel.markerGroup == item && viewModel.customObject == null,
                    onClick = { viewModel.selectMarkerGroup(item) },
                    label = { Text(item.title) }
                )
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(viewModel.markerTypes()) { item ->
                FilterChip(
                    selected = viewModel.markerType == item && viewModel.customObject == null,
                    onClick = { viewModel.selectMarkerType(item) },
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
    Column {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                FilterChip(
                    selected = viewModel.labelCurved,
                    onClick = { viewModel.labelCurved = !viewModel.labelCurved },
                    label = { Text(if (viewModel.labelCurved) "〜 вдоль кривой" else "• в точке") }
                )
            }
            items(LabelStyle.entries.toList()) { item ->
                FilterChip(
                    selected = viewModel.labelStyle == item,
                    onClick = { viewModel.labelStyle = item },
                    label = { Text(item.title) }
                )
            }
        }
        if (viewModel.labelCurved) {
            Text(
                "Проведите линию — подпись изогнётся по ней.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp)
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

/**
 * Строка авторских заготовок: свои картинки, поставленные в библиотеку.
 * Первая кнопка открывает саму библиотеку, где картинку можно добавить.
 */
@Composable
private fun CustomAssetRow(
    viewModel: EditorViewModel,
    kind: CustomKind,
    selected: CustomAsset?,
    onOpenAssets: () -> Unit
) {
    val assets = viewModel.assetsOf(kind)
    LazyRow(
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            FilterChip(
                selected = false,
                onClick = onOpenAssets,
                label = { Text(if (assets.isEmpty()) "＋ своё, с картинкой" else "＋ своё") }
            )
        }
        items(assets) { asset ->
            FilterChip(
                selected = selected?.id == asset.id,
                onClick = {
                    if (selected?.id == asset.id) viewModel.clearAsset(kind) else viewModel.selectAsset(asset)
                },
                label = { Text(asset.title) },
                leadingIcon = { AssetThumb(viewModel, asset) }
            )
        }
    }
}

/** Значок авторской заготовки: её же картинка. */
@Composable
fun AssetThumb(viewModel: EditorViewModel, asset: CustomAsset, size: Dp = 22.dp) {
    val bitmap = remember(asset.id, asset.createdAt) { viewModel.assetStore.texture(asset.id) }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(4.dp))
        )
    } else {
        ColorDot(Color(asset.color))
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
