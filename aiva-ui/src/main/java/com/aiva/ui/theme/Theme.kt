package com.aiva.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    primaryContainer = Color(0xFF1565C0),
    secondary = Color(0xFF81D4FA),
    secondaryContainer = Color(0xFF01579B),
    tertiary = Color(0xFF4FC3F7),
    surface = Color(0xFF121212),
    background = Color(0xFF000000)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    primaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFF01579B),
    secondaryContainer = Color(0xFF81D4FA),
    tertiary = Color(0xFF0288D1),
    surface = Color(0xFFFFFFFF),
    background = Color(0xFFFAFAFA)
)

@Composable
fun AivaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography(),
        content = content
    )
}
