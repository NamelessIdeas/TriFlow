package com.app.triflow.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.triflow.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "triflow_settings")

/**
 * Cache locale delle preferenze utente.
 *
 * Funziona offline-first: l'app può leggere l'ultimo valore noto anche senza rete.
 * Il backend resta autorità: ogni `update` da server ricarica qui via [save].
 */
class SettingsStore(private val context: Context) {

    private object Keys {
        val PomodoroDuration = intPreferencesKey("pomodoro_duration_min")
        val ShortBreak = intPreferencesKey("short_break_min")
        val LongBreak = intPreferencesKey("long_break_min")
        val PomodorosUntilLongBreak = intPreferencesKey("pomodoros_until_long_break")
        val Timezone = stringPreferencesKey("timezone")
    }

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val default = UserPreferences.Default
        UserPreferences(
            pomodoroDurationMin = prefs[Keys.PomodoroDuration] ?: default.pomodoroDurationMin,
            shortBreakMin = prefs[Keys.ShortBreak] ?: default.shortBreakMin,
            longBreakMin = prefs[Keys.LongBreak] ?: default.longBreakMin,
            pomodorosUntilLongBreak = prefs[Keys.PomodorosUntilLongBreak] ?: default.pomodorosUntilLongBreak,
            timezone = prefs[Keys.Timezone] ?: default.timezone,
        )
    }

    suspend fun save(preferences: UserPreferences) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PomodoroDuration] = preferences.pomodoroDurationMin
            prefs[Keys.ShortBreak] = preferences.shortBreakMin
            prefs[Keys.LongBreak] = preferences.longBreakMin
            prefs[Keys.PomodorosUntilLongBreak] = preferences.pomodorosUntilLongBreak
            prefs[Keys.Timezone] = preferences.timezone
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
