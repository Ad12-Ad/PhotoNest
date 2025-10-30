package com.example.photonest.ui.screens.notification

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.Resource
import com.example.photonest.data.model.Notification
import com.example.photonest.domain.repository.INotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: INotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("NotificationVM", "Loading notifications...")

            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = true) }
            }

            notificationRepository.getUserNotifications().collect { result ->
                withContext(Dispatchers.Main) {
                    when (result) {
                        is Resource.Success -> {
                            Log.d("NotificationVM", "Loaded ${result.data?.size ?: 0} notifications")
                            _uiState.update {
                                it.copy(
                                    notifications = result.data ?: emptyList(),
                                    isLoading = false,
                                    error = null
                                )
                            }
                        }
                        is Resource.Error -> {
                            Log.e("NotificationVM", "Error loading notifications: ${result.message}")
                            _uiState.update {
                                it.copy(
                                    error = result.message,
                                    isLoading = false
                                )
                            }
                        }
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("NotificationVM", "Marking notification $notificationId as read")

            withContext(Dispatchers.Main) {
                _uiState.update { currentState ->
                    currentState.copy(
                        notifications = currentState.notifications.map { notification ->
                            if (notification.id == notificationId) {
                                notification.copy(isRead = true)
                            } else {
                                notification
                            }
                        }
                    )
                }
            }

            val result = notificationRepository.markNotificationAsRead(notificationId)

            withContext(Dispatchers.Main) {
                when (result) {
                    is Resource.Success -> {
                        Log.d("NotificationVM", "Successfully marked as read")
                    }
                    is Resource.Error -> {
                        Log.e("NotificationVM", "Failed to mark as read: ${result.message}")
                        _uiState.update { currentState ->
                            currentState.copy(
                                notifications = currentState.notifications.map { notification ->
                                    if (notification.id == notificationId) {
                                        notification.copy(isRead = false)
                                    } else {
                                        notification
                                    }
                                },
                                error = result.message
                            )
                        }
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("NotificationVM", "Marking all notifications as read")

            withContext(Dispatchers.Main) {
                _uiState.update { currentState ->
                    currentState.copy(
                        notifications = currentState.notifications.map { it.copy(isRead = true) }
                    )
                }
            }

            val result = notificationRepository.markAllNotificationsAsRead()

            withContext(Dispatchers.Main) {
                when (result) {
                    is Resource.Success -> {
                        Log.d("NotificationVM", "Successfully marked all as read")
                    }
                    is Resource.Error -> {
                        Log.e("NotificationVM", "Failed to mark all as read: ${result.message}")
                        _uiState.update {
                            it.copy(error = result.message)
                        }
                        loadNotifications()
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val deletedNotification = _uiState.value.notifications.find { it.id == notificationId }

            withContext(Dispatchers.Main) {
                _uiState.update { currentState ->
                    currentState.copy(
                        notifications = currentState.notifications.filter { it.id != notificationId }
                    )
                }
            }

            val result = notificationRepository.deleteNotification(notificationId)

            withContext(Dispatchers.Main) {
                when (result) {
                    is Resource.Success -> {
                        Log.d("NotificationVM", "Notification deleted")
                    }
                    is Resource.Error -> {
                        if (deletedNotification != null) {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    notifications = (currentState.notifications + deletedNotification)
                                        .sortedByDescending { it.timestamp },
                                    error = result.message
                                )
                            }
                        }
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null, showErrorDialog = false) }
    }

    fun getUnreadCount(): Int {
        return _uiState.value.notifications.count { !it.isRead }
    }
}

data class NotificationUiState(
    val isLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val error: String? = null,
    val showErrorDialog: Boolean = false
)