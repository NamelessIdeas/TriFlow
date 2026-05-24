package com.app.triflow.presentation.common

import com.app.triflow.domain.common.DomainError

/**
 * Stato UI per pagine "single-shot" (form, dettagli):
 *  - Idle: niente da mostrare
 *  - Loading: spinner
 *  - Success(value): contenuto pronto
 *  - Error(message): errore con messaggio user-friendly
 */
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val value: T) : UiState<T>
    data class Error(val error: DomainError) : UiState<Nothing>
}
