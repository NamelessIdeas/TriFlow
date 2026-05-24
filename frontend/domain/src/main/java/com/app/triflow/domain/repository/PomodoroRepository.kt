package com.app.triflow.domain.repository

import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.common.Page
import com.app.triflow.domain.common.Paged
import com.app.triflow.domain.model.ActiveTimer
import com.app.triflow.domain.model.PomodoroSession
import com.app.triflow.domain.model.PomodoroStats
import com.app.triflow.domain.model.StartPomodoroParams
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface PomodoroRepository {

    val activeTimer: Flow<ActiveTimer?>

    suspend fun start(params: StartPomodoroParams): Outcome<ActiveTimer>

    suspend fun pause(): Outcome<ActiveTimer>

    suspend fun resume(): Outcome<ActiveTimer>

    suspend fun current(): Outcome<ActiveTimer?>

    suspend fun complete(): Outcome<PomodoroSession>

    suspend fun abort(): Outcome<PomodoroSession>

    suspend fun listSessions(
        from: Instant? = null,
        to: Instant? = null,
        page: Page = Page(),
    ): Outcome<Paged<PomodoroSession>>

    suspend fun stats(from: Instant? = null, to: Instant? = null): Outcome<PomodoroStats>
}
