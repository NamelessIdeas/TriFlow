package com.app.triflow.presentation.feature.gtd.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.triflow.domain.model.InboxItem
import com.app.triflow.domain.model.TaskStatus
import com.app.triflow.presentation.common.EmptyView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    viewModel: InboxViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()

    var processing by remember { mutableStateOf<InboxItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        CaptureBar(
            text = state.captureText,
            submitting = state.capturing,
            onTextChange = viewModel::onCaptureText,
            onSubmit = viewModel::capture,
        )
        if (state.error != null) {
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
        if (items.isEmpty()) {
            EmptyView(
                modifier = Modifier.fillMaxSize(),
                icon = Icons.Outlined.Inbox,
                title = "Inbox vuoto",
                subtitle = "Cattura un'idea sopra. La processeremo dopo.",
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
            ) {
                items(items = items, key = { it.id }) { item ->
                    InboxRow(item = item, onClick = { processing = item })
                }
            }
        }
    }

    if (processing != null) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { processing = null },
        ) {
            ProcessSheet(
                item = processing!!,
                processing = state.processing,
                onAsTask = { title, status ->
                    viewModel.processAsTask(processing!!.id, title, status)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { processing = null }
                },
                onAsProject = { title ->
                    viewModel.processAsProject(processing!!.id, title)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { processing = null }
                },
                onDiscard = {
                    viewModel.discard(processing!!.id)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { processing = null }
                },
            )
        }
    }
}

@Composable
private fun CaptureBar(
    text: String,
    submitting: Boolean,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("Cattura un'idea, una task, qualsiasi cosa…") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Sentences,
            ),
        )
        Spacer(Modifier.padding(start = 8.dp))
        IconButton(
            onClick = onSubmit,
            enabled = !submitting && text.isNotBlank(),
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "Aggiungi", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun InboxRow(item: InboxItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Inbox, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = item.rawText,
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        AssistChip(
            onClick = onClick,
            label = { Text("Processa") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
}

@Composable
private fun ProcessSheet(
    item: InboxItem,
    processing: Boolean,
    onAsTask: (title: String, status: TaskStatus) -> Unit,
    onAsProject: (title: String) -> Unit,
    onDiscard: () -> Unit,
) {
    var title by remember(item.id) { mutableStateOf(item.rawText) }
    var status by remember(item.id) { mutableStateOf(TaskStatus.NextAction) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Processa", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titolo") },
            modifier = Modifier.fillMaxWidth(),
        )
        Text("Status", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(TaskStatus.NextAction, TaskStatus.Waiting, TaskStatus.Scheduled).forEach { s ->
                AssistChip(
                    onClick = { status = s },
                    label = { Text(s.name) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (s == status) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = if (s == status) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        ProcessAction(
            color = MaterialTheme.colorScheme.primary,
            label = "Crea task",
            enabled = !processing && title.isNotBlank(),
            onClick = { onAsTask(title.trim(), status) },
        )
        ProcessAction(
            color = MaterialTheme.colorScheme.secondary,
            label = "Crea progetto",
            enabled = !processing && title.isNotBlank(),
            onClick = { onAsProject(title.trim()) },
        )
        ProcessAction(
            color = MaterialTheme.colorScheme.error,
            label = "Scarta",
            enabled = !processing,
            onClick = onDiscard,
        )
    }
}

@Composable
private fun ProcessAction(
    color: androidx.compose.ui.graphics.Color,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = if (enabled) 0.15f else 0.05f),
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier
                .background(androidx.compose.ui.graphics.Color.Transparent)
                .padding(16.dp),
            style = MaterialTheme.typography.titleSmall,
            color = if (enabled) color else color.copy(alpha = 0.4f),
        )
    }
}
