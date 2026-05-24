package com.app.triflow.domain.model

import com.app.triflow.domain.common.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

enum class PomodoroKind { Focus, ShortBreak, LongBreak }
enum class PomodoroSessionStatus { Completed, Aborted }

data class ActiveTimer(
    val taskId: Uuid?,
    val kind: PomodoroKind,
    val plannedDurationSec: Int,
    val cycleIndex: Int,
    val startedAt: Instant,
    val pausedAt: Instant?,
    val elapsedBeforePauseSec: Int,
) {
    val isPaused: Boolean get() = pausedAt != null

    fun elapsedSec(now: Instant = Clock.System.now()): Int {
        return if (isPaused) {
            elapsedBeforePauseSec
        } else {
            elapsedBeforePauseSec + (now.epochSeconds - startedAt.epochSeconds).toInt()
        }
    }

    fun remainingSec(now: Instant = Clock.System.now()): Int =
        (plannedDurationSec - elapsedSec(now)).coerceAtLeast(0)

    fun progress(now: Instant = Clock.System.now()): Float =
        if (plannedDurationSec <= 0) 0f
        else (elapsedSec(now).toFloat() / plannedDurationSec).coerceIn(0f, 1f)
}

data class PomodoroSession(
    val id: Uuid,
    val taskId: Uuid?,
    val kind: PomodoroKind,
    val plannedDurationSec: Int,
    val actualDurationSec: Int,
    val cycleIndex: Int,
    val startedAt: Instant,
    val endedAt: Instant,
    val status: PomodoroSessionStatus,
    val createdAt: Instant,
)

data class PomodoroStats(
    val pomodorosCompleted: Int,
    val focusSeconds: Int,
    val byDay: Map<LocalDate, Int>,
    val byTask: Map<Uuid, Int>,
)

data class StartPomodoroParams(
    val taskId: Uuid? = null,
    val kind: PomodoroKind = PomodoroKind.Focus,
    val cycleIndex: Int = 1,
    val durationSec: Int? = null,
)
