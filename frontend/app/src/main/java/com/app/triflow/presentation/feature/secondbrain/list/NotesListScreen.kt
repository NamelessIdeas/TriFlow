package com.app.triflow.presentation.feature.secondbrain.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.triflow.domain.model.Note
import com.app.triflow.domain.model.ParaCategory
import com.app.triflow.presentation.common.EmptyView
import com.app.triflow.presentation.common.Method
import com.app.triflow.presentation.common.MethodBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    onOpenNote: (String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onOpenNote(null) },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
            ) { Icon(Icons.Outlined.Add, contentDescription = "Nuova nota") }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {

            OutlinedTextField(
                value = state.filter.query.orEmpty(),
                onValueChange = viewModel::setQuery,
                placeholder = { Text("Cerca nelle note…") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )

            CategoryFilters(
                selected = state.filter.paraCategory,
                onChange = viewModel::setCategory,
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
                if (notes.isEmpty()) {
                    EmptyView(
                        icon = Icons.Outlined.AutoStories,
                        title = "Nessuna nota",
                        subtitle = "Crea la prima con il bottone +. Markdown supportato.",
                    )
                } else {
                    LazyColumn {
                        items(items = notes, key = { it.id }) { note ->
                            NoteRow(note = note, onClick = { onOpenNote(note.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilters(selected: ParaCategory?, onChange: (ParaCategory?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onChange(null) },
            label = { Text("Tutte") },
        )
        ParaCategory.entries.forEach { cat ->
            FilterChip(
                selected = cat == selected,
                onClick = { onChange(if (cat == selected) null else cat) },
                label = { Text(cat.name.lowercase()) },
            )
        }
    }
}

@Composable
private fun NoteRow(note: Note, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            val para = note.paraCategory
            if (para != null) {
                MethodBadge(method = Method.SecondBrain, label = para.name.lowercase())
            }
        }
        val preview = note.contentMd.lineSequence().firstOrNull { it.isNotBlank() }?.take(120)
        if (!preview.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = preview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        if (note.tags.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = note.tags.joinToString(" ") { "#$it" },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
}
