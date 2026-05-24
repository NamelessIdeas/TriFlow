package com.app.triflow.presentation.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.triflow.domain.usecase.auth.ObserveAuthStateUseCase
import com.app.triflow.presentation.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val observeAuthState: ObserveAuthStateUseCase,
) : ViewModel() {

    private val _target = MutableStateFlow<Destination?>(null)
    val target: StateFlow<Destination?> = _target.asStateFlow()

    init {
        viewModelScope.launch {
            val loggedIn = observeAuthState().first()
            _target.value = if (loggedIn) Destination.HomeGraph else Destination.AuthGraph
        }
    }
}
