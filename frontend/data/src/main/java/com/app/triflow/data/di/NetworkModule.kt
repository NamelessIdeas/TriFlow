package com.app.triflow.data.di

import com.app.triflow.data.BuildConfig
import com.app.triflow.core.security.EncryptedTokenStore
import com.app.triflow.data.remote.api.AuthApi
import com.app.triflow.data.remote.api.ContextsApi
import com.app.triflow.data.remote.api.DashboardApi
import com.app.triflow.data.remote.api.InboxApi
import com.app.triflow.data.remote.api.NotesApi
import com.app.triflow.data.remote.api.PomodoroApi
import com.app.triflow.data.remote.api.ProjectsApi
import com.app.triflow.data.remote.api.QuizApi
import com.app.triflow.data.remote.api.ReviewsApi
import com.app.triflow.data.remote.api.TasksApi
import com.app.triflow.data.remote.api.UsersApi
import com.app.triflow.data.remote.auth.AuthInterceptor
import com.app.triflow.data.remote.auth.TokenAuthenticator
import com.app.triflow.data.remote.auth.TokenRefresher
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RefreshClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RefreshRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL_KEY = "BASE_URL"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        explicitNulls = false
        coerceInputValues = true
        @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
        namingStrategy = JsonNamingStrategy.SnakeCase
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.ENABLE_HTTP_LOGGING) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenStore: EncryptedTokenStore): AuthInterceptor =
        AuthInterceptor(tokenStore)

    // OkHttp dedicato al refresh: NIENTE Authenticator (evita cicli).
    @Provides
    @Singleton
    @RefreshClient
    fun provideRefreshClient(logging: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

    @Provides
    @Singleton
    @RefreshRetrofit
    fun provideRefreshRetrofit(
        @RefreshClient client: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideTokenRefresher(@RefreshRetrofit retrofit: Retrofit): TokenRefresher =
        retrofit.create(TokenRefresher::class.java)

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        tokenStore: EncryptedTokenStore,
        refresher: TokenRefresher,
    ): TokenAuthenticator = TokenAuthenticator(tokenStore, refresher)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        logging: HttpLoggingInterceptor,
        authenticator: TokenAuthenticator,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .authenticator(authenticator)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides @Singleton fun provideAuthApi(r: Retrofit): AuthApi = r.create(AuthApi::class.java)
    @Provides @Singleton fun provideUsersApi(r: Retrofit): UsersApi = r.create(UsersApi::class.java)
    @Provides @Singleton fun provideInboxApi(r: Retrofit): InboxApi = r.create(InboxApi::class.java)
    @Provides @Singleton fun provideTasksApi(r: Retrofit): TasksApi = r.create(TasksApi::class.java)
    @Provides @Singleton fun provideProjectsApi(r: Retrofit): ProjectsApi = r.create(ProjectsApi::class.java)
    @Provides @Singleton fun provideContextsApi(r: Retrofit): ContextsApi = r.create(ContextsApi::class.java)
    @Provides @Singleton fun provideReviewsApi(r: Retrofit): ReviewsApi = r.create(ReviewsApi::class.java)
    @Provides @Singleton fun providePomodoroApi(r: Retrofit): PomodoroApi = r.create(PomodoroApi::class.java)
    @Provides @Singleton fun provideNotesApi(r: Retrofit): NotesApi = r.create(NotesApi::class.java)
    @Provides @Singleton fun provideDashboardApi(r: Retrofit): DashboardApi = r.create(DashboardApi::class.java)
    @Provides @Singleton fun provideQuizApi(r: Retrofit): QuizApi = r.create(QuizApi::class.java)
}
