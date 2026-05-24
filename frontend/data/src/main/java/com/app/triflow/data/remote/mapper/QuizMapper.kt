package com.app.triflow.data.remote.mapper

import com.app.triflow.data.remote.dto.MethodScoreDto
import com.app.triflow.data.remote.dto.QuizAnswersDto
import com.app.triflow.data.remote.dto.QuizResultDto
import com.app.triflow.domain.model.MethodScore
import com.app.triflow.domain.model.QuizAnswers
import com.app.triflow.domain.model.QuizResult

fun QuizAnswers.toDto(): QuizAnswersDto = QuizAnswersDto(
    mainProblem = mainProblem.toApi(),
    workStyle = workStyle.toApi(),
    setupTolerance = setupTolerance.toApi(),
    goal = goal.toApi(),
)

fun MethodScoreDto.toDomain(): MethodScore = MethodScore(
    method = method.toRecommendedMethod(),
    score = score,
    explanation = explanation,
)

fun QuizResultDto.toDomain(): QuizResult = QuizResult(
    recommended = recommendedMethod.toRecommendedMethod(),
    reasoning = reasoning,
    scores = scores.map { it.toDomain() },
)
