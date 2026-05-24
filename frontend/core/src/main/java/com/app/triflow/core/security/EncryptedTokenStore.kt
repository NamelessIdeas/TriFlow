package com.app.triflow.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.app.triflow.domain.model.AuthSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Instant

/**
 * Storage cifrato per l'[AuthSession] dell'utente.
 *
 * Backed da [EncryptedSharedPreferences] (AES-256 GCM con master key Android Keystore).
 * Espone uno [StateFlow] sincronizzato: ogni `save`/`clear` aggiorna immediatamente
 * gli osservatori senza necessità di ricaricare il file.
 */
class EncryptedTokenStore(context: Context) {

    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context.applicationContext,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private val _session = MutableStateFlow(readFromDisk())
    val session: StateFlow<AuthSession?> = _session.asStateFlow()

    fun current(): AuthSession? = _session.value

    fun save(session: AuthSession) {
        prefs.edit()
            .putString(KEY_ACCESS, session.accessToken)
            .putString(KEY_REFRESH, session.refreshToken)
            .putLong(KEY_EXPIRES_EPOCH, session.accessExpiresAt.epochSeconds)
            .apply()
        _session.value = session
    }

    fun clear() {
        prefs.edit().clear().apply()
        _session.value = null
    }

    private fun readFromDisk(): AuthSession? {
        val access = prefs.getString(KEY_ACCESS, null) ?: return null
        val refresh = prefs.getString(KEY_REFRESH, null) ?: return null
        val epoch = prefs.getLong(KEY_EXPIRES_EPOCH, 0L)
        if (epoch <= 0L) return null
        return AuthSession(
            accessToken = access,
            refreshToken = refresh,
            accessExpiresAt = Instant.fromEpochSeconds(epoch),
        )
    }

    private companion object {
        const val FILE_NAME = "triflow_auth"
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_EXPIRES_EPOCH = "access_expires_epoch"
    }
}
