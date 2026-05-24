package com.app.triflow.data.repository

import com.app.triflow.core.network.ApiCallExecutor
import com.app.triflow.data.local.datastore.SettingsStore
import com.app.triflow.data.remote.api.UsersApi
import com.app.triflow.data.remote.dto.UpdateProfileRequestDto
import com.app.triflow.data.remote.mapper.toDomain
import com.app.triflow.data.remote.mapper.toDto
import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.common.onSuccess
import com.app.triflow.domain.model.User
import com.app.triflow.domain.model.UserPreferences
import com.app.triflow.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: UsersApi,
    private val settings: SettingsStore,
    private val executor: ApiCallExecutor,
) : UserRepository {

    override suspend fun getMe(): Outcome<User> =
        executor(mapper = { it.toDomain() }) { api.me() }

    override suspend fun updateProfile(displayName: String): Outcome<User> =
        executor(mapper = { it.toDomain() }) {
            api.updateMe(UpdateProfileRequestDto(displayName))
        }

    override suspend fun getPreferences(): Outcome<UserPreferences> {
        val result = executor(mapper = { it.toDomain() }) { api.preferences() }
        result.onSuccess { settings.save(it) }
        return result
    }

    override suspend fun updatePreferences(preferences: UserPreferences): Outcome<UserPreferences> {
        val result = executor(mapper = { it.toDomain() }) {
            api.updatePreferences(preferences.toDto())
        }
        result.onSuccess { settings.save(it) }
        return result
    }
}
