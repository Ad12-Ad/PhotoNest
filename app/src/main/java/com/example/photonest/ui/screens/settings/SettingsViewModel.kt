package com.example.photonest.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        viewModelScope.launch(Dispatchers.IO) {
            preferencesManager.getThemeMode().collect { themeMode ->
                withContext(Dispatchers.Main){
                    _uiState.value = _uiState.value.copy(
                        themeMode = when (themeMode) {
                            "light" -> ThemeMode.LIGHT
                            "dark" -> ThemeMode.DARK
                            else -> ThemeMode.SYSTEM
                        }
                    )
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            preferencesManager.getPushNotificationsEnabled().collect { enabled ->
                withContext(Dispatchers.Main){
                    _uiState.value = _uiState.value.copy(pushNotificationsEnabled = enabled)
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            preferencesManager.getLikeNotificationsEnabled().collect { enabled ->
                withContext(Dispatchers.Main){
                    _uiState.value = _uiState.value.copy(likeNotificationsEnabled = enabled)
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            preferencesManager.getCommentNotificationsEnabled().collect { enabled ->
                withContext(Dispatchers.Main){
                    _uiState.value = _uiState.value.copy(commentNotificationsEnabled = enabled)
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            preferencesManager.getFollowNotificationsEnabled().collect { enabled ->
                withContext(Dispatchers.Main){
                    _uiState.value = _uiState.value.copy(followNotificationsEnabled = enabled)
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            preferencesManager.getPrivateAccount().collect { isPrivate ->
                withContext(Dispatchers.Main){
                    _uiState.value = _uiState.value.copy(privateAccount = isPrivate)
                }
            }
        }
    }

    fun setTheme(themeMode: ThemeMode) {
        viewModelScope.launch(Dispatchers.IO) {
            val mode = when (themeMode) {
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
                ThemeMode.SYSTEM -> "system"
            }
            preferencesManager.setThemeMode(mode)

            withContext(Dispatchers.Main){
                _uiState.value = _uiState.value.copy(themeMode = themeMode, showThemeDialog = false)
            }
        }
    }

    fun showThemeDialog() {
        _uiState.value = _uiState.value.copy(showThemeDialog = true)
    }

    fun hideThemeDialog() {
        _uiState.value = _uiState.value.copy(showThemeDialog = false)
    }

    fun togglePushNotifications(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesManager.setPushNotificationsEnabled(enabled)
        }
    }

    fun toggleLikeNotifications(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesManager.setLikeNotificationsEnabled(enabled)
        }
    }

    fun toggleCommentNotifications(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesManager.setCommentNotificationsEnabled(enabled)
        }
    }

    fun toggleFollowNotifications(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesManager.setFollowNotificationsEnabled(enabled)
        }
    }

    fun togglePrivateAccount(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesManager.setPrivateAccount(enabled)
        }
    }

    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
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