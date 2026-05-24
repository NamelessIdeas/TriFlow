package com.app.triflow.data.remote.mapper

import com.app.triflow.data.remote.dto.DashboardDto
import com.app.triflow.domain.model.Dashboard

fun DashboardDto.toDomain(): Dashboard = Dashboard(
    todayTasks = todayTasks.map { it.toDomain() },
    activeTimer = activeTimer?.toDomain(),
    recentNotes = recentNotes.map { it.toDomain() },
    pomodorosToday = pomodorosToday,
    focusSecondsWeek = focusSecondsWeek,
    generatedAt = generatedAt,
)
