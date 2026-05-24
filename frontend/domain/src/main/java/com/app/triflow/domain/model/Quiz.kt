package com.app.triflow.domain.model

enum class MainProblem { Overwhelm, Distraction, KnowledgeLoss }
enum class WorkStyle { Structured, Flexible, Creative }
enum class SetupTolerance { Low, Medium, High }
enum class Goal { ShipTasks, FocusTime, BuildKnowledge }
enum class RecommendedMethod { Gtd, Pomodoro, SecondBrain }

data class QuizAnswers(
    val mainProblem: MainProblem,
    val workStyle: WorkStyle,
    val setupTolerance: SetupTolerance,
    val goal: Goal,
)

data class MethodScore(
    val method: RecommendedMethod,
    val score: Int,
    val explanation: String,
)

data class QuizResult(
    val recommended: RecommendedMethod,
    val reasoning: String,
    val scores: List<MethodScore>,
)
