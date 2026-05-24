package com.app.triflow.data.remote.api

import com.app.triflow.core.network.ApiResponse
import com.app.triflow.data.remote.dto.QuizAnswersDto
import com.app.triflow.data.remote.dto.QuizResultDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface QuizApi {

    @POST("quiz/score")
    suspend fun score(@Body body: QuizAnswersDto): Response<ApiResponse<QuizResultDto>>
}
