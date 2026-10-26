package ru.velopodmoskovye.ui.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.velopodmoskovye.domain.model.SpiralPlace
import ru.velopodmoskovye.ui.Format
import ru.velopodmoskovye.ui.common.openInMaps

@Composable
fun MapScreen(onOpenPlace: (String) -> Unit, vm: MapViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selected by remember { mutableStateOf<SpiralPlace?>(null) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { vm.locate() }

    Box(Modifier.fillMaxSize()) {
        MapLibreView(
            layers = state.layers,
            modifier = Modifier.fillMaxSize(),
            initialZoom = 7.2,
            onMapClick = { p, mpp -> selected = vm.placeAt(p, mpp) },
        )
        Column(Modifier.fillMaxWidth().padding(8.dp)) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StarFilter.entries.forEach { f ->
                    FilterChip(selected = state.filter.stars == f, onClick = { vm.setStars(f) }, label = { Text(f.label) })
                }
            }
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PlaceCategory.entries.forEach { c ->
                    FilterChip(selected = c in state.filter.categories, onClick = { vm.toggleCategory(c) }, label = { Text(c.label) })
                }
            }
        }
        SmallFloatingActionButton(
            onClick = {
                permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) { Icon(Icons.Filled.MyLocation, contentDescription = "Моё положение") }

        selected?.let { p ->
            Card(Modifier.align(Alignment.BottomStart).padding(12.dp).fillMaxWidth(0.82f)) {
                Column(Modifier.padding(14.dp)) {
                    Text("${p.num}. ${p.name} ${Format.stars(p.mustHave)}", style = MaterialTheme.typography.titleMedium)
                    Text("${p.kindNote} · ${Format.km(p.distanceFromKremlinKm)} от Кремля", style = MaterialTheme.typography.bodySmall)
                    Text(
                        when {
                            p.id in state.completePlaceIds -> "Пройдено — зона открыта"
                            p.destinationId != null -> "Маршрут готов"
                            else -> "Маршрут ещё готовится"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Row {
                        val destId = p.destinationId
                        if (destId != null) Button(onClick = { onOpenPlace(destId) }) { Text("Открыть") }
                        else TextButton(onClick = { context.openInMaps(p.coords, p.name) }) { Text("На карте") }
                        TextButton(onClick = { selected = null }) { Text("Закрыть") }
                    }
                }
            }
        }
    }
}
