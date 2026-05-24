package com.app.triflow.domain.usecase.notes

import com.app.triflow.domain.common.Outcome
import com.app.triflow.domain.common.Uuid
import com.app.triflow.domain.model.Note
import com.app.triflow.domain.model.NoteDraft
import com.app.triflow.domain.model.NoteFilter
import com.app.triflow.domain.model.NotePatch
import com.app.triflow.domain.model.NoteRefType
import com.app.triflow.domain.model.PromoteNoteParams
import com.app.triflow.domain.model.Task
import com.app.triflow.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNotesUseCase @Inject constructor(private val repo: NotesRepository) {
    operator fun invoke(filter: NoteFilter = NoteFilter()): Flow<List<Note>> =
        repo.observeNotes(filter)
}

class GetNoteUseCase @Inject constructor(private val repo: NotesRepository) {
    suspend operator fun invoke(id: Uuid): Outcome<Note> = repo.getNote(id)
}

class CreateNoteUseCase @Inject constructor(private val repo: NotesRepository) {
    suspend operator fun invoke(draft: NoteDraft): Outcome<Note> = repo.createNote(draft)
}

class UpdateNoteUseCase @Inject constructor(private val repo: NotesRepository) {
    suspend operator fun invoke(id: Uuid, patch: NotePatch): Outcome<Note> =
        repo.updateNote(id, patch)
}

class DeleteNoteUseCase @Inject constructor(private val repo: NotesRepository) {
    suspend operator fun invoke(id: Uuid): Outcome<Unit> = repo.deleteNote(id)
}

class GetBacklinksUseCase @Inject constructor(private val repo: NotesRepository) {
    suspend operator fun invoke(id: Uuid): Outcome<List<Note>> = repo.getBacklinks(id)
}

class GetLinkedNotesUseCase @Inject constructor(private val repo: NotesRepository) {
    suspend operator fun invoke(id: Uuid): Outcome<List<Note>> = repo.getLinkedNotes(id)
}

class LinkNotesUseCase @Inject constructor(private val repo: NotesRepository) {
    suspend operator fun invoke(sourceId: Uuid, targetId: Uuid): Outcome<Unit> =
        repo.addLink(sourceId, targetId)
}

class UnlinkNotesUseCase @Inject constructor(private val repo: NotesRepository) {
    suspend operator fun invoke(sourceId: Uuid, targetId: Uuid): Outcome<Unit> =
        repo.removeLink(sourceId, targetId)
}

class AddNoteRefUseCase @Inject constructor(private val repo: NotesRepository) {
    suspend operator fun invoke(noteId: Uuid, refType: NoteRefType, refId: Uuid): Outcome<Unit> =
        repo.addRef(noteId, refType, refId)
}

class PromoteNoteToTaskUseCase @Inject constructor(private val repo: NotesRepository) {
    suspend operator fun invoke(id: Uuid, params: PromoteNoteParams = PromoteNoteParams()): Outcome<Task> =
        repo.promoteToTask(id, params)
}
