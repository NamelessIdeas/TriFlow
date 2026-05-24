package com.app.triflow.domain.model

import kotlinx.datetime.Instant

data class WeeklyReview(
    val inboxToProcess: List<InboxItem>,
    val waitingTasks: List<Task>,
    val projectsWithoutNextAction: List<Project>,
    val generatedAt: Instant,
)
