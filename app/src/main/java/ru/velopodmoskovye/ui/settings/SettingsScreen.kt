package ru.velopodmoskovye.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.velopodmoskovye.BuildConfig
import ru.velopodmoskovye.data.progress.ProgressRepository
import ru.velopodmoskovye.data.settings.Settings
import ru.velopodmoskovye.data.settings.SettingsRepository
import ru.velopodmoskovye.data.settings.ThemeMode
import ru.velopodmoskovye.ui.common.SectionTitle
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository,
    private val progress: ProgressRepository,
) : ViewModel() {
    val settings: StateFlow<Settings?> = repo.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setKmPerDay(v: Int) = viewModelScope.launch { repo.setKmPerDay(v) }
    fun setRadius(v: Int) = viewModelScope.launch { repo.setRadius(v) }
    fun setSpeed(v: Int) = viewModelScope.launch { repo.setSpeed(v) }
    fun setTheme(v: ThemeMode) = viewModelScope.launch { repo.setTheme(v) }
    fun setPair(v: Boolean) = viewModelScope.launch { repo.setPairMode(v) }
    fun reset() = viewModelScope.launch { progress.resetAll() }
}

@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel()) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val settings = s ?: return
    var confirmReset by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Настройки", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        IntSlider("Километров в день", settings.kmPerDay, 40..120, step = 5, unit = "км", onChange = vm::setKmPerDay,
            hint = "Под эту цифру спираль разбивается на дни и подсказывает ночёвки.")
        IntSlider("Радиус отметки на точке", settings.geofenceRadiusM, 50..500, step = 10, unit = "м", onChange = vm::setRadius,
            hint = "Для больших объектов (поле, парк) радиус задан в контенте и не меняется.")
        IntSlider("Средняя скорость", settings.speedKmh, 8..25, step = 1, unit = "км/ч", onChange = vm::setSpeed,
            hint = "Для проверки «успеваю до заката» и оценки перегонов.")

        SectionTitle("Тема")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(ThemeMode.SYSTEM to "Как в системе", ThemeMode.LIGHT to "Светлая", ThemeMode.DARK to "Тёмная").forEach { (m, label) ->
                FilterChip(selected = settings.theme == m, onClick = { vm.setTheme(m) }, label = { Text(label) })
            }
        }

        SectionTitle("Едем вдвоём")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Показывать парные варианты званий", Modifier.weight(1f))
            Switch(checked = settings.pairMode, onCheckedChange = vm::setPair)
        }

        SectionTitle("Офлайн-карты")
        Text(
            "Скачивание карты области для работы без сети запланировано на следующий этап. " +
                "Сейчас карта грузится из сети (OpenFreeMap), а просмотренные участки кешируются.",
            style = MaterialTheme.typography.bodySmall,
        )

        SectionTitle("Прогресс")
        OutlinedButton(onClick = { confirmReset = true }) { Text("Сбросить все отметки и звания") }

        Text(
            "Версия ${BuildConfig.VERSION_NAME}. Карта © участники OpenStreetMap, тайлы OpenFreeMap.",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 24.dp),
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Сбросить прогресс?") },
            text = { Text("Удалятся все отметки на точках, звания и статистика. Это нельзя отменить.") },
            confirmButton = { TextButton(onClick = { vm.reset(); confirmReset = false }) { Text("Сбросить") } },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Отмена") } },
        )
    }
}

@Composable
private fun IntSlider(title: String, value: Int, range: IntRange, step: Int, unit: String, onChange: (Int) -> Unit, hint: String) {
    var local by remember(value) { mutableFloatStateOf(value.toFloat()) }
    SectionTitle("$title: ${local.toInt()} $unit")
    Slider(
        value = local,
        onValueChange = { local = (Math.round(it / step) * step).toFloat() },
        onValueChangeFinished = { onChange(local.toInt()) },
        valueRange = range.first.toFloat()..range.last.toFloat(),
        steps = (range.last - range.first) / step - 1,
        modifier = Modifier.fillMaxWidth(),
    )
    Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
