package com.app.triflow.data.remote.mapper

import com.app.triflow.data.remote.dto.ActiveTimerDto
import com.app.triflow.data.remote.dto.PomodoroSessionDto
import com.app.triflow.data.remote.dto.PomodoroStartRequestDto
import com.app.triflow.data.remote.dto.PomodoroStatsDto
import com.app.triflow.domain.common.Uuid
import com.app.triflow.domain.model.ActiveTimer
import com.app.triflow.domain.model.PomodoroSession
import com.app.triflow.domain.model.PomodoroStats
import com.app.triflow.domain.model.StartPomodoroParams
import kotlinx.datetime.LocalDate

fun ActiveTimerDto.toDomain(): ActiveTimer = ActiveTimer(
    taskId = taskId,
    kind = kind.toPomodoroKind(),
    plannedDurationSec = plannedDurationSec,
    cycleIndex = cycleIndex,
    startedAt = startedAt,
    pausedAt = pausedAt,
    elapsedBeforePauseSec = elapsedBeforePauseSec,
)

fun PomodoroSessionDto.toDomain(): PomodoroSession = PomodoroSession(
    id = id,
    taskId = taskId,
    kind = kind.toPomodoroKind(),
    plannedDurationSec = plannedDurationSec,
    actualDurationSec = actualDurationSec,
    cycleIndex = cycleIndex,
    startedAt = startedAt,
    endedAt = endedAt,
    status = status.toPomodoroSessionStatus(),
    createdAt = createdAt,
)

fun PomodoroStatsDto.toDomain(): PomodoroStats = PomodoroStats(
    pomodorosCompleted = pomodorosCompleted,
    focusSeconds = focusSeconds,
    byDay = byDay.mapKeys { (k, _) -> LocalDate.parse(k) },
    byTask = byTask.mapKeys { (k, _) -> k as Uuid },
)

fun StartPomodoroParams.toRequest(): PomodoroStartRequestDto = PomodoroStartRequestDto(
    taskId = taskId,
    kind = kind.toApi(),
    cycleIndex = cycleIndex,
    durationSec = durationSec,
)
