package com.app.triflow.domain.common

sealed class DomainError(open val message: String? = null) {
    data object Network : DomainError("No connection")
    data object Unauthorized : DomainError("Not authenticated")
    data class InvalidCredentials(override val message: String? = null) : DomainError(message)
    data class InvalidInput(override val message: String? = null, val details: Map<String, String> = emptyMap()) : DomainError(message)
    data object NotFound : DomainError("Resource not found")
    data class AlreadyExists(override val message: String? = null) : DomainError(message)
    data class Conflict(override val message: String? = null) : DomainError(message)
    data object TimerActive : DomainError("A timer is already running")
    data object TimerNotFound : DomainError("No active timer")
    data class RateLimited(override val message: String? = null) : DomainError(message)
    data class Server(override val message: String? = null) : DomainError(message)
    data class Unknown(override val message: String? = null, val cause: Throwable? = null) : DomainError(message)
}
