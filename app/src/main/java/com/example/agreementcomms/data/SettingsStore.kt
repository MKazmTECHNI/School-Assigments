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
    val token: String = "",
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val statusText: String = "Online",
    val bio: String = "",
    val avatarUrl: String = "",
    val pushEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val compactModeEnabled: Boolean = false
)

class SettingsStore(private val context: Context) {

    private object Keys {
        val token = stringPreferencesKey("auth_token")
        val userId = stringPreferencesKey("user_id")
        val username = stringPreferencesKey("username")
        val displayName = stringPreferencesKey("display_name")
        val statusText = stringPreferencesKey("status_text")
        val bio = stringPreferencesKey("bio")
        val avatarUrl = stringPreferencesKey("avatar_url")
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
            prefs[Keys.token] = settings.token
            prefs[Keys.userId] = settings.userId
            prefs[Keys.username] = settings.username
            prefs[Keys.displayName] = settings.displayName
            prefs[Keys.statusText] = settings.statusText
            prefs[Keys.bio] = settings.bio
            prefs[Keys.avatarUrl] = settings.avatarUrl
            prefs[Keys.pushEnabled] = settings.pushEnabled
            prefs[Keys.vibrationEnabled] = settings.vibrationEnabled
            prefs[Keys.compactModeEnabled] = settings.compactModeEnabled
        }
    }

    suspend fun clearAuth() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.token)
            prefs.remove(Keys.userId)
            prefs.remove(Keys.username)
        }
    }

    private fun Preferences.toUserSettings(): UserSettings {
        return UserSettings(
            token = this[Keys.token].orEmpty(),
            userId = this[Keys.userId].orEmpty(),
            username = this[Keys.username].orEmpty(),
            displayName = this[Keys.displayName].orEmpty(),
            statusText = this[Keys.statusText] ?: "Online",
            bio = this[Keys.bio].orEmpty(),
            avatarUrl = this[Keys.avatarUrl].orEmpty(),
            pushEnabled = this[Keys.pushEnabled] ?: true,
            vibrationEnabled = this[Keys.vibrationEnabled] ?: true,
            compactModeEnabled = this[Keys.compactModeEnabled] ?: false
        )
    }
}
