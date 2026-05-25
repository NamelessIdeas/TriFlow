package com.app.triflow.data.remote.mapper

import com.app.triflow.domain.model.Energy
import com.app.triflow.domain.model.Goal
import com.app.triflow.domain.model.MainProblem
import com.app.triflow.domain.model.ParaCategory
import com.app.triflow.domain.model.PomodoroKind
import com.app.triflow.domain.model.PomodoroSessionStatus
import com.app.triflow.domain.model.ProjectStatus
import com.app.triflow.domain.model.RecommendedMethod
import com.app.triflow.domain.model.SetupTolerance
import com.app.triflow.domain.model.TaskStatus
import com.app.triflow.domain.model.WorkStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EnumMappersTest {

    @Test
    fun `task status round-trip preserves all variants`() {
        TaskStatus.entries.forEach { status ->
            assertEquals(status, status.toApi().toTaskStatus())
        }
    }

    @Test
    fun `task status falls back to Inbox on unknown string`() {
        assertEquals(TaskStatus.Inbox, "totally_unknown".toTaskStatus())
    }

    @Test
    fun `task status api wire format matches contract`() {
        assertEquals("inbox", TaskStatus.Inbox.toApi())
        assertEquals("next_action", TaskStatus.NextAction.toApi())
        assertEquals("waiting", TaskStatus.Waiting.toApi())
        assertEquals("scheduled", TaskStatus.Scheduled.toApi())
        assertEquals("done", TaskStatus.Done.toApi())
    }

    @Test
    fun `energy round-trip preserves all variants`() {
        Energy.entries.forEach { energy ->
            assertEquals(energy, energy.toApi().toEnergy())
        }
    }

    @Test
    fun `energy returns null on unknown or empty`() {
        assertNull("".toEnergy())
        assertNull("super".toEnergy())
    }

    @Test
    fun `project status round-trip preserves all variants`() {
        ProjectStatus.entries.forEach { status ->
            assertEquals(status, status.toApi().toProjectStatus())
        }
    }

    @Test
    fun `project status falls back to Active on unknown`() {
        assertEquals(ProjectStatus.Active, "xyz".toProjectStatus())
    }

    @Test
    fun `pomodoro kind round-trip preserves all variants`() {
        PomodoroKind.entries.forEach { kind ->
            assertEquals(kind, kind.toApi().toPomodoroKind())
        }
    }

    @Test
    fun `pomodoro kind wire format uses underscores`() {
        assertEquals("short_break", PomodoroKind.ShortBreak.toApi())
        assertEquals("long_break", PomodoroKind.LongBreak.toApi())
        assertEquals("focus", PomodoroKind.Focus.toApi())
    }

    @Test
    fun `pomodoro session status maps known values and falls back`() {
        assertEquals(PomodoroSessionStatus.Completed, "completed".toPomodoroSessionStatus())
        assertEquals(PomodoroSessionStatus.Aborted, "aborted".toPomodoroSessionStatus())
        // Fallback contract: unknown → Completed (see EnumMappers.kt)
        assertEquals(PomodoroSessionStatus.Completed, "weird".toPomodoroSessionStatus())
    }

    @Test
    fun `para category round-trip and unknown returns null`() {
        ParaCategory.entries.forEach { cat ->
            assertEquals(cat, cat.toApi().toParaCategory())
        }
        assertNull("".toParaCategory())
        assertNull("inbox".toParaCategory())
    }

    @Test
    fun `quiz main problem maps to wire format`() {
        assertEquals("overwhelm", MainProblem.Overwhelm.toApi())
        assertEquals("distraction", MainProblem.Distraction.toApi())
        assertEquals("knowledge_loss", MainProblem.KnowledgeLoss.toApi())
    }

    @Test
    fun `quiz work style maps to wire format`() {
        assertEquals("structured", WorkStyle.Structured.toApi())
        assertEquals("flexible", WorkStyle.Flexible.toApi())
        assertEquals("creative", WorkStyle.Creative.toApi())
    }

    @Test
    fun `quiz setup tolerance maps to wire format`() {
        assertEquals("low", SetupTolerance.Low.toApi())
        assertEquals("medium", SetupTolerance.Medium.toApi())
        assertEquals("high", SetupTolerance.High.toApi())
    }

    @Test
    fun `quiz goal maps to wire format`() {
        assertEquals("ship_tasks", Goal.ShipTasks.toApi())
        assertEquals("focus_time", Goal.FocusTime.toApi())
        assertEquals("build_knowledge", Goal.BuildKnowledge.toApi())
    }

    @Test
    fun `recommended method parses known and falls back to Gtd`() {
        assertEquals(RecommendedMethod.Gtd, "gtd".toRecommendedMethod())
        assertEquals(RecommendedMethod.Pomodoro, "pomodoro".toRecommendedMethod())
        assertEquals(RecommendedMethod.SecondBrain, "second_brain".toRecommendedMethod())
        // Fallback contract per EnumMappers.kt
        assertEquals(RecommendedMethod.Gtd, "unknown".toRecommendedMethod())
    }
}
