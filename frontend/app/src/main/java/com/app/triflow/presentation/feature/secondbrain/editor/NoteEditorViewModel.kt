package com.app.triflow.presentation.feature.secondbrain.editor

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.Note
import com.app.triflow.domain.model.NoteDraft
import com.app.triflow.domain.model.NotePatch
import com.app.triflow.domain.model.ParaCategory
import com.app.triflow.domain.model.PromoteNoteParams
import com.app.triflow.domain.model.TaskStatus
import com.app.triflow.domain.usecase.notes.CreateNoteUseCase
import com.app.triflow.domain.usecase.notes.DeleteNoteUseCase
import com.app.triflow.domain.usecase.notes.GetBacklinksUseCase
import com.app.triflow.domain.usecase.notes.GetLinkedNotesUseCase
import com.app.triflow.domain.usecase.notes.GetNoteUseCase
import com.app.triflow.domain.usecase.notes.PromoteNoteToTaskUseCase
import com.app.triflow.domain.usecase.notes.UpdateNoteUseCase
import com.app.triflow.presentation.common.userMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoteEditorUiState(
    val loading: Boolean = false,
    val noteId: String? = null,
    val title: String = "",
    val content: TextFieldValue = TextFieldValue(""),
    val paraCategory: ParaCategory? = null,
    val tagsInput: String = "",
    val backlinks: List<Note> = emptyList(),
    val linkedNotes: List<Note> = emptyList(),
    val saving: Boolean = false,
    val saved: Boolean = false,
    val deleted: Boolean = false,
    val promotedTaskId: String? = null,
    val error: String? = null,
)

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val getNote: GetNoteUseCase,
    private val createNote: CreateNoteUseCase,
    private val updateNote: UpdateNoteUseCase,
    private val deleteNote: DeleteNoteUseCase,
    private val getBacklinks: GetBacklinksUseCase,
    private val getLinkedNotes: GetLinkedNotesUseCase,
    private val promoteUseCase: PromoteNoteToTaskUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(NoteEditorUiState())
    val state: StateFlow<NoteEditorUiState> = _state.asStateFlow()

    fun load(noteId: String?) {
        if (noteId == null) {
            _state.value = NoteEditorUiState(noteId = null)
            return
        }
        if (_state.value.noteId == noteId && !_state.value.loading) return
        _state.update { it.copy(loading = true, noteId = noteId, error = null) }
        viewModelScope.launch {
            when (val out = getNote(noteId)) {
                is Outcome.Success -> {
                    val n = out.value
                    _state.update {
                        it.copy(
                            loading = false,
                            noteId = n.id,
                            title = n.title,
                            content = TextFieldValue(n.contentMd),
                            paraCategory = n.paraCategory,
                            tagsInput = n.tags.joinToString(", "),
                        )
                    }
                    // Carica backlinks / linked in parallelo (best effort)
                    launch { reloadBacklinks(n.id) }
                    launch { reloadLinkedNotes(n.id) }
                }
                is Outcome.Failure -> _state.update {
                    it.copy(loading = false, error = out.error.userMessage())
                }
            }
        }
    }

    fun onTitle(value: String) = _state.update { it.copy(title = value, error = null) }
    fun onContent(value: TextFieldValue) = _state.update { it.copy(content = value, error = null) }
    fun onPara(value: ParaCategory?) = _state.update { it.copy(paraCategory = value) }
    fun onTags(value: String) = _state.update { it.copy(tagsInput = value) }

    fun save() {
        val s = _state.value
        if (s.saving || s.title.isBlank()) {
            if (s.title.isBlank()) _state.update { it.copy(error = "Il titolo è obbligatorio.") }
            return
        }
        _state.update { it.copy(saving = true, error = null) }
        val tags = s.tagsInput.split(',', ';')
            .map { it.trim().removePrefix("#") }
            .filter { it.isNotBlank() }
        viewModelScope.launch {
            val out = if (s.noteId == null) {
                createNote(NoteDraft(title = s.title.trim(), contentMd = s.content.text, paraCategory = s.paraCategory, tags = tags))
            } else {
                updateNote(
                    s.noteId,
                    NotePatch(
                        title = s.title.trim(),
                        contentMd = s.content.text,
                        paraCategory = s.paraCategory,
                        tags = tags,
                    ),
                )
            }
            _state.update {
                when (out) {
                    is Outcome.Success -> it.copy(saving = false, saved = true, noteId = out.value.id)
                    is Outcome.Failure -> it.copy(saving = false, error = out.error.userMessage())
                }
            }
        }
    }

    fun delete() {
        val id = _state.value.noteId ?: return
        viewModelScope.launch {
            val out = deleteNote(id)
            if (out is Outcome.Success) _state.update { it.copy(deleted = true) }
            else if (out is Outcome.Failure) _state.update { it.copy(error = out.error.userMessage()) }
        }
    }

    fun promoteToTask(title: String, status: TaskStatus, priority: Int?) {
        val id = _state.value.noteId ?: return
        viewModelScope.launch {
            val out = promoteUseCase(
                id,
                PromoteNoteParams(title = title.trim().ifEmpty { null }, status = status, priority = priority),
            )
            when (out) {
                is Outcome.Success -> _state.update { it.copy(promotedTaskId = out.value.id) }
                is Outcome.Failure -> _state.update { it.copy(error = out.error.userMessage()) }
            }
        }
    }

    fun consumeSaved() = _state.update { it.copy(saved = false) }
    fun consumePromoted() = _state.update { it.copy(promotedTaskId = null) }
    fun clearError() = _state.update { it.copy(error = null) }

    private suspend fun reloadBacklinks(id: String) {
        when (val out = getBacklinks(id)) {
            is Outcome.Success -> _state.update { it.copy(backlinks = out.value) }
            is Outcome.Failure -> Unit
        }
    }

    private suspend fun reloadLinkedNotes(id: String) {
        when (val out = getLinkedNotes(id)) {
            is Outcome.Success -> _state.update { it.copy(linkedNotes = out.value) }
            is Outcome.Failure -> Unit
        }
    }
}
