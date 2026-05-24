package com.app.triflow.domain.model

import com.app.triflow.domain.common.Uuid
import kotlinx.datetime.Instant

enum class ProjectStatus { Active, Someday, Completed }

data class Project(
    val id: Uuid,
    val title: String,
    val description: String,
    val status: ProjectStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    val completedAt: Instant?,
)
