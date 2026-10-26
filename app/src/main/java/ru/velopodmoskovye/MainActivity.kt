package ru.velopodmoskovye

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import ru.velopodmoskovye.ui.AppRoot
import ru.velopodmoskovye.ui.AppViewModel
import ru.velopodmoskovye.ui.theme.VeloTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by appViewModel.settings.collectAsStateWithLifecycle()
            VeloTheme(settings.theme) {
                AppRoot(appViewModel)
            }
        }
    }
}
