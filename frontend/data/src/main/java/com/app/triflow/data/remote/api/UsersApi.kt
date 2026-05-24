package com.app.triflow.data.remote.api

import com.app.triflow.core.network.ApiResponse
import com.app.triflow.data.remote.dto.UpdateProfileRequestDto
import com.app.triflow.data.remote.dto.UserDto
import com.app.triflow.data.remote.dto.UserPreferencesDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UsersApi {

    @GET("users/me")
    suspend fun me(): Response<ApiResponse<UserDto>>

    @PUT("users/me")
    suspend fun updateMe(@Body body: UpdateProfileRequestDto): Response<ApiResponse<UserDto>>

    @GET("users/me/preferences")
    suspend fun preferences(): Response<ApiResponse<UserPreferencesDto>>

    @PUT("users/me/preferences")
    suspend fun updatePreferences(@Body body: UserPreferencesDto): Response<ApiResponse<UserPreferencesDto>>
}
