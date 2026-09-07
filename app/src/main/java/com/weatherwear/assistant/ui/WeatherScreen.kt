package com.weatherwear.assistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherwear.assistant.UiState
import com.weatherwear.assistant.data.DailyPoint
import com.weatherwear.assistant.data.HourlyPoint
import com.weatherwear.assistant.data.WeatherBundle
import com.weatherwear.assistant.domain.Advice
import com.weatherwear.assistant.domain.UmbrellaVerdict
import com.weatherwear.assistant.domain.WeatherCodes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    state: UiState,
    onRefresh: () -> Unit,
    onUseLocation: () -> Unit,
    onOpenSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onQueryChange: (String) -> Unit,
    onPlaceSelected: (com.weatherwear.assistant.data.Place) -> Unit,
) {
    if (state.searchOpen) {
        SearchScreen(
            query = state.searchQuery,
            searching = state.searching,
            results = state.searchResults,
            onQueryChange = onQueryChange,
            onPlaceSelected = onPlaceSelected,
            onClose = onCloseSearch,
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.bundle?.place?.name ?: "Что надеть?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        val subtitle = state.bundle?.place?.subtitle
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onUseLocation) {
                        Icon(Icons.Filled.MyLocation, contentDescription = "Моё местоположение")
                    }
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Filled.Search, contentDescription = "Выбрать город")
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Обновить")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            Column(Modifier.fillMaxSize()) {
                if (state.loading) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }

                val bundle = state.bundle
                val advice = state.advice

                if (bundle == null || advice == null) {
                    EmptyState(state = state, onRefresh = onRefresh)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        state.locationHint?.let { hint ->
                            item { HintCard(hint) }
                        }
                        state.error?.let { error ->
                            item { ErrorCard(error, onRefresh) }
                        }
                        item { CurrentCard(bundle) }
                        item { AdviceCard(advice) }
                        item { UmbrellaCard(advice) }
                        item { OutfitCard(advice) }
                        if (advice.accessories.isNotEmpty()) {
                            item { AccessoriesCard(advice) }
                        }
                        if (advice.warnings.isNotEmpty()) {
                            item { WarningsCard(advice) }
                        }
                        if (bundle.hourly.isNotEmpty()) {
                            item { HourlyCard(bundle) }
                        }
                        if (bundle.daily.isNotEmpty()) {
                            item { DailyCard(bundle) }
                        }
                        item { FooterNote(bundle) }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(state: UiState, onRefresh: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (state.loading) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text(state.locationHint ?: "Загружаем прогноз…")
            } else {
                Text(
                    text = state.error ?: "Прогноз пока не загружен",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onRefresh) { Text("Повторить") }
            }
        }
    }
}

@Composable
private fun HintCard(hint: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Text(
            text = hint,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun ErrorCard(error: String, onRefresh: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            TextButton(onClick = onRefresh) { Text("Повторить") }
        }
    }
}

@Composable
private fun CurrentCard(bundle: WeatherBundle) {
    val current = bundle.current
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = WeatherCodes.emoji(current.weatherCode, current.isDay),
                    fontSize = 52.sp,
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = formatSignedTemp(current.temperature),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = WeatherCodes.description(current.weatherCode),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MetricColumn("Ощущается", formatSignedTemp(current.apparentTemperature))
                MetricColumn("Ветер", formatWind(current.windSpeed))
                MetricColumn("Порывы", formatWind(current.windGusts))
                MetricColumn("Влажность", "${current.humidity}%")
            }
        }
    }
}

@Composable
private fun MetricColumn(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun AdviceCard(advice: Advice) {
    Card {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = advice.headline,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = advice.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun UmbrellaCard(advice: Advice) {
    val emoji = when (advice.umbrella) {
        UmbrellaVerdict.NOT_NEEDED -> "🚫☂️"
        UmbrellaVerdict.JUST_IN_CASE -> "🌂"
        UmbrellaVerdict.TAKE_IT -> "☔"
        UmbrellaVerdict.USE_HOOD_INSTEAD -> "🧥"
        UmbrellaVerdict.SNOW_EXPECTED -> "❄️"
    }
    val container = when (advice.umbrella) {
        UmbrellaVerdict.NOT_NEEDED -> MaterialTheme.colorScheme.surfaceVariant
        UmbrellaVerdict.JUST_IN_CASE -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    Card(colors = CardDefaults.cardColors(containerColor = container)) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = emoji, fontSize = 34.sp)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = "Зонт",
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = advice.umbrellaText,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun OutfitCard(advice: Advice) {
    Card {
        Column(Modifier.padding(20.dp)) {
            SectionTitle("👕 Как одеться")
            Spacer(Modifier.height(10.dp))
            advice.layers.forEach { layer ->
                BulletRow(layer)
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👟", fontSize = 20.sp)
                Spacer(Modifier.width(10.dp))
                Text(advice.footwear, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun AccessoriesCard(advice: Advice) {
    Card {
        Column(Modifier.padding(20.dp)) {
            SectionTitle("🎒 Не забудьте")
            Spacer(Modifier.height(10.dp))
            advice.accessories.forEach { item -> BulletRow(item) }
        }
    }
}

@Composable
private fun WarningsCard(advice: Advice) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = "⚠️ Обратите внимание",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(Modifier.height(10.dp))
            advice.warnings.forEach { warning ->
                Text(
                    text = "• $warning",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(vertical = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun HourlyCard(bundle: WeatherBundle) {
    val startOfHour = bundle.localNow.withMinute(0).withSecond(0).withNano(0)
    val hours = bundle.hourly.filter { !it.time.isBefore(startOfHour) }.take(24)
    if (hours.isEmpty()) return

    Card {
        Column(Modifier.padding(vertical = 20.dp)) {
            Box(Modifier.padding(horizontal = 20.dp)) {
                SectionTitle("🕒 Ближайшие часы")
            }
            Spacer(Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(hours) { hour -> HourColumn(hour) }
            }
        }
    }
}

@Composable
private fun HourColumn(hour: HourlyPoint) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp),
    ) {
        Text(
            text = formatHour(hour.time),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(text = WeatherCodes.emoji(hour.weatherCode, hour.time.hour in 7..20), fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = formatSignedTemp(hour.temperature),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = if (hour.precipitationProbability > 0) "${hour.precipitationProbability}%" else " ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun DailyCard(bundle: WeatherBundle) {
    Card {
        Column(Modifier.padding(20.dp)) {
            SectionTitle("📅 Ближайшие дни")
            Spacer(Modifier.height(8.dp))
            bundle.daily.forEachIndexed { index, day ->
                if (index > 0) HorizontalDivider(Modifier.padding(vertical = 6.dp))
                DailyRow(day, bundle.localNow.toLocalDate())
            }
        }
    }
}

@Composable
private fun DailyRow(day: DailyPoint, today: java.time.LocalDate) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.width(112.dp)) {
            Text(
                text = formatDayTitle(day.date, today),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = formatDate(day.date),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(text = WeatherCodes.emoji(day.weatherCode), fontSize = 22.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (day.precipitationProbabilityMax > 0) "${day.precipitationProbabilityMax}%" else "",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(48.dp),
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = "${formatSignedTemp(day.temperatureMin)} … ${formatSignedTemp(day.temperatureMax)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun FooterNote(bundle: WeatherBundle) {
    Column(Modifier.padding(top = 8.dp, bottom = 24.dp)) {
        Text(
            text = "Обновлено в ${formatTime(bundle.localNow)} по местному времени",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Данные: Open-Meteo.com (бесплатный API без ключа)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun BulletRow(text: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp, end = 10.dp)
                .size(6.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp)),
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}
