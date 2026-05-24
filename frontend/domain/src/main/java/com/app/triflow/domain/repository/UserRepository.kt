package com.app.triflow.domain.repository

import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.User
import com.app.triflow.domain.model.UserPreferences

interface UserRepository {

    suspend fun getMe(): Outcome<User>

    suspend fun updateProfile(displayName: String): Outcome<User>

    suspend fun getPreferences(): Outcome<UserPreferences>

    suspend fun updatePreferences(preferences: UserPreferences): Outcome<UserPreferences>
}
