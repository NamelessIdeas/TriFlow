package com.app.triflow.data.remote.api

import com.app.triflow.core.network.ApiResponse
import com.app.triflow.data.remote.dto.ActiveTimerDto
import com.app.triflow.data.remote.dto.PomodoroSessionDto
import com.app.triflow.data.remote.dto.PomodoroStartRequestDto
import com.app.triflow.data.remote.dto.PomodoroStatsDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PomodoroApi {

    @POST("pomodoros/start")
    suspend fun start(@Body body: PomodoroStartRequestDto): Response<ApiResponse<ActiveTimerDto>>

    @POST("pomodoros/pause")
    suspend fun pause(): Response<ApiResponse<ActiveTimerDto>>

    @POST("pomodoros/resume")
    suspend fun resume(): Response<ApiResponse<ActiveTimerDto>>

    @GET("pomodoros/current")
    suspend fun current(): Response<ApiResponse<ActiveTimerDto>>

    @POST("pomodoros/complete")
    suspend fun complete(): Response<ApiResponse<PomodoroSessionDto>>

    @POST("pomodoros/abort")
    suspend fun abort(): Response<ApiResponse<PomodoroSessionDto>>

    @GET("pomodoros/sessions")
    suspend fun sessions(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
    ): Response<ApiResponse<List<PomodoroSessionDto>>>

    @GET("pomodoros/stats")
    suspend fun stats(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
    ): Response<ApiResponse<PomodoroStatsDto>>
}
