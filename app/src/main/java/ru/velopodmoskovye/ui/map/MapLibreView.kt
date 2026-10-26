package ru.velopodmoskovye.ui.map

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import ru.velopodmoskovye.domain.model.GeoPoint

/** Векторные OSM-тайлы OpenFreeMap — без ключа и без регистрации. */
const val MAP_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"

/** Слой карты: GeoJSON-источник плюс способ его нарисовать. Порядок в списке = порядок отрисовки. */
data class MapLayerSpec(
    val id: String,
    val geoJson: String,
    val kind: Kind,
    val color: String,
    val opacity: Float = 1f,
    val width: Float = 3f,
    val radius: Float = 5f,
    val strokeColor: String = "#FFFFFF",
    val dashed: Boolean = false,
    val minZoom: Float = 0f,
) {
    enum class Kind { FILL, LINE, CIRCLE }
}

/**
 * Обёртка над MapLibre MapView для Compose: жизненный цикл, стиль, обновляемые слои.
 * [fitTo] — точки, в которые камера впишется один раз после загрузки стиля.
 */
@Composable
fun MapLibreView(
    layers: List<MapLayerSpec>,
    modifier: Modifier = Modifier,
    fitTo: List<GeoPoint> = emptyList(),
    initialCenter: GeoPoint = GeoPoint(55.75, 37.62),
    initialZoom: Double = 7.0,
    onMapClick: ((GeoPoint, metersPerPixel: Double) -> Unit)? = null,
    gesturesEnabled: Boolean = true,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember { MapView(context).apply { onCreate(Bundle()) } }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var style by remember { mutableStateOf<Style?>(null) }
    var fitted by remember { mutableStateOf(false) }

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) mapView.onStart()
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) mapView.onResume()
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    LaunchedEffect(mapView) {
        mapView.getMapAsync { m ->
            map = m
            m.uiSettings.isRotateGesturesEnabled = false
            m.uiSettings.isTiltGesturesEnabled = false
            m.uiSettings.setAllGesturesEnabled(gesturesEnabled)
            m.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(initialCenter.lat, initialCenter.lon), initialZoom))
            m.setStyle(Style.Builder().fromUri(MAP_STYLE_URL)) { s -> style = s }
        }
    }

    val clickHandler by rememberUpdatedState(onMapClick)
    LaunchedEffect(map) {
        val m = map ?: return@LaunchedEffect
        m.addOnMapClickListener { latLng ->
            val handler = clickHandler ?: return@addOnMapClickListener false
            handler(GeoPoint(latLng.latitude, latLng.longitude), m.projection.getMetersPerPixelAtLatitude(latLng.latitude))
            true
        }
    }

    LaunchedEffect(style, layers) {
        val s = style ?: return@LaunchedEffect
        applyLayers(s, layers)
    }

    LaunchedEffect(style, fitTo) {
        val m = map ?: return@LaunchedEffect
        if (style == null || fitted || fitTo.isEmpty()) return@LaunchedEffect
        fitted = true
        if (fitTo.size == 1) {
            m.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(fitTo[0].lat, fitTo[0].lon), 13.0))
        } else {
            val b = LatLngBounds.Builder()
            fitTo.forEach { b.include(LatLng(it.lat, it.lon)) }
            m.moveCamera(CameraUpdateFactory.newLatLngBounds(b.build(), 80))
        }
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}

private fun applyLayers(style: Style, layers: List<MapLayerSpec>) {
    // Удаляем свои слои, которых больше нет.
    val wanted = layers.map { it.id }.toSet()
    style.layers.map { it.id }.filter { it.startsWith(PREFIX) && it.removePrefix(PREFIX) !in wanted }.forEach { id ->
        style.removeLayer(id)
        style.removeSource(id)
    }
    for (spec in layers) {
        val id = PREFIX + spec.id
        val source = style.getSourceAs<GeoJsonSource>(id)
        if (source != null) {
            source.setGeoJson(spec.geoJson)
            continue
        }
        style.addSource(GeoJsonSource(id, spec.geoJson))
        val layer: Layer = when (spec.kind) {
            MapLayerSpec.Kind.FILL -> FillLayer(id, id).withProperties(
                PropertyFactory.fillColor(spec.color),
                PropertyFactory.fillOpacity(spec.opacity),
            )
            MapLayerSpec.Kind.LINE -> LineLayer(id, id).withProperties(
                PropertyFactory.lineColor(spec.color),
                PropertyFactory.lineWidth(spec.width),
                PropertyFactory.lineOpacity(spec.opacity),
                PropertyFactory.lineCap("round"),
                PropertyFactory.lineJoin("round"),
            ).also { if (spec.dashed) it.setProperties(PropertyFactory.lineDasharray(arrayOf(2f, 2f))) }
            MapLayerSpec.Kind.CIRCLE -> CircleLayer(id, id).withProperties(
                PropertyFactory.circleColor(spec.color),
                PropertyFactory.circleRadius(spec.radius),
                PropertyFactory.circleOpacity(spec.opacity),
                PropertyFactory.circleStrokeColor(spec.strokeColor),
                PropertyFactory.circleStrokeWidth(1.5f),
            )
        }
        if (spec.minZoom > 0f) layer.minZoom = spec.minZoom
        style.addLayer(layer)
    }
}

private const val PREFIX = "velo-"
