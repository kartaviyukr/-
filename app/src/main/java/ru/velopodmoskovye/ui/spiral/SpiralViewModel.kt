package ru.velopodmoskovye.ui.spiral

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import ru.velopodmoskovye.data.content.ContentPack
import ru.velopodmoskovye.data.content.ContentRepository
import ru.velopodmoskovye.data.progress.ProgressRepository
import ru.velopodmoskovye.domain.model.SpiralPlace
import ru.velopodmoskovye.domain.progress.DestinationProgress
import ru.velopodmoskovye.domain.progress.ProgressCalculator
import javax.inject.Inject

data class SpiralRow(
    val place: SpiralPlace,
    val regionName: String,
    val progress: DestinationProgress?,
    val complete: Boolean,
)

enum class SpiralFilter(val label: String) { ALL("Все"), STARS("★ и ☆"), READY("С маршрутом"), TODO("Не пройдено") }

data class SpiralUiState(
    val loading: Boolean = true,
    val rows: List<SpiralRow> = emptyList(),
    val filter: SpiralFilter = SpiralFilter.ALL,
    val total: Int = 0,
    val complete: Int = 0,
    /** Первое непройденное место по спирали — куда ехать дальше. */
    val next: SpiralPlace? = null,
    val activeDestinationId: String? = null,
)

@HiltViewModel
class SpiralViewModel @Inject constructor(
    content: ContentRepository,
    progress: ProgressRepository,
) : ViewModel() {
    private val filter = MutableStateFlow(SpiralFilter.ALL)

    val state: StateFlow<SpiralUiState> = combine(
        flow { emit(content.pack()) }, progress.visitedIds, filter, progress.activeRoute,
    ) { pack, visited, f, active ->
        val rows = buildRows(pack, visited)
        val shown = rows.filter { r ->
            when (f) {
                SpiralFilter.ALL -> true
                SpiralFilter.STARS -> r.place.mustHave >= 2
                SpiralFilter.READY -> r.place.destinationId != null
                SpiralFilter.TODO -> !r.complete
            }
        }
        SpiralUiState(
            loading = false, rows = shown, filter = f,
            total = rows.size, complete = rows.count { it.complete },
            next = rows.firstOrNull { !it.complete }?.place,
            activeDestinationId = active?.destinationId,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpiralUiState())

    fun setFilter(f: SpiralFilter) { filter.value = f }

    companion object {
        /** Строго по спирали: одно место — один веломаршрут. */
        fun buildRows(pack: ContentPack, visited: Set<String>): List<SpiralRow> = pack.spiral.places.map { p ->
            val prog = p.destinationId?.let { pack.destinations[it] }?.let { ProgressCalculator.destination(it, visited) }
            SpiralRow(p, pack.regionNames[p.regionId] ?: p.regionId, prog, prog?.complete == true)
        }
    }
}
