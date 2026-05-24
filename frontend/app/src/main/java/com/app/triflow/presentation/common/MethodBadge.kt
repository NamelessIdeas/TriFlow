package com.app.triflow.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.triflow.ui.theme.TriFlowTheme

enum class Method { Gtd, Pomodoro, SecondBrain }

@Composable
fun MethodBadge(method: Method, label: String, modifier: Modifier = Modifier) {
    val palette = TriFlowTheme.methodColors
    val (background, foreground) = when (method) {
        Method.Gtd -> palette.gtdSurface to palette.gtdSoft
        Method.Pomodoro -> palette.pomodoroSurface to palette.pomodoroSoft
        Method.SecondBrain -> palette.brainSurface to palette.brainSoft
    }
    BadgeBox(label = label, background = background, foreground = foreground, modifier = modifier)
}

@Composable
fun BadgeBox(
    label: String,
    background: Color,
    foreground: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = foreground,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
