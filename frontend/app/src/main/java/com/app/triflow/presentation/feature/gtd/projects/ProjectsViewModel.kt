package com.app.triflow.presentation.feature.gtd.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.Project
import com.app.triflow.domain.model.ProjectStatus
import com.app.triflow.domain.repository.GtdRepository
import com.app.triflow.domain.usecase.gtd.CreateProjectUseCase
import com.app.triflow.domain.usecase.gtd.ObserveProjectsUseCase
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

data class ProjectsUiState(
    val filter: ProjectStatus? = ProjectStatus.Active,
    val refreshing: Boolean = false,
    val creating: Boolean = false,
    val error: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProjectsViewModel @Inject constructor(
    observeProjects: ObserveProjectsUseCase,
    private val createUseCase: CreateProjectUseCase,
    private val repo: GtdRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectsUiState())
    val state: StateFlow<ProjectsUiState> = _state.asStateFlow()

    val projects: StateFlow<List<Project>> = _state
        .flatMapLatest { observeProjects(it.filter) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init { refresh() }

    fun setFilter(status: ProjectStatus?) = _state.update { it.copy(filter = status) }

    fun refresh() {
        _state.update { it.copy(refreshing = true, error = null) }
        viewModelScope.launch {
            val out = repo.listProjects(_state.value.filter)
            _state.update {
                when (out) {
                    is Outcome.Success -> it.copy(refreshing = false)
                    is Outcome.Failure -> it.copy(refreshing = false, error = out.error.userMessage())
                }
            }
        }
    }

    fun create(title: String, description: String, status: ProjectStatus) {
        if (_state.value.creating || title.isBlank()) return
        _state.update { it.copy(creating = true, error = null) }
        viewModelScope.launch {
            val out = createUseCase(title.trim(), description.takeIf { it.isNotBlank() }, status)
            _state.update {
                when (out) {
                    is Outcome.Success -> it.copy(creating = false)
                    is Outcome.Failure -> it.copy(creating = false, error = out.error.userMessage())
                }
            }
        }
    }
}
