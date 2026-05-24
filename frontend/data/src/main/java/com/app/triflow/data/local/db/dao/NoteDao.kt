package com.app.triflow.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.app.triflow.data.local.db.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Upsert
    suspend fun upsert(note: NoteEntity): Long

    @Upsert
    suspend fun upsertAll(notes: List<NoteEntity>): List<Long>

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM notes")
    suspend fun clear(): Int

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE paraCategory = :category ORDER BY updatedAt DESC")
    fun observeByCategory(category: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: String): NoteEntity?
}
