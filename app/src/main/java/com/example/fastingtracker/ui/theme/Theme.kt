package com.example.fastingtracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Lime40,
    primaryContainer = Lime80,
    secondary = Lime20,
    onPrimary = Black,
    onSecondary = Black,
    background = White,
    surface = White,
    onBackground = Black,
    onSurface = Black,
)

@Composable
fun FastingTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,  // podes criar Typography.kt se quiseres, senão remove
        content = content
    )
}
