package com.app.triflow.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class QuizAnswersDto(
    val mainProblem: String,
    val workStyle: String,
    val setupTolerance: String,
    val goal: String,
)

@Serializable
data class MethodScoreDto(
    val method: String,
    val score: Int,
    val explanation: String = "",
)

@Serializable
data class QuizResultDto(
    val recommendedMethod: String,
    val reasoning: String = "",
    val scores: List<MethodScoreDto> = emptyList(),
)
