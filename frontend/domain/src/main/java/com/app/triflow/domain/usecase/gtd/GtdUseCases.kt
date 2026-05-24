package com.app.triflow.domain.usecase.gtd

import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.common.Uuid
import com.app.triflow.domain.model.GtdContext
import com.app.triflow.domain.model.InboxItem
import com.app.triflow.domain.model.Project
import com.app.triflow.domain.model.ProjectStatus
import com.app.triflow.domain.model.Task
import com.app.triflow.domain.model.TaskDraft
import com.app.triflow.domain.model.TaskFilter
import com.app.triflow.domain.model.TaskPatch
import com.app.triflow.domain.model.TaskStatus
import com.app.triflow.domain.model.TaskWithRelations
import com.app.triflow.domain.model.WeeklyReview
import com.app.triflow.domain.repository.GtdRepository
import com.app.triflow.domain.repository.ProcessInboxAction
import com.app.triflow.domain.repository.ProcessInboxResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Inbox
class ObserveInboxUseCase @Inject constructor(private val repo: GtdRepository) {
    operator fun invoke(): Flow<List<InboxItem>> = repo.observeInbox()
}

class CaptureInboxUseCase @Inject constructor(private val repo: GtdRepository) {
    suspend operator fun invoke(rawText: String): Outcome<InboxItem> =
        repo.captureInbox(rawText.trim())
}

class ProcessInboxItemUseCase @Inject constructor(private val repo: GtdRepository) {
    suspend operator fun invoke(
        id: Uuid,
        action: ProcessInboxAction,
        draft: TaskDraft? = null,
        projectTitle: String? = null,
    ): Outcome<ProcessInboxResult> = repo.processInbox(id, action, draft, projectTitle)
}

// Tasks
class ObserveTasksUseCase @Inject constructor(private val repo: GtdRepository) {
    operator fun invoke(filter: TaskFilter = TaskFilter()): Flow<List<Task>> =
        repo.observeTasks(filter)
}

class GetTaskUseCase @Inject constructor(private val repo: GtdRepository) {
    suspend operator fun invoke(id: Uuid): Outcome<Task> = repo.getTask(id)
}

class GetTaskWithRelationsUseCase @Inject constructor(private val repo: GtdRepository) {
    suspend operator fun invoke(id: Uuid): Outcome<TaskWithRelations> =
        repo.getTaskWithRelations(id)
}

class CreateTaskUseCase @Inject constructor(private val repo: GtdRepository) {
    suspend operator fun invoke(draft: TaskDraft): Outcome<Task> = repo.createTask(draft)
}

class UpdateTaskUseCase @Inject constructor(private val repo: GtdRepository) {
    suspend operator fun invoke(id: Uuid, patch: TaskPatch): Outcome<Task> =
        repo.updateTask(id, patch)
}

class ToggleTaskDoneUseCase @Inject constructor(private val repo: GtdRepository) {
    suspend operator fun invoke(task: Task): Outcome<Task> {
        val newStatus = if (task.isDone) TaskStatus.NextAction else TaskStatus.Done
        return repo.updateTask(task.id, TaskPatch(status = newStatus))
    }
}

class DeleteTaskUseCase @Inject constructor(private val repo: GtdRepository) {
    suspend operator fun invoke(id: Uuid): Outcome<Unit> = repo.deleteTask(id)
}

// Projects
class ObserveProjectsUseCase @Inject constructor(private val repo: GtdRepository) {
    operator fun invoke(status: ProjectStatus? = null): Flow<List<Project>> =
        repo.observeProjects(status)
}

class CreateProjectUseCase @Inject constructor(private val repo: GtdRepository) {
    suspend operator fun invoke(
        title: String,
        description: String? = null,
        status: ProjectStatus? = null,
    ): Outcome<Project> = repo.createProject(title.trim(), description, status)
}

// Contexts
class ObserveContextsUseCase @Inject constructor(private val repo: GtdRepository) {
    operator fun invoke(): Flow<List<GtdContext>> = repo.observeContexts()
}

class CreateContextUseCase @Inject constructor(private val repo: GtdRepository) {
    suspend operator fun invoke(name: String): Outcome<GtdContext> =
        repo.createContext(name.trim())
}

// Weekly review
class GetWeeklyReviewUseCase @Inject constructor(private val repo: GtdRepository) {
    suspend operator fun invoke(): Outcome<WeeklyReview> = repo.getWeeklyReview()
}
