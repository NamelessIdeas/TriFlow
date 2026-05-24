package com.app.triflow.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val meta: PageMeta? = null,
    val error: ApiError? = null,
)

@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val details: JsonObject? = null,
)

@Serializable
data class PageMeta(
    val limit: Int,
    val offset: Int,
    val total: Int,
)
