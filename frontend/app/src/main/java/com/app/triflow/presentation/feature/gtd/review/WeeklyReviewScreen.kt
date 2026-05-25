package com.app.triflow.presentation.feature.gtd.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.triflow.domain.model.InboxItem
import com.app.triflow.domain.model.Project
import com.app.triflow.domain.model.Task
import com.app.triflow.domain.model.WeeklyReview
import com.app.triflow.presentation.common.EmptyView
import com.app.triflow.presentation.common.ErrorView
import com.app.triflow.presentation.common.LoadingView
import com.app.triflow.presentation.common.SectionHeader
import com.app.triflow.presentation.common.UiState

@Composable
fun WeeklyReviewScreen(
    onOpenTaskDetail: (String) -> Unit,
    viewModel: WeeklyReviewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val s = state) {
        is UiState.Loading -> LoadingView()
        is UiState.Error -> ErrorView(error = s.error, onRetry = viewModel::refresh)
        is UiState.Success -> Content(review = s.value, onOpenTaskDetail = onOpenTaskDetail)
        UiState.Idle -> Unit
    }
}

@Composable
private fun Content(review: WeeklyReview, onOpenTaskDetail: (String) -> Unit) {
    val nothing = review.inboxToProcess.isEmpty() &&
        review.waitingTasks.isEmpty() &&
        review.projectsWithoutNextAction.isEmpty()
    if (nothing) {
        EmptyView(
            icon = Icons.AutoMirrored.Outlined.EventNote,
            title = "Tutto pulito",
            subtitle = "Nessun item in sospeso. Buon lavoro.",
        )
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (review.inboxToProcess.isNotEmpty()) {
            item { SectionHeader(title = "Da processare (${review.inboxToProcess.size})") {} }
            items(items = review.inboxToProcess, key = { it.id }) { InboxRow(it) }
        }
        if (review.waitingTasks.isNotEmpty()) {
            item { SectionHeader(title = "In attesa (${review.waitingTasks.size})") {} }
            items(items = review.waitingTasks, key = { it.id }) { task ->
                TaskRow(task = task, onClick = { onOpenTaskDetail(task.id) })
            }
        }
        if (review.projectsWithoutNextAction.isNotEmpty()) {
            item { SectionHeader(title = "Progetti senza next action (${review.projectsWithoutNextAction.size})") {} }
            items(items = review.projectsWithoutNextAction, key = { it.id }) { ProjectRow(it) }
        }
    }
}

@Composable
private fun InboxRow(item: InboxItem) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 10.dp)) {
        Text(item.rawText, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
}

@Composable
private fun TaskRow(task: Task, onClick: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = 20.dp, vertical = 10.dp)) {
        Text(task.title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(
            "waiting",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
}

@Composable
private fun ProjectRow(project: Project) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 10.dp)) {
        Text(project.title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(
            project.status.name.lowercase(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
}
