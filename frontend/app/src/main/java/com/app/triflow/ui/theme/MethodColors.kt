package com.app.triflow.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colori dedicati ai 3 metodi (GTD, Pomodoro, Second Brain).
 * Material3 espone solo primary/secondary/tertiary, quindi li passiamo
 * via CompositionLocal per evitare di "rubarli" allo schema globale.
 */
@Immutable
data class MethodPalette(
    val gtd: Color,
    val gtdSoft: Color,
    val gtdSurface: Color,
    val pomodoro: Color,
    val pomodoroSoft: Color,
    val pomodoroSurface: Color,
    val brain: Color,
    val brainSoft: Color,
    val brainSurface: Color,
)

internal val DefaultMethodPalette = MethodPalette(
    gtd = GtdPurple,
    gtdSoft = GtdPurpleSoft,
    gtdSurface = GtdPurpleSurface,
    pomodoro = PomodoroGreen,
    pomodoroSoft = PomodoroGreenSoft,
    pomodoroSurface = PomodoroGreenSurface,
    brain = BrainAmber,
    brainSoft = BrainAmberSoft,
    brainSurface = BrainAmberSurface,
)

val LocalMethodColors = staticCompositionLocalOf { DefaultMethodPalette }

object TriFlowTheme {
    val methodColors: MethodPalette
        @Composable @ReadOnlyComposable
        get() = LocalMethodColors.current
}
