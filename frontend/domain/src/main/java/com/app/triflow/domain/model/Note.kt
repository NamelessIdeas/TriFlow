package com.app.triflow.domain.model

import com.app.triflow.domain.common.Uuid
import kotlinx.datetime.Instant

enum class ParaCategory { Project, Area, Resource, Archive }

enum class NoteRefType { Task, Project }

data class Note(
    val id: Uuid,
    val title: String,
    val contentMd: String,
    val paraCategory: ParaCategory?,
    val tags: List<String>,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class NoteDraft(
    val title: String,
    val contentMd: String? = null,
    val paraCategory: ParaCategory? = null,
    val tags: List<String>? = null,
)

data class NotePatch(
    val title: String? = null,
    val contentMd: String? = null,
    val paraCategory: ParaCategory? = null,
    val tags: List<String>? = null,
)

data class NoteFilter(
    val paraCategory: ParaCategory? = null,
    val tag: String? = null,
    val query: String? = null,
)

data class PromoteNoteParams(
    val title: String? = null,
    val projectId: Uuid? = null,
    val contextId: Uuid? = null,
    val status: TaskStatus? = null,
    val dueDate: kotlinx.datetime.Instant? = null,
    val priority: Int? = null,
)
