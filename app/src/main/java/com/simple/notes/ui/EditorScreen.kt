package com.simple.notes.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.simple.notes.data.Match
import com.simple.notes.data.Note
import com.simple.notes.data.NoteSearch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    note: Note,
    busy: Boolean,
    loadPhoto: suspend (String) -> Bitmap?,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onPhotoPicked: (Uri) -> Unit,
    onPhotoCaptured: (File) -> Unit,
    onPhotoRemoved: (String) -> Unit,
    onExternalPickerStart: () -> Unit
) {
    val context = LocalContext.current
    var confirmDelete by remember { mutableStateOf(false) }
    var fullscreen by remember { mutableStateOf<String?>(null) }
    var pendingShot by remember { mutableStateOf<File?>(null) }

    // Поиск внутри заметки
    var searching by remember(note.id) { mutableStateOf(false) }
    var query by remember(note.id) { mutableStateOf("") }
    var currentMatch by remember(note.id) { mutableStateOf(0) }
    val matches = remember(note.title, note.body, query) {
        NoteSearch.find(note.title, note.body, query)
    }
    val activeMatch = matches.getOrNull(currentMatch.coerceIn(0, (matches.size - 1).coerceAtLeast(0)))

    fun closeSearch() {
        searching = false
        query = ""
        currentMatch = 0
    }

    BackHandler(enabled = searching) { closeSearch() }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(onPhotoPicked)
    }
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val file = pendingShot
        pendingShot = null
        if (success && file != null) onPhotoCaptured(file) else file?.delete()
    }

    Scaffold(
        topBar = {
            if (searching) {
                SearchTopBar(
                    query = query,
                    onQueryChange = {
                        query = it
                        currentMatch = 0
                    },
                    onClose = { closeSearch() }
                )
            } else {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                        }
                    },
                    actions = {
                        IconButton(onClick = { searching = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Найти в заметке")
                        }
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить заметку")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        },
        bottomBar = {
            if (searching) {
                MatchNavigationBar(
                    query = query,
                    total = matches.size,
                    current = currentMatch,
                    onPrevious = {
                        if (matches.isNotEmpty()) {
                            currentMatch = (currentMatch - 1 + matches.size) % matches.size
                        }
                    },
                    onNext = {
                        if (matches.isNotEmpty()) {
                            currentMatch = (currentMatch + 1) % matches.size
                        }
                    }
                )
            } else {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onExternalPickerStart()
                            pickPhoto.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Галерея")
                    }
                    OutlinedButton(
                        onClick = {
                            onExternalPickerStart()
                            val file = newCameraFile(context)
                            pendingShot = file
                            takePhoto.launch(cameraUri(context, file))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Камера")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
        ) {
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())

            if (searching) {
                SearchResultView(note = note, matches = matches, active = activeMatch)
            } else {
                TextField(
                    value = note.title,
                    onValueChange = onTitleChange,
                    placeholder = { Text("Заголовок") },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
                    colors = transparentFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (note.photos.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(note.photos, key = { it }) { photoId ->
                            Box {
                                VaultImage(
                                    photoId = photoId,
                                    loadPhoto = loadPhoto,
                                    modifier = Modifier
                                        .size(104.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { fullscreen = photoId }
                                )
                                Box(
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.55f))
                                        .clickable { onPhotoRemoved(photoId) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Удалить фото",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                TextField(
                    value = note.body,
                    onValueChange = onBodyChange,
                    placeholder = { Text("Текст заметки") },
                    colors = transparentFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Удалить заметку?") },
            text = { Text("Заметка и прикреплённые фотографии будут удалены.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Отмена") }
            }
        )
    }

    fullscreen?.let { photoId ->
        PhotoViewer(photoId = photoId, loadPhoto = loadPhoto, onClose = { fullscreen = null })
    }
}

/** Строка ввода поискового запроса вместо обычной шапки. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Найти в заметке") },
                singleLine = true,
                colors = transparentFieldColors(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Закрыть поиск")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

/** Счётчик совпадений и переход к предыдущему или следующему. */
@Composable
private fun MatchNavigationBar(
    query: String,
    total: Int,
    current: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            when {
                query.isBlank() -> "Введите, что искать"
                total == 0 -> "Ничего не найдено"
                else -> "${current + 1} из $total"
            },
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onPrevious, enabled = total > 0) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Предыдущее совпадение")
        }
        IconButton(onClick = onNext, enabled = total > 0) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Следующее совпадение")
        }
    }
}

/**
 * Заметка на время поиска показывается только для чтения: так все совпадения
 * можно подсветить, а к текущему — прокрутить.
 */
@Composable
private fun SearchResultView(note: Note, matches: List<Match>, active: Match?) {
    val scroll = rememberScrollState()
    val normalColor = MaterialTheme.colorScheme.primaryContainer
    val activeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)

    var bodyTop by remember { mutableStateOf(0f) }
    var bodyLayout by remember { mutableStateOf<TextLayoutResult?>(null) }

    LaunchedEffect(active, bodyLayout, bodyTop) {
        val target = active ?: return@LaunchedEffect
        if (target.inTitle) {
            scroll.animateScrollTo(0)
            return@LaunchedEffect
        }
        val layout = bodyLayout ?: return@LaunchedEffect
        val offset = target.start.coerceIn(0, layout.layoutInput.text.length)
        val top = layout.getBoundingBox(offset).top
        scroll.animateScrollTo((bodyTop + top - 80f).toInt().coerceAtLeast(0))
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = highlighted(
                text = note.title.ifBlank { "Без названия" },
                matches = if (note.title.isBlank()) emptyList() else matches.filter { it.inTitle },
                active = active,
                normalColor = normalColor,
                activeColor = activeColor
            ),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = highlighted(
                text = note.body,
                matches = matches.filter { !it.inTitle },
                active = active,
                normalColor = normalColor,
                activeColor = activeColor
            ),
            style = MaterialTheme.typography.bodyLarge,
            onTextLayout = { bodyLayout = it },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { bodyTop = it.positionInParent().y }
        )
        Spacer(Modifier.height(32.dp))
    }
}

private fun highlighted(
    text: String,
    matches: List<Match>,
    active: Match?,
    normalColor: Color,
    activeColor: Color
): AnnotatedString = buildAnnotatedString {
    append(text)
    matches.forEach { match ->
        val start = match.start.coerceIn(0, text.length)
        val end = match.end.coerceIn(start, text.length)
        if (start == end) return@forEach
        val color = if (match == active) activeColor else normalColor
        addStyle(SpanStyle(background = color), start, end)
    }
}

/** Показывает расшифрованное фото. Открытая копия существует только в памяти. */
@Composable
fun VaultImage(
    photoId: String,
    loadPhoto: suspend (String) -> Bitmap?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, photoId) {
        value = loadPhoto(photoId)
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        val current = bitmap
        if (current == null) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Image(
                bitmap = current.asImageBitmap(),
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun PhotoViewer(photoId: String, loadPhoto: suspend (String) -> Bitmap?, onClose: () -> Unit) {
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        var scale by remember { mutableStateOf(1f) }
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }

        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(photoId) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 6f)
                        if (scale > 1f) {
                            offsetX += pan.x
                            offsetY += pan.y
                        } else {
                            offsetX = 0f; offsetY = 0f
                        }
                    }
                }
        ) {
            VaultImage(
                photoId = photoId,
                loadPhoto = loadPhoto,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun transparentFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent
)

private fun newCameraFile(context: Context): File {
    val dir = File(context.cacheDir, "camera").apply { mkdirs() }
    return File(dir, "shot_${System.currentTimeMillis()}.jpg")
}

private fun cameraUri(context: Context, file: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
