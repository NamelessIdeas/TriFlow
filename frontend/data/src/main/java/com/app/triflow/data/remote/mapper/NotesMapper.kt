package com.app.triflow.data.remote.mapper

import com.app.triflow.data.remote.dto.NoteDto
import com.app.triflow.data.remote.dto.NoteRequestDto
import com.app.triflow.data.remote.dto.PromoteNoteRequestDto
import com.app.triflow.domain.model.Note
import com.app.triflow.domain.model.NoteDraft
import com.app.triflow.domain.model.NotePatch
import com.app.triflow.domain.model.PromoteNoteParams

fun NoteDto.toDomain(): Note = Note(
    id = id,
    title = title,
    contentMd = contentMd,
    paraCategory = paraCategory?.toParaCategory(),
    tags = tags,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun NoteDraft.toRequest(): NoteRequestDto = NoteRequestDto(
    title = title,
    contentMd = contentMd,
    paraCategory = paraCategory?.toApi(),
    tags = tags,
)

fun NotePatch.toRequest(): NoteRequestDto = NoteRequestDto(
    title = title.orEmpty(),
    contentMd = contentMd,
    paraCategory = paraCategory?.toApi(),
    tags = tags,
)

fun PromoteNoteParams.toRequest(): PromoteNoteRequestDto = PromoteNoteRequestDto(
    title = title,
    projectId = projectId,
    contextId = contextId,
    status = status?.toApi(),
    dueDate = dueDate,
    priority = priority,
)
