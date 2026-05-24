package com.app.triflow.data.repository

import com.app.triflow.core.network.ApiCallExecutor
import com.app.triflow.core.security.EncryptedTokenStore
import com.app.triflow.data.local.datastore.SettingsStore
import com.app.triflow.data.remote.api.AuthApi
import com.app.triflow.data.remote.dto.LoginRequestDto
import com.app.triflow.data.remote.dto.RefreshRequestDto
import com.app.triflow.data.remote.dto.RegisterRequestDto
import com.app.triflow.data.remote.mapper.toDomain
import com.app.triflow.domain.common.DomainError
import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.AuthSession
import com.app.triflow.domain.model.User
import com.app.triflow.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenStore: EncryptedTokenStore,
    private val settings: SettingsStore,
    private val executor: ApiCallExecutor,
) : AuthRepository {

    override val session: Flow<AuthSession?> = tokenStore.session
    override val isLoggedIn: Flow<Boolean> = tokenStore.session.map { it != null }

    override suspend fun register(email: String, password: String, displayName: String): Outcome<User> =
        executor(mapper = { it.toDomain() }) {
            api.register(RegisterRequestDto(email, password, displayName))
        }

    override suspend fun login(email: String, password: String): Outcome<Pair<User, AuthSession>> {
        val result = executor(mapper = { it }) {
            api.login(LoginRequestDto(email, password))
        }
        return when (result) {
            is Outcome.Success -> {
                val user = result.value.user.toDomain()
                val session = result.value.tokens.toDomain()
                tokenStore.save(session)
                Outcome.Success(user to session)
            }
            is Outcome.Failure -> result
        }
    }

    override suspend fun logout(): Outcome<Unit> {
        val refresh = tokenStore.current()?.refreshToken
        return try {
            if (refresh != null) {
                runCatching { api.logout(RefreshRequestDto(refresh)) }
            }
            tokenStore.clear()
            settings.clear()
            Outcome.Success(Unit)
        } catch (t: Throwable) {
            tokenStore.clear()
            settings.clear()
            Outcome.Failure(DomainError.Unknown(t.message, t))
        }
    }

    override suspend fun refresh(): Outcome<AuthSession> {
        val refreshToken = tokenStore.current()?.refreshToken
            ?: return Outcome.Failure(DomainError.Unauthorized)
        val result = executor(mapper = { it.toDomain() }) {
            api.refresh(RefreshRequestDto(refreshToken))
        }
        if (result is Outcome.Success) {
            tokenStore.save(result.value)
        } else if (result is Outcome.Failure && result.error is DomainError.Unauthorized) {
            tokenStore.clear()
        }
        return result
    }
}
