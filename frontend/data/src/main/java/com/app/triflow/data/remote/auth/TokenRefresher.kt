package com.app.triflow.data.remote.auth

import com.app.triflow.core.network.ApiResponse
import com.app.triflow.data.remote.dto.RefreshRequestDto
import com.app.triflow.data.remote.dto.TokenPairDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API minimale dedicata al refresh, deliberatamente separata da [AuthApi]
 * per essere costruita su un Retrofit che NON monta il [TokenAuthenticator]
 * (così evitiamo cicli durante il rinnovo).
 */
interface TokenRefresher {

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequestDto): Response<ApiResponse<TokenPairDto>>
}
