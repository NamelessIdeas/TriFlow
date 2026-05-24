package com.app.triflow.data.local.db.mapper

import com.app.triflow.data.local.db.entity.NoteEntity
import com.app.triflow.data.local.db.entity.TaskEntity
import com.app.triflow.data.remote.dto.NoteDto
import com.app.triflow.data.remote.dto.TaskDto
import com.app.triflow.data.remote.mapper.toEnergy
import com.app.triflow.data.remote.mapper.toParaCategory
import com.app.triflow.data.remote.mapper.toTaskStatus
import com.app.triflow.domain.model.Note
import com.app.triflow.domain.model.Task

// --- Task

fun TaskDto.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    notes = notes,
    projectId = projectId,
    contextId = contextId,
    status = status,
    energy = energy,
    estimatedMinutes = estimatedMinutes,
    priority = priority,
    dueDate = dueDate,
    deferDate = deferDate,
    completedAt = completedAt,
    tags = tags,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun TaskEntity.toDomain(): Task = Task(
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

// --- Note

fun NoteDto.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    contentMd = contentMd,
    paraCategory = paraCategory,
    tags = tags,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    contentMd = contentMd,
    paraCategory = paraCategory?.toParaCategory(),
    tags = tags,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
