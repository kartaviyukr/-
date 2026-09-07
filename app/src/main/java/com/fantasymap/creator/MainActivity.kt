package com.fantasymap.creator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fantasymap.creator.editor.EditorViewModel
import com.fantasymap.creator.ui.EditorScreen
import com.fantasymap.creator.ui.FantasyMapTheme
import com.fantasymap.creator.ui.ProjectsScreen

class MainActivity : ComponentActivity() {

    private var viewModelRef: EditorViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FantasyMapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: EditorViewModel = viewModel()
                    viewModelRef = viewModel
                    if (viewModel.project == null) {
                        ProjectsScreen(viewModel)
                    } else {
                        EditorScreen(viewModel)
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Карта не должна теряться при сворачивании приложения.
        viewModelRef?.saveNow()
    }
}
