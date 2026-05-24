package com.app.triflow.data.repository

import com.app.triflow.core.network.ApiCallExecutor
import com.app.triflow.data.local.db.dao.NoteDao
import com.app.triflow.data.local.db.entity.NoteEntity
import com.app.triflow.data.local.db.mapper.toDomain as noteEntityToDomain
import com.app.triflow.data.remote.api.NotesApi
import com.app.triflow.data.remote.dto.NoteLinkRequestDto
import com.app.triflow.data.remote.dto.NoteRefRequestDto
import com.app.triflow.data.remote.mapper.toApi
import com.app.triflow.data.remote.mapper.toDomain
import com.app.triflow.data.remote.mapper.toRequest
import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.common.Page
import com.app.triflow.domain.common.Paged
import com.app.triflow.domain.common.Uuid
import com.app.triflow.domain.common.onSuccess
import com.app.triflow.domain.model.Note
import com.app.triflow.domain.model.NoteDraft
import com.app.triflow.domain.model.NoteFilter
import com.app.triflow.domain.model.NotePatch
import com.app.triflow.domain.model.NoteRefType
import com.app.triflow.domain.model.PromoteNoteParams
import com.app.triflow.domain.model.Task
import com.app.triflow.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesRepositoryImpl @Inject constructor(
    private val api: NotesApi,
    private val dao: NoteDao,
    private val executor: ApiCallExecutor,
) : NotesRepository {

    override fun observeNotes(filter: NoteFilter): Flow<List<Note>> {
        val para = filter.paraCategory
        val tag = filter.tag
        val query = filter.query
        val source = if (para != null) {
            dao.observeByCategory(para.toApi())
        } else {
            dao.observeAll()
        }
        return source.map { list ->
            list.map { it.noteEntityToDomain() }
                .filter { note ->
                    (tag == null || tag in note.tags) &&
                        (query.isNullOrBlank() ||
                            note.title.contains(query, ignoreCase = true) ||
                            note.contentMd.contains(query, ignoreCase = true))
                }
        }
    }

    override suspend fun listNotes(filter: NoteFilter, page: Page): Outcome<Paged<Note>> {
        val result = executor(mapper = { dto -> dto.map { it.toDomain() } }) {
            api.list(
                paraCategory = filter.paraCategory?.toApi(),
                tag = filter.tag,
                query = filter.query,
                limit = page.limit,
                offset = page.offset,
            )
        }
        result.onSuccess { notes ->
            dao.upsertAll(notes.map { noteToEntity(it) })
        }
        return when (result) {
            is Outcome.Success -> Outcome.Success(
                Paged(items = result.value, limit = page.limit, offset = page.offset, total = result.value.size)
            )
            is Outcome.Failure -> result
        }
    }

    override suspend fun getNote(id: Uuid): Outcome<Note> {
        val result = executor(mapper = { it.toDomain() }) { api.get(id) }
        result.onSuccess { dao.upsert(noteToEntity(it)) }
        return result
    }

    override suspend fun createNote(draft: NoteDraft): Outcome<Note> {
        val result = executor(mapper = { it.toDomain() }) { api.create(draft.toRequest()) }
        result.onSuccess { dao.upsert(noteToEntity(it)) }
        return result
    }

    override suspend fun updateNote(id: Uuid, patch: NotePatch): Outcome<Note> {
        val result = executor(mapper = { it.toDomain() }) { api.patch(id, patch.toRequest()) }
        result.onSuccess { dao.upsert(noteToEntity(it)) }
        return result
    }

    override suspend fun deleteNote(id: Uuid): Outcome<Unit> {
        val result = executor.unit { api.delete(id) }
        result.onSuccess { dao.deleteById(id) }
        return result
    }

    override suspend fun getBacklinks(id: Uuid): Outcome<List<Note>> =
        executor(mapper = { dto -> dto.map { it.toDomain() } }) { api.backlinks(id) }

    override suspend fun getLinkedNotes(id: Uuid): Outcome<List<Note>> =
        executor(mapper = { dto -> dto.map { it.toDomain() } }) { api.links(id) }

    override suspend fun addLink(sourceId: Uuid, targetId: Uuid): Outcome<Unit> =
        executor.unit { api.addLink(sourceId, NoteLinkRequestDto(targetId)) }

    override suspend fun removeLink(sourceId: Uuid, targetId: Uuid): Outcome<Unit> =
        executor.unit { api.removeLink(sourceId, targetId) }

    override suspend fun addRef(noteId: Uuid, refType: NoteRefType, refId: Uuid): Outcome<Unit> =
        executor.unit { api.addRef(noteId, NoteRefRequestDto(refType.toApi(), refId)) }

    override suspend fun removeRef(noteId: Uuid, refType: NoteRefType, refId: Uuid): Outcome<Unit> =
        executor.unit { api.removeRef(noteId, refType.toApi(), refId) }

    override suspend fun promoteToTask(id: Uuid, params: PromoteNoteParams): Outcome<Task> =
        executor(mapper = { it.toDomain() }) {
            api.promoteToTask(id, params.toRequest())
        }

    private fun noteToEntity(note: Note): NoteEntity = NoteEntity(
        id = note.id,
        title = note.title,
        contentMd = note.contentMd,
        paraCategory = note.paraCategory?.toApi(),
        tags = note.tags,
        createdAt = note.createdAt,
        updatedAt = note.updatedAt,
    )
}
