package com.app.triflow.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val displayName: String,
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class RefreshRequestDto(
    val refreshToken: String,
)

@Serializable
data class TokenPairDto(
    val accessToken: String,
    val refreshToken: String,
    val accessExpiresAt: Instant,
)

@Serializable
data class LoginDataDto(
    val user: UserDto,
    val tokens: TokenPairDto,
)
