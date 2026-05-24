package com.app.triflow.data.remote.api

import com.app.triflow.core.network.ApiResponse
import com.app.triflow.data.remote.dto.LoginDataDto
import com.app.triflow.data.remote.dto.LoginRequestDto
import com.app.triflow.data.remote.dto.RefreshRequestDto
import com.app.triflow.data.remote.dto.RegisterRequestDto
import com.app.triflow.data.remote.dto.TokenPairDto
import com.app.triflow.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestDto): Response<ApiResponse<UserDto>>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): Response<ApiResponse<LoginDataDto>>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequestDto): Response<ApiResponse<TokenPairDto>>

    @POST("auth/logout")
    suspend fun logout(@Body body: RefreshRequestDto): Response<Unit>
}
