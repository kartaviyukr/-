package ru.velopodmoskovye.ui.spiral

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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

/** Найденная точка маршрута: открывается сразу экраном точки. */
data class StopHit(
    val destinationId: String,
    val stopId: String,
    val stopName: String,
    val placeName: String,
)

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
    /** Строка поиска по местам и точкам. */
    val query: String = "",
    /** Точки маршрутов, подходящие под поиск. */
    val stopHits: List<StopHit> = emptyList(),
)

@HiltViewModel
class SpiralViewModel @Inject constructor(
    content: ContentRepository,
    progress: ProgressRepository,
    private val tracker: LocationTracker,
) : ViewModel() {
    private val filter = MutableStateFlow(SpiralFilter.ALL)
    private val sort = MutableStateFlow(SpiralSort.SPIRAL)
    private val query = MutableStateFlow("")

    val state: StateFlow<SpiralUiState> = combine(
        combine(content.packs, progress.visitedIds, progress.activeRoute) { a, b, c -> Triple(a, b, c) },
        filter, sort, tracker.last, query,
    ) { (pack, visited, active), f, s, me, q ->
        val rows = buildRows(pack, visited, me?.point)
        val found = search(pack, rows, q)
        val filtered = found.rows.filter { r ->
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
            query = q, stopHits = found.stops,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpiralUiState())

    fun setFilter(f: SpiralFilter) { filter.value = f }

    fun setQuery(q: String) { query.value = q }

    fun setSort(s: SpiralSort) {
        sort.value = s
        if (s == SpiralSort.NEAR_ME && tracker.last.value == null) viewModelScope.launch { tracker.current() }
    }

    data class SearchResult(val rows: List<SpiralRow>, val stops: List<StopHit>)

    companion object {
        private fun norm(s: String) = s.lowercase().replace('ё', 'е')

        /**
         * Поиск по названию места, округа и по точкам маршрутов (название, рассказ не учитывается).
         * Пустой запрос — всё без изменений; место попадает в список, если совпало оно само или любая его точка.
         */
        fun search(pack: ContentPack, rows: List<SpiralRow>, query: String, maxStops: Int = 40): SearchResult {
            val words = norm(query).split(' ', ',').filter { it.isNotBlank() }
            if (words.isEmpty()) return SearchResult(rows, emptyList())
            fun hit(text: String) = norm(text).let { t -> words.all { it in t } }
            val stops = mutableListOf<StopHit>()
            val matchedPlaces = mutableSetOf<String>()
            for (r in rows) {
                val dest = r.place.destinationId?.let { pack.destinations[it] }
                if (hit(r.place.name) || hit(r.regionName)) matchedPlaces += r.place.id
                dest?.stops?.forEach { st ->
                    if (hit(st.name) || hit("${st.name} ${r.place.name}")) {
                        matchedPlaces += r.place.id
                        if (stops.size < maxStops) stops += StopHit(dest.id, st.id, st.name, r.place.name)
                    }
                }
            }
            return SearchResult(rows.filter { it.place.id in matchedPlaces }, stops)
        }

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
