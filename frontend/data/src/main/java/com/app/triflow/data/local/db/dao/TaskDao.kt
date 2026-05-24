package com.app.triflow.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.app.triflow.data.local.db.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Upsert
    suspend fun upsert(task: TaskEntity): Long

    @Upsert
    suspend fun upsertAll(tasks: List<TaskEntity>): List<Long>

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM tasks")
    suspend fun clear(): Int

    @Query("SELECT * FROM tasks ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY priority DESC, dueDate ASC")
    fun observeByStatus(status: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY priority DESC, dueDate ASC")
    fun observeByProject(projectId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?
}
