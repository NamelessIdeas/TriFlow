package com.app.triflow.data.remote.api

import com.app.triflow.core.network.ApiResponse
import com.app.triflow.data.remote.dto.CaptureInboxRequestDto
import com.app.triflow.data.remote.dto.InboxItemDto
import com.app.triflow.data.remote.dto.ProcessInboxRequestDto
import com.app.triflow.data.remote.dto.ProcessInboxResultDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface InboxApi {

    @POST("inbox")
    suspend fun capture(@Body body: CaptureInboxRequestDto): Response<ApiResponse<InboxItemDto>>

    @GET("inbox")
    suspend fun list(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
    ): Response<ApiResponse<List<InboxItemDto>>>

    @POST("inbox/{id}/process")
    suspend fun process(
        @Path("id") id: String,
        @Body body: ProcessInboxRequestDto,
    ): Response<ApiResponse<ProcessInboxResultDto>>
}
