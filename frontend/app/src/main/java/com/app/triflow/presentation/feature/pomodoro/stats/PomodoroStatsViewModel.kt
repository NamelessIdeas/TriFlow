package com.app.triflow.presentation.feature.pomodoro.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.PomodoroStats
import com.app.triflow.domain.model.Task
import com.app.triflow.domain.model.TaskFilter
import com.app.triflow.domain.usecase.gtd.ObserveTasksUseCase
import com.app.triflow.domain.usecase.pomodoro.GetPomodoroStatsUseCase
import com.app.triflow.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

@HiltViewModel
class PomodoroStatsViewModel @Inject constructor(
    private val getStats: GetPomodoroStatsUseCase,
    observeTasks: ObserveTasksUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<PomodoroStats>>(UiState.Loading)
    val state: StateFlow<UiState<PomodoroStats>> = _state.asStateFlow()

    val tasks: StateFlow<List<Task>> = observeTasks(TaskFilter())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init { refresh() }

    fun refresh() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            val now = Clock.System.now()
            val from = now - 7.days
            _state.value = when (val out = getStats(from = from, to = now)) {
                is Outcome.Success -> UiState.Success(out.value)
                is Outcome.Failure -> UiState.Error(out.error)
            }
        }
    }

    fun last7Days(): List<LocalDate> {
        val zone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(zone).date
        return (6 downTo 0).map { today.minus(it.toLong().days) }
    }
}

private operator fun LocalDate.minus(duration: kotlin.time.Duration): LocalDate {
    val daysToSub = duration.inWholeDays.toInt()
    return kotlinx.datetime.LocalDate.fromEpochDays(this.toEpochDays() - daysToSub)
}
