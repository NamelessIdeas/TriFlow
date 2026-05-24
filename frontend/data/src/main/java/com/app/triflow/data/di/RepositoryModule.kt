package com.app.triflow.data.di

import com.app.triflow.data.repository.AuthRepositoryImpl
import com.app.triflow.data.repository.DashboardRepositoryImpl
import com.app.triflow.data.repository.GtdRepositoryImpl
import com.app.triflow.data.repository.NotesRepositoryImpl
import com.app.triflow.data.repository.PomodoroRepositoryImpl
import com.app.triflow.data.repository.QuizRepositoryImpl
import com.app.triflow.data.repository.UserRepositoryImpl
import com.app.triflow.domain.repository.AuthRepository
import com.app.triflow.domain.repository.DashboardRepository
import com.app.triflow.domain.repository.GtdRepository
import com.app.triflow.domain.repository.NotesRepository
import com.app.triflow.domain.repository.PomodoroRepository
import com.app.triflow.domain.repository.QuizRepository
import com.app.triflow.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton abstract fun bindAuth(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindUser(impl: UserRepositoryImpl): UserRepository
    @Binds @Singleton abstract fun bindGtd(impl: GtdRepositoryImpl): GtdRepository
    @Binds @Singleton abstract fun bindPomodoro(impl: PomodoroRepositoryImpl): PomodoroRepository
    @Binds @Singleton abstract fun bindNotes(impl: NotesRepositoryImpl): NotesRepository
    @Binds @Singleton abstract fun bindDashboard(impl: DashboardRepositoryImpl): DashboardRepository
    @Binds @Singleton abstract fun bindQuiz(impl: QuizRepositoryImpl): QuizRepository
}
