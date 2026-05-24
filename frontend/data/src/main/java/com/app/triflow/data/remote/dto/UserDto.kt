package com.app.triflow.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val displayName: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Serializable
data class UpdateProfileRequestDto(
    val displayName: String,
)

@Serializable
data class UserPreferencesDto(
    val pomodoroDurationMin: Int,
    val shortBreakMin: Int,
    val longBreakMin: Int,
    val pomodorosUntilLongBreak: Int,
    val timezone: String,
)
