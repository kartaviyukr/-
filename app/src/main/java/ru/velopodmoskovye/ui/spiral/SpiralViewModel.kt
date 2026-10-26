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
import ru.velopodmoskovye.data.progress.ProgressRepository
import ru.velopodmoskovye.data.settings.SettingsRepository
import ru.velopodmoskovye.domain.model.GeoPoint
import ru.velopodmoskovye.domain.model.SpiralPlace
import ru.velopodmoskovye.domain.progress.DestinationProgress
import ru.velopodmoskovye.domain.progress.ProgressCalculator
import ru.velopodmoskovye.domain.spiral.DayPlanner
import ru.velopodmoskovye.domain.spiral.Leg
import ru.velopodmoskovye.domain.spiral.SpiralBuilder
import ru.velopodmoskovye.domain.spiral.SpiralNode
import ru.velopodmoskovye.domain.spiral.Transfer
import ru.velopodmoskovye.domain.spiral.Transfers
import ru.velopodmoskovye.data.content.ContentRepository
import javax.inject.Inject

data class SpiralRow(
    val place: SpiralPlace,
    val regionName: String,
    val progress: DestinationProgress?,
    val complete: Boolean,
    /** Место входит в маршрут другого места (усадьба-перемычка). */
    val coveredByName: String?,
    val transferIn: Transfer?,
    val day: Int,
    val dayStart: Boolean,
    val overnight: Boolean,
    val dayKm: Double,
)

enum class SpiralFilter(val label: String) { ALL("Все"), STARS("★ и ☆"), READY("С маршрутом"), TODO("Не пройдено") }

data class SpiralUiState(
    val loading: Boolean = true,
    val rows: List<SpiralRow> = emptyList(),
    val filter: SpiralFilter = SpiralFilter.ALL,
    val kmPerDay: Int = 70,
    val total: Int = 0,
    val complete: Int = 0,
    val days: Int = 0,
    val activeDestinationId: String? = null,
)

@HiltViewModel
class SpiralViewModel @Inject constructor(
    content: ContentRepository,
    progress: ProgressRepository,
    settings: SettingsRepository,
) : ViewModel() {
    private val filter = MutableStateFlow(SpiralFilter.ALL)

    val state: StateFlow<SpiralUiState> = combine(
        flow { emit(content.pack()) }, progress.visitedIds, settings.settings, filter, progress.activeRoute,
    ) { pack, visited, s, f, active ->
        val rows = buildRows(pack, visited, s.kmPerDay.toDouble(), s.speedKmh.toDouble())
        val shown = rows.filter { r ->
            when (f) {
                SpiralFilter.ALL -> true
                SpiralFilter.STARS -> r.place.mustHave >= 2
                SpiralFilter.READY -> r.place.destinationId != null
                SpiralFilter.TODO -> !r.complete
            }
        }
        SpiralUiState(
            loading = false, rows = shown, filter = f, kmPerDay = s.kmPerDay,
            total = rows.size, complete = rows.count { it.complete },
            days = rows.maxOfOrNull { it.day } ?: 0, activeDestinationId = active?.destinationId,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpiralUiState())

    fun setFilter(f: SpiralFilter) { filter.value = f }

    companion object {
        /** Сколько км по месту без готового маршрута — грубая оценка для разбивки на дни. */
        const val DEFAULT_PLACE_KM = 10.0

        fun buildRows(pack: ContentPack, visited: Set<String>, kmPerDay: Double, speedKmh: Double): List<SpiralRow> {
            val places = pack.spiral.places
            val nodes = places.map { SpiralNode(it, it.num, it.ring, it.distanceFromKremlinKm, it.azimuthDeg) }
            val blocks = SpiralBuilder.azimuthBlocks(nodes)
            val seenDest = mutableSetOf<String>()
            var prevEnd: Pair<String, GeoPoint>? = null
            val transfers = mutableListOf<Transfer?>()
            val legs = places.mapIndexed { i, p ->
                val dest = p.destinationId?.let { pack.destinations[it] }
                val first = dest != null && seenDest.add(dest.id)
                val start = if (first) dest!!.startPoint.coords else p.coords
                val end = if (first) dest!!.endPoint.coords else p.coords
                val t = prevEnd?.let { (id, at) -> Transfers.between(id, at, p.id, start, speedKmh) }
                transfers += t
                prevEnd = p.id to end
                val placeKm = when {
                    dest == null -> DEFAULT_PLACE_KM
                    first -> dest.route.distanceKm
                    else -> 0.0
                }
                Leg(p.id, t?.rideKm ?: 0.0, placeKm, blocks[i])
            }
            val days = DayPlanner.plan(legs, kmPerDay)
            val dayOf = HashMap<String, Int>()
            val dayKm = HashMap<Int, Double>()
            days.forEach { d -> d.placeIds.forEach { dayOf[it] = d.index }; dayKm[d.index] = d.km }
            val overnight = days.map { it.overnightAt }.toSet()
            return places.mapIndexed { i, p ->
                val dest = p.destinationId?.let { pack.destinations[it] }
                val prog = dest?.let { ProgressCalculator.destination(it, visited) }
                val complete = prog?.complete == true || (p.finish && ProgressCalculator.KREMLIN_STOP_ID in visited)
                val day = dayOf[p.id] ?: 0
                SpiralRow(
                    place = p,
                    regionName = pack.regionNames[p.regionId] ?: p.regionId,
                    progress = prog,
                    complete = complete,
                    coveredByName = dest?.takeIf { it.id != p.id }?.name,
                    transferIn = transfers[i],
                    day = day,
                    dayStart = i == 0 || dayOf[places[i - 1].id] != day,
                    overnight = p.id in overnight,
                    dayKm = dayKm[day] ?: 0.0,
                )
            }
        }
    }
}
