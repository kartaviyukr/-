package ru.velopodmoskovye.ui.route

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.velopodmoskovye.domain.model.Stop
import ru.velopodmoskovye.service.RouteTrackingService
import ru.velopodmoskovye.ui.Format
import ru.velopodmoskovye.ui.common.FactItem
import ru.velopodmoskovye.ui.common.Paragraphs
import ru.velopodmoskovye.ui.common.SectionTitle
import ru.velopodmoskovye.ui.common.WarningCard
import ru.velopodmoskovye.ui.common.rememberTts
import ru.velopodmoskovye.ui.map.MapData
import ru.velopodmoskovye.ui.map.MapLibreView
import ru.velopodmoskovye.ui.theme.ProgressGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveRouteScreen(
    onBack: () -> Unit,
    onOpenStop: (String, String) -> Unit,
    vm: ActiveRouteViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val manual by vm.manual.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val tts = rememberTts()
    val dest = state.destination

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(dest?.name ?: "Маршрут") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") } },
                actions = {
                    if (state.isActive) {
                        TextButton(onClick = {
                            RouteTrackingService.stop(context)
                            vm.finish(onBack)
                        }) {
                            Icon(Icons.Filled.Stop, null)
                            Text("Завершить")
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (state.loading || dest == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp)) {
            item {
                val p = state.progress
                if (p != null) {
                    Text(
                        "Точки: ${p.requiredVisited + p.optionalVisited} из ${p.required + p.optional}" +
                            if (p.complete) " · звание получено" else "",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (p.complete) ProgressGreen else MaterialTheme.colorScheme.onSurface,
                    )
                    LinearProgressIndicator(progress = { p.fraction }, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp))
                }
                if (!state.isActive) {
                    Text(
                        "Слежение не запущено — отметки ставятся только вручную. Запустите маршрут на экране места.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                MapLibreView(
                    layers = MapData.routeLayers(dest, state.visits.keys) + MapData.me(state.fix?.point),
                    fitTo = MapData.routePoints(dest),
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                )
            }
            val current = state.current
            if (current == null) {
                item {
                    Card(Modifier.fillMaxWidth().padding(top = 12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Все точки пройдены!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Финиш: ${dest.endPoint.name} (${dest.endPoint.transport})")
                        }
                    }
                }
            } else {
                item { CurrentStop(current, state.distanceToCurrentKm, tts.ready, tts.speaking, onSpeak = {
                    if (tts.speaking) tts.stop() else tts.speak(listOf(current.name) + current.guideStory)
                }, onHere = { vm.requestManualMark(current) }, onOpen = { onOpenStop(dest.id, current.id) }) }
            }
            item { SectionTitle("Чек-лист") }
            items(dest.stops, key = { it.id }) { s ->
                val visit = state.visits[s.id]
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp).testTag("check_${s.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (visit != null) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked, null,
                        tint = if (visit != null) ProgressGreen else MaterialTheme.colorScheme.outline,
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(s.name + if (s.optional) " (по желанию)" else "", style = MaterialTheme.typography.bodyLarge)
                        if (visit != null) {
                            Text(
                                (if (visit.method == "auto") "отмечено автоматически" else "отмечено вручную") +
                                    " · " + Format.dateTime(visit.visitedAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    TextButton(onClick = { onOpenStop(dest.id, s.id) }) { Text("Рассказ") }
                }
            }
        }
    }

    when (val m = manual) {
        is ManualMarkState.Confirm -> AlertDialog(
            onDismissRequest = vm::dismissManual,
            title = { Text("Вы на точке «${m.stop.name}»?") },
            text = {
                Text(
                    "До точки ${Format.meters(m.distanceM)} по GPS (точность ±${m.fix.accuracyM.toInt()} м). " +
                        "Координаты и время отметки сохранятся.",
                )
            },
            confirmButton = { Button(onClick = vm::confirmManualMark, modifier = Modifier.testTag("confirm_here")) { Text("Я здесь") } },
            dismissButton = { TextButton(onClick = vm::dismissManual) { Text("Отмена") } },
        )
        is ManualMarkState.Refused -> AlertDialog(
            onDismissRequest = vm::dismissManual,
            title = { Text("Отметить не получится") },
            text = { Text(m.message) },
            confirmButton = { TextButton(onClick = vm::dismissManual) { Text("Понятно") } },
        )
        ManualMarkState.Idle -> Unit
    }
}

@Composable
private fun CurrentStop(
    stop: Stop, distanceKm: Double?, ttsReady: Boolean, speaking: Boolean,
    onSpeak: () -> Unit, onHere: () -> Unit, onOpen: () -> Unit,
) {
    Card(Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Следующая точка", style = MaterialTheme.typography.labelMedium)
            Text(stop.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                (distanceKm?.let { "через ${Format.km(it)} по прямой" } ?: "ищем координаты…") + " · ${stop.kind}",
                style = MaterialTheme.typography.bodyMedium,
            )
            stop.warnings.forEach { WarningCard(it) }
            Paragraphs(stop.guideStory)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onHere, modifier = Modifier.testTag("im_here")) { Text("Я здесь") }
                if (ttsReady) {
                    OutlinedButton(onClick = onSpeak) {
                        Icon(Icons.Filled.RecordVoiceOver, null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (speaking) "Стоп" else "Озвучить")
                    }
                }
                TextButton(onClick = onOpen) { Text("Факты") }
            }
            stop.facts.take(3).forEach { FactItem(it) }
        }
    }
}
