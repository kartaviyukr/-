package com.simple.notes.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF8A6A00),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE082),
    onPrimaryContainer = Color(0xFF2B2000),
    secondary = Color(0xFF6B5E3E),
    background = Color(0xFFFFFBF2),
    surface = Color(0xFFFFFBF2),
    surfaceVariant = Color(0xFFF0E6D2),
    onSurface = Color(0xFF1E1B16)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF2C744),
    onPrimary = Color(0xFF3A2E00),
    primaryContainer = Color(0xFF544300),
    onPrimaryContainer = Color(0xFFFFE082),
    secondary = Color(0xFFD5C6A1),
    background = Color(0xFF15130E),
    surface = Color(0xFF15130E),
    surfaceVariant = Color(0xFF3B3529),
    onSurface = Color(0xFFE9E1D4)
)

@Composable
fun NotesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
