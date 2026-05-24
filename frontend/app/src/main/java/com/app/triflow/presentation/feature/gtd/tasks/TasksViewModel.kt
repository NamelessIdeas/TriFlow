package com.app.triflow.presentation.feature.gtd.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.GtdContext
import com.app.triflow.domain.model.Project
import com.app.triflow.domain.model.Task
import com.app.triflow.domain.model.TaskDraft
import com.app.triflow.domain.model.TaskFilter
import com.app.triflow.domain.model.TaskStatus
import com.app.triflow.domain.repository.GtdRepository
import com.app.triflow.domain.usecase.gtd.CreateTaskUseCase
import com.app.triflow.domain.usecase.gtd.ObserveContextsUseCase
import com.app.triflow.domain.usecase.gtd.ObserveProjectsUseCase
import com.app.triflow.domain.usecase.gtd.ObserveTasksUseCase
import com.app.triflow.domain.usecase.gtd.ToggleTaskDoneUseCase
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

data class TasksUiState(
    val filter: TaskFilter = TaskFilter(status = TaskStatus.NextAction),
    val refreshing: Boolean = false,
    val creating: Boolean = false,
    val error: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TasksViewModel @Inject constructor(
    observeTasks: ObserveTasksUseCase,
    observeContexts: ObserveContextsUseCase,
    observeProjects: ObserveProjectsUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val toggleDoneUseCase: ToggleTaskDoneUseCase,
    private val repo: GtdRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TasksUiState())
    val state: StateFlow<TasksUiState> = _state.asStateFlow()

    val tasks: StateFlow<List<Task>> = _state
        .flatMapLatest { observeTasks(it.filter) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val contexts: StateFlow<List<GtdContext>> = observeContexts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val projects: StateFlow<List<Project>> = observeProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init { refresh() }

    fun setStatus(status: TaskStatus?) =
        _state.update { it.copy(filter = it.filter.copy(status = status)) }

    fun setContext(contextId: String?) =
        _state.update { it.copy(filter = it.filter.copy(contextId = contextId)) }

    fun refresh() {
        _state.update { it.copy(refreshing = true, error = null) }
        viewModelScope.launch {
            // Sync contexts e projects in parallelo (per le chip e per il sheet)
            repo.listContexts()
            repo.listProjects()
            val out = repo.listTasks(_state.value.filter)
            _state.update {
                when (out) {
                    is Outcome.Success -> it.copy(refreshing = false)
                    is Outcome.Failure -> it.copy(refreshing = false, error = out.error.userMessage())
                }
            }
        }
    }

    fun createTask(draft: TaskDraft) {
        if (_state.value.creating) return
        _state.update { it.copy(creating = true, error = null) }
        viewModelScope.launch {
            val out = createTaskUseCase(draft)
            _state.update {
                when (out) {
                    is Outcome.Success -> it.copy(creating = false)
                    is Outcome.Failure -> it.copy(creating = false, error = out.error.userMessage())
                }
            }
        }
    }

    fun toggleDone(task: Task) {
        viewModelScope.launch {
            val out = toggleDoneUseCase(task)
            if (out is Outcome.Failure) {
                _state.update { it.copy(error = out.error.userMessage()) }
            }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
