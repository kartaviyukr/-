package com.simple.notes

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simple.notes.ui.ChangePasswordDialog
import com.simple.notes.ui.EditorScreen
import com.simple.notes.ui.LockScreen
import com.simple.notes.ui.NewProfileDialog
import com.simple.notes.ui.NotesListScreen
import com.simple.notes.ui.NotesTheme
import com.simple.notes.ui.SetupScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Запрет скриншотов и превью в списке недавних приложений
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContent {
            NotesTheme {
                App(isChangingConfigurations = { isChangingConfigurations })
            }
        }
    }
}

@Composable
private fun App(isChangingConfigurations: () -> Boolean) {
    val vm: AppViewModel = viewModel()
    val state by vm.state.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> vm.onAppStarted()
                Lifecycle.Event.ON_STOP -> vm.onAppStopped(isChangingConfigurations())
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when (state.stage) {
        Stage.SETUP -> SetupScreen(
            busy = state.busy,
            error = state.setupError,
            onCreate = vm::setupPasswords
        )

        Stage.LOCK -> LockScreen(
            busy = state.busy,
            message = state.message,
            onUnlock = vm::unlock
        )

        Stage.LIST -> {
            NotesListScreen(
                notes = state.visibleNotes,
                query = state.query,
                message = state.message,
                onQueryChange = vm::setQuery,
                onOpen = vm::openNote,
                onCreate = vm::createNote,
                onDelete = { vm.deleteNote(it) },
                onLock = { vm.lock() },
                onNewProfile = { vm.openDialog(PasswordDialog.NEW_PROFILE) },
                onChangePassword = { vm.openDialog(PasswordDialog.CHANGE_PASSWORD) },
                onMessageShown = vm::clearMessage
            )

            when (state.dialog) {
                PasswordDialog.NEW_PROFILE -> NewProfileDialog(
                    busy = state.busy,
                    error = state.dialogError,
                    onConfirm = vm::createProfile,
                    onDismiss = vm::closeDialog
                )

                PasswordDialog.CHANGE_PASSWORD -> ChangePasswordDialog(
                    busy = state.busy,
                    error = state.dialogError,
                    onConfirm = vm::changePassword,
                    onDismiss = vm::closeDialog
                )

                PasswordDialog.NONE -> Unit
            }
        }

        Stage.EDITOR -> {
            val note = state.editing
            if (note == null) {
                LaunchedEffect(Unit) { vm.closeEditor() }
            } else {
                BackHandler { vm.closeEditor() }
                EditorScreen(
                    note = note,
                    busy = state.busy,
                    loadPhoto = vm::loadPhoto,
                    onTitleChange = vm::editTitle,
                    onBodyChange = vm::editBody,
                    onBack = vm::closeEditor,
                    onDelete = { vm.deleteNote(note) },
                    onPhotoPicked = vm::attachPhotoFromUri,
                    onPhotoCaptured = vm::attachPhotoFromCamera,
                    onPhotoRemoved = vm::removePhoto,
                    onExternalPickerStart = vm::markExternalActivity
                )
            }
        }
    }
}
