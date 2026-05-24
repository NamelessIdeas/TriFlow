package com.app.triflow.domain.usecase.user

import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.User
import com.app.triflow.domain.model.UserPreferences
import com.app.triflow.domain.repository.UserRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(): Outcome<User> = repository.getMe()
}

class UpdateProfileUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(displayName: String): Outcome<User> =
        repository.updateProfile(displayName.trim())
}

class GetPreferencesUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(): Outcome<UserPreferences> = repository.getPreferences()
}

class UpdatePreferencesUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(preferences: UserPreferences): Outcome<UserPreferences> =
        repository.updatePreferences(preferences)
}
