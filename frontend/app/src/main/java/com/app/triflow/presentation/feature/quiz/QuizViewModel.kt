package com.app.triflow.presentation.feature.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.Goal
import com.app.triflow.domain.model.MainProblem
import com.app.triflow.domain.model.QuizAnswers
import com.app.triflow.domain.model.QuizResult
import com.app.triflow.domain.model.SetupTolerance
import com.app.triflow.domain.model.WorkStyle
import com.app.triflow.domain.usecase.quiz.SubmitQuizUseCase
import com.app.triflow.presentation.common.userMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class QuizStep { MainProblem, WorkStyle, SetupTolerance, Goal }

data class QuizUiState(
    val step: QuizStep = QuizStep.MainProblem,
    val mainProblem: MainProblem? = null,
    val workStyle: WorkStyle? = null,
    val setupTolerance: SetupTolerance? = null,
    val goal: Goal? = null,
    val submitting: Boolean = false,
    val result: QuizResult? = null,
    val error: String? = null,
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val submitUseCase: SubmitQuizUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizUiState())
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    fun choose(option: Any) {
        when (option) {
            is MainProblem -> _state.update { it.copy(mainProblem = option, step = QuizStep.WorkStyle, error = null) }
            is WorkStyle -> _state.update { it.copy(workStyle = option, step = QuizStep.SetupTolerance, error = null) }
            is SetupTolerance -> _state.update { it.copy(setupTolerance = option, step = QuizStep.Goal, error = null) }
            is Goal -> {
                _state.update { it.copy(goal = option, error = null) }
                submit()
            }
        }
    }

    fun back() {
        val s = _state.value
        val previous = when (s.step) {
            QuizStep.MainProblem -> null
            QuizStep.WorkStyle -> QuizStep.MainProblem
            QuizStep.SetupTolerance -> QuizStep.WorkStyle
            QuizStep.Goal -> QuizStep.SetupTolerance
        }
        if (previous != null) _state.update { it.copy(step = previous, error = null) }
    }

    fun restart() {
        _state.value = QuizUiState()
    }

    private fun submit() {
        val s = _state.value
        val mp = s.mainProblem
        val ws = s.workStyle
        val st = s.setupTolerance
        val gl = s.goal
        if (mp == null || ws == null || st == null || gl == null) return
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            val out = submitUseCase(QuizAnswers(mp, ws, st, gl))
            _state.update {
                when (out) {
                    is Outcome.Success -> it.copy(submitting = false, result = out.value)
                    is Outcome.Failure -> it.copy(submitting = false, error = out.error.userMessage())
                }
            }
        }
    }
}
