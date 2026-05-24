package com.app.triflow.domain.repository

import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.common.Page
import com.app.triflow.domain.common.Paged
import com.app.triflow.domain.common.Uuid
import com.app.triflow.domain.model.GtdContext
import com.app.triflow.domain.model.InboxItem
import com.app.triflow.domain.model.Project
import com.app.triflow.domain.model.ProjectStatus
import com.app.triflow.domain.model.Task
import com.app.triflow.domain.model.TaskDraft
import com.app.triflow.domain.model.TaskFilter
import com.app.triflow.domain.model.TaskPatch
import com.app.triflow.domain.model.TaskWithRelations
import com.app.triflow.domain.model.WeeklyReview
import kotlinx.coroutines.flow.Flow

interface GtdRepository {

    // Inbox
    fun observeInbox(): Flow<List<InboxItem>>
    suspend fun listInbox(page: Page = Page()): Outcome<Paged<InboxItem>>
    suspend fun captureInbox(rawText: String): Outcome<InboxItem>
    suspend fun processInbox(
        id: Uuid,
        action: ProcessInboxAction,
        draft: TaskDraft? = null,
        projectTitle: String? = null,
    ): Outcome<ProcessInboxResult>

    // Tasks
    fun observeTasks(filter: TaskFilter = TaskFilter()): Flow<List<Task>>
    suspend fun listTasks(filter: TaskFilter = TaskFilter(), page: Page = Page()): Outcome<Paged<Task>>
    suspend fun getTask(id: Uuid): Outcome<Task>
    suspend fun getTaskWithRelations(id: Uuid): Outcome<TaskWithRelations>
    suspend fun createTask(draft: TaskDraft): Outcome<Task>
    suspend fun updateTask(id: Uuid, patch: TaskPatch): Outcome<Task>
    suspend fun deleteTask(id: Uuid): Outcome<Unit>

    // Projects
    fun observeProjects(status: ProjectStatus? = null): Flow<List<Project>>
    suspend fun listProjects(status: ProjectStatus? = null, page: Page = Page()): Outcome<Paged<Project>>
    suspend fun getProject(id: Uuid): Outcome<Project>
    suspend fun createProject(title: String, description: String? = null, status: ProjectStatus? = null): Outcome<Project>
    suspend fun updateProject(id: Uuid, title: String, description: String? = null, status: ProjectStatus? = null): Outcome<Project>
    suspend fun deleteProject(id: Uuid): Outcome<Unit>

    // Contexts
    fun observeContexts(): Flow<List<GtdContext>>
    suspend fun listContexts(): Outcome<List<GtdContext>>
    suspend fun createContext(name: String): Outcome<GtdContext>
    suspend fun deleteContext(id: Uuid): Outcome<Unit>

    // Weekly review
    suspend fun getWeeklyReview(): Outcome<WeeklyReview>
}

enum class ProcessInboxAction { Task, Project, Discard }

data class ProcessInboxResult(
    val task: Task? = null,
    val project: Project? = null,
)
