package com.app.triflow.domain.model

import kotlinx.datetime.Instant

data class Dashboard(
    val todayTasks: List<Task>,
    val activeTimer: ActiveTimer?,
    val recentNotes: List<Note>,
    val pomodorosToday: Int,
    val focusSecondsWeek: Int,
    val generatedAt: Instant,
)
