package com.app.triflow.data.remote.mapper

import com.app.triflow.data.remote.dto.ContextDto
import com.app.triflow.data.remote.dto.ContextRequestDto
import com.app.triflow.data.remote.dto.InboxItemDto
import com.app.triflow.data.remote.dto.ProcessInboxRequestDto
import com.app.triflow.data.remote.dto.ProcessInboxResultDto
import com.app.triflow.data.remote.dto.ProjectDto
import com.app.triflow.data.remote.dto.ProjectRequestDto
import com.app.triflow.data.remote.dto.TaskContextDto
import com.app.triflow.data.remote.dto.TaskDto
import com.app.triflow.data.remote.dto.TaskRequestDto
import com.app.triflow.data.remote.dto.WeeklyReviewDto
import com.app.triflow.domain.model.GtdContext
import com.app.triflow.domain.model.InboxItem
import com.app.triflow.domain.model.Project
import com.app.triflow.domain.model.Task
import com.app.triflow.domain.model.TaskDraft
import com.app.triflow.domain.model.TaskPatch
import com.app.triflow.domain.model.TaskWithRelations
import com.app.triflow.domain.model.WeeklyReview
import com.app.triflow.domain.repository.ProcessInboxAction
import com.app.triflow.domain.repository.ProcessInboxResult

// Inbox

fun InboxItemDto.toDomain(): InboxItem = InboxItem(
    id = id,
    rawText = rawText,
    processedAt = processedAt,
    createdAt = createdAt,
)

fun ProcessInboxResultDto.toDomain(): ProcessInboxResult = ProcessInboxResult(
    task = task?.toDomain(),
    project = project?.toDomain(),
)

fun buildProcessInboxRequest(
    action: ProcessInboxAction,
    draft: TaskDraft?,
    projectTitle: String?,
): ProcessInboxRequestDto = ProcessInboxRequestDto(
    action = action.toApi(),
    title = projectTitle ?: draft?.title,
    notes = draft?.notes,
    status = draft?.status?.toApi(),
    contextId = draft?.contextId,
    projectId = draft?.projectId,
    energy = draft?.energy?.toApi(),
    dueDate = draft?.dueDate,
    priority = draft?.priority,
    tags = draft?.tags,
)

// Project

fun ProjectDto.toDomain(): Project = Project(
    id = id,
    title = title,
    description = description,
    status = status.toProjectStatus(),
    createdAt = createdAt,
    updatedAt = updatedAt,
    completedAt = completedAt,
)

fun ProjectRequest(
    title: String,
    description: String?,
    status: com.app.triflow.domain.model.ProjectStatus?,
): ProjectRequestDto = ProjectRequestDto(
    title = title,
    description = description,
    status = status?.toApi(),
)

// Context

fun ContextDto.toDomain(): GtdContext = GtdContext(
    id = id,
    name = name,
    createdAt = createdAt,
)

fun ContextRequest(name: String): ContextRequestDto = ContextRequestDto(name = name)

// Task

fun TaskDto.toDomain(): Task = Task(
    id = id,
    title = title,
    notes = notes,
    projectId = projectId,
    contextId = contextId,
    status = status.toTaskStatus(),
    energy = energy?.toEnergy(),
    estimatedMinutes = estimatedMinutes,
    priority = priority,
    dueDate = dueDate,
    deferDate = deferDate,
    completedAt = completedAt,
    tags = tags,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun TaskDraft.toRequest(): TaskRequestDto = TaskRequestDto(
    title = title,
    notes = notes,
    projectId = projectId,
    contextId = contextId,
    status = status?.toApi(),
    energy = energy?.toApi(),
    estimatedMinutes = estimatedMinutes,
    priority = priority,
    dueDate = dueDate,
    deferDate = deferDate,
    tags = tags,
)

fun TaskPatch.toRequest(): TaskRequestDto = TaskRequestDto(
    title = title.orEmpty(),
    notes = notes,
    projectId = projectId,
    contextId = contextId,
    status = status?.toApi(),
    energy = energy?.toApi(),
    estimatedMinutes = estimatedMinutes,
    priority = priority,
    dueDate = dueDate,
    deferDate = deferDate,
    tags = tags,
)

fun TaskContextDto.toDomain(): TaskWithRelations = TaskWithRelations(
    task = task.toDomain(),
    pomodoroSessions = pomodoroSessions.map { it.toDomain() },
    linkedNotes = linkedNotes.map { it.toDomain() },
)

// Weekly review

fun WeeklyReviewDto.toDomain(): WeeklyReview = WeeklyReview(
    inboxToProcess = inboxToProcess.map { it.toDomain() },
    waitingTasks = waitingTasks.map { it.toDomain() },
    projectsWithoutNextAction = projectsWithoutNextAction.map { it.toDomain() },
    generatedAt = generatedAt,
)
