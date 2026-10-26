package ru.velopodmoskovye.ui.spiral

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.velopodmoskovye.domain.progress.ProgressCalculator
import ru.velopodmoskovye.domain.spiral.SpiralBuilder
import ru.velopodmoskovye.ui.Format
import ru.velopodmoskovye.ui.theme.ProgressGreen
import ru.velopodmoskovye.ui.theme.StarGold

@Composable
fun SpiralScreen(
    onOpenPlace: (String) -> Unit,
    onOpenActive: (String) -> Unit,
    vm: SpiralViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    if (state.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp)) {
            item {
                Column(Modifier.padding(16.dp)) {
                    Text("Спираль от Кремля", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "${state.total} мест: старт в ЦАО, затем округа Москвы внутри МКАД и дальше кольцами наружу. " +
                            "У каждого места — свой веломаршрут. Пройдено: ${state.complete}.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    state.next?.let {
                        Text(
                            "Следующее по спирали: ${it.num}. ${it.name}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(Modifier.padding(4.dp))
                    LinearProgressIndicator(
                        progress = { if (state.total == 0) 0f else state.complete.toFloat() / state.total },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(Modifier.horizontalScroll(rememberScrollState()).padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SpiralFilter.entries.forEach { f ->
                            FilterChip(selected = state.filter == f, onClick = { vm.setFilter(f) }, label = { Text(f.label) })
                        }
                    }
                }
            }
            val rows = state.rows
            items(rows, key = { it.place.id }) { row ->
                val ringHeader = rows.indexOf(row).let { i -> i == 0 || rows[i - 1].place.ring != row.place.ring }
                Column {
                    if (ringHeader) RingHeader(row.place.ring)
                    PlaceRow(row, state.activeDestinationId) {
                        val id = row.place.destinationId
                        if (id != null) {
                            if (id == state.activeDestinationId) onOpenActive(id) else onOpenPlace(id)
                        }
                    }
                }
            }
        }
        state.activeDestinationId?.let { id ->
            ExtendedFloatingActionButton(
                onClick = { onOpenActive(id) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                icon = { Icon(Icons.AutoMirrored.Filled.DirectionsBike, null) },
                text = { Text("Активный маршрут") },
            )
        }
    }
}

@Composable
private fun RingHeader(ring: Int) {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Text(
            if (ring == SpiralBuilder.INSIDE_MKAD_RING) ProgressCalculator.ringName(ring) else "Кольцо ${ProgressCalculator.ringName(ring)}",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun PlaceRow(row: SpiralRow, activeId: String?, onClick: () -> Unit) {
    val p = row.place
    val ready = p.destinationId != null
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp)
            .alpha(if (ready || row.complete) 1f else 0.7f)
            .clickable(enabled = ready, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (p.destinationId != null && p.destinationId == activeId) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = if (row.complete) ProgressGreen else MaterialTheme.colorScheme.surface, modifier = Modifier.size(34.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    if (row.complete) Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.surface)
                    else Text("${p.num}", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(p.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (p.mustHave >= 2) Text("  ${Format.stars(p.mustHave)}", color = StarGold, fontWeight = FontWeight.Bold)
                }
                Text(
                    "${p.kindNote} · ${row.regionName} · ${Format.km(p.distanceFromKremlinKm)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                when {
                    p.start -> Text("старт спирали · маршрут ${if (ready) "готов" else "готовится"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    ready -> Text("маршрут готов", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    else -> Text("маршрут готовится", style = MaterialTheme.typography.labelSmall)
                }
                row.progress?.takeIf { it.started && !it.complete }?.let {
                    LinearProgressIndicator(progress = { it.fraction }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                }
            }
        }
    }
}
