package com.app.triflow.presentation.feature.pomodoro.timer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.triflow.domain.model.PomodoroKind
import com.app.triflow.domain.usecase.gtd.ObserveTasksUseCase
import com.app.triflow.ui.theme.TriFlowTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroTimerScreen(viewModel: PomodoroTimerViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val palette = TriFlowTheme.methodColors

    var selectingTask by remember { mutableStateOf(false) }
    var pickedTaskId by remember { mutableStateOf<String?>(null) }
    var pickedTaskTitle by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        if (state.error != null) {
            Text(
                state.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        ProgressCircle(
            progress = state.progress,
            remainingSec = state.remainingSec,
            label = kindLabel(state.kind),
            color = when (state.kind) {
                PomodoroKind.Focus -> palette.pomodoro
                PomodoroKind.ShortBreak -> palette.brain
                PomodoroKind.LongBreak -> palette.gtd
            },
        )

        if (state.active != null) {
            Text(
                text = state.taskTitle ?: if (state.active!!.taskId == null) "Senza task collegata" else "Task",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Ciclo ${state.cycleIndex}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            // Selezione task pre-start
            AssistChip(
                onClick = { selectingTask = true },
                label = { Text(pickedTaskTitle ?: "Collega una task (opzionale)") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }

        Spacer(Modifier.height(8.dp))
        Controls(
            isRunning = state.isRunning,
            isPaused = state.isPaused,
            hasActive = state.active != null,
            busy = state.busy,
            onStartFocus = { viewModel.start(pickedTaskId) },
            onStartShort = { viewModel.startBreak(PomodoroKind.ShortBreak) },
            onStartLong = { viewModel.startBreak(PomodoroKind.LongBreak) },
            onPause = viewModel::pause,
            onResume = viewModel::resume,
            onComplete = viewModel::complete,
            onAbort = viewModel::abort,
        )
    }

    if (selectingTask) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { selectingTask = false },
        ) {
            TaskPickerSheet(
                onPick = { id, title ->
                    pickedTaskId = id
                    pickedTaskTitle = title
                    selectingTask = false
                },
                onClear = {
                    pickedTaskId = null
                    pickedTaskTitle = null
                    selectingTask = false
                },
            )
        }
    }
}

@Composable
private fun ProgressCircle(
    progress: Float,
    remainingSec: Int,
    label: String,
    color: Color,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "progress",
    )
    val track = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    Box(
        modifier = Modifier
            .size(260.dp)
            .clip(androidx.compose.foundation.shape.CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 18.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            val inset = stroke.width / 2
            val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatTime(remainingSec),
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(label, style = MaterialTheme.typography.labelLarge, color = color)
        }
    }
}

@Composable
private fun Controls(
    isRunning: Boolean,
    isPaused: Boolean,
    hasActive: Boolean,
    busy: Boolean,
    onStartFocus: () -> Unit,
    onStartShort: () -> Unit,
    onStartLong: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onComplete: () -> Unit,
    onAbort: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when {
            !hasActive -> {
                Button(
                    onClick = onStartFocus,
                    enabled = !busy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                    Spacer(Modifier.height(0.dp))
                    Text("Inizia focus")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledTonalButton(
                        onClick = onStartShort,
                        enabled = !busy,
                        modifier = Modifier.weight(1f),
                    ) { Text("Pausa breve") }
                    FilledTonalButton(
                        onClick = onStartLong,
                        enabled = !busy,
                        modifier = Modifier.weight(1f),
                    ) { Text("Pausa lunga") }
                }
            }
            isRunning -> {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledTonalButton(
                        onClick = onPause,
                        enabled = !busy,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Outlined.Pause, contentDescription = null)
                        Spacer(Modifier.height(0.dp))
                        Text("Pausa")
                    }
                    Button(
                        onClick = onComplete,
                        enabled = !busy,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                        ),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Outlined.Stop, contentDescription = null)
                        Spacer(Modifier.height(0.dp))
                        Text("Completa")
                    }
                }
            }
            isPaused -> {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onResume,
                        enabled = !busy,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                        ),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                        Spacer(Modifier.height(0.dp))
                        Text("Riprendi")
                    }
                    FilledTonalButton(
                        onClick = onAbort,
                        enabled = !busy,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Outlined.Cancel, contentDescription = null)
                        Spacer(Modifier.height(0.dp))
                        Text("Annulla")
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskPickerSheet(
    onPick: (id: String, title: String) -> Unit,
    onClear: () -> Unit,
    viewModel: TaskPickerViewModel = hiltViewModel(),
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Collega una task", style = MaterialTheme.typography.titleMedium)
        FilledTonalButton(onClick = onClear, modifier = Modifier.fillMaxWidth()) {
            Text("Senza task")
        }
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(items = tasks, key = { it.id }) { task ->
                AssistChip(
                    onClick = { onPick(task.id, task.title) },
                    label = { Text(task.title) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
        }
    }
}

private fun kindLabel(kind: PomodoroKind): String = when (kind) {
    PomodoroKind.Focus -> "Focus"
    PomodoroKind.ShortBreak -> "Pausa breve"
    PomodoroKind.LongBreak -> "Pausa lunga"
}

private fun formatTime(totalSec: Int): String {
    val m = (totalSec / 60).coerceAtLeast(0)
    val s = (totalSec % 60).coerceAtLeast(0)
    return "%02d:%02d".format(m, s)
}
