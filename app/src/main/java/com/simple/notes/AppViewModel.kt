package com.simple.notes

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.LruCache
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simple.notes.data.ImageUtils
import com.simple.notes.data.Note
import com.simple.notes.data.Vault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class Stage { SETUP, LOCK, LIST, EDITOR }

/** Не больше восьмой части доступной памяти под расшифрованные фотографии. */
private fun cacheBudgetKb(): Int =
    (Runtime.getRuntime().maxMemory() / 8 / 1024).coerceIn(8 * 1024, 96 * 1024).toInt()

data class UiState(
    val stage: Stage = Stage.LOCK,
    val busy: Boolean = false,
    val notes: List<Note> = emptyList(),
    val query: String = "",
    val editing: Note? = null,
    val setupError: String? = null
) {
    val visibleNotes: List<Note>
        get() = if (query.isBlank()) notes else notes.filter {
            it.title.contains(query, true) || it.body.contains(query, true)
        }
}

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val vaults = (app as NotesApp).vaults

    private val _state = MutableStateFlow(
        UiState(stage = if (vaults.isConfigured) Stage.LOCK else Stage.SETUP)
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var vault: Vault? = null
    // Ограничение по памяти: полноразмерный снимок весит мегабайты, счёт по штукам приводит к OOM.
    private val photoCache = object : LruCache<String, Bitmap>(cacheBudgetKb()) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    // --- Вход ---

    /** Первый запуск: пользователь задаёт по паролю на каждый из своих блокнотов. */
    fun setupPasswords(passwords: List<String>) {
        val error = when {
            passwords.any { it.length < 4 } ->
                "Заполните все поля, минимум 4 символа в каждом"
            passwords.distinct().size != passwords.size ->
                "Пароли блокнотов должны быть разными"
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(setupError = error) }
            return
        }
        _state.update { it.copy(busy = true, setupError = null) }
        viewModelScope.launch {
            val opened = withContext(Dispatchers.IO) {
                vaults.createMainVaults(passwords.map { it.toCharArray() })
            }
            enter(opened)
        }
    }

    /**
     * Открывает хранилище введённого пароля. Ошибки «неверный пароль» здесь
     * не существует: любой пароль открывает своё хранилище с заметками.
     */
    fun unlock(password: String) {
        if (password.isEmpty() || _state.value.busy) return
        _state.update { it.copy(busy = true) }
        viewModelScope.launch {
            val opened = withContext(Dispatchers.IO) {
                vaults.openVault(password.toCharArray())
            }
            enter(opened)
        }
    }

    private suspend fun enter(opened: Vault) {
        val loaded = withContext(Dispatchers.IO) { opened.loadNotes() }
        vault = opened
        photoCache.evictAll()
        _state.update {
            it.copy(
                stage = Stage.LIST,
                busy = false,
                notes = loaded.sortedByDescending { note -> note.updatedAt },
                query = "",
                editing = null,
                setupError = null
            )
        }
    }

    /** Блокировка: ключ стирается из памяти, заметки и фото уходят с экрана. */
    fun lock() {
        vault?.close()
        vault = null
        photoCache.evictAll()
        _state.value = UiState(stage = if (vaults.isConfigured) Stage.LOCK else Stage.SETUP)
    }

    // --- Автоблокировка ---

    private var externalActivityPending = false

    /** Вызывается перед открытием галереи или камеры, чтобы уход в другое приложение не блокировал хранилище. */
    fun markExternalActivity() { externalActivityPending = true }

    fun onAppStarted() { externalActivityPending = false }

    /** Приложение свернули — стираем ключ из памяти. */
    fun onAppStopped(changingConfiguration: Boolean) {
        if (changingConfiguration || externalActivityPending) return
        if (_state.value.stage == Stage.LIST || _state.value.stage == Stage.EDITOR) {
            val note = _state.value.editing
            if (note != null && (note.title.isNotBlank() || note.body.isNotBlank() || note.photos.isNotEmpty())) {
                persistBlocking(note.copy(updatedAt = System.currentTimeMillis()))
            }
            lock()
        }
    }

    private fun persistBlocking(note: Note) {
        val current = vault ?: return
        val updated = (_state.value.notes.filterNot { it.id == note.id } + note)
            .sortedByDescending { it.updatedAt }
        current.saveNotes(updated)
    }

    // --- Заметки ---

    fun setQuery(value: String) = _state.update { it.copy(query = value) }

    fun createNote() = _state.update { it.copy(stage = Stage.EDITOR, editing = Note()) }

    fun openNote(note: Note) = _state.update { it.copy(stage = Stage.EDITOR, editing = note) }

    fun editTitle(value: String) = _state.update { s -> s.copy(editing = s.editing?.copy(title = value)) }

    fun editBody(value: String) = _state.update { s -> s.copy(editing = s.editing?.copy(body = value)) }

    /** Выход из редактора: пустая заметка не сохраняется. */
    fun closeEditor() {
        val note = _state.value.editing
        if (note != null) {
            if (note.title.isBlank() && note.body.isBlank() && note.photos.isEmpty()) {
                deleteNote(note, silent = true)
            } else {
                persist(note.copy(updatedAt = System.currentTimeMillis()))
            }
        }
        _state.update { it.copy(stage = Stage.LIST, editing = null) }
    }

    fun deleteNote(note: Note, silent: Boolean = false) {
        val updated = _state.value.notes.filterNot { it.id == note.id }
        note.photos.forEach { photoCache.remove(it) }
        _state.update { it.copy(notes = updated, stage = if (silent) it.stage else Stage.LIST, editing = if (silent) it.editing else null) }
        val current = vault ?: return
        viewModelScope.launch(Dispatchers.IO) {
            current.deletePhotos(note.photos)
            current.saveNotes(updated)
        }
    }

    private fun persist(note: Note) {
        val existing = _state.value.notes
        val updated = (existing.filterNot { it.id == note.id } + note)
            .sortedByDescending { it.updatedAt }
        _state.update { it.copy(notes = updated) }
        val current = vault ?: return
        viewModelScope.launch(Dispatchers.IO) { current.saveNotes(updated) }
    }

    // --- Фотографии ---

    fun attachPhotoFromUri(uri: Uri) = attach { ImageUtils.fromUri(getApplication<Application>(), uri) }

    fun attachPhotoFromCamera(file: File) = attach {
        val bytes = ImageUtils.fromFile(file)
        file.delete() // незашифрованный снимок в кэше не остаётся
        bytes
    }

    private fun attach(read: suspend () -> ByteArray?) {
        val current = vault ?: return
        val note = _state.value.editing ?: return
        _state.update { it.copy(busy = true) }
        viewModelScope.launch {
            val photoId = withContext(Dispatchers.IO) {
                read()?.let { bytes -> current.savePhoto(bytes) }
            }
            _state.update { s ->
                val target = s.editing
                if (photoId == null || target == null || target.id != note.id) {
                    s.copy(busy = false)
                } else {
                    s.copy(busy = false, editing = target.copy(photos = target.photos + photoId))
                }
            }
            _state.value.editing?.let { persist(it.copy(updatedAt = System.currentTimeMillis())) }
        }
    }

    fun removePhoto(photoId: String) {
        val current = vault ?: return
        photoCache.remove(photoId)
        _state.update { s ->
            val target = s.editing ?: return@update s
            s.copy(editing = target.copy(photos = target.photos.filterNot { it == photoId }))
        }
        viewModelScope.launch(Dispatchers.IO) { current.deletePhotos(listOf(photoId)) }
        _state.value.editing?.let { persist(it) }
    }

    /** Расшифровывает фотографию для показа. Ничего не пишет на диск в открытом виде. */
    suspend fun loadPhoto(photoId: String): Bitmap? {
        photoCache.get(photoId)?.let { return it }
        val current = vault ?: return null
        return withContext(Dispatchers.IO) {
            val bytes = current.readPhoto(photoId) ?: return@withContext null
            ImageUtils.decode(bytes)?.also { photoCache.put(photoId, it) }
        }
    }

    override fun onCleared() {
        lock()
        super.onCleared()
    }
}
