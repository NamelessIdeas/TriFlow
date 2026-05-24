package com.app.triflow.presentation.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.Dashboard
import com.app.triflow.domain.usecase.auth.LogoutUseCase
import com.app.triflow.domain.usecase.dashboard.GetDashboardUseCase
import com.app.triflow.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboard: GetDashboardUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<Dashboard>>(UiState.Loading)
    val state: StateFlow<UiState<Dashboard>> = _state.asStateFlow()

    private val _loggedOut = MutableStateFlow(false)
    val loggedOut: StateFlow<Boolean> = _loggedOut.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = when (val out = getDashboard()) {
                is Outcome.Success -> UiState.Success(out.value)
                is Outcome.Failure -> UiState.Error(out.error)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _loggedOut.value = true
        }
    }
}
