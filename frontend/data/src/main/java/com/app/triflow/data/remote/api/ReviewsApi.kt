package com.app.triflow.data.remote.api

import com.app.triflow.core.network.ApiResponse
import com.app.triflow.data.remote.dto.WeeklyReviewDto
import retrofit2.Response
import retrofit2.http.GET

interface ReviewsApi {

    @GET("reviews/weekly")
    suspend fun weekly(): Response<ApiResponse<WeeklyReviewDto>>
}
