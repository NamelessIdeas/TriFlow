package com.app.triflow.core.network

import com.app.triflow.domain.common.DomainError
import com.app.triflow.domain.common.Outcome
import kotlinx.serialization.json.Json
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralizza l'esecuzione di una chiamata Retrofit che ritorna [ApiResponse],
 * traducendo qualsiasi esito in [Outcome].
 *
 *  - response.isSuccessful + body.success + body.data non null → Success(mapper(data))
 *  - response.isSuccessful + body.success ma data null (es. 204) → Success(unit-mapper)
 *  - response.isSuccessful = false → parse errorBody come ApiResponse e mappa l'error code
 *  - eccezioni di rete/HTTP → toDomainError()
 */
@Singleton
class ApiCallExecutor @Inject constructor(
    private val json: Json,
) {

    suspend operator fun <Dto, Domain> invoke(
        mapper: (Dto) -> Domain,
        call: suspend () -> Response<ApiResponse<Dto>>,
    ): Outcome<Domain> = runCatching {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            when {
                body == null -> Outcome.Failure(DomainError.Unknown("Empty body"))
                body.success && body.data != null -> Outcome.Success(mapper(body.data))
                body.success && body.data == null -> Outcome.Failure(DomainError.Unknown("Missing data"))
                else -> Outcome.Failure(body.toException(response.code()).toDomainError())
            }
        } else {
            Outcome.Failure(response.parseError().toDomainError())
        }
    }.getOrElse { Outcome.Failure(it.toDomainError()) }

    /** Variante per chiamate che ritornano 204/No Content (nessun body). */
    suspend fun unit(call: suspend () -> Response<Unit>): Outcome<Unit> = runCatching {
        val response = call()
        if (response.isSuccessful) {
            Outcome.Success(Unit)
        } else {
            Outcome.Failure(response.parseError().toDomainError())
        }
    }.getOrElse { Outcome.Failure(it.toDomainError()) }

    /**
     * Variante per quando il payload "data" non interessa nemmeno (es. logout).
     * Tollera anche envelope con error sul successful path.
     */
    suspend fun ignored(call: suspend () -> Response<ApiResponse<kotlinx.serialization.json.JsonElement>>): Outcome<Unit> =
        invoke(mapper = { Unit }, call = call)

    private fun ApiResponse<*>.toException(httpStatus: Int): ApiException {
        val err = error
        return if (err != null) {
            ApiException(err.code, httpStatus, err.message)
        } else {
            ApiException(ApiErrorCode.INTERNAL_ERROR, httpStatus, "Unknown error")
        }
    }

    private fun <T> Response<T>.parseError(): ApiException {
        val raw = errorBody()?.string()
        if (!raw.isNullOrBlank()) {
            runCatching {
                json.decodeFromString<ApiResponse<kotlinx.serialization.json.JsonElement>>(raw)
            }.getOrNull()?.let { envelope ->
                envelope.error?.let { return ApiException(it.code, code(), it.message) }
            }
        }
        val fallbackCode = when (code()) {
            401 -> ApiErrorCode.UNAUTHORIZED
            403 -> ApiErrorCode.FORBIDDEN
            404 -> ApiErrorCode.NOT_FOUND
            409 -> ApiErrorCode.CONFLICT
            429 -> ApiErrorCode.RATE_LIMITED
            in 500..599 -> ApiErrorCode.INTERNAL_ERROR
            else -> ApiErrorCode.INVALID_INPUT
        }
        return ApiException(fallbackCode, code(), message().orEmpty().ifBlank { "HTTP ${code()}" })
    }
}
