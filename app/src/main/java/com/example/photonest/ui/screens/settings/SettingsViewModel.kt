package com.example.photonest.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            preferencesManager.getThemeMode().collect { themeMode ->
                _uiState.value = _uiState.value.copy(
                    themeMode = when (themeMode) {
                        "light" -> ThemeMode.LIGHT
                        "dark" -> ThemeMode.DARK
                        else -> ThemeMode.SYSTEM
                    }
                )
            }
        }

        viewModelScope.launch {
            preferencesManager.getPushNotificationsEnabled().collect { enabled ->
                _uiState.value = _uiState.value.copy(pushNotificationsEnabled = enabled)
            }
        }

        viewModelScope.launch {
            preferencesManager.getLikeNotificationsEnabled().collect { enabled ->
                _uiState.value = _uiState.value.copy(likeNotificationsEnabled = enabled)
            }
        }

        viewModelScope.launch {
            preferencesManager.getCommentNotificationsEnabled().collect { enabled ->
                _uiState.value = _uiState.value.copy(commentNotificationsEnabled = enabled)
            }
        }

        viewModelScope.launch {
            preferencesManager.getFollowNotificationsEnabled().collect { enabled ->
                _uiState.value = _uiState.value.copy(followNotificationsEnabled = enabled)
            }
        }

        viewModelScope.launch {
            preferencesManager.getPrivateAccount().collect { isPrivate ->
                _uiState.value = _uiState.value.copy(privateAccount = isPrivate)
            }
        }
    }

    fun setTheme(themeMode: ThemeMode) {
        viewModelScope.launch {
            val mode = when (themeMode) {
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
                ThemeMode.SYSTEM -> "system"
            }
            preferencesManager.setThemeMode(mode)
            _uiState.value = _uiState.value.copy(themeMode = themeMode, showThemeDialog = false)
        }
    }

    fun showThemeDialog() {
        _uiState.value = _uiState.value.copy(showThemeDialog = true)
    }

    fun hideThemeDialog() {
        _uiState.value = _uiState.value.copy(showThemeDialog = false)
    }

    fun togglePushNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setPushNotificationsEnabled(enabled)
        }
    }

    fun toggleLikeNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setLikeNotificationsEnabled(enabled)
        }
    }

    fun toggleCommentNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setCommentNotificationsEnabled(enabled)
        }
    }

    fun toggleFollowNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setFollowNotificationsEnabled(enabled)
        }
    }

    fun togglePrivateAccount(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setPrivateAccount(enabled)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            // TODO: Implement cache clearing
            // For now, just show a message that cache has been cleared
        }
    }
}

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showThemeDialog: Boolean = false,
    val pushNotificationsEnabled: Boolean = true,
    val likeNotificationsEnabled: Boolean = true,
    val commentNotificationsEnabled: Boolean = true,
    val followNotificationsEnabled: Boolean = true,
    val privateAccount: Boolean = false
)