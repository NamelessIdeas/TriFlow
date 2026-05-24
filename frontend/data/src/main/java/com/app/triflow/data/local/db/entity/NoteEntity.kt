package com.app.triflow.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val contentMd: String,
    val paraCategory: String?,
    val tags: List<String>,
    val createdAt: Instant,
    val updatedAt: Instant,
)
