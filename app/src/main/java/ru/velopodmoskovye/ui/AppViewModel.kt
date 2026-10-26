package ru.velopodmoskovye.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.velopodmoskovye.data.content.ContentRepository
import ru.velopodmoskovye.data.db.ActiveRouteEntity
import ru.velopodmoskovye.data.progress.ProgressRepository
import ru.velopodmoskovye.data.settings.Settings
import ru.velopodmoskovye.data.settings.SettingsRepository
import ru.velopodmoskovye.domain.model.Title
import javax.inject.Inject

/** Новое звание для анимации: если это звание места — с легендой и ритуалом. */
data class AwardUi(val id: String, val name: String, val description: String, val placeTitle: Title?)

@HiltViewModel
class AppViewModel @Inject constructor(
    settingsRepo: SettingsRepository,
    private val progress: ProgressRepository,
    private val content: ContentRepository,
) : ViewModel() {
    val settings: StateFlow<Settings> = settingsRepo.settings.stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

    val activeRoute: StateFlow<ActiveRouteEntity?> =
        progress.activeRoute.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val pendingAward: StateFlow<AwardUi?> = combine(progress.titles, settingsRepo.settings) { titles, _ -> titles }
        .map { titles ->
            val t = titles.lastOrNull { !it.seen } ?: return@map null
            val placeTitle = if (t.titleId.startsWith("place:") || t.titleId.startsWith("bonus:")) {
                content.pack().destinations[t.titleId.substringAfter(':')]?.title
            } else null
            AwardUi(t.titleId, t.name, t.description, placeTitle)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        // Если контент обновился, звания пересчитываются при запуске.
        viewModelScope.launch { progress.recomputeTitles() }
    }

    fun dismissAward(id: String) {
        viewModelScope.launch { progress.markTitleSeen(id) }
    }
}
