package com.app.triflow.presentation.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.WbIncandescent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.triflow.domain.model.ActiveTimer
import com.app.triflow.domain.model.Dashboard
import com.app.triflow.domain.model.Note
import com.app.triflow.domain.model.PomodoroKind
import com.app.triflow.domain.model.Task
import com.app.triflow.presentation.common.ErrorView
import com.app.triflow.presentation.common.LoadingView
import com.app.triflow.presentation.common.Method
import com.app.triflow.presentation.common.MethodBadge
import com.app.triflow.presentation.common.SectionHeader
import com.app.triflow.presentation.common.UiState
import com.app.triflow.ui.theme.TriFlowTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit,
    onOpenQuiz: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val loggedOut by viewModel.loggedOut.collectAsStateWithLifecycle()
    val refreshState = rememberPullToRefreshState()

    LaunchedEffect(loggedOut) { if (loggedOut) onLogout() }

    PullToRefreshBox(
        isRefreshing = state is UiState.Loading,
        onRefresh = viewModel::refresh,
        state = refreshState,
        modifier = modifier,
    ) {
        when (val s = state) {
            is UiState.Loading -> LoadingView()
            is UiState.Error -> ErrorView(error = s.error, onRetry = viewModel::refresh)
            is UiState.Success -> DashboardContent(
                dashboard = s.value,
                onLogout = viewModel::logout,
                onOpenQuiz = onOpenQuiz,
            )
            UiState.Idle -> Unit
        }
    }
}

@Composable
private fun DashboardContent(
    dashboard: Dashboard,
    onLogout: () -> Unit,
    onOpenQuiz: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { TimerCard(dashboard.activeTimer) }
        item { StatsRow(pomodorosToday = dashboard.pomodorosToday, focusSecondsWeek = dashboard.focusSecondsWeek) }

        item {
            SectionHeader(title = "Oggi", actionLabel = if (dashboard.todayTasks.isNotEmpty()) "Vedi tutti" else null) {}
        }
        if (dashboard.todayTasks.isEmpty()) {
            item { EmptySectionCard(text = "Nessuna task per oggi. Aggiungi qualcosa dall'Inbox.") }
        } else {
            items(items = dashboard.todayTasks, key = { it.id }) { TaskRow(task = it) }
        }

        item { SectionHeader(title = "Note recenti") {} }
        if (dashboard.recentNotes.isEmpty()) {
            item { EmptySectionCard(text = "Cattura un'idea in Second Brain per vederla qui.") }
        } else {
            items(items = dashboard.recentNotes, key = { it.id }) { NoteRow(note = it) }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onOpenQuiz) { Text("Suggeriscimi un metodo") }
                TextButton(onClick = onLogout) { Text("Logout") }
            }
        }
    }
}

@Composable
private fun TimerCard(activeTimer: ActiveTimer?) {
    val palette = TriFlowTheme.methodColors
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = palette.pomodoroSurface),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.PlayCircleOutline,
                contentDescription = null,
                tint = palette.pomodoro,
                modifier = Modifier.size(36.dp),
            )
            Spacer(Modifier.height(0.dp))
            Column(modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)) {
                MethodBadge(method = Method.Pomodoro, label = "Pomodoro")
                if (activeTimer == null) {
                    Text(
                        "Nessun timer attivo",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    Text(
                        "Avvia una sessione di focus collegata a una task.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    val kindLabel = when (activeTimer.kind) {
                        PomodoroKind.Focus -> "Focus"
                        PomodoroKind.ShortBreak -> "Pausa breve"
                        PomodoroKind.LongBreak -> "Pausa lunga"
                    }
                    Text(
                        "$kindLabel · ciclo ${activeTimer.cycleIndex}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    val remaining = activeTimer.remainingSec()
                    Text(
                        "Rimanenti ${remaining / 60}m ${remaining % 60}s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.pomodoroSoft,
                    )
                }
            }
            IconButton(onClick = { /* TODO collegamento al timer */ }) {
                Icon(Icons.Outlined.RestartAlt, contentDescription = "Apri timer", tint = palette.pomodoro)
            }
        }
    }
}

@Composable
private fun StatsRow(pomodorosToday: Int, focusSecondsWeek: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatChip(
            value = pomodorosToday.toString(),
            label = "Pomodori oggi",
            modifier = Modifier.weight(1f),
        )
        val hours = focusSecondsWeek / 3600
        val minutes = (focusSecondsWeek % 3600) / 60
        StatChip(
            value = "${hours}h ${minutes}m",
            label = "Focus settimana",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatChip(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TaskRow(task: Task) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (task.isDone) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (task.isDone) TriFlowTheme.methodColors.pomodoro else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(modifier = Modifier
            .padding(start = 12.dp)
            .weight(1f)) {
            Text(task.title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            if (task.estimatedMinutes != null) {
                Text(
                    "${task.estimatedMinutes} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        MethodBadge(method = Method.Gtd, label = "GTD")
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
}

@Composable
private fun NoteRow(note: Note) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.WbIncandescent,
            contentDescription = null,
            tint = TriFlowTheme.methodColors.brain,
        )
        Text(
            text = note.title,
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        MethodBadge(method = Method.SecondBrain, label = "Brain")
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
}

@Composable
private fun EmptySectionCard(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
