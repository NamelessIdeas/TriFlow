package com.app.triflow.presentation.feature.secondbrain.editor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MoveUp
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.triflow.domain.model.Note
import com.app.triflow.domain.model.ParaCategory
import com.app.triflow.domain.model.TaskStatus
import com.app.triflow.presentation.feature.secondbrain.markdown.MarkdownActions
import com.app.triflow.presentation.feature.secondbrain.markdown.MarkdownToolbar
import dev.jeziellago.compose.markdowntext.MarkdownText

private enum class EditorTab { Editor, Preview }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: String?,
    onBack: () -> Unit,
    onOpenTaskDetail: (String) -> Unit,
    viewModel: NoteEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(noteId) { viewModel.load(noteId) }
    LaunchedEffect(state.deleted) { if (state.deleted) onBack() }

    LaunchedEffect(state.promotedTaskId) {
        val taskId = state.promotedTaskId
        if (taskId != null) {
            viewModel.consumePromoted()
            onOpenTaskDetail(taskId)
        }
    }

    var tab by remember { mutableStateOf(EditorTab.Editor) }
    var promoteOpen by remember { mutableStateOf(false) }
    var linkDialogOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.noteId == null) "Nuova nota" else "Modifica nota") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::save, enabled = !state.saving) {
                        Icon(Icons.Outlined.Save, contentDescription = "Salva")
                    }
                    if (state.noteId != null) {
                        IconButton(onClick = { promoteOpen = true }) {
                            Icon(Icons.Outlined.MoveUp, contentDescription = "Promuovi a task", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = viewModel::delete) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Elimina", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {

            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitle,
                placeholder = { Text("Titolo") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            CategoryAndTags(
                paraCategory = state.paraCategory,
                onPara = viewModel::onPara,
                tags = state.tagsInput,
                onTags = viewModel::onTags,
            )

            if (state.error != null) {
                Text(
                    state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            SecondaryTabRow(
                selectedTabIndex = tab.ordinal,
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                EditorTab.entries.forEach { entry ->
                    Tab(
                        selected = entry == tab,
                        onClick = { tab = entry },
                        text = { Text(if (entry == EditorTab.Editor) "Editor" else "Preview", style = MaterialTheme.typography.labelMedium) },
                    )
                }
            }

            when (tab) {
                EditorTab.Editor -> EditorPane(
                    content = state.content,
                    onContent = viewModel::onContent,
                    onOpenLinkDialog = { linkDialogOpen = true },
                )
                EditorTab.Preview -> PreviewPane(content = state.content.text)
            }

            if (state.backlinks.isNotEmpty() || state.linkedNotes.isNotEmpty()) {
                RelatedNotes(
                    backlinks = state.backlinks,
                    linked = state.linkedNotes,
                )
            }
        }

        if (promoteOpen) {
            PromoteDialog(
                defaultTitle = state.title,
                onDismiss = { promoteOpen = false },
                onConfirm = { title, status, priority ->
                    viewModel.promoteToTask(title, status, priority)
                    promoteOpen = false
                },
            )
        }
        if (linkDialogOpen) {
            LinkDialog(
                onDismiss = { linkDialogOpen = false },
                onConfirm = { url ->
                    viewModel.onContent(MarkdownActions.link(state.content, url))
                    linkDialogOpen = false
                },
            )
        }
    }
}

@Composable
private fun CategoryAndTags(
    paraCategory: ParaCategory?,
    onPara: (ParaCategory?) -> Unit,
    tags: String,
    onTags: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FilterChip(
            selected = paraCategory == null,
            onClick = { onPara(null) },
            label = { Text("nessuna") },
        )
        ParaCategory.entries.forEach { cat ->
            FilterChip(
                selected = cat == paraCategory,
                onClick = { onPara(if (cat == paraCategory) null else cat) },
                label = { Text(cat.name.lowercase()) },
            )
        }
    }
    OutlinedTextField(
        value = tags,
        onValueChange = onTags,
        placeholder = { Text("Tag separati da virgola (es. mvp, idee)") },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

@Composable
private fun EditorPane(
    content: TextFieldValue,
    onContent: (TextFieldValue) -> Unit,
    onOpenLinkDialog: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        MarkdownToolbar(
            onBold = { onContent(MarkdownActions.bold(content)) },
            onItalic = { onContent(MarkdownActions.italic(content)) },
            onStrike = { onContent(MarkdownActions.strike(content)) },
            onH1 = { onContent(MarkdownActions.h1(content)) },
            onH2 = { onContent(MarkdownActions.h2(content)) },
            onList = { onContent(MarkdownActions.listItem(content)) },
            onQuote = { onContent(MarkdownActions.quote(content)) },
            onCodeInline = { onContent(MarkdownActions.codeInline(content)) },
            onCodeBlock = { onContent(MarkdownActions.codeBlock(content)) },
            onLink = onOpenLinkDialog,
        )
        OutlinedTextField(
            value = content,
            onValueChange = onContent,
            placeholder = { Text("Scrivi in markdown…") },
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun PreviewPane(content: String) {
    val text = content.ifBlank { "_(nota vuota)_" }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        MarkdownText(
            markdown = text,
            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        )
    }
}

@Composable
private fun RelatedNotes(backlinks: List<Note>, linked: List<Note>) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        if (linked.isNotEmpty()) {
            SectionLabel("Linkate da questa nota (${linked.size})")
            linked.forEach { NoteMini(it) }
        }
        if (backlinks.isNotEmpty()) {
            SectionLabel("Backlinks (${backlinks.size})")
            backlinks.forEach { NoteMini(it) }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
}

@Composable
private fun NoteMini(note: Note) {
    Text(
        text = "• ${note.title}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
    )
}

@Composable
private fun PromoteDialog(
    defaultTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, status: TaskStatus, priority: Int?) -> Unit,
) {
    var title by remember(defaultTitle) { mutableStateOf(defaultTitle) }
    var status by remember { mutableStateOf(TaskStatus.NextAction) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Promuovi a task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titolo task") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    listOf(TaskStatus.NextAction, TaskStatus.Waiting, TaskStatus.Scheduled).forEach { s ->
                        FilterChip(
                            selected = s == status,
                            onClick = { status = s },
                            label = { Text(s.name) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title.trim(), status, null) }) { Text("Crea task") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        },
    )
}

@Composable
private fun LinkDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var url by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Inserisci link") },
        text = {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (url.isNotBlank()) onConfirm(url.trim()) },
            ) { Text("Inserisci") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        },
    )
}
