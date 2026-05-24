package com.app.triflow.domain.repository

import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.AuthSession
import com.app.triflow.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val session: Flow<AuthSession?>

    val isLoggedIn: Flow<Boolean>

    suspend fun register(email: String, password: String, displayName: String): Outcome<User>

    suspend fun login(email: String, password: String): Outcome<Pair<User, AuthSession>>

    suspend fun logout(): Outcome<Unit>

    suspend fun refresh(): Outcome<AuthSession>
}
