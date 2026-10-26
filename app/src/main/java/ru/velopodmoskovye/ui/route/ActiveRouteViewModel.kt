package ru.velopodmoskovye.ui.route

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.velopodmoskovye.data.content.ContentRepository
import ru.velopodmoskovye.data.db.VisitEntity
import ru.velopodmoskovye.data.location.LocationTracker
import ru.velopodmoskovye.data.progress.ProgressRepository
import ru.velopodmoskovye.data.settings.SettingsRepository
import ru.velopodmoskovye.domain.geo.Geo
import ru.velopodmoskovye.domain.model.Destination
import ru.velopodmoskovye.domain.model.Stop
import ru.velopodmoskovye.domain.progress.DestinationProgress
import ru.velopodmoskovye.domain.progress.Fix
import ru.velopodmoskovye.domain.progress.Geofence
import ru.velopodmoskovye.domain.progress.ProgressCalculator
import javax.inject.Inject

data class ActiveUiState(
    val loading: Boolean = true,
    val destination: Destination? = null,
    val visits: Map<String, VisitEntity> = emptyMap(),
    val current: Stop? = null,
    val distanceToCurrentKm: Double? = null,
    val fix: Fix? = null,
    val progress: DestinationProgress? = null,
    val isActive: Boolean = false,
)

sealed interface ManualMarkState {
    data object Idle : ManualMarkState
    data class Confirm(val stop: Stop, val distanceM: Double, val fix: Fix) : ManualMarkState
    data class Refused(val stop: Stop, val message: String) : ManualMarkState
}

@HiltViewModel
class ActiveRouteViewModel @Inject constructor(
    savedState: SavedStateHandle,
    content: ContentRepository,
    private val progress: ProgressRepository,
    settings: SettingsRepository,
    private val tracker: LocationTracker,
) : ViewModel() {
    private val id: String = checkNotNull(savedState["id"])

    val state: StateFlow<ActiveUiState> = combine(
        flow { emit(content.pack()) }, progress.visits, tracker.last, progress.activeRoute, settings.settings,
    ) { pack, visits, fix, active, _ ->
        val dest = pack.destinations[id] ?: return@combine ActiveUiState(loading = false)
        val byStop = visits.filter { it.destinationId == dest.id }.associateBy { it.stopId }
        val current = dest.stops.firstOrNull { it.id !in byStop }
        ActiveUiState(
            loading = false,
            destination = dest,
            visits = byStop,
            current = current,
            distanceToCurrentKm = if (current != null && fix != null) Geo.distanceKm(fix.point, current.coords) else null,
            fix = fix,
            progress = ProgressCalculator.destination(dest, byStop.keys),
            isActive = active?.destinationId == dest.id,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ActiveUiState())

    private val _manual = MutableStateFlow<ManualMarkState>(ManualMarkState.Idle)
    val manual: StateFlow<ManualMarkState> = _manual.asStateFlow()

    init {
        viewModelScope.launch { tracker.current() }
    }

    /** «Я здесь»: сначала свежая координата, потом проверка расстояния, потом подтверждение. */
    fun requestManualMark(stop: Stop) {
        viewModelScope.launch {
            val fix = tracker.current() ?: tracker.last.value
            _manual.value = when (val r = Geofence.manualCheck(stop, fix)) {
                is Geofence.ManualResult.Allowed -> ManualMarkState.Confirm(stop, r.distanceM, fix!!)
                is Geofence.ManualResult.TooFar -> ManualMarkState.Refused(stop, "До точки ${(r.distanceM / 1000).let { "%.1f".format(it) }} км. Ручная отметка возможна не дальше 1 км.")
                Geofence.ManualResult.NoFix -> ManualMarkState.Refused(stop, "Нет координат. Включите геолокацию и попробуйте ещё раз.")
            }
        }
    }

    fun confirmManualMark() {
        val c = _manual.value as? ManualMarkState.Confirm ?: return
        _manual.value = ManualMarkState.Idle
        viewModelScope.launch { progress.markVisited(c.stop.id, id, c.fix, "manual", c.distanceM) }
    }

    fun dismissManual() { _manual.value = ManualMarkState.Idle }

    fun finish(onDone: () -> Unit) {
        viewModelScope.launch {
            progress.finishRoute()
            onDone()
        }
    }
}
