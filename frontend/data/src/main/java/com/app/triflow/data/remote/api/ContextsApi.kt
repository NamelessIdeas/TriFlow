package com.app.triflow.data.remote.api

import com.app.triflow.core.network.ApiResponse
import com.app.triflow.data.remote.dto.ContextDto
import com.app.triflow.data.remote.dto.ContextRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ContextsApi {

    @POST("contexts")
    suspend fun create(@Body body: ContextRequestDto): Response<ApiResponse<ContextDto>>

    @GET("contexts")
    suspend fun list(): Response<ApiResponse<List<ContextDto>>>

    @DELETE("contexts/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}
