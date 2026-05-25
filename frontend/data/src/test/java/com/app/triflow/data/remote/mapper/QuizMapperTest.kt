package com.app.triflow.data.remote.mapper

import com.app.triflow.data.remote.dto.MethodScoreDto
import com.app.triflow.data.remote.dto.QuizResultDto
import com.app.triflow.domain.model.Goal
import com.app.triflow.domain.model.MainProblem
import com.app.triflow.domain.model.QuizAnswers
import com.app.triflow.domain.model.RecommendedMethod
import com.app.triflow.domain.model.SetupTolerance
import com.app.triflow.domain.model.WorkStyle
import org.junit.Assert.assertEquals
import org.junit.Test

class QuizMapperTest {

    @Test
    fun `QuizAnswers toDto serializes all four fields to wire format`() {
        val answers = QuizAnswers(
            mainProblem = MainProblem.KnowledgeLoss,
            workStyle = WorkStyle.Creative,
            setupTolerance = SetupTolerance.High,
            goal = Goal.BuildKnowledge,
        )
        val dto = answers.toDto()
        assertEquals("knowledge_loss", dto.mainProblem)
        assertEquals("creative", dto.workStyle)
        assertEquals("high", dto.setupTolerance)
        assertEquals("build_knowledge", dto.goal)
    }

    @Test
    fun `MethodScoreDto toDomain maps fields and method enum`() {
        val dto = MethodScoreDto(method = "pomodoro", score = 73, explanation = "ti aiuta a focalizzarti")
        val domain = dto.toDomain()
        assertEquals(RecommendedMethod.Pomodoro, domain.method)
        assertEquals(73, domain.score)
        assertEquals("ti aiuta a focalizzarti", domain.explanation)
    }

    @Test
    fun `QuizResultDto toDomain preserves scores order and content`() {
        val dto = QuizResultDto(
            recommendedMethod = "second_brain",
            reasoning = "vuoi costruire conoscenza",
            scores = listOf(
                MethodScoreDto("second_brain", 80, "PARA + linking"),
                MethodScoreDto("gtd", 50, ""),
                MethodScoreDto("pomodoro", 30, ""),
            ),
        )
        val domain = dto.toDomain()
        assertEquals(RecommendedMethod.SecondBrain, domain.recommended)
        assertEquals("vuoi costruire conoscenza", domain.reasoning)
        assertEquals(3, domain.scores.size)
        assertEquals(RecommendedMethod.SecondBrain, domain.scores[0].method)
        assertEquals(80, domain.scores[0].score)
        assertEquals(RecommendedMethod.Gtd, domain.scores[1].method)
        assertEquals(RecommendedMethod.Pomodoro, domain.scores[2].method)
    }

    @Test
    fun `QuizResultDto with empty scores maps to empty list`() {
        val dto = QuizResultDto(recommendedMethod = "gtd")
        val domain = dto.toDomain()
        assertEquals(RecommendedMethod.Gtd, domain.recommended)
        assertEquals("", domain.reasoning)
        assertEquals(emptyList<Any>(), domain.scores)
    }

    @Test
    fun `unknown recommended method falls back to Gtd by contract`() {
        val dto = QuizResultDto(recommendedMethod = "alien_method")
        assertEquals(RecommendedMethod.Gtd, dto.toDomain().recommended)
    }
}
