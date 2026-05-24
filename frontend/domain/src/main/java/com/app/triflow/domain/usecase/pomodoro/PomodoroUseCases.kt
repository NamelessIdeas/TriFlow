package com.app.triflow.domain.usecase.pomodoro

import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.common.Page
import com.app.triflow.domain.common.Paged
import com.app.triflow.domain.model.ActiveTimer
import com.app.triflow.domain.model.PomodoroSession
import com.app.triflow.domain.model.PomodoroStats
import com.app.triflow.domain.model.StartPomodoroParams
import com.app.triflow.domain.repository.PomodoroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import javax.inject.Inject

class ObserveActiveTimerUseCase @Inject constructor(private val repo: PomodoroRepository) {
    operator fun invoke(): Flow<ActiveTimer?> = repo.activeTimer
}

class StartPomodoroUseCase @Inject constructor(private val repo: PomodoroRepository) {
    suspend operator fun invoke(params: StartPomodoroParams = StartPomodoroParams()): Outcome<ActiveTimer> =
        repo.start(params)
}

class PausePomodoroUseCase @Inject constructor(private val repo: PomodoroRepository) {
    suspend operator fun invoke(): Outcome<ActiveTimer> = repo.pause()
}

class ResumePomodoroUseCase @Inject constructor(private val repo: PomodoroRepository) {
    suspend operator fun invoke(): Outcome<ActiveTimer> = repo.resume()
}

class CompletePomodoroUseCase @Inject constructor(private val repo: PomodoroRepository) {
    suspend operator fun invoke(): Outcome<PomodoroSession> = repo.complete()
}

class AbortPomodoroUseCase @Inject constructor(private val repo: PomodoroRepository) {
    suspend operator fun invoke(): Outcome<PomodoroSession> = repo.abort()
}

class RestoreActiveTimerUseCase @Inject constructor(private val repo: PomodoroRepository) {
    suspend operator fun invoke(): Outcome<ActiveTimer?> = repo.current()
}

class GetPomodoroSessionsUseCase @Inject constructor(private val repo: PomodoroRepository) {
    suspend operator fun invoke(
        from: Instant? = null,
        to: Instant? = null,
        page: Page = Page(),
    ): Outcome<Paged<PomodoroSession>> = repo.listSessions(from, to, page)
}

class GetPomodoroStatsUseCase @Inject constructor(private val repo: PomodoroRepository) {
    suspend operator fun invoke(from: Instant? = null, to: Instant? = null): Outcome<PomodoroStats> =
        repo.stats(from, to)
}
