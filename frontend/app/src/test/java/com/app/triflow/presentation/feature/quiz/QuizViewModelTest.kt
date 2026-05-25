package com.app.triflow.presentation.feature.quiz

import com.app.triflow.domain.common.DomainError
import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.model.Goal
import com.app.triflow.domain.model.MainProblem
import com.app.triflow.domain.model.MethodScore
import com.app.triflow.domain.model.QuizAnswers
import com.app.triflow.domain.model.QuizResult
import com.app.triflow.domain.model.RecommendedMethod
import com.app.triflow.domain.model.SetupTolerance
import com.app.triflow.domain.model.WorkStyle
import com.app.triflow.domain.usecase.quiz.SubmitQuizUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var useCase: SubmitQuizUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        useCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is MainProblem step with no answers`() = runTest {
        val vm = QuizViewModel(useCase)
        val s = vm.state.value
        assertEquals(QuizStep.MainProblem, s.step)
        assertNull(s.mainProblem)
        assertNull(s.workStyle)
        assertNull(s.setupTolerance)
        assertNull(s.goal)
        assertNull(s.result)
    }

    @Test
    fun `choose advances step by step until submit on Goal`() = runTest {
        coEvery { useCase(any()) } returns Outcome.Success(sampleResult())
        val vm = QuizViewModel(useCase)

        vm.choose(MainProblem.Overwhelm)
        assertEquals(QuizStep.WorkStyle, vm.state.value.step)
        assertEquals(MainProblem.Overwhelm, vm.state.value.mainProblem)

        vm.choose(WorkStyle.Structured)
        assertEquals(QuizStep.SetupTolerance, vm.state.value.step)
        assertEquals(WorkStyle.Structured, vm.state.value.workStyle)

        vm.choose(SetupTolerance.Medium)
        assertEquals(QuizStep.Goal, vm.state.value.step)
        assertEquals(SetupTolerance.Medium, vm.state.value.setupTolerance)

        // L'ultimo choose triggera submit asincrono.
        vm.choose(Goal.ShipTasks)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            useCase(
                QuizAnswers(
                    mainProblem = MainProblem.Overwhelm,
                    workStyle = WorkStyle.Structured,
                    setupTolerance = SetupTolerance.Medium,
                    goal = Goal.ShipTasks,
                )
            )
        }
        val finalState = vm.state.value
        assertEquals(false, finalState.submitting)
        assertNotNull(finalState.result)
        assertEquals(RecommendedMethod.Gtd, finalState.result?.recommended)
    }

    @Test
    fun `submit success exposes result and stops submitting`() = runTest {
        coEvery { useCase(any()) } returns Outcome.Success(sampleResult())
        val vm = QuizViewModel(useCase)

        vm.choose(MainProblem.Distraction)
        vm.choose(WorkStyle.Flexible)
        vm.choose(SetupTolerance.Low)
        vm.choose(Goal.FocusTime)

        // Prima di advance: il submit è schedulato ma non ancora eseguito.
        assertEquals(true, vm.state.value.submitting)

        dispatcher.scheduler.advanceUntilIdle()
        val s = vm.state.value
        assertEquals(false, s.submitting)
        assertNotNull(s.result)
        assertEquals(RecommendedMethod.Gtd, s.result?.recommended)
        assertNull(s.error)
    }

    @Test
    fun `failure on submit sets error message and clears submitting`() = runTest {
        coEvery { useCase(any()) } returns Outcome.Failure(DomainError.Network)
        val vm = QuizViewModel(useCase)

        vm.choose(MainProblem.KnowledgeLoss)
        vm.choose(WorkStyle.Creative)
        vm.choose(SetupTolerance.High)
        vm.choose(Goal.BuildKnowledge)
        dispatcher.scheduler.advanceUntilIdle()

        val s = vm.state.value
        assertEquals(false, s.submitting)
        assertNull(s.result)
        assertNotNull(s.error)
    }

    @Test
    fun `back walks the step stack and stops at MainProblem`() = runTest {
        coEvery { useCase(any()) } returns Outcome.Success(sampleResult())
        val vm = QuizViewModel(useCase)

        vm.choose(MainProblem.Overwhelm)
        vm.choose(WorkStyle.Structured)
        assertEquals(QuizStep.SetupTolerance, vm.state.value.step)

        vm.back()
        assertEquals(QuizStep.WorkStyle, vm.state.value.step)
        vm.back()
        assertEquals(QuizStep.MainProblem, vm.state.value.step)
        // back() su MainProblem è no-op
        vm.back()
        assertEquals(QuizStep.MainProblem, vm.state.value.step)
    }

    @Test
    fun `restart resets all answers and step`() = runTest {
        coEvery { useCase(any()) } returns Outcome.Success(sampleResult())
        val vm = QuizViewModel(useCase)

        vm.choose(MainProblem.Overwhelm)
        vm.choose(WorkStyle.Structured)
        vm.choose(SetupTolerance.Medium)
        vm.choose(Goal.ShipTasks)
        dispatcher.scheduler.advanceUntilIdle()
        assertNotNull(vm.state.value.result)

        vm.restart()
        val s = vm.state.value
        assertEquals(QuizStep.MainProblem, s.step)
        assertNull(s.mainProblem)
        assertNull(s.workStyle)
        assertNull(s.setupTolerance)
        assertNull(s.goal)
        assertNull(s.result)
        assertNull(s.error)
        assertEquals(false, s.submitting)
    }

    private fun sampleResult(): QuizResult = QuizResult(
        recommended = RecommendedMethod.Gtd,
        reasoning = "Test reasoning",
        scores = listOf(
            MethodScore(RecommendedMethod.Gtd, 80, "primary"),
            MethodScore(RecommendedMethod.Pomodoro, 50, ""),
            MethodScore(RecommendedMethod.SecondBrain, 30, ""),
        ),
    )
}
