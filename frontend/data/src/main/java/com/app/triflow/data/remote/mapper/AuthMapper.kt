package com.app.triflow.data.remote.mapper

import com.app.triflow.data.remote.dto.TokenPairDto
import com.app.triflow.data.remote.dto.UserDto
import com.app.triflow.data.remote.dto.UserPreferencesDto
import com.app.triflow.domain.model.AuthSession
import com.app.triflow.domain.model.User
import com.app.triflow.domain.model.UserPreferences

fun UserDto.toDomain(): User = User(
    id = id,
    email = email,
    displayName = displayName,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun TokenPairDto.toDomain(): AuthSession = AuthSession(
    accessToken = accessToken,
    refreshToken = refreshToken,
    accessExpiresAt = accessExpiresAt,
)

fun UserPreferencesDto.toDomain(): UserPreferences = UserPreferences(
    pomodoroDurationMin = pomodoroDurationMin,
    shortBreakMin = shortBreakMin,
    longBreakMin = longBreakMin,
    pomodorosUntilLongBreak = pomodorosUntilLongBreak,
    timezone = timezone,
)

fun UserPreferences.toDto(): UserPreferencesDto = UserPreferencesDto(
    pomodoroDurationMin = pomodoroDurationMin,
    shortBreakMin = shortBreakMin,
    longBreakMin = longBreakMin,
    pomodorosUntilLongBreak = pomodorosUntilLongBreak,
    timezone = timezone,
)
