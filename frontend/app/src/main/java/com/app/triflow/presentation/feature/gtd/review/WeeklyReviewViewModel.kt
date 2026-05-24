package com.app.triflow.presentation.feature.gtd.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.WeeklyReview
import com.app.triflow.domain.usecase.gtd.GetWeeklyReviewUseCase
import com.app.triflow.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeeklyReviewViewModel @Inject constructor(
    private val getReview: GetWeeklyReviewUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<WeeklyReview>>(UiState.Loading)
    val state: StateFlow<UiState<WeeklyReview>> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = when (val out = getReview()) {
                is Outcome.Success -> UiState.Success(out.value)
                is Outcome.Failure -> UiState.Error(out.error)
            }
        }
    }
}
