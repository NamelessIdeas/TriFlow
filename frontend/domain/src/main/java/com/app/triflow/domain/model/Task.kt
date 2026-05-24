package com.app.triflow.domain.model

import com.app.triflow.domain.common.Uuid
import kotlinx.datetime.Instant

enum class TaskStatus { Inbox, NextAction, Waiting, Scheduled, Done }
enum class Energy { Low, Medium, High }

data class Task(
    val id: Uuid,
    val title: String,
    val notes: String,
    val projectId: Uuid?,
    val contextId: Uuid?,
    val status: TaskStatus,
    val energy: Energy?,
    val estimatedMinutes: Int?,
    val priority: Int,
    val dueDate: Instant?,
    val deferDate: Instant?,
    val completedAt: Instant?,
    val tags: List<String>,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val isDone: Boolean get() = status == TaskStatus.Done
}

data class TaskWithRelations(
    val task: Task,
    val pomodoroSessions: List<PomodoroSession>,
    val linkedNotes: List<Note>,
)

data class TaskFilter(
    val status: TaskStatus? = null,
    val projectId: Uuid? = null,
    val contextId: Uuid? = null,
    val dueBefore: Instant? = null,
    val tag: String? = null,
)

data class TaskDraft(
    val title: String,
    val notes: String? = null,
    val projectId: Uuid? = null,
    val contextId: Uuid? = null,
    val status: TaskStatus? = null,
    val energy: Energy? = null,
    val estimatedMinutes: Int? = null,
    val priority: Int? = null,
    val dueDate: Instant? = null,
    val deferDate: Instant? = null,
    val tags: List<String>? = null,
)

data class TaskPatch(
    val title: String? = null,
    val notes: String? = null,
    val projectId: Uuid? = null,
    val contextId: Uuid? = null,
    val status: TaskStatus? = null,
    val energy: Energy? = null,
    val estimatedMinutes: Int? = null,
    val priority: Int? = null,
    val dueDate: Instant? = null,
    val deferDate: Instant? = null,
    val tags: List<String>? = null,
)
