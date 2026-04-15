package com.example.agreementcomms.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val SETTINGS_STORE_NAME = "accordance_settings"

private val Context.dataStore by preferencesDataStore(name = SETTINGS_STORE_NAME)

data class UserSettings(
    val displayName: String = "",
    val statusText: String = "Online",
    val pushEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val compactModeEnabled: Boolean = false
)

class SettingsStore(private val context: Context) {

    private object Keys {
        val displayName = stringPreferencesKey("display_name")
        val statusText = stringPreferencesKey("status_text")
        val pushEnabled = booleanPreferencesKey("push_enabled")
        val vibrationEnabled = booleanPreferencesKey("vibration_enabled")
        val compactModeEnabled = booleanPreferencesKey("compact_mode_enabled")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { ex ->
            if (ex is IOException) emit(emptyPreferences()) else throw ex
        }
        .map { prefs -> prefs.toUserSettings() }

    suspend fun save(settings: UserSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.displayName] = settings.displayName
            prefs[Keys.statusText] = settings.statusText
            prefs[Keys.pushEnabled] = settings.pushEnabled
            prefs[Keys.vibrationEnabled] = settings.vibrationEnabled
            prefs[Keys.compactModeEnabled] = settings.compactModeEnabled
        }
    }

    private fun Preferences.toUserSettings(): UserSettings {
        return UserSettings(
            displayName = this[Keys.displayName].orEmpty(),
            statusText = this[Keys.statusText] ?: "Online",
            pushEnabled = this[Keys.pushEnabled] ?: true,
            vibrationEnabled = this[Keys.vibrationEnabled] ?: true,
            compactModeEnabled = this[Keys.compactModeEnabled] ?: false
        )
    }
}
