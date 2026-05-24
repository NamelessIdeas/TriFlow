package com.app.triflow.domain.model

import com.app.triflow.domain.common.Uuid
import kotlinx.datetime.Instant

data class GtdContext(
    val id: Uuid,
    val name: String,
    val createdAt: Instant,
)
