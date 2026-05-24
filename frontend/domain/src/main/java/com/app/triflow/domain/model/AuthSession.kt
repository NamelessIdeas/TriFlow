package com.app.triflow.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val accessExpiresAt: Instant,
) {
    fun isExpired(now: Instant = Clock.System.now()): Boolean = now >= accessExpiresAt

    fun isAboutToExpire(thresholdSeconds: Long = 30, now: Instant = Clock.System.now()): Boolean {
        val diff = accessExpiresAt.epochSeconds - now.epochSeconds
        return diff <= thresholdSeconds
    }
}
