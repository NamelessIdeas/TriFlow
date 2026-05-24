package com.app.triflow.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class DashboardDto(
    val todayTasks: List<TaskDto> = emptyList(),
    val activeTimer: ActiveTimerDto? = null,
    val recentNotes: List<NoteDto> = emptyList(),
    val pomodorosToday: Int = 0,
    val focusSecondsWeek: Int = 0,
    val generatedAt: Instant,
)
