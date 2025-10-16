package com.example.photonest.domain.repository

import com.example.photonest.core.utils.Resource
import com.example.photonest.data.model.Notification
import kotlinx.coroutines.flow.Flow

interface INotificationRepository {
    fun getUserNotifications(): Flow<Resource<List<Notification>>>
    suspend fun markNotificationAsRead(notificationId: String): Resource<Unit>
    suspend fun markAllNotificationsAsRead(): Resource<Unit>
    suspend fun createNotification(notification: Notification): Resource<Unit>
    suspend fun deleteNotification(notificationId: String): Resource<Unit>
    suspend fun getUnreadNotificationCount(): Resource<Int>
}