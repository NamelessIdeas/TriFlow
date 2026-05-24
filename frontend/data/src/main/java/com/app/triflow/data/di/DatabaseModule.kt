package com.app.triflow.data.di

import android.content.Context
import androidx.room.Room
import com.app.triflow.data.local.db.TriFlowDatabase
import com.app.triflow.data.local.db.dao.NoteDao
import com.app.triflow.data.local.db.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TriFlowDatabase =
        Room.databaseBuilder(
            context,
            TriFlowDatabase::class.java,
            "triflow.db",
        ).build()

    @Provides
    @Singleton
    fun provideTaskDao(db: TriFlowDatabase): TaskDao = db.taskDao()

    @Provides
    @Singleton
    fun provideNoteDao(db: TriFlowDatabase): NoteDao = db.noteDao()
}
