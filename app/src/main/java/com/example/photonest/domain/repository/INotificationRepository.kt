package com.example.photonest.domain.repository

import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.data.model.Notification
import kotlinx.coroutines.flow.Flow

interface INotificationRepository {
    fun getUserNotifications(): Flow<NetworkResult<List<Notification>>>
    suspend fun markNotificationAsRead(notificationId: String): NetworkResult<Unit>
    suspend fun markAllNotificationsAsRead(): NetworkResult<Unit>
    suspend fun createNotification(notification: Notification): NetworkResult<Unit>
    suspend fun deleteNotification(notificationId: String): NetworkResult<Unit>
    suspend fun getUnreadNotificationCount(): NetworkResult<Int>
}