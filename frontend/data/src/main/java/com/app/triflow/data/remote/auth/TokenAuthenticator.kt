package com.app.triflow.data.remote.auth

import com.app.triflow.core.security.EncryptedTokenStore
import com.app.triflow.data.remote.dto.RefreshRequestDto
import com.app.triflow.data.remote.mapper.toDomain
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Su risposta 401 prova un refresh transparente. Se ha successo aggiorna il
 * [EncryptedTokenStore] e ritenta la stessa request col nuovo access token.
 *
 * Niente loop infinito: dopo `MAX_RETRY` tentativi rinuncia (e cancella la sessione).
 */
class TokenAuthenticator(
    private val tokenStore: EncryptedTokenStore,
    private val refresher: TokenRefresher,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.responseCount() >= MAX_RETRY) return null

        val current = tokenStore.current() ?: return null
        val newSession = runBlocking {
            runCatching {
                val resp = refresher.refresh(RefreshRequestDto(current.refreshToken))
                if (resp.isSuccessful) resp.body()?.data?.toDomain() else null
            }.getOrNull()
        }
        if (newSession == null) {
            tokenStore.clear()
            return null
        }
        tokenStore.save(newSession)
        return response.request.newBuilder()
            .header("Authorization", "Bearer ${newSession.accessToken}")
            .build()
    }

    private fun Response.responseCount(): Int {
        var count = 1
        var prior = priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private companion object {
        const val MAX_RETRY = 2
    }
}
