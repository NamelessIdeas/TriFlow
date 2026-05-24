package com.app.triflow.presentation.feature.secondbrain.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.Note
import com.app.triflow.domain.model.NoteFilter
import com.app.triflow.domain.model.ParaCategory
import com.app.triflow.domain.repository.NotesRepository
import com.app.triflow.domain.usecase.notes.ObserveNotesUseCase
import com.app.triflow.presentation.common.userMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotesListUiState(
    val filter: NoteFilter = NoteFilter(),
    val refreshing: Boolean = false,
    val error: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotesListViewModel @Inject constructor(
    observeNotes: ObserveNotesUseCase,
    private val repo: NotesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(NotesListUiState())
    val state: StateFlow<NotesListUiState> = _state.asStateFlow()

    val notes: StateFlow<List<Note>> = _state
        .flatMapLatest { observeNotes(it.filter) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init { refresh() }

    fun setQuery(query: String) =
        _state.update { it.copy(filter = it.filter.copy(query = query.takeIf { q -> q.isNotBlank() })) }

    fun setCategory(category: ParaCategory?) =
        _state.update { it.copy(filter = it.filter.copy(paraCategory = category)) }

    fun refresh() {
        _state.update { it.copy(refreshing = true, error = null) }
        viewModelScope.launch {
            val out = repo.listNotes(_state.value.filter)
            _state.update {
                when (out) {
                    is Outcome.Success -> it.copy(refreshing = false)
                    is Outcome.Failure -> it.copy(refreshing = false, error = out.error.userMessage())
                }
            }
        }
    }
}
