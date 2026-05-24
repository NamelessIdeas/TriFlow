package com.app.triflow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val TriFlowDarkScheme = darkColorScheme(
    primary = GtdPurple,
    onPrimary = OnBackground,
    primaryContainer = GtdPurpleSurface,
    onPrimaryContainer = GtdPurpleSoft,
    secondary = PomodoroGreen,
    onSecondary = Background,
    secondaryContainer = PomodoroGreenSurface,
    onSecondaryContainer = PomodoroGreenSoft,
    tertiary = BrainAmber,
    onTertiary = Background,
    tertiaryContainer = BrainAmberSurface,
    onTertiaryContainer = BrainAmberSoft,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
    error = ErrorRed,
    onError = OnError,
)

@Composable
fun TriFlowTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalMethodColors provides DefaultMethodPalette) {
        MaterialTheme(
            colorScheme = TriFlowDarkScheme,
            typography = TriFlowTypography,
            content = content,
        )
    }
}
