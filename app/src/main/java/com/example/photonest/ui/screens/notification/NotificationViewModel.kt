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

    fun loadNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("NotificationVM", "====== VM: Loading notifications ======")

            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = true) }
            }

            notificationRepository.getUserNotifications().collect { result ->
                withContext(Dispatchers.Main) {
                    when (result) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    notifications = result.data ?: emptyList(),
                                    isLoading = false,
                                    error = null
                                )
                            }
                        }
                        is Resource.Error -> {
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
            val result = notificationRepository.markNotificationAsRead(notificationId)

            withContext(Dispatchers.Main) {
                when (result) {
                    is Resource.Success -> {
                        loadNotifications()
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(error = result.message)
                        }
                    }
                    is Resource.Loading -> { }
                }
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = notificationRepository.markAllNotificationsAsRead()

            withContext(Dispatchers.Main) {
                when (result) {
                    is Resource.Success -> {
                        loadNotifications()
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(error = result.message)
                        }
                    }
                    is Resource.Loading -> { }
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null, showErrorDialog = false) }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = notificationRepository.deleteNotification(notificationId)

            withContext(Dispatchers.Main) {
                when (result) {
                    is Resource.Success -> {
                        loadNotifications()
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(error = result.message)
                        }
                    }
                    is Resource.Loading -> { }
                }
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