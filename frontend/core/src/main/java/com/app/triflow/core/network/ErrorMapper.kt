package com.app.triflow.core.network

import com.app.triflow.domain.common.DomainError
import java.io.IOException
import retrofit2.HttpException

/** Mappa un'eccezione/errore di rete sul corrispondente DomainError. */
fun Throwable.toDomainError(): DomainError = when (this) {
    is ApiException -> when (code) {
        ApiErrorCode.UNAUTHORIZED,
        ApiErrorCode.TOKEN_INVALID -> DomainError.Unauthorized
        ApiErrorCode.INVALID_CREDENTIALS -> DomainError.InvalidCredentials(message)
        ApiErrorCode.INVALID_INPUT -> DomainError.InvalidInput(message)
        ApiErrorCode.FORBIDDEN -> DomainError.Unauthorized
        ApiErrorCode.NOT_FOUND,
        ApiErrorCode.TIMER_NOT_FOUND -> DomainError.NotFound
        ApiErrorCode.ALREADY_EXISTS -> DomainError.AlreadyExists(message)
        ApiErrorCode.CONFLICT -> DomainError.Conflict(message)
        ApiErrorCode.TIMER_ACTIVE -> DomainError.TimerActive
        ApiErrorCode.RATE_LIMITED -> DomainError.RateLimited(message)
        ApiErrorCode.INTERNAL_ERROR -> DomainError.Server(message)
        else -> DomainError.Unknown(message, this)
    }
    is HttpException -> when (code()) {
        401 -> DomainError.Unauthorized
        403 -> DomainError.Unauthorized
        404 -> DomainError.NotFound
        409 -> DomainError.Conflict(message())
        429 -> DomainError.RateLimited(message())
        in 500..599 -> DomainError.Server(message())
        else -> DomainError.Unknown(message(), this)
    }
    is IOException -> DomainError.Network
    else -> DomainError.Unknown(message, this)
}
