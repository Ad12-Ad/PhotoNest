package com.example.photonest.data.repository

import com.example.photonest.core.utils.Constants
import com.example.photonest.core.utils.Resource
import com.example.photonest.data.local.dao.NotificationDao
import com.example.photonest.data.mapper.toEntity
import com.example.photonest.data.mapper.toNotification
import com.example.photonest.data.model.Notification
import com.example.photonest.domain.repository.INotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : INotificationRepository {

    override fun getUserNotifications(): Flow<Resource<List<Notification>>> = flow {
        emit(Resource.Loading())

        try {
            val currentUserId = firebaseAuth.currentUser?.uid ?: throw Exception("Not authenticated")

            val query = firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            val notifications = query.documents.mapNotNull { doc ->
                doc.toObject(Notification::class.java)?.copy(id = doc.id)
            }

            // Cache locally
            notificationDao.insertNotifications(notifications.map { it.toEntity() })
            emit(Resource.Success(notifications))

        } catch (e: Exception) {
            // Fallback to local cache
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId != null) {
                val localNotifications = notificationDao.getUnreadNotifications(currentUserId)
                    .map { it.toNotification() }
                emit(Resource.Success(localNotifications))
            } else {
                emit(Resource.Error("Not authenticated: ${e.message}"))
            }
        }
    }

    override suspend fun markNotificationAsRead(notificationId: String): Resource<Unit> {
        return try {
            firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
                .document(notificationId)
                .update("isRead", true)
                .await()

            notificationDao.markNotificationAsRead(notificationId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mark notification as read")
        }
    }

    override suspend fun markAllNotificationsAsRead(): Resource<Unit> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid ?: throw Exception("Not authenticated")

            val batch = firestore.batch()
            val notifications = firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            notifications.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }

            batch.commit().await()
            notificationDao.markAllNotificationsAsRead(currentUserId)

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mark all notifications as read")
        }
    }

    override suspend fun createNotification(notification: Notification): Resource<Unit> {
        return try {
            firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
                .add(notification)
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create notification")
        }
    }

    override suspend fun deleteNotification(notificationId: String): Resource<Unit> {
        return try {
            firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
                .document(notificationId)
                .delete()
                .await()

            notificationDao.deleteNotificationById(notificationId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete notification")
        }
    }

    override suspend fun getUnreadNotificationCount(): Resource<Int> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid ?: throw Exception("Not authenticated")

            val count = firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
                .size()

            Resource.Success(count)
        } catch (e: Exception) {
            // Fallback to local count
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId != null) {
                val localCount = notificationDao.getUnreadNotificationCount(currentUserId)
                Resource.Success(localCount)
            } else {
                Resource.Error("Not authenticated: ${e.message}")
            }
        }
    }
}