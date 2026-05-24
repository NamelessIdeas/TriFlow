package com.app.triflow.presentation.feature.gtd.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.InboxItem
import com.app.triflow.domain.model.TaskDraft
import com.app.triflow.domain.model.TaskStatus
import com.app.triflow.domain.repository.GtdRepository
import com.app.triflow.domain.repository.ProcessInboxAction
import com.app.triflow.domain.usecase.gtd.CaptureInboxUseCase
import com.app.triflow.domain.usecase.gtd.ObserveInboxUseCase
import com.app.triflow.domain.usecase.gtd.ProcessInboxItemUseCase
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

data class InboxUiState(
    val captureText: String = "",
    val capturing: Boolean = false,
    val processing: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class InboxViewModel @Inject constructor(
    observeInbox: ObserveInboxUseCase,
    private val captureUseCase: CaptureInboxUseCase,
    private val processUseCase: ProcessInboxItemUseCase,
    private val repo: GtdRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(InboxUiState())
    val state: StateFlow<InboxUiState> = _state.asStateFlow()

    val items: StateFlow<List<InboxItem>> = observeInbox()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init { refresh() }

    fun onCaptureText(value: String) = _state.update { it.copy(captureText = value, error = null) }

    fun refresh() {
        viewModelScope.launch {
            when (val out = repo.listInbox()) {
                is Outcome.Failure -> _state.update { it.copy(error = out.error.userMessage()) }
                is Outcome.Success -> Unit
            }
        }
    }

    fun capture() {
        val text = _state.value.captureText.trim()
        if (text.isEmpty() || _state.value.capturing) return
        _state.update { it.copy(capturing = true, error = null) }
        viewModelScope.launch {
            val result = captureUseCase(text)
            _state.update {
                when (result) {
                    is Outcome.Success -> it.copy(capturing = false, captureText = "")
                    is Outcome.Failure -> it.copy(capturing = false, error = result.error.userMessage())
                }
            }
        }
    }

    fun processAsTask(id: String, title: String, status: TaskStatus) {
        if (_state.value.processing) return
        _state.update { it.copy(processing = true, error = null) }
        viewModelScope.launch {
            val out = processUseCase(
                id = id,
                action = ProcessInboxAction.Task,
                draft = TaskDraft(title = title, status = status),
            )
            _state.update {
                when (out) {
                    is Outcome.Success -> it.copy(processing = false)
                    is Outcome.Failure -> it.copy(processing = false, error = out.error.userMessage())
                }
            }
        }
    }

    fun processAsProject(id: String, title: String) {
        if (_state.value.processing) return
        _state.update { it.copy(processing = true, error = null) }
        viewModelScope.launch {
            val out = processUseCase(
                id = id,
                action = ProcessInboxAction.Project,
                projectTitle = title,
            )
            _state.update {
                when (out) {
                    is Outcome.Success -> it.copy(processing = false)
                    is Outcome.Failure -> it.copy(processing = false, error = out.error.userMessage())
                }
            }
        }
    }

    fun discard(id: String) {
        if (_state.value.processing) return
        _state.update { it.copy(processing = true, error = null) }
        viewModelScope.launch {
            val out = processUseCase(id = id, action = ProcessInboxAction.Discard)
            _state.update {
                when (out) {
                    is Outcome.Success -> it.copy(processing = false)
                    is Outcome.Failure -> it.copy(processing = false, error = out.error.userMessage())
                }
            }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
