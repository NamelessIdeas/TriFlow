package com.app.triflow.domain.model

import com.app.triflow.domain.common.Uuid
import kotlinx.datetime.Instant

data class InboxItem(
    val id: Uuid,
    val rawText: String,
    val processedAt: Instant?,
    val createdAt: Instant,
) {
    val isProcessed: Boolean get() = processedAt != null
}
