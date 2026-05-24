package com.app.triflow.domain.model

import com.app.triflow.domain.common.Uuid
import kotlinx.datetime.Instant

data class User(
    val id: Uuid,
    val email: String,
    val displayName: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
