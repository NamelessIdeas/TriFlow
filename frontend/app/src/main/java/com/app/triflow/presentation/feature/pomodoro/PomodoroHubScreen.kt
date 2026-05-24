package com.app.triflow.presentation.feature.pomodoro

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.app.triflow.presentation.feature.pomodoro.stats.PomodoroStatsScreen
import com.app.triflow.presentation.feature.pomodoro.timer.PomodoroTimerScreen

private enum class PomodoroSection(val label: String) {
    Timer("Timer"),
    Stats("Statistiche"),
}

@Composable
fun PomodoroHubScreen(modifier: Modifier = Modifier) {
    var section by rememberSaveable { mutableStateOf(PomodoroSection.Timer) }

    Column(modifier = modifier.fillMaxSize()) {
        SecondaryTabRow(
            selectedTabIndex = section.ordinal,
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            PomodoroSection.entries.forEach { entry ->
                Tab(
                    selected = entry == section,
                    onClick = { section = entry },
                    text = { Text(entry.label, style = MaterialTheme.typography.labelMedium) },
                )
            }
        }
        when (section) {
            PomodoroSection.Timer -> PomodoroTimerScreen()
            PomodoroSection.Stats -> PomodoroStatsScreen()
        }
    }
}
