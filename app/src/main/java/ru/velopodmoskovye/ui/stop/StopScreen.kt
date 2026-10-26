package ru.velopodmoskovye.ui.stop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import ru.velopodmoskovye.data.content.ContentRepository
import ru.velopodmoskovye.data.db.VisitEntity
import ru.velopodmoskovye.data.progress.ProgressRepository
import ru.velopodmoskovye.domain.model.Stop
import ru.velopodmoskovye.domain.progress.Geofence
import ru.velopodmoskovye.ui.Format
import ru.velopodmoskovye.ui.common.FactItem
import ru.velopodmoskovye.ui.common.InfoLine
import ru.velopodmoskovye.ui.common.Paragraphs
import ru.velopodmoskovye.ui.common.SectionTitle
import ru.velopodmoskovye.ui.common.WarningCard
import ru.velopodmoskovye.ui.common.openInMaps
import ru.velopodmoskovye.ui.common.rememberTts
import ru.velopodmoskovye.ui.theme.ProgressGreen
import javax.inject.Inject

data class StopUiState(val loading: Boolean = true, val stop: Stop? = null, val destinationName: String = "", val visit: VisitEntity? = null)

@HiltViewModel
class StopViewModel @Inject constructor(
    savedState: SavedStateHandle,
    content: ContentRepository,
    progress: ProgressRepository,
) : ViewModel() {
    private val destId: String = checkNotNull(savedState["destId"])
    private val stopId: String = checkNotNull(savedState["stopId"])

    val state: StateFlow<StopUiState> = combine(flow { emit(content.pack()) }, progress.visits) { pack, visits ->
        val dest = pack.destinations[destId]
        StopUiState(false, dest?.stops?.firstOrNull { it.id == stopId }, dest?.name ?: "", visits.firstOrNull { it.stopId == stopId })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StopUiState())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopScreen(onBack: () -> Unit, vm: StopViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val tts = rememberTts()
    val stop = state.stop
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.destinationName) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") } },
                actions = {
                    if (stop != null) IconButton(onClick = { context.openInMaps(stop.coords, stop.name) }) { Icon(Icons.Filled.Place, "На карте") }
                },
            )
        },
    ) { padding ->
        if (stop == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if (state.loading) CircularProgressIndicator() else Text("Точка не найдена")
            }
            return@Scaffold
        }
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp)) {
            item {
                Text(stop.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    stop.kind + (if (stop.optional) " · по желанию" else "") + " · ~${Format.minutes(stop.visitMinutes)} на точке",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                state.visit?.let {
                    Text("Пройдено: ${Format.dateTime(it.visitedAt)}", color = ProgressGreen, style = MaterialTheme.typography.labelLarge)
                }
                Column(Modifier.padding(top = 8.dp)) {
                    InfoLine("Адрес", stop.address ?: "")
                    InfoLine("Часы работы", stop.openingHoursNote)
                    InfoLine("Билеты", stop.ticketNote)
                    InfoLine("Велосипед", stop.bikeParkingNote)
                    InfoLine("Зона отметки", "${Geofence.radiusFor(stop)} м" + if (stop.zonePolygon != null) " или контур объекта" else "")
                }
                stop.warnings.forEach { WarningCard(it) }
                if (tts.ready) {
                    OutlinedButton(
                        onClick = { if (tts.speaking) tts.stop() else tts.speak(listOf(stop.name) + stop.guideStory) },
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.RecordVoiceOver, null)
                            Spacer(Modifier.width(6.dp))
                            Text(if (tts.speaking) "Остановить" else "Озвучить рассказ")
                        }
                    }
                }
                SectionTitle("Рассказ экскурсовода")
                Paragraphs(stop.guideStory)
                SectionTitle("Факты")
            }
            items(stop.facts) { FactItem(it) }
        }
    }
}
