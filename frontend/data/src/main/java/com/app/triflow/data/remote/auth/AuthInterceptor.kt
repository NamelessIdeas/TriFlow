package com.app.triflow.data.remote.auth

import com.app.triflow.core.security.EncryptedTokenStore
import okhttp3.Interceptor
import okhttp3.Response

/** Aggiunge l'header Bearer alla richiesta se è autenticata. */
class AuthInterceptor(
    private val tokenStore: EncryptedTokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.shouldSkipAuth()) return chain.proceed(request)

        val token = tokenStore.current()?.accessToken
            ?: return chain.proceed(request) // lascia il backend rispondere 401

        val authed = request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(authed)
    }

    private fun okhttp3.Request.shouldSkipAuth(): Boolean {
        val path = url.encodedPath
        return path.contains("/auth/login") ||
            path.contains("/auth/register") ||
            path.contains("/auth/refresh") ||
            path.contains("/auth/logout") ||
            path.contains("/quiz/score") ||
            header("Authorization-Skip") == "true"
    }
}
