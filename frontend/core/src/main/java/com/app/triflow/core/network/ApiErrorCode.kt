package com.app.triflow.core.network

/** Codici errore standardizzati del backend (vedi API_CONTRACT.md). */
object ApiErrorCode {
    const val INVALID_INPUT = "invalid_input"
    const val UNAUTHORIZED = "unauthorized"
    const val INVALID_CREDENTIALS = "invalid_credentials"
    const val TOKEN_INVALID = "token_invalid"
    const val FORBIDDEN = "forbidden"
    const val NOT_FOUND = "not_found"
    const val ALREADY_EXISTS = "already_exists"
    const val CONFLICT = "conflict"
    const val TIMER_ACTIVE = "timer_active"
    const val TIMER_NOT_FOUND = "timer_not_found"
    const val RATE_LIMITED = "rate_limited"
    const val INTERNAL_ERROR = "internal_error"
}
