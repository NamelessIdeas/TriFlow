package com.app.triflow.core.network

/** Eccezione che porta con sé il codice del contratto. Usata solo internamente al data layer. */
class ApiException(
    val code: String,
    val httpStatus: Int,
    message: String,
) : RuntimeException(message)
