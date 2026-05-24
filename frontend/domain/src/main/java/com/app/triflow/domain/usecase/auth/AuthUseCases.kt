package com.app.triflow.domain.usecase.auth

import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.AuthSession
import com.app.triflow.domain.model.User
import com.app.triflow.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String, displayName: String): Outcome<User> =
        repository.register(email.trim(), password, displayName.trim())
}

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Outcome<Pair<User, AuthSession>> =
        repository.login(email.trim(), password)
}

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): Outcome<Unit> = repository.logout()
}

class ObserveAuthStateUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.isLoggedIn
}
