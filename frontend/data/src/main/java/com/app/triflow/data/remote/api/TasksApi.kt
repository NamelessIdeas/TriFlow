package com.app.triflow.data.remote.api

import com.app.triflow.core.network.ApiResponse
import com.app.triflow.data.remote.dto.TaskContextDto
import com.app.triflow.data.remote.dto.TaskDto
import com.app.triflow.data.remote.dto.TaskRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TasksApi {

    @POST("tasks")
    suspend fun create(@Body body: TaskRequestDto): Response<ApiResponse<TaskDto>>

    @GET("tasks")
    suspend fun list(
        @Query("status") status: String? = null,
        @Query("project_id") projectId: String? = null,
        @Query("context_id") contextId: String? = null,
        @Query("due_before") dueBefore: String? = null,
        @Query("tag") tag: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
    ): Response<ApiResponse<List<TaskDto>>>

    @GET("tasks/{id}")
    suspend fun get(@Path("id") id: String): Response<ApiResponse<TaskDto>>

    @PATCH("tasks/{id}")
    suspend fun patch(
        @Path("id") id: String,
        @Body body: TaskRequestDto,
    ): Response<ApiResponse<TaskDto>>

    @DELETE("tasks/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>

    @GET("tasks/{id}/context")
    suspend fun context(@Path("id") id: String): Response<ApiResponse<TaskContextDto>>
}
