package ru.velopodmoskovye.data.map

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionStatus
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition
import ru.velopodmoskovye.domain.model.GeoPoint
import ru.velopodmoskovye.ui.map.MAP_STYLE_URL
import javax.inject.Inject
import javax.inject.Singleton

/** Состояние офлайн-региона по ключу ("oblast" или id места). */
sealed interface OfflineState {
    data object None : OfflineState
    data class Downloading(val done: Long, val total: Long) : OfflineState {
        val fraction: Float get() = if (total <= 0) 0f else (done.toFloat() / total).coerceIn(0f, 1f)
    }
    data object Ready : OfflineState
    data class Failed(val message: String) : OfflineState
}

/**
 * Офлайн-карты MapLibre. Вся область с крупным масштабом весит гигабайты, поэтому
 * качаем два вида регионов: обзор области (до 10-го зума) и район конкретного маршрута (до 16-го).
 */
@Singleton
class OfflineMaps @Inject constructor(@ApplicationContext private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val manager by lazy { OfflineManager.getInstance(context) }
    private val _states = MutableStateFlow<Map<String, OfflineState>>(emptyMap())
    val states: StateFlow<Map<String, OfflineState>> = _states.asStateFlow()

    init {
        scope.launch { refresh() }
    }

    fun refresh() {
        manager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>?) {
                offlineRegions.orEmpty().forEach { region -> watch(keyOf(region), region) }
            }

            override fun onError(error: String) = Unit
        })
    }

    fun downloadOblast() = download(OBLAST_KEY, OBLAST_SOUTH_WEST, OBLAST_NORTH_EAST, 5.0, 10.0)

    /** Район маршрута: охват точек с запасом ~1 км. */
    fun downloadArea(key: String, points: List<GeoPoint>) {
        if (points.isEmpty()) return
        val pad = 0.01
        download(
            key,
            GeoPoint(points.minOf { it.lat } - pad, points.minOf { it.lon } - pad),
            GeoPoint(points.maxOf { it.lat } + pad, points.maxOf { it.lon } + pad),
            10.0, 16.0,
        )
    }

    private fun download(key: String, sw: GeoPoint, ne: GeoPoint, minZoom: Double, maxZoom: Double) {
        if (_states.value[key] is OfflineState.Downloading) return
        set(key, OfflineState.Downloading(0, 0))
        val bounds = LatLngBounds.from(ne.lat, ne.lon, sw.lat, sw.lon)
        val definition = OfflineTilePyramidRegionDefinition(
            MAP_STYLE_URL, bounds, minZoom, maxZoom, context.resources.displayMetrics.density,
        )
        manager.createOfflineRegion(definition, key.toByteArray(Charsets.UTF_8), object : OfflineManager.CreateOfflineRegionCallback {
            override fun onCreate(offlineRegion: OfflineRegion) {
                offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)
                watch(key, offlineRegion)
            }

            override fun onError(error: String) = set(key, OfflineState.Failed(error))
        })
    }

    /** Без подписки на события: опрашиваем статус раз в секунду, пока регион не скачан. */
    private fun watch(key: String, region: OfflineRegion) {
        scope.launch {
            while (true) {
                var finished = false
                region.getStatus(object : OfflineRegion.OfflineRegionStatusCallback {
                    override fun onStatus(status: OfflineRegionStatus?) {
                        if (status == null) return
                        if (status.isComplete) {
                            set(key, OfflineState.Ready)
                            finished = true
                        } else {
                            set(key, OfflineState.Downloading(status.completedResourceCount, status.requiredResourceCount))
                        }
                    }

                    override fun onError(error: String?) {
                        set(key, OfflineState.Failed(error ?: "ошибка"))
                        finished = true
                    }
                })
                delay(1_000)
                if (finished) break
            }
        }
    }

    fun delete(key: String) {
        manager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>?) {
                offlineRegions.orEmpty().filter { keyOf(it) == key }.forEach { r ->
                    r.setDownloadState(OfflineRegion.STATE_INACTIVE)
                    r.delete(object : OfflineRegion.OfflineRegionDeleteCallback {
                        override fun onDelete() = set(key, OfflineState.None)
                        override fun onError(error: String) = Unit
                    })
                }
            }

            override fun onError(error: String) = Unit
        })
    }

    private fun set(key: String, state: OfflineState) = _states.update { it + (key to state) }

    private fun keyOf(region: OfflineRegion) = String(region.metadata, Charsets.UTF_8)

    companion object {
        const val OBLAST_KEY = "oblast"
        val OBLAST_SOUTH_WEST = GeoPoint(52.5, 31.5)
        val OBLAST_NORTH_EAST = GeoPoint(59.0, 43.5)
    }
}
