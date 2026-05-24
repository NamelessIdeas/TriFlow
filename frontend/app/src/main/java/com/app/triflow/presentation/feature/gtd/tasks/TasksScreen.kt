package com.app.triflow.presentation.feature.gtd.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.triflow.domain.model.GtdContext
import com.app.triflow.domain.model.Task
import com.app.triflow.domain.model.TaskDraft
import com.app.triflow.domain.model.TaskStatus
import com.app.triflow.presentation.common.EmptyView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onOpenTaskDetail: (String) -> Unit,
    viewModel: TasksViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val contexts by viewModel.contexts.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()

    var creating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { creating = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) { Icon(Icons.Outlined.Add, contentDescription = "Nuova task") }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {

            FilterRow(
                selectedStatus = state.filter.status,
                contexts = contexts,
                selectedContextId = state.filter.contextId,
                onStatus = viewModel::setStatus,
                onContext = viewModel::setContext,
            )

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                if (tasks.isEmpty()) {
                    EmptyView(
                        icon = Icons.Outlined.Task,
                        title = "Nessuna task",
                        subtitle = "Cambia filtro o creane una con il +.",
                    )
                } else {
                    LazyColumn {
                        items(items = tasks, key = { it.id }) { task ->
                            TaskRow(
                                task = task,
                                onClick = { onOpenTaskDetail(task.id) },
                                onToggle = { viewModel.toggleDone(task) },
                            )
                        }
                    }
                }
            }
        }

        if (creating) {
            ModalBottomSheet(onDismissRequest = { creating = false }) {
                CreateTaskSheet(
                    contexts = contexts,
                    projects = projects,
                    submitting = state.creating,
                    onSubmit = { draft ->
                        viewModel.createTask(draft)
                        scope.launch { /* close */ }.invokeOnCompletion { creating = false }
                    },
                )
            }
        }
    }
}

@Composable
private fun FilterRow(
    selectedStatus: TaskStatus?,
    contexts: List<GtdContext>,
    selectedContextId: String?,
    onStatus: (TaskStatus?) -> Unit,
    onContext: (String?) -> Unit,
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = selectedStatus == null,
                onClick = { onStatus(null) },
                label = { Text("Tutte") },
            )
            TaskStatus.entries.forEach { s ->
                FilterChip(
                    selected = s == selectedStatus,
                    onClick = { onStatus(if (s == selectedStatus) null else s) },
                    label = { Text(statusLabel(s)) },
                )
            }
        }
        if (contexts.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = selectedContextId == null,
                    onClick = { onContext(null) },
                    label = { Text("Tutti i contesti") },
                )
                contexts.forEach { ctx ->
                    FilterChip(
                        selected = ctx.id == selectedContextId,
                        onClick = { onContext(if (ctx.id == selectedContextId) null else ctx.id) },
                        label = { Text("@${ctx.name}") },
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskRow(task: Task, onClick: () -> Unit, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier
            .padding(end = 12.dp)
            .clickable(onClick = onToggle)) {
            Icon(
                imageVector = if (task.isDone) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = if (task.isDone) "Riapri" else "Completa",
                tint = if (task.isDone) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
            )
            val sub = listOfNotNull(
                statusLabel(task.status),
                task.estimatedMinutes?.let { "${it} min" },
                task.tags.firstOrNull()?.let { "#$it" },
            ).joinToString(" · ")
            if (sub.isNotEmpty()) {
                Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
}

@Composable
private fun CreateTaskSheet(
    contexts: List<GtdContext>,
    projects: List<com.app.triflow.domain.model.Project>,
    submitting: Boolean,
    onSubmit: (TaskDraft) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(TaskStatus.NextAction) }
    var contextId by remember { mutableStateOf<String?>(null) }
    var projectId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Nuova task", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titolo") },
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Status", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TaskStatus.entries.filter { it != TaskStatus.Done }.forEach { s ->
                FilterChip(
                    selected = s == status,
                    onClick = { status = s },
                    label = { Text(statusLabel(s)) },
                )
            }
        }

        if (contexts.isNotEmpty()) {
            Text("Contesto", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = contextId == null,
                    onClick = { contextId = null },
                    label = { Text("Nessuno") },
                )
                contexts.forEach { ctx ->
                    FilterChip(
                        selected = ctx.id == contextId,
                        onClick = { contextId = if (ctx.id == contextId) null else ctx.id },
                        label = { Text("@${ctx.name}") },
                    )
                }
            }
        }
        if (projects.isNotEmpty()) {
            Text("Progetto", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = projectId == null,
                    onClick = { projectId = null },
                    label = { Text("Nessuno") },
                )
                projects.forEach { p ->
                    FilterChip(
                        selected = p.id == projectId,
                        onClick = { projectId = if (p.id == projectId) null else p.id },
                        label = { Text(p.title) },
                    )
                }
            }
        }

        AssistChip(
            onClick = {
                if (title.isNotBlank() && !submitting) {
                    onSubmit(
                        TaskDraft(
                            title = title.trim(),
                            status = status,
                            contextId = contextId,
                            projectId = projectId,
                        )
                    )
                }
            },
            label = { Text(if (submitting) "Creo..." else "Crea task") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primary,
                labelColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

internal fun statusLabel(status: TaskStatus): String = when (status) {
    TaskStatus.Inbox -> "Inbox"
    TaskStatus.NextAction -> "Next"
    TaskStatus.Waiting -> "Waiting"
    TaskStatus.Scheduled -> "Scheduled"
    TaskStatus.Done -> "Done"
}
