package ru.velopodmoskovye.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ru.velopodmoskovye.data.settings.ThemeMode

/** Цвет «открытой» области и прогресса. */
val ProgressGreen = Color(0xFF2E7D32)
val FogGray = Color(0xFF5F6B73)
val StarGold = Color(0xFFF2B01E)
val UnverifiedAmber = Color(0xFFB26A00)

private val Light = lightColorScheme(
    primary = Color(0xFF1F6F4A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7F0CF),
    onPrimaryContainer = Color(0xFF002112),
    secondary = Color(0xFF8A5A00),
    secondaryContainer = Color(0xFFFFDDB0),
    tertiary = Color(0xFF3B6470),
    tertiaryContainer = Color(0xFFBEEAF7),
    background = Color(0xFFF8FAF5),
    surface = Color(0xFFF8FAF5),
)

private val Dark = darkColorScheme(
    primary = Color(0xFF8CD7AE),
    onPrimary = Color(0xFF003822),
    primaryContainer = Color(0xFF005234),
    onPrimaryContainer = Color(0xFFB7F0CF),
    secondary = Color(0xFFFFB955),
    secondaryContainer = Color(0xFF684300),
    tertiary = Color(0xFFA3CDDB),
    tertiaryContainer = Color(0xFF214C57),
    background = Color(0xFF111412),
    surface = Color(0xFF111412),
)

@Composable
fun VeloTheme(mode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(colorScheme = if (dark) Dark else Light, content = content)
}
