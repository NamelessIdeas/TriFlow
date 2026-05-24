package com.app.triflow.data.remote.api

import com.app.triflow.core.network.ApiResponse
import com.app.triflow.data.remote.dto.DashboardDto
import retrofit2.Response
import retrofit2.http.GET

interface DashboardApi {

    @GET("dashboard")
    suspend fun get(): Response<ApiResponse<DashboardDto>>
}
