package com.aiva.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    primaryContainer = Color(0xFF1565C0),
    secondary = Color(0xFF81D4FA),
    secondaryContainer = Color(0xFF01579B),
    tertiary = Color(0xFF4FC3F7),
    tertiaryContainer = Color(0xFF0288D1),
    surface = Color(0xFF121212),
    surfaceContainerHighest = Color(0xFF1E1E1E),
    background = Color(0xFF000000),
    onPrimary = Color(0xFF000000),
    onPrimaryContainer = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    outline = Color(0xFF757575)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    primaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFF01579B),
    secondaryContainer = Color(0xFF81D4FA),
    tertiary = Color(0xFF0288D1),
    tertiaryContainer = Color(0xFF4FC3F7),
    surface = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFF5F5F5),
    background = Color(0xFFFAFAFA),
    onPrimary = Color(0xFFFFFFFF),
    onPrimaryContainer = Color(0xFF000000),
    onSurface = Color(0xFF000000),
    outline = Color(0xFF757575)
)

@Composable
fun AivaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val context = LocalContext.current
    
    SideEffect {
        val window = (context as? android.app.Activity)?.window
        val controller = WindowInsetsControllerCompat(window!!, window.decorView)
        controller.isAppearanceLightStatusBars = !darkTheme
        controller.isAppearanceLightNavigationBars = !darkTheme
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

object Typography {
    val displayLarge = androidx.compose.material3.TypographyDefaults.DisplayLarge
    val displayMedium = androidx.compose.material3.TypographyDefaults.DisplayMedium
    val displaySmall = androidx.compose.material3.TypographyDefaults.DisplaySmall
    val headlineLarge = androidx.compose.material3.TypographyDefaults.HeadlineLarge
    val headlineMedium = androidx.compose.material3.TypographyDefaults.HeadlineMedium
    val headlineSmall = androidx.compose.material3.TypographyDefaults.HeadlineSmall
    val titleLarge = androidx.compose.material3.TypographyDefaults.TitleLarge
    val titleMedium = androidx.compose.material3.TypographyDefaults.TitleMedium
    val titleSmall = androidx.compose.material3.TypographyDefaults.TitleSmall
    val bodyLarge = androidx.compose.material3.TypographyDefaults.BodyLarge
    val bodyMedium = androidx.compose.material3.TypographyDefaults.BodyMedium
    val bodySmall = androidx.compose.material3.TypographyDefaults.BodySmall
    val labelLarge = androidx.compose.material3.TypographyDefaults.LabelLarge
    val labelMedium = androidx.compose.material3.TypographyDefaults.LabelMedium
    val labelSmall = androidx.compose.material3.TypographyDefaults.LabelSmall
}