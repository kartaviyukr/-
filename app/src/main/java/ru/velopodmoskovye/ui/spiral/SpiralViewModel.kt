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
import kotlinx.coroutines.launch
import ru.velopodmoskovye.data.content.ContentPack
import ru.velopodmoskovye.data.content.ContentRepository
import ru.velopodmoskovye.data.location.LocationTracker
import ru.velopodmoskovye.data.progress.ProgressRepository
import ru.velopodmoskovye.domain.geo.Geo
import ru.velopodmoskovye.domain.model.GeoPoint
import ru.velopodmoskovye.domain.model.SpiralPlace
import ru.velopodmoskovye.domain.progress.DestinationProgress
import ru.velopodmoskovye.domain.progress.ProgressCalculator
import javax.inject.Inject

data class SpiralRow(
    val place: SpiralPlace,
    val regionName: String,
    val progress: DestinationProgress?,
    val complete: Boolean,
    /** Длина веломаршрута, км (null — маршрута нет). */
    val routeKm: Double? = null,
    val stopsCount: Int = 0,
    /** Расстояние от меня до места, км — если известна моя позиция. */
    val fromMeKm: Double? = null,
)

enum class SpiralSort(val label: String) {
    SPIRAL("По спирали"),
    ROUTE_LONG("Длинные маршруты"),
    ROUTE_SHORT("Короткие маршруты"),
    NEAR_ME("Ближе ко мне"),
    KREMLIN_NEAR("Ближе к Кремлю"),
    KREMLIN_FAR("Дальше от Кремля"),
    STOPS("Больше точек"),
}

enum class SpiralFilter(val label: String) { ALL("Все"), STARS("★ и ☆"), READY("С маршрутом"), TODO("Не пройдено") }

data class SpiralUiState(
    val loading: Boolean = true,
    val rows: List<SpiralRow> = emptyList(),
    val filter: SpiralFilter = SpiralFilter.ALL,
    val sort: SpiralSort = SpiralSort.SPIRAL,
    /** Сортировка «ближе ко мне» выбрана, а позиции пока нет. */
    val waitingLocation: Boolean = false,
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
    private val tracker: LocationTracker,
) : ViewModel() {
    private val filter = MutableStateFlow(SpiralFilter.ALL)
    private val sort = MutableStateFlow(SpiralSort.SPIRAL)

    val state: StateFlow<SpiralUiState> = combine(
        combine(flow { emit(content.pack()) }, progress.visitedIds, progress.activeRoute) { a, b, c -> Triple(a, b, c) },
        filter, sort, tracker.last,
    ) { (pack, visited, active), f, s, me ->
        val rows = buildRows(pack, visited, me?.point)
        val filtered = rows.filter { r ->
            when (f) {
                SpiralFilter.ALL -> true
                SpiralFilter.STARS -> r.place.mustHave >= 2
                SpiralFilter.READY -> r.place.destinationId != null
                SpiralFilter.TODO -> !r.complete
            }
        }
        val shown = sorted(filtered, s)
        SpiralUiState(
            loading = false, rows = shown, filter = f, sort = s,
            waitingLocation = s == SpiralSort.NEAR_ME && me == null,
            total = rows.size, complete = rows.count { it.complete },
            next = rows.firstOrNull { !it.complete }?.place,
            activeDestinationId = active?.destinationId,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpiralUiState())

    fun setFilter(f: SpiralFilter) { filter.value = f }

    fun setSort(s: SpiralSort) {
        sort.value = s
        if (s == SpiralSort.NEAR_ME && tracker.last.value == null) viewModelScope.launch { tracker.current() }
    }

    companion object {
        /** Строго по спирали: одно место — один веломаршрут. */
        fun buildRows(pack: ContentPack, visited: Set<String>, me: GeoPoint? = null): List<SpiralRow> = pack.spiral.places.map { p ->
            val dest = p.destinationId?.let { pack.destinations[it] }
            val prog = dest?.let { ProgressCalculator.destination(it, visited) }
            SpiralRow(
                p, pack.regionNames[p.regionId] ?: p.regionId, prog, prog?.complete == true,
                routeKm = dest?.route?.distanceKm,
                stopsCount = dest?.stops?.size ?: 0,
                fromMeKm = me?.let { Geo.distanceM(it, p.coords) / 1000.0 },
            )
        }

        /** Сортировка списка. Места без нужного значения уходят в конец, при равенстве — порядок спирали. */
        fun sorted(rows: List<SpiralRow>, sort: SpiralSort): List<SpiralRow> {
            val spiral = compareBy<SpiralRow> { it.place.num }
            fun <T : Comparable<T>> by(key: (SpiralRow) -> T?, desc: Boolean = false): Comparator<SpiralRow> =
                Comparator<SpiralRow> { a, b ->
                    val ka = key(a); val kb = key(b)
                    when {
                        ka == null && kb == null -> 0
                        ka == null -> 1
                        kb == null -> -1
                        desc -> kb.compareTo(ka)
                        else -> ka.compareTo(kb)
                    }
                }.then(spiral)
            return when (sort) {
                SpiralSort.SPIRAL -> rows.sortedWith(spiral)
                SpiralSort.ROUTE_LONG -> rows.sortedWith(by({ it.routeKm }, desc = true))
                SpiralSort.ROUTE_SHORT -> rows.sortedWith(by({ it.routeKm }))
                SpiralSort.NEAR_ME -> rows.sortedWith(by({ it.fromMeKm }))
                SpiralSort.KREMLIN_NEAR -> rows.sortedWith(by({ it.place.distanceFromKremlinKm }))
                SpiralSort.KREMLIN_FAR -> rows.sortedWith(by({ it.place.distanceFromKremlinKm }, desc = true))
                SpiralSort.STOPS -> rows.sortedWith(by({ it.stopsCount.takeIf { n -> n > 0 } }, desc = true))
            }
        }
    }
}
