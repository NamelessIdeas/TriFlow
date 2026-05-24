package com.app.triflow.domain.usecase.quiz

import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.QuizAnswers
import com.app.triflow.domain.model.QuizResult
import com.app.triflow.domain.repository.QuizRepository
import javax.inject.Inject

class SubmitQuizUseCase @Inject constructor(
    private val repository: QuizRepository,
) {
    suspend operator fun invoke(answers: QuizAnswers): Outcome<QuizResult> =
        repository.score(answers)
}
