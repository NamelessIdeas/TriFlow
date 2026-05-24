package com.app.triflow.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.triflow.data.local.db.converters.RoomConverters
import com.app.triflow.data.local.db.dao.NoteDao
import com.app.triflow.data.local.db.dao.TaskDao
import com.app.triflow.data.local.db.entity.NoteEntity
import com.app.triflow.data.local.db.entity.TaskEntity

@Database(
    entities = [TaskEntity::class, NoteEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(RoomConverters::class)
abstract class TriFlowDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
}
