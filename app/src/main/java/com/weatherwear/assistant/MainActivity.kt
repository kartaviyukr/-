package com.weatherwear.assistant

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weatherwear.assistant.ui.WeatherScreen
import com.weatherwear.assistant.ui.theme.ChtoNadetTheme

class MainActivity : ComponentActivity() {

    private var viewModelRef: WeatherViewModel? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        val allowed = granted[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            granted[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (allowed) viewModelRef?.onLocationPermissionGranted()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChtoNadetTheme {
                val vm: WeatherViewModel = viewModel()
                viewModelRef = vm
                val state by vm.state.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    WeatherScreen(
                        state = state,
                        onRefresh = vm::refresh,
                        onUseLocation = { requestLocation(vm) },
                        onOpenSearch = vm::openSearch,
                        onCloseSearch = vm::closeSearch,
                        onQueryChange = vm::onSearchQueryChange,
                        onPlaceSelected = vm::selectPlace,
                    )
                }
            }
        }
    }

    private fun requestLocation(viewModel: WeatherViewModel) {
        if (viewModel.hasLocationPermission()) {
            viewModel.refreshByLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ),
            )
        }
    }
}
