package com.app.triflow.presentation.feature.gtd.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.TaskWithRelations
import com.app.triflow.domain.usecase.gtd.DeleteTaskUseCase
import com.app.triflow.domain.usecase.gtd.GetTaskWithRelationsUseCase
import com.app.triflow.domain.usecase.gtd.ToggleTaskDoneUseCase
import com.app.triflow.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val getRelations: GetTaskWithRelationsUseCase,
    private val toggleDone: ToggleTaskDoneUseCase,
    private val deleteTask: DeleteTaskUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<TaskWithRelations>>(UiState.Loading)
    val state: StateFlow<UiState<TaskWithRelations>> = _state.asStateFlow()

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    fun load(taskId: String) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = when (val out = getRelations(taskId)) {
                is Outcome.Success -> UiState.Success(out.value)
                is Outcome.Failure -> UiState.Error(out.error)
            }
        }
    }

    fun onToggleDone() {
        val current = (_state.value as? UiState.Success)?.value?.task ?: return
        viewModelScope.launch {
            val out = toggleDone(current)
            if (out is Outcome.Success) load(current.id)
        }
    }

    fun onDelete() {
        val current = (_state.value as? UiState.Success)?.value?.task ?: return
        viewModelScope.launch {
            val out = deleteTask(current.id)
            if (out is Outcome.Success) _deleted.value = true
        }
    }
}
