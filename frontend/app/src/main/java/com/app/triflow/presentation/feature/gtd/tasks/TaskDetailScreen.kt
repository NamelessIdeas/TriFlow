package com.app.triflow.presentation.feature.gtd.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.triflow.domain.model.Note
import com.app.triflow.domain.model.PomodoroKind
import com.app.triflow.domain.model.PomodoroSession
import com.app.triflow.domain.model.Task
import com.app.triflow.presentation.common.ErrorView
import com.app.triflow.presentation.common.LoadingView
import com.app.triflow.presentation.common.Method
import com.app.triflow.presentation.common.MethodBadge
import com.app.triflow.presentation.common.SectionHeader
import com.app.triflow.presentation.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    onBack: () -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val deleted by viewModel.deleted.collectAsStateWithLifecycle()

    LaunchedEffect(taskId) { viewModel.load(taskId) }
    LaunchedEffect(deleted) { if (deleted) onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dettaglio task") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::onToggleDone) {
                        val isDone = (state as? UiState.Success)?.value?.task?.isDone == true
                        Icon(
                            imageVector = if (isDone) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = if (isDone) "Riapri" else "Completa",
                            tint = if (isDone) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = viewModel::onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Elimina", tint = MaterialTheme.colorScheme.error)
                    }
                },
            )
        },
    ) { padding ->
        when (val s = state) {
            is UiState.Loading -> LoadingView(Modifier.padding(padding))
            is UiState.Error -> ErrorView(error = s.error, modifier = Modifier.padding(padding)) { viewModel.load(taskId) }
            is UiState.Success -> Content(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                task = s.value.task,
                sessions = s.value.pomodoroSessions,
                notes = s.value.linkedNotes,
            )
            UiState.Idle -> Unit
        }
    }
}

@Composable
private fun Content(
    modifier: Modifier,
    task: Task,
    sessions: List<PomodoroSession>,
    notes: List<Note>,
) {
    LazyColumn(modifier = modifier, contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)) {
        item { TaskCard(task) }
        item { SectionHeader(title = "Pomodori (${sessions.size})") {} }
        if (sessions.isEmpty()) {
            item {
                Text(
                    "Nessuna sessione collegata.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
        } else {
            items(items = sessions, key = { it.id }) { PomodoroRow(it) }
        }

        item { SectionHeader(title = "Note collegate (${notes.size})") {} }
        if (notes.isEmpty()) {
            item {
                Text(
                    "Nessuna nota collegata.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
        } else {
            items(items = notes, key = { it.id }) { NoteRow(it) }
        }
    }
}

@Composable
private fun TaskCard(task: Task) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                MethodBadge(method = Method.Gtd, label = statusLabel(task.status))
                Spacer(Modifier.height(0.dp))
            }
            Text(task.title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
            if (task.notes.isNotBlank()) {
                Text(task.notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val meta = buildList {
                task.estimatedMinutes?.let { add("${it} min") }
                task.energy?.let { add(it.name.lowercase()) }
                task.dueDate?.let { add("scadenza ${it}") }
            }
            if (meta.isNotEmpty()) {
                Text(meta.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (task.tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    task.tags.forEach { tag ->
                        AssistChip(
                            onClick = {},
                            label = { Text("#$tag") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PomodoroRow(session: PomodoroSession) {
    val kindLabel = when (session.kind) {
        PomodoroKind.Focus -> "Focus"
        PomodoroKind.ShortBreak -> "Pausa breve"
        PomodoroKind.LongBreak -> "Pausa lunga"
    }
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 10.dp)) {
        Text(
            "$kindLabel · ${session.actualDurationSec / 60} min",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            "${session.status.name.lowercase()} · ciclo ${session.cycleIndex}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
}

@Composable
private fun NoteRow(note: Note) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 10.dp)) {
        Text(note.title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        val para = note.paraCategory
        if (para != null) {
            Text(
                para.name.lowercase(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
}
