package com.app.triflow.data.remote.api

import com.app.triflow.core.network.ApiResponse
import com.app.triflow.data.remote.dto.NoteDto
import com.app.triflow.data.remote.dto.NoteLinkRequestDto
import com.app.triflow.data.remote.dto.NoteRefRequestDto
import com.app.triflow.data.remote.dto.NoteRequestDto
import com.app.triflow.data.remote.dto.PromoteNoteRequestDto
import com.app.triflow.data.remote.dto.TaskDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotesApi {

    @POST("notes")
    suspend fun create(@Body body: NoteRequestDto): Response<ApiResponse<NoteDto>>

    @GET("notes")
    suspend fun list(
        @Query("para_category") paraCategory: String? = null,
        @Query("tag") tag: String? = null,
        @Query("q") query: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
    ): Response<ApiResponse<List<NoteDto>>>

    @GET("notes/{id}")
    suspend fun get(@Path("id") id: String): Response<ApiResponse<NoteDto>>

    @PATCH("notes/{id}")
    suspend fun patch(
        @Path("id") id: String,
        @Body body: NoteRequestDto,
    ): Response<ApiResponse<NoteDto>>

    @DELETE("notes/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>

    @GET("notes/{id}/backlinks")
    suspend fun backlinks(@Path("id") id: String): Response<ApiResponse<List<NoteDto>>>

    @GET("notes/{id}/links")
    suspend fun links(@Path("id") id: String): Response<ApiResponse<List<NoteDto>>>

    @POST("notes/{id}/links")
    suspend fun addLink(
        @Path("id") id: String,
        @Body body: NoteLinkRequestDto,
    ): Response<Unit>

    @DELETE("notes/{id}/links/{targetId}")
    suspend fun removeLink(
        @Path("id") id: String,
        @Path("targetId") targetId: String,
    ): Response<Unit>

    @POST("notes/{id}/refs")
    suspend fun addRef(
        @Path("id") id: String,
        @Body body: NoteRefRequestDto,
    ): Response<Unit>

    @DELETE("notes/{id}/refs/{refType}/{refId}")
    suspend fun removeRef(
        @Path("id") id: String,
        @Path("refType") refType: String,
        @Path("refId") refId: String,
    ): Response<Unit>

    @POST("notes/{id}/promote-to-task")
    suspend fun promoteToTask(
        @Path("id") id: String,
        @Body body: PromoteNoteRequestDto,
    ): Response<ApiResponse<TaskDto>>
}
