package com.fantasymap.creator.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF7A5230),
    onPrimary = Color(0xFFFFF6E4),
    primaryContainer = Color(0xFFEBD9B6),
    onPrimaryContainer = Color(0xFF2E1D0C),
    secondary = Color(0xFF3D6E8E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDE2EF),
    onSecondaryContainer = Color(0xFF0E2B3D),
    tertiary = Color(0xFF5A6B3B),
    tertiaryContainer = Color(0xFFDBE6C4),
    onTertiaryContainer = Color(0xFF1D2610),
    background = Color(0xFFF6EEDC),
    onBackground = Color(0xFF2E2519),
    surface = Color(0xFFFBF4E4),
    onSurface = Color(0xFF2E2519),
    surfaceVariant = Color(0xFFE7DCC4),
    onSurfaceVariant = Color(0xFF4E4436),
    outline = Color(0xFF8B7C63),
    error = Color(0xFF9B2C2C),
    errorContainer = Color(0xFFFBDAD5),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE3C08A),
    onPrimary = Color(0xFF3B2712),
    primaryContainer = Color(0xFF54391D),
    onPrimaryContainer = Color(0xFFF7E3C2),
    secondary = Color(0xFF9CC7E0),
    onSecondary = Color(0xFF10303F),
    secondaryContainer = Color(0xFF2A4E63),
    onSecondaryContainer = Color(0xFFD3E9F6),
    tertiary = Color(0xFFB6C68F),
    tertiaryContainer = Color(0xFF3D4A28),
    background = Color(0xFF17140F),
    onBackground = Color(0xFFEBE1D0),
    surface = Color(0xFF1F1B15),
    onSurface = Color(0xFFEBE1D0),
    surfaceVariant = Color(0xFF3B342A),
    onSurfaceVariant = Color(0xFFD5C9B4),
    outline = Color(0xFF9A8B72),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF6D2020),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun FantasyMapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
