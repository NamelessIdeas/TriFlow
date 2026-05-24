package com.app.triflow.presentation.feature.pomodoro.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.triflow.domain.model.Task
import com.app.triflow.domain.model.TaskFilter
import com.app.triflow.domain.model.TaskStatus
import com.app.triflow.domain.usecase.gtd.ObserveTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TaskPickerViewModel @Inject constructor(
    observeTasks: ObserveTasksUseCase,
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = observeTasks(TaskFilter(status = TaskStatus.NextAction))
        .map { it.take(50) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
