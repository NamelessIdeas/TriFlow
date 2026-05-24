package com.app.triflow.data.repository

import com.app.triflow.core.network.ApiCallExecutor
import com.app.triflow.data.local.db.dao.TaskDao
import com.app.triflow.data.local.db.mapper.toDomain as taskEntityToDomain
import com.app.triflow.data.local.db.mapper.toEntity
import com.app.triflow.data.remote.api.ContextsApi
import com.app.triflow.data.remote.api.InboxApi
import com.app.triflow.data.remote.api.ProjectsApi
import com.app.triflow.data.remote.api.ReviewsApi
import com.app.triflow.data.remote.api.TasksApi
import com.app.triflow.data.remote.dto.CaptureInboxRequestDto
import com.app.triflow.data.remote.dto.ContextRequestDto
import com.app.triflow.data.remote.dto.ProjectRequestDto
import com.app.triflow.data.remote.mapper.buildProcessInboxRequest
import com.app.triflow.data.remote.mapper.toApi
import com.app.triflow.data.remote.mapper.toDomain
import com.app.triflow.data.remote.mapper.toRequest
import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.common.Page
import com.app.triflow.domain.common.Paged
import com.app.triflow.domain.common.Uuid
import com.app.triflow.domain.common.onSuccess
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
import com.app.triflow.domain.repository.GtdRepository
import com.app.triflow.domain.repository.ProcessInboxAction
import com.app.triflow.domain.repository.ProcessInboxResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GtdRepositoryImpl @Inject constructor(
    private val tasksApi: TasksApi,
    private val projectsApi: ProjectsApi,
    private val contextsApi: ContextsApi,
    private val inboxApi: InboxApi,
    private val reviewsApi: ReviewsApi,
    private val taskDao: TaskDao,
    private val executor: ApiCallExecutor,
) : GtdRepository {

    // In-memory caches per progetti/contesti/inbox (per ora niente Room su questi).
    private val inbox = MutableStateFlow<List<InboxItem>>(emptyList())
    private val projects = MutableStateFlow<List<Project>>(emptyList())
    private val contexts = MutableStateFlow<List<GtdContext>>(emptyList())

    // --- Inbox -------------------------------------------------------------

    override fun observeInbox(): Flow<List<InboxItem>> = inbox.asStateFlow()

    override suspend fun listInbox(page: Page): Outcome<Paged<InboxItem>> {
        val result = executor(mapper = { dto -> dto.map { it.toDomain() } }) {
            inboxApi.list(limit = page.limit, offset = page.offset)
        }
        result.onSuccess { inbox.value = it }
        return result.toPaged(page)
    }

    override suspend fun captureInbox(rawText: String): Outcome<InboxItem> {
        val result = executor(mapper = { it.toDomain() }) {
            inboxApi.capture(CaptureInboxRequestDto(rawText))
        }
        result.onSuccess { item -> inbox.value = listOf(item) + inbox.value }
        return result
    }

    override suspend fun processInbox(
        id: Uuid,
        action: ProcessInboxAction,
        draft: TaskDraft?,
        projectTitle: String?,
    ): Outcome<ProcessInboxResult> {
        val result = executor(mapper = { it.toDomain() }) {
            inboxApi.process(id, buildProcessInboxRequest(action, draft, projectTitle))
        }
        result.onSuccess {
            inbox.value = inbox.value.filterNot { i -> i.id == id }
            it.task?.let { task -> taskDao.upsert(taskFromDomainEntity(task)) }
        }
        return result
    }

    // --- Tasks -------------------------------------------------------------

    override fun observeTasks(filter: TaskFilter): Flow<List<Task>> {
        val status = filter.status
        val projectId = filter.projectId
        val contextId = filter.contextId
        val tag = filter.tag
        val dueBefore = filter.dueBefore
        val source = when {
            status != null -> taskDao.observeByStatus(status.toApi())
            projectId != null -> taskDao.observeByProject(projectId)
            else -> taskDao.observeAll()
        }
        return source.map { list ->
            list.map { it.taskEntityToDomain() }
                .filter { task ->
                    (contextId == null || task.contextId == contextId) &&
                        (tag == null || tag in task.tags) &&
                        (dueBefore == null || (task.dueDate != null && task.dueDate!! <= dueBefore))
                }
        }
    }

    override suspend fun listTasks(filter: TaskFilter, page: Page): Outcome<Paged<Task>> {
        val result = executor(mapper = { dto -> dto.map { it.toDomain() } }) {
            tasksApi.list(
                status = filter.status?.toApi(),
                projectId = filter.projectId,
                contextId = filter.contextId,
                dueBefore = filter.dueBefore?.toString(),
                tag = filter.tag,
                limit = page.limit,
                offset = page.offset,
            )
        }
        result.onSuccess { tasks ->
            taskDao.upsertAll(tasks.map { taskFromDomainEntity(it) })
        }
        return result.toPaged(page)
    }

    override suspend fun getTask(id: Uuid): Outcome<Task> {
        val result = executor(mapper = { it.toDomain() }) { tasksApi.get(id) }
        result.onSuccess { taskDao.upsert(taskFromDomainEntity(it)) }
        return result
    }

    override suspend fun getTaskWithRelations(id: Uuid): Outcome<TaskWithRelations> =
        executor(mapper = { it.toDomain() }) { tasksApi.context(id) }

    override suspend fun createTask(draft: TaskDraft): Outcome<Task> {
        val result = executor(mapper = { it.toDomain() }) {
            tasksApi.create(draft.toRequest())
        }
        result.onSuccess { taskDao.upsert(taskFromDomainEntity(it)) }
        return result
    }

    override suspend fun updateTask(id: Uuid, patch: TaskPatch): Outcome<Task> {
        val result = executor(mapper = { it.toDomain() }) {
            tasksApi.patch(id, patch.toRequest())
        }
        result.onSuccess { taskDao.upsert(taskFromDomainEntity(it)) }
        return result
    }

    override suspend fun deleteTask(id: Uuid): Outcome<Unit> {
        val result = executor.unit { tasksApi.delete(id) }
        result.onSuccess { taskDao.deleteById(id) }
        return result
    }

    // --- Projects ----------------------------------------------------------

    override fun observeProjects(status: ProjectStatus?): Flow<List<Project>> =
        projects.map { list -> if (status == null) list else list.filter { it.status == status } }

    override suspend fun listProjects(status: ProjectStatus?, page: Page): Outcome<Paged<Project>> {
        val result = executor(mapper = { dto -> dto.map { it.toDomain() } }) {
            projectsApi.list(status = status?.toApi(), limit = page.limit, offset = page.offset)
        }
        result.onSuccess { projects.value = it }
        return result.toPaged(page)
    }

    override suspend fun getProject(id: Uuid): Outcome<Project> =
        executor(mapper = { it.toDomain() }) { projectsApi.get(id) }

    override suspend fun createProject(title: String, description: String?, status: ProjectStatus?): Outcome<Project> {
        val result = executor(mapper = { it.toDomain() }) {
            projectsApi.create(ProjectRequestDto(title, description, status?.toApi()))
        }
        result.onSuccess { p -> projects.value = listOf(p) + projects.value }
        return result
    }

    override suspend fun updateProject(id: Uuid, title: String, description: String?, status: ProjectStatus?): Outcome<Project> {
        val result = executor(mapper = { it.toDomain() }) {
            projectsApi.update(id, ProjectRequestDto(title, description, status?.toApi()))
        }
        result.onSuccess { updated ->
            projects.value = projects.value.map { if (it.id == id) updated else it }
        }
        return result
    }

    override suspend fun deleteProject(id: Uuid): Outcome<Unit> {
        val result = executor.unit { projectsApi.delete(id) }
        result.onSuccess { projects.value = projects.value.filterNot { it.id == id } }
        return result
    }

    // --- Contexts ----------------------------------------------------------

    override fun observeContexts(): Flow<List<GtdContext>> = contexts.asStateFlow()

    override suspend fun listContexts(): Outcome<List<GtdContext>> {
        val result = executor(mapper = { dto -> dto.map { it.toDomain() } }) {
            contextsApi.list()
        }
        result.onSuccess { contexts.value = it }
        return result
    }

    override suspend fun createContext(name: String): Outcome<GtdContext> {
        val result = executor(mapper = { it.toDomain() }) {
            contextsApi.create(ContextRequestDto(name))
        }
        result.onSuccess { c -> contexts.value = contexts.value + c }
        return result
    }

    override suspend fun deleteContext(id: Uuid): Outcome<Unit> {
        val result = executor.unit { contextsApi.delete(id) }
        result.onSuccess { contexts.value = contexts.value.filterNot { it.id == id } }
        return result
    }

    // --- Weekly review -----------------------------------------------------

    override suspend fun getWeeklyReview(): Outcome<WeeklyReview> =
        executor(mapper = { it.toDomain() }) { reviewsApi.weekly() }

    // ----------------------------------------------------------------------

    private fun taskFromDomainEntity(task: Task) =
        com.app.triflow.data.local.db.entity.TaskEntity(
            id = task.id,
            title = task.title,
            notes = task.notes,
            projectId = task.projectId,
            contextId = task.contextId,
            status = task.status.toApi(),
            energy = task.energy?.toApi(),
            estimatedMinutes = task.estimatedMinutes,
            priority = task.priority,
            dueDate = task.dueDate,
            deferDate = task.deferDate,
            completedAt = task.completedAt,
            tags = task.tags,
            createdAt = task.createdAt,
            updatedAt = task.updatedAt,
        )

    private fun <T> Outcome<List<T>>.toPaged(page: Page): Outcome<Paged<T>> = when (this) {
        is Outcome.Success -> Outcome.Success(
            Paged(items = value, limit = page.limit, offset = page.offset, total = value.size)
        )
        is Outcome.Failure -> this
    }
}
