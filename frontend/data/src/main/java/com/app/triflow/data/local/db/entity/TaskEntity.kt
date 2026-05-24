package com.app.triflow.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val notes: String,
    val projectId: String?,
    val contextId: String?,
    val status: String,
    val energy: String?,
    val estimatedMinutes: Int?,
    val priority: Int,
    val dueDate: Instant?,
    val deferDate: Instant?,
    val completedAt: Instant?,
    val tags: List<String>,
    val createdAt: Instant,
    val updatedAt: Instant,
)
