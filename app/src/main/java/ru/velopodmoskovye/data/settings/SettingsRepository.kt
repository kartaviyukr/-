package ru.velopodmoskovye.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.velopodmoskovye.domain.progress.Geofence
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class Settings(
    val geofenceRadiusM: Int = Geofence.DEFAULT_RADIUS_M,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    /** Едем вдвоём — показываем парный вариант званий. */
    val pairMode: Boolean = false,
    val speedKmh: Int = 12,
)

private val Context.dataStore by preferencesDataStore("settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private object Keys {
        val radius = intPreferencesKey("geofence_radius")
        val theme = stringPreferencesKey("theme")
        val pair = booleanPreferencesKey("pair_mode")
        val speed = intPreferencesKey("speed_kmh")
    }

    val settings: Flow<Settings> = context.dataStore.data.map { p ->
        Settings(
            geofenceRadiusM = (p[Keys.radius] ?: Geofence.DEFAULT_RADIUS_M).coerceIn(RADIUS_RANGE),
            theme = p[Keys.theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            pairMode = p[Keys.pair] ?: false,
            speedKmh = (p[Keys.speed] ?: 12).coerceIn(SPEED_RANGE),
        )
    }

    suspend fun setRadius(v: Int) = context.dataStore.edit { it[Keys.radius] = v.coerceIn(RADIUS_RANGE) }
    suspend fun setTheme(v: ThemeMode) = context.dataStore.edit { it[Keys.theme] = v.name }
    suspend fun setPairMode(v: Boolean) = context.dataStore.edit { it[Keys.pair] = v }
    suspend fun setSpeed(v: Int) = context.dataStore.edit { it[Keys.speed] = v.coerceIn(SPEED_RANGE) }

    companion object {
        val RADIUS_RANGE = 50..500
        val SPEED_RANGE = 8..25
    }
}
