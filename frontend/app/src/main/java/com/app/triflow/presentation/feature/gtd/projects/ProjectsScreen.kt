package com.app.triflow.presentation.feature.gtd.projects

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Folder
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.triflow.domain.model.Project
import com.app.triflow.domain.model.ProjectStatus
import com.app.triflow.presentation.common.EmptyView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(viewModel: ProjectsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()

    var creating by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { creating = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) { Icon(Icons.Outlined.Add, contentDescription = "Nuovo progetto") }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            FilterRow(state.filter, viewModel::setFilter)
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
                if (projects.isEmpty()) {
                    EmptyView(
                        icon = Icons.Outlined.Folder,
                        title = "Nessun progetto",
                        subtitle = "Aggiungine uno con il +.",
                    )
                } else {
                    LazyColumn {
                        items(items = projects, key = { it.id }) { ProjectRow(it) }
                    }
                }
            }
        }

        if (creating) {
            ModalBottomSheet(onDismissRequest = { creating = false }) {
                CreateProjectSheet(
                    submitting = state.creating,
                    onSubmit = { title, desc, status ->
                        viewModel.create(title, desc, status)
                        creating = false
                    },
                )
            }
        }
    }
}

@Composable
private fun FilterRow(filter: ProjectStatus?, onChange: (ProjectStatus?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = filter == null,
            onClick = { onChange(null) },
            label = { Text("Tutti") },
        )
        ProjectStatus.entries.forEach { s ->
            FilterChip(
                selected = s == filter,
                onClick = { onChange(if (s == filter) null else s) },
                label = { Text(s.name.lowercase()) },
            )
        }
    }
}

@Composable
private fun ProjectRow(project: Project) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(project.title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(
            project.status.name.lowercase(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (project.description.isNotBlank()) {
            Text(
                project.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
}

@Composable
private fun CreateProjectSheet(
    submitting: Boolean,
    onSubmit: (title: String, description: String, status: ProjectStatus) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(ProjectStatus.Active) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Nuovo progetto", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titolo") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descrizione (opzionale)") },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ProjectStatus.entries.forEach { s ->
                FilterChip(
                    selected = s == status,
                    onClick = { status = s },
                    label = { Text(s.name.lowercase()) },
                )
            }
        }
        AssistChip(
            onClick = { if (!submitting) onSubmit(title, description, status) },
            label = { Text(if (submitting) "Creo..." else "Crea progetto") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primary,
                labelColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
