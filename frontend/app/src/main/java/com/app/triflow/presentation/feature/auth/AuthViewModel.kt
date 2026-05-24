package com.app.triflow.presentation.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.usecase.auth.LoginUseCase
import com.app.triflow.domain.usecase.auth.RegisterUseCase
import com.app.triflow.presentation.common.userMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthFormState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val submitting: Boolean = false,
    val errorMessage: String? = null,
    val succeeded: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthFormState())
    val state: StateFlow<AuthFormState> = _state.asStateFlow()

    fun onEmail(value: String) = _state.update { it.copy(email = value, errorMessage = null) }
    fun onPassword(value: String) = _state.update { it.copy(password = value, errorMessage = null) }
    fun onDisplayName(value: String) = _state.update { it.copy(displayName = value, errorMessage = null) }

    fun login() {
        val s = _state.value
        if (s.submitting) return
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.update { it.copy(errorMessage = "Compila email e password.") }
            return
        }
        _state.update { it.copy(submitting = true, errorMessage = null) }
        viewModelScope.launch {
            when (val outcome = loginUseCase(s.email, s.password)) {
                is Outcome.Success -> _state.update { it.copy(submitting = false, succeeded = true) }
                is Outcome.Failure -> _state.update {
                    it.copy(submitting = false, errorMessage = outcome.error.userMessage())
                }
            }
        }
    }

    fun register() {
        val s = _state.value
        if (s.submitting) return
        if (s.email.isBlank() || s.password.length < 8 || s.displayName.isBlank()) {
            _state.update {
                it.copy(errorMessage = "Compila tutti i campi (password almeno 8 caratteri).")
            }
            return
        }
        _state.update { it.copy(submitting = true, errorMessage = null) }
        viewModelScope.launch {
            when (val out = registerUseCase(s.email, s.password, s.displayName)) {
                is Outcome.Success -> {
                    // Dopo register effettuiamo subito login
                    when (val login = loginUseCase(s.email, s.password)) {
                        is Outcome.Success -> _state.update { it.copy(submitting = false, succeeded = true) }
                        is Outcome.Failure -> _state.update {
                            it.copy(submitting = false, errorMessage = login.error.userMessage())
                        }
                    }
                }
                is Outcome.Failure -> _state.update {
                    it.copy(submitting = false, errorMessage = out.error.userMessage())
                }
            }
        }
    }

    fun consumeSuccess() = _state.update { it.copy(succeeded = false) }
}
