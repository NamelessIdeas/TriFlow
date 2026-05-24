package com.app.triflow.domain.repository

import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.common.Page
import com.app.triflow.domain.common.Paged
import com.app.triflow.domain.common.Uuid
import com.app.triflow.domain.model.Note
import com.app.triflow.domain.model.NoteDraft
import com.app.triflow.domain.model.NoteFilter
import com.app.triflow.domain.model.NotePatch
import com.app.triflow.domain.model.NoteRefType
import com.app.triflow.domain.model.PromoteNoteParams
import com.app.triflow.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface NotesRepository {

    fun observeNotes(filter: NoteFilter = NoteFilter()): Flow<List<Note>>

    suspend fun listNotes(filter: NoteFilter = NoteFilter(), page: Page = Page()): Outcome<Paged<Note>>

    suspend fun getNote(id: Uuid): Outcome<Note>

    suspend fun createNote(draft: NoteDraft): Outcome<Note>

    suspend fun updateNote(id: Uuid, patch: NotePatch): Outcome<Note>

    suspend fun deleteNote(id: Uuid): Outcome<Unit>

    suspend fun getBacklinks(id: Uuid): Outcome<List<Note>>

    suspend fun getLinkedNotes(id: Uuid): Outcome<List<Note>>

    suspend fun addLink(sourceId: Uuid, targetId: Uuid): Outcome<Unit>

    suspend fun removeLink(sourceId: Uuid, targetId: Uuid): Outcome<Unit>

    suspend fun addRef(noteId: Uuid, refType: NoteRefType, refId: Uuid): Outcome<Unit>

    suspend fun removeRef(noteId: Uuid, refType: NoteRefType, refId: Uuid): Outcome<Unit>

    suspend fun promoteToTask(id: Uuid, params: PromoteNoteParams): Outcome<Task>
}
