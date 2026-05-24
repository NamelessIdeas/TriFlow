package com.app.triflow.presentation.feature.pomodoro.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.ActiveTimer
import com.app.triflow.domain.model.PomodoroKind
import com.app.triflow.domain.model.StartPomodoroParams
import com.app.triflow.domain.model.Task
import com.app.triflow.domain.usecase.gtd.ObserveTasksUseCase
import com.app.triflow.domain.usecase.pomodoro.AbortPomodoroUseCase
import com.app.triflow.domain.usecase.pomodoro.CompletePomodoroUseCase
import com.app.triflow.domain.usecase.pomodoro.ObserveActiveTimerUseCase
import com.app.triflow.domain.usecase.pomodoro.PausePomodoroUseCase
import com.app.triflow.domain.usecase.pomodoro.RestoreActiveTimerUseCase
import com.app.triflow.domain.usecase.pomodoro.ResumePomodoroUseCase
import com.app.triflow.domain.usecase.pomodoro.StartPomodoroUseCase
import com.app.triflow.presentation.common.userMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject

data class TimerUiState(
    val active: ActiveTimer? = null,
    val taskTitle: String? = null,
    val remainingSec: Int = 0,
    val progress: Float = 0f,
    val kind: PomodoroKind = PomodoroKind.Focus,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val cycleIndex: Int = 1,
    val busy: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class PomodoroTimerViewModel @Inject constructor(
    observeActiveTimer: ObserveActiveTimerUseCase,
    observeTasks: ObserveTasksUseCase,
    private val startUseCase: StartPomodoroUseCase,
    private val pauseUseCase: PausePomodoroUseCase,
    private val resumeUseCase: ResumePomodoroUseCase,
    private val completeUseCase: CompletePomodoroUseCase,
    private val abortUseCase: AbortPomodoroUseCase,
    private val restoreUseCase: RestoreActiveTimerUseCase,
) : ViewModel() {

    private val busy = MutableStateFlow(false)
    private val transient = MutableStateFlow<String?>(null)

    private val ticker: kotlinx.coroutines.flow.Flow<Instant> = flow {
        while (true) {
            emit(Clock.System.now())
            delay(1_000)
        }
    }

    private val tasks: StateFlow<List<Task>> = observeTasks(com.app.triflow.domain.model.TaskFilter())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val state: StateFlow<TimerUiState> =
        combine(
            observeActiveTimer(),
            ticker,
            tasks,
            busy,
            transient,
        ) { active, now, tasksList, isBusy, err ->
            if (active == null) {
                TimerUiState(busy = isBusy, error = err)
            } else {
                TimerUiState(
                    active = active,
                    taskTitle = active.taskId?.let { id -> tasksList.firstOrNull { it.id == id }?.title },
                    remainingSec = active.remainingSec(now),
                    progress = active.progress(now),
                    kind = active.kind,
                    isRunning = !active.isPaused,
                    isPaused = active.isPaused,
                    cycleIndex = active.cycleIndex,
                    busy = isBusy,
                    error = err,
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimerUiState())

    init {
        viewModelScope.launch {
            restoreUseCase() // sincronizza /pomodoros/current → activeTimer flow
        }
    }

    fun start(taskId: String?) = runAction("start") {
        startUseCase(StartPomodoroParams(taskId = taskId, kind = PomodoroKind.Focus))
    }

    fun startBreak(kind: PomodoroKind) = runAction("break") {
        startUseCase(StartPomodoroParams(kind = kind))
    }

    fun pause() = runAction("pause") { pauseUseCase() }
    fun resume() = runAction("resume") { resumeUseCase() }
    fun complete() = runAction("complete") { completeUseCase() }
    fun abort() = runAction("abort") { abortUseCase() }

    fun clearError() = transient.update { null }

    private inline fun runAction(@Suppress("UNUSED_PARAMETER") tag: String, crossinline block: suspend () -> Outcome<*>) {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch {
            val out = block()
            busy.value = false
            if (out is Outcome.Failure) transient.value = out.error.userMessage()
        }
    }
}
