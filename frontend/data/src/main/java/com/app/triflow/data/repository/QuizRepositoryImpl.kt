package com.app.triflow.data.repository

import com.app.triflow.core.network.ApiCallExecutor
import com.app.triflow.data.remote.api.QuizApi
import com.app.triflow.data.remote.mapper.toDomain
import com.app.triflow.data.remote.mapper.toDto
import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.QuizAnswers
import com.app.triflow.domain.model.QuizResult
import com.app.triflow.domain.repository.QuizRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepositoryImpl @Inject constructor(
    private val api: QuizApi,
    private val executor: ApiCallExecutor,
) : QuizRepository {

    override suspend fun score(answers: QuizAnswers): Outcome<QuizResult> =
        executor(mapper = { it.toDomain() }) { api.score(answers.toDto()) }
}
