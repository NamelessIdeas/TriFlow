package com.app.triflow.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

// --- Inbox

@Serializable
data class InboxItemDto(
    val id: String,
    val userId: String? = null,
    val rawText: String,
    val processedAt: Instant? = null,
    val createdAt: Instant,
)

@Serializable
data class CaptureInboxRequestDto(
    val rawText: String,
)

@Serializable
data class ProcessInboxRequestDto(
    val action: String,
    val title: String? = null,
    val notes: String? = null,
    val status: String? = null,
    val contextId: String? = null,
    val projectId: String? = null,
    val energy: String? = null,
    val dueDate: Instant? = null,
    val priority: Int? = null,
    val tags: List<String>? = null,
)

@Serializable
data class ProcessInboxResultDto(
    val task: TaskDto? = null,
    val project: ProjectDto? = null,
)

// --- Projects

@Serializable
data class ProjectDto(
    val id: String,
    val userId: String? = null,
    val title: String,
    val description: String = "",
    val status: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val completedAt: Instant? = null,
)

@Serializable
data class ProjectRequestDto(
    val title: String,
    val description: String? = null,
    val status: String? = null,
)

// --- Contexts

@Serializable
data class ContextDto(
    val id: String,
    val userId: String? = null,
    val name: String,
    val createdAt: Instant,
)

@Serializable
data class ContextRequestDto(
    val name: String,
)

// --- Tasks

@Serializable
data class TaskDto(
    val id: String,
    val userId: String? = null,
    val projectId: String? = null,
    val contextId: String? = null,
    val title: String,
    val notes: String = "",
    val status: String,
    val energy: String? = null,
    val estimatedMinutes: Int? = null,
    val priority: Int = 0,
    val dueDate: Instant? = null,
    val deferDate: Instant? = null,
    val completedAt: Instant? = null,
    val tags: List<String> = emptyList(),
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Serializable
data class TaskRequestDto(
    val title: String,
    val notes: String? = null,
    val projectId: String? = null,
    val contextId: String? = null,
    val status: String? = null,
    val energy: String? = null,
    val estimatedMinutes: Int? = null,
    val priority: Int? = null,
    val dueDate: Instant? = null,
    val deferDate: Instant? = null,
    val tags: List<String>? = null,
)

@Serializable
data class TaskContextDto(
    val task: TaskDto,
    val pomodoroSessions: List<PomodoroSessionDto> = emptyList(),
    val linkedNotes: List<NoteDto> = emptyList(),
)

// --- Weekly review

@Serializable
data class WeeklyReviewDto(
    val inboxToProcess: List<InboxItemDto> = emptyList(),
    val waitingTasks: List<TaskDto> = emptyList(),
    val projectsWithoutNextAction: List<ProjectDto> = emptyList(),
    val generatedAt: Instant,
)
