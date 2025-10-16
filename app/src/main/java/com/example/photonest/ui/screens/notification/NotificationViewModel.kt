package com.example.photonest.ui.screens.notification

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.Resource
import com.example.photonest.data.model.Notification
import com.example.photonest.domain.repository.INotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: INotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    fun loadNotifications() {
        viewModelScope.launch {
            Log.d("NotificationVM", "====== VM: Loading notifications ======")

            notificationRepository.getUserNotifications().collect { result ->
                Log.d("NotificationVM", "VM received result: ${result::class.simpleName}")

                when (result) {
                    is Resource.Success -> {
                        val notifications = result.data ?: emptyList()
                        Log.d("NotificationVM", "✅ VM: Success with ${notifications.size} notifications")
                        notifications.forEachIndexed { index, notif ->
                            Log.d("NotificationVM", "  [$index] ${notif.fromUsername}: ${notif.message}")
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                notifications = notifications,
                                error = null
                            )
                        }
                        Log.d("NotificationVM", "UI State updated - notifications count: ${_uiState.value.notifications.size}")
                    }

                    is Resource.Error -> {
                        Log.e("NotificationVM", "VM: Error - ${result.message}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                notifications = emptyList(),
                                error = result.message
                            )
                        }
                    }

                    is Resource.Loading -> {
                        Log.d("NotificationVM", "⏳ VM: Loading...")
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }


    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markNotificationAsRead(notificationId)
            // Update local state
            _uiState.update { state ->
                state.copy(
                    notifications = state.notifications.map { notification ->
                        if (notification.id == notificationId) {
                            notification.copy(isRead = true)
                        } else {
                            notification
                        }
                    }
                )
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllNotificationsAsRead()
            // Update local state
            _uiState.update { state ->
                state.copy(
                    notifications = state.notifications.map { notification ->
                        notification.copy(isRead = true)
                    }
                )
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null, showErrorDialog = false) }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
            _uiState.update { state ->
                state.copy(
                    notifications = state.notifications.filter { it.id != notificationId }
                )
            }
        }
    }
}

data class NotificationUiState(
    val isLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val error: String? = null,
    val showErrorDialog: Boolean = false
)