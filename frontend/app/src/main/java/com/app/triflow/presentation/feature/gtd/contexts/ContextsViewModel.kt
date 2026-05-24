package com.app.triflow.presentation.feature.gtd.contexts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.GtdContext
import com.app.triflow.domain.repository.GtdRepository
import com.app.triflow.domain.usecase.gtd.CreateContextUseCase
import com.app.triflow.domain.usecase.gtd.ObserveContextsUseCase
import com.app.triflow.presentation.common.userMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContextsUiState(
    val newName: String = "",
    val creating: Boolean = false,
    val refreshing: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ContextsViewModel @Inject constructor(
    observeContexts: ObserveContextsUseCase,
    private val createUseCase: CreateContextUseCase,
    private val repo: GtdRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ContextsUiState())
    val state: StateFlow<ContextsUiState> = _state.asStateFlow()

    val contexts: StateFlow<List<GtdContext>> = observeContexts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init { refresh() }

    fun onName(value: String) = _state.update { it.copy(newName = value, error = null) }

    fun refresh() {
        _state.update { it.copy(refreshing = true, error = null) }
        viewModelScope.launch {
            val out = repo.listContexts()
            _state.update {
                when (out) {
                    is Outcome.Success -> it.copy(refreshing = false)
                    is Outcome.Failure -> it.copy(refreshing = false, error = out.error.userMessage())
                }
            }
        }
    }

    fun create() {
        val name = _state.value.newName.trim()
        if (name.isBlank() || _state.value.creating) return
        _state.update { it.copy(creating = true, error = null) }
        viewModelScope.launch {
            val out = createUseCase(name)
            _state.update {
                when (out) {
                    is Outcome.Success -> it.copy(creating = false, newName = "")
                    is Outcome.Failure -> it.copy(creating = false, error = out.error.userMessage())
                }
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            val out = repo.deleteContext(id)
            if (out is Outcome.Failure) {
                _state.update { it.copy(error = out.error.userMessage()) }
            }
        }
    }
}
