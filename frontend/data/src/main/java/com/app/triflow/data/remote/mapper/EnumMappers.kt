package com.app.triflow.data.remote.mapper

import com.app.triflow.domain.model.Energy
import com.app.triflow.domain.model.Goal
import com.app.triflow.domain.model.MainProblem
import com.app.triflow.domain.model.NoteRefType
import com.app.triflow.domain.model.ParaCategory
import com.app.triflow.domain.model.PomodoroKind
import com.app.triflow.domain.model.PomodoroSessionStatus
import com.app.triflow.domain.model.ProjectStatus
import com.app.triflow.domain.model.RecommendedMethod
import com.app.triflow.domain.model.SetupTolerance
import com.app.triflow.domain.model.TaskStatus
import com.app.triflow.domain.model.WorkStyle
import com.app.triflow.domain.repository.ProcessInboxAction

// --- Task status

fun String.toTaskStatus(): TaskStatus = when (this) {
    "inbox" -> TaskStatus.Inbox
    "next_action" -> TaskStatus.NextAction
    "waiting" -> TaskStatus.Waiting
    "scheduled" -> TaskStatus.Scheduled
    "done" -> TaskStatus.Done
    else -> TaskStatus.Inbox
}

fun TaskStatus.toApi(): String = when (this) {
    TaskStatus.Inbox -> "inbox"
    TaskStatus.NextAction -> "next_action"
    TaskStatus.Waiting -> "waiting"
    TaskStatus.Scheduled -> "scheduled"
    TaskStatus.Done -> "done"
}

// --- Energy

fun String.toEnergy(): Energy? = when (this) {
    "low" -> Energy.Low
    "medium" -> Energy.Medium
    "high" -> Energy.High
    else -> null
}

fun Energy.toApi(): String = when (this) {
    Energy.Low -> "low"
    Energy.Medium -> "medium"
    Energy.High -> "high"
}

// --- Project status

fun String.toProjectStatus(): ProjectStatus = when (this) {
    "active" -> ProjectStatus.Active
    "someday" -> ProjectStatus.Someday
    "completed" -> ProjectStatus.Completed
    else -> ProjectStatus.Active
}

fun ProjectStatus.toApi(): String = when (this) {
    ProjectStatus.Active -> "active"
    ProjectStatus.Someday -> "someday"
    ProjectStatus.Completed -> "completed"
}

// --- Pomodoro

fun String.toPomodoroKind(): PomodoroKind = when (this) {
    "focus" -> PomodoroKind.Focus
    "short_break" -> PomodoroKind.ShortBreak
    "long_break" -> PomodoroKind.LongBreak
    else -> PomodoroKind.Focus
}

fun PomodoroKind.toApi(): String = when (this) {
    PomodoroKind.Focus -> "focus"
    PomodoroKind.ShortBreak -> "short_break"
    PomodoroKind.LongBreak -> "long_break"
}

fun String.toPomodoroSessionStatus(): PomodoroSessionStatus = when (this) {
    "completed" -> PomodoroSessionStatus.Completed
    "aborted" -> PomodoroSessionStatus.Aborted
    else -> PomodoroSessionStatus.Completed
}

// --- PARA

fun String.toParaCategory(): ParaCategory? = when (this) {
    "project" -> ParaCategory.Project
    "area" -> ParaCategory.Area
    "resource" -> ParaCategory.Resource
    "archive" -> ParaCategory.Archive
    else -> null
}

fun ParaCategory.toApi(): String = when (this) {
    ParaCategory.Project -> "project"
    ParaCategory.Area -> "area"
    ParaCategory.Resource -> "resource"
    ParaCategory.Archive -> "archive"
}

// --- Note ref type

fun NoteRefType.toApi(): String = when (this) {
    NoteRefType.Task -> "task"
    NoteRefType.Project -> "project"
}

// --- Process inbox action

fun ProcessInboxAction.toApi(): String = when (this) {
    ProcessInboxAction.Task -> "task"
    ProcessInboxAction.Project -> "project"
    ProcessInboxAction.Discard -> "discard"
}

// --- Quiz enums

fun MainProblem.toApi(): String = when (this) {
    MainProblem.Overwhelm -> "overwhelm"
    MainProblem.Distraction -> "distraction"
    MainProblem.KnowledgeLoss -> "knowledge_loss"
}

fun WorkStyle.toApi(): String = when (this) {
    WorkStyle.Structured -> "structured"
    WorkStyle.Flexible -> "flexible"
    WorkStyle.Creative -> "creative"
}

fun SetupTolerance.toApi(): String = when (this) {
    SetupTolerance.Low -> "low"
    SetupTolerance.Medium -> "medium"
    SetupTolerance.High -> "high"
}

fun Goal.toApi(): String = when (this) {
    Goal.ShipTasks -> "ship_tasks"
    Goal.FocusTime -> "focus_time"
    Goal.BuildKnowledge -> "build_knowledge"
}

fun String.toRecommendedMethod(): RecommendedMethod = when (this) {
    "gtd" -> RecommendedMethod.Gtd
    "pomodoro" -> RecommendedMethod.Pomodoro
    "second_brain" -> RecommendedMethod.SecondBrain
    else -> RecommendedMethod.Gtd
}
