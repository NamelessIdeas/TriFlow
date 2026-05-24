package com.app.triflow.data.di

import android.content.Context
import com.app.triflow.core.security.EncryptedTokenStore
import com.app.triflow.data.local.datastore.SettingsStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideTokenStore(@ApplicationContext context: Context): EncryptedTokenStore =
        EncryptedTokenStore(context)

    @Provides
    @Singleton
    fun provideSettingsStore(@ApplicationContext context: Context): SettingsStore =
        SettingsStore(context)
}
