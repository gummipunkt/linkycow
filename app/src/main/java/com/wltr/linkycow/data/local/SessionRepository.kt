package com.wltr.linkycow.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore instance - single source of truth for session data
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

/**
 * Repository for managing user session data using DataStore.
 * Handles authentication state persistence across app launches.
 */
class SessionRepository(private val context: Context) {

    /**
     * Preference keys for secure session storage
     */
    private object PreferencesKeys {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val INSTANCE_URL = stringPreferencesKey("instance_url")
        val USERNAME = stringPreferencesKey("username")
    }

    /**
     * Flow of authentication token - empty string means not authenticated
     */
    val authTokenFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.AUTH_TOKEN] ?: ""
        }

    /**
     * Flow of Linkwarden server URL
     */
    val instanceUrlFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.INSTANCE_URL] ?: ""
        }

    /**
     * Flow of logged-in username
     */
    val usernameFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USERNAME] ?: ""
        }

    /**
     * Save minimal session data (for quick auth)
     */
    suspend fun saveSession(token: String, url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTH_TOKEN] = token
            preferences[PreferencesKeys.INSTANCE_URL] = url
        }
    }

    /**
     * Save complete session data (typically after login)
     * @param instanceUrl Server URL
     * @param username User's login name
     * @param password User's password (not stored for security)
     * @param token Authentication token from server
     */
    suspend fun saveSession(instanceUrl: String, username: String, password: String, token: String = "") {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INSTANCE_URL] = instanceUrl
            preferences[PreferencesKeys.USERNAME] = username
            // Only save token if provided (successful auth)
            if (token.isNotEmpty()) {
                preferences[PreferencesKeys.AUTH_TOKEN] = token
            }
        }
    }

    /**
     * Clear all session data on logout
     */
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.AUTH_TOKEN)
            preferences.remove(PreferencesKeys.INSTANCE_URL)
            preferences.remove(PreferencesKeys.USERNAME)
        }
    }
} 