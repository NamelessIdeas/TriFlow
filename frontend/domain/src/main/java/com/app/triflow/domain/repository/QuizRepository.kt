package com.app.triflow.domain.repository

import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.QuizAnswers
import com.app.triflow.domain.model.QuizResult

interface QuizRepository {
    suspend fun score(answers: QuizAnswers): Outcome<QuizResult>
}
