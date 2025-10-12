package com.example.photonest.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.photonest.core.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.DATASTORE_NAME
)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val IS_LOGGED_IN_KEY = booleanPreferencesKey(Constants.PreferenceKeys.IS_LOGGED_IN)
        val USER_ID_KEY = stringPreferencesKey(Constants.PreferenceKeys.USER_ID)
        val THEME_MODE_KEY = stringPreferencesKey(Constants.PreferenceKeys.THEME_MODE)
        val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey(Constants.PreferenceKeys.NOTIFICATIONS_ENABLED)
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN_KEY] ?: false
    }

    val userId: Flow<String> = dataStore.data.map { preferences ->
        preferences[USER_ID_KEY] ?: ""
    }

    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: "system"
    }

    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED_KEY] ?: true
    }

    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN_KEY] = isLoggedIn
        }
    }

    suspend fun setUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun setThemeMode(themeMode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    suspend fun clearUserData() {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN_KEY] = false
            preferences[USER_ID_KEY] = ""
        }
    }
}
