package ru.velopodmoskovye.ui.map

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
import ru.velopodmoskovye.data.content.ContentRepository
import ru.velopodmoskovye.data.location.LocationTracker
import ru.velopodmoskovye.data.progress.ProgressRepository
import ru.velopodmoskovye.data.settings.SettingsRepository
import ru.velopodmoskovye.domain.fog.Fog
import ru.velopodmoskovye.domain.fog.OpenZones
import ru.velopodmoskovye.domain.fog.Zone
import ru.velopodmoskovye.domain.geo.Geo
import ru.velopodmoskovye.domain.model.GeoPoint
import ru.velopodmoskovye.domain.model.SpiralPlace
import ru.velopodmoskovye.domain.progress.ProgressCalculator
import javax.inject.Inject

/** Группы мест для фильтра на карте. */
enum class PlaceCategory(val label: String) {
    CITY("Города"), SETTLEMENT("Посёлки и сёла"), ESTATE("Усадьбы"), MONASTERY("Монастыри"),
    NATURE("Природа"), MEMORIAL("Памятные места"), OTHER("Другое");

    companion object {
        fun of(kindNote: String): PlaceCategory {
            val k = kindNote.lowercase()
            return when {
                "природа" in k -> NATURE
                "монастыр" in k && !k.startsWith("с.") -> MONASTERY
                "усадьб" in k && !k.startsWith("г.") && !k.startsWith("рп") -> ESTATE
                "памятное" in k || "бои" in k || "мемориал" in k -> MEMORIAL
                k.startsWith("г.") || k.startsWith("бывш. г") || k.startsWith("мкр") -> CITY
                k.startsWith("рп") || k.startsWith("дп") || k.startsWith("с.") || k.startsWith("д.") ||
                    "посёлок" in k || "деревн" in k || k.startsWith("бывш.") -> SETTLEMENT
                else -> OTHER
            }
        }
    }
}

enum class StarFilter(val label: String) { ALL("Все"), STAR_OR_HALF("★ и ☆"), STAR("Только ★") }

data class MapFilter(val stars: StarFilter = StarFilter.ALL, val categories: Set<PlaceCategory> = PlaceCategory.entries.toSet())

data class MapUiState(
    val loading: Boolean = true,
    val layers: List<MapLayerSpec> = emptyList(),
    val places: List<SpiralPlace> = emptyList(),
    val completePlaceIds: Set<String> = emptySet(),
    val filter: MapFilter = MapFilter(),
    val me: GeoPoint? = null,
)

@HiltViewModel
class MapViewModel @Inject constructor(
    content: ContentRepository,
    progress: ProgressRepository,
    settings: SettingsRepository,
    private val tracker: LocationTracker,
) : ViewModel() {
    private val filter = MutableStateFlow(MapFilter())

    val state: StateFlow<MapUiState> = combine(
        flow { emit(content.pack()) }, progress.visitedIds, settings.settings, filter, tracker.last,
    ) { pack, visited, s, f, fix ->
        val zones = OpenZones.compute(pack.spiral, pack.destinations, visited, s.geofenceRadiusM)
        val completeDest = pack.destinations.values.filter { ProgressCalculator.destination(it, visited).complete }.map { it.id }.toSet()
        val complete = pack.spiral.places.filter { it.destinationId in completeDest || (it.finish && ProgressCalculator.KREMLIN_STOP_ID in visited) }
            .map { it.id }.toSet()
        val shown = pack.spiral.places.filter { p ->
            val starOk = when (f.stars) {
                StarFilter.ALL -> true
                StarFilter.STAR_OR_HALF -> p.mustHave >= 2
                StarFilter.STAR -> p.mustHave == 3
            }
            starOk && (p.finish || PlaceCategory.of(p.kindNote) in f.categories)
        }
        val routeStops = pack.destinations.values.flatMap { d -> d.stops }
        val layers = buildList {
            add(MapLayerSpec("fog", Fog.fogGeoJson(zones.map { it.ring }), MapLayerSpec.Kind.FILL, "#5F6B73", opacity = 0.55f))
            add(MapLayerSpec("zones-place", MapData.zones(zones, Zone.Level.PLACE), MapLayerSpec.Kind.FILL, "#2E7D32", opacity = 0.25f))
            add(MapLayerSpec("zones-region", MapData.zones(zones, Zone.Level.REGION), MapLayerSpec.Kind.FILL, "#1F6F4A", opacity = 0.18f))
            add(MapLayerSpec("zones-stop", MapData.zones(zones, Zone.Level.STOP), MapLayerSpec.Kind.FILL, "#66BB6A", opacity = 0.3f))
            add(MapLayerSpec("spiral", MapData.line(pack.spiral.places.map { it.coords }), MapLayerSpec.Kind.LINE, "#8A5A00", opacity = 0.35f, width = 1.5f, dashed = true))
            pack.destinations.values.forEach { d ->
                add(MapLayerSpec("route-${d.id}", MapData.line(MapData.routePoints(d)), MapLayerSpec.Kind.LINE, "#1F6F4A", opacity = 0.8f, width = 3f, minZoom = 10f))
            }
            add(MapLayerSpec("stops-todo", MapData.points(routeStops.filter { it.id !in visited }.map { it.id to it.coords }), MapLayerSpec.Kind.CIRCLE, "#8A5A00", radius = 5f, minZoom = 11f))
            add(MapLayerSpec("stops-done", MapData.points(routeStops.filter { it.id in visited }.map { it.id to it.coords }), MapLayerSpec.Kind.CIRCLE, "#2E7D32", radius = 5f, minZoom = 11f))
            add(MapLayerSpec("places-todo", MapData.points(shown.filter { it.id !in complete && it.destinationId == null }.map { it.id to it.coords }), MapLayerSpec.Kind.CIRCLE, "#9E9E9E", radius = 5f))
            add(MapLayerSpec("places-ready", MapData.points(shown.filter { it.id !in complete && it.destinationId != null }.map { it.id to it.coords }), MapLayerSpec.Kind.CIRCLE, "#F2B01E", radius = 7f))
            add(MapLayerSpec("places-done", MapData.points(shown.filter { it.id in complete }.map { it.id to it.coords }), MapLayerSpec.Kind.CIRCLE, "#2E7D32", radius = 7f))
            add(MapData.me(fix?.point))
        }
        MapUiState(false, layers, shown, complete, f, fix?.point)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MapUiState())

    fun setStars(v: StarFilter) { filter.value = filter.value.copy(stars = v) }

    fun toggleCategory(c: PlaceCategory) {
        val cur = filter.value.categories
        filter.value = filter.value.copy(categories = if (c in cur) cur - c else cur + c)
    }

    fun locate() { viewModelScope.launch { tracker.current() } }

    /** Ближайшее к нажатию место в пределах ~40 px. */
    fun placeAt(p: GeoPoint, metersPerPixel: Double): SpiralPlace? {
        val maxM = metersPerPixel * 40
        return state.value.places.minByOrNull { Geo.distanceM(it.coords, p) }?.takeIf { Geo.distanceM(it.coords, p) <= maxM }
    }
}
