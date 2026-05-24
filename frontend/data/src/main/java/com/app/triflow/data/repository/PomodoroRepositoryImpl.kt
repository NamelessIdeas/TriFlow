package com.app.triflow.data.repository

import com.app.triflow.core.network.ApiCallExecutor
import com.app.triflow.data.remote.api.PomodoroApi
import com.app.triflow.data.remote.mapper.toDomain
import com.app.triflow.data.remote.mapper.toRequest
import com.app.triflow.domain.common.DomainError
import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.common.Page
import com.app.triflow.domain.common.Paged
import com.app.triflow.domain.common.onSuccess
import com.app.triflow.domain.model.ActiveTimer
import com.app.triflow.domain.model.PomodoroSession
import com.app.triflow.domain.model.PomodoroStats
import com.app.triflow.domain.model.StartPomodoroParams
import com.app.triflow.domain.repository.PomodoroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PomodoroRepositoryImpl @Inject constructor(
    private val api: PomodoroApi,
    private val executor: ApiCallExecutor,
) : PomodoroRepository {

    private val _activeTimer = MutableStateFlow<ActiveTimer?>(null)
    override val activeTimer: Flow<ActiveTimer?> = _activeTimer.asStateFlow()

    override suspend fun start(params: StartPomodoroParams): Outcome<ActiveTimer> {
        val result = executor(mapper = { it.toDomain() }) { api.start(params.toRequest()) }
        result.onSuccess { _activeTimer.value = it }
        return result
    }

    override suspend fun pause(): Outcome<ActiveTimer> {
        val result = executor(mapper = { it.toDomain() }) { api.pause() }
        result.onSuccess { _activeTimer.value = it }
        return result
    }

    override suspend fun resume(): Outcome<ActiveTimer> {
        val result = executor(mapper = { it.toDomain() }) { api.resume() }
        result.onSuccess { _activeTimer.value = it }
        return result
    }

    override suspend fun current(): Outcome<ActiveTimer?> {
        val result = executor(mapper = { it.toDomain() }) { api.current() }
        return when (result) {
            is Outcome.Success -> {
                _activeTimer.value = result.value
                Outcome.Success(result.value)
            }
            is Outcome.Failure -> when (result.error) {
                DomainError.NotFound -> {
                    _activeTimer.value = null
                    Outcome.Success(null)
                }
                else -> result
            }
        }
    }

    override suspend fun complete(): Outcome<PomodoroSession> {
        val result = executor(mapper = { it.toDomain() }) { api.complete() }
        result.onSuccess { _activeTimer.value = null }
        return result
    }

    override suspend fun abort(): Outcome<PomodoroSession> {
        val result = executor(mapper = { it.toDomain() }) { api.abort() }
        result.onSuccess { _activeTimer.value = null }
        return result
    }

    override suspend fun listSessions(
        from: Instant?,
        to: Instant?,
        page: Page,
    ): Outcome<Paged<PomodoroSession>> {
        val result = executor(mapper = { dto -> dto.map { it.toDomain() } }) {
            api.sessions(
                from = from?.toString(),
                to = to?.toString(),
                limit = page.limit,
                offset = page.offset,
            )
        }
        return when (result) {
            is Outcome.Success -> Outcome.Success(
                Paged(items = result.value, limit = page.limit, offset = page.offset, total = result.value.size)
            )
            is Outcome.Failure -> result
        }
    }

    override suspend fun stats(from: Instant?, to: Instant?): Outcome<PomodoroStats> =
        executor(mapper = { it.toDomain() }) {
            api.stats(from = from?.toString(), to = to?.toString())
        }
}
