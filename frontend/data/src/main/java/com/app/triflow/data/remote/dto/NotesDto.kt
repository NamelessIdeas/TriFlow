package com.app.triflow.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class NoteDto(
    val id: String,
    val userId: String? = null,
    val title: String,
    val contentMd: String = "",
    val paraCategory: String? = null,
    val tags: List<String> = emptyList(),
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Serializable
data class NoteRequestDto(
    val title: String,
    val contentMd: String? = null,
    val paraCategory: String? = null,
    val tags: List<String>? = null,
)

@Serializable
data class NoteLinkRequestDto(
    val targetNoteId: String,
)

@Serializable
data class NoteRefRequestDto(
    val refType: String,
    val refId: String,
)

@Serializable
data class PromoteNoteRequestDto(
    val title: String? = null,
    val projectId: String? = null,
    val contextId: String? = null,
    val status: String? = null,
    val dueDate: Instant? = null,
    val priority: Int? = null,
)
