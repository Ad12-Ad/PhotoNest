package com.example.photonest.ui.screens.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.data.local.dao.NotificationDao
import com.example.photonest.domain.repository.INotificationRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationBadgeViewModel @Inject constructor(
    private val notificationDao: NotificationDao,
    private val notificationRepository: INotificationRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                // Observe Room for changes and count unread consistently
                notificationDao
                    .getNotificationsFlow(uid)
                    .map { list -> list.count { !it.isRead } }
                    .collectLatest { _unreadCount.value = it }
            }
        }
    }

    // Optional: trigger a one-shot refresh to seed local cache after app start
    fun refreshOnce() {
        viewModelScope.launch {
            notificationRepository.getUserNotifications().collect { /* ignore UI state here */ }
        }
    }
}
