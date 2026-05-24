package com.app.triflow.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PomodoroStartRequestDto(
    val taskId: String? = null,
    val kind: String? = null,
    val cycleIndex: Int? = null,
    val durationSec: Int? = null,
)

@Serializable
data class ActiveTimerDto(
    val userId: String? = null,
    val taskId: String? = null,
    val kind: String,
    val plannedDurationSec: Int,
    val cycleIndex: Int,
    val startedAt: Instant,
    val pausedAt: Instant? = null,
    val elapsedBeforePauseSec: Int = 0,
)

@Serializable
data class PomodoroSessionDto(
    val id: String,
    val userId: String? = null,
    val taskId: String? = null,
    val kind: String,
    val plannedDurationSec: Int,
    val actualDurationSec: Int,
    val cycleIndex: Int,
    val startedAt: Instant,
    val endedAt: Instant,
    val status: String,
    val createdAt: Instant,
)

@Serializable
data class PomodoroStatsDto(
    val pomodorosCompleted: Int = 0,
    val focusSeconds: Int = 0,
    val byDay: Map<String, Int> = emptyMap(),
    val byTask: Map<String, Int> = emptyMap(),
)
