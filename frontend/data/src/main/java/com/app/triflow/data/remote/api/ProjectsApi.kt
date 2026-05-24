package com.app.triflow.data.remote.api

import com.app.triflow.core.network.ApiResponse
import com.app.triflow.data.remote.dto.ProjectDto
import com.app.triflow.data.remote.dto.ProjectRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProjectsApi {

    @POST("projects")
    suspend fun create(@Body body: ProjectRequestDto): Response<ApiResponse<ProjectDto>>

    @GET("projects")
    suspend fun list(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
    ): Response<ApiResponse<List<ProjectDto>>>

    @GET("projects/{id}")
    suspend fun get(@Path("id") id: String): Response<ApiResponse<ProjectDto>>

    @PUT("projects/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body body: ProjectRequestDto,
    ): Response<ApiResponse<ProjectDto>>

    @DELETE("projects/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}
