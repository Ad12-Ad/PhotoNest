package com.example.photonest.data.repository

import com.example.photonest.core.utils.Constants
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.core.utils.safeFirebaseCall
import com.example.photonest.data.local.dao.NotificationDao
import com.example.photonest.data.mapper.toEntity
import com.example.photonest.data.mapper.toNotification
import com.example.photonest.domain.model.Notification
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

    override fun getUserNotifications(): Flow<NetworkResult<List<Notification>>> = flow {
        emit(NetworkResult.Loading())

        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            emit(NetworkResult.Error("User not authenticated"))
            return@flow
        }

        val remoteResult = safeFirebaseCall {
            val snapshot = firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            val merged = snapshot.documents.mapNotNull { doc ->
                val remote = doc.toObject(Notification::class.java)?.copy(id = doc.id) ?: return@mapNotNull null
                val local = notificationDao.getNotificationById(doc.id)?.toNotification()
                remote.copy(isRead = (local?.isRead == true) || remote.isRead)
            }

            notificationDao.insertNotifications(merged.map { it.toEntity() })
            merged
        }

        when (remoteResult) {
            is NetworkResult.Success -> emit(remoteResult)
            else -> {
                val local = notificationDao.getNotificationsPaged(currentUserId, limit = 200, offset = 0)
                    .map { it.toNotification() }
                emit(NetworkResult.Success(local))
            }
        }
    }

    override suspend fun markNotificationAsRead(notificationId: String): NetworkResult<Unit> = safeFirebaseCall {
        firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
            .document(notificationId)
            .update("isRead", true)
            .await()

        notificationDao.markNotificationAsRead(notificationId)
    }

    override suspend fun markAllNotificationsAsRead(): NetworkResult<Unit> = safeFirebaseCall {
        val currentUserId = firebaseAuth.currentUser?.uid ?: throw java.lang.IllegalStateException("Not authenticated")
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
    }

    override suspend fun createNotification(notification: Notification): NetworkResult<Unit> = safeFirebaseCall {
        firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
            .add(notification)
            .await()
    }

    override suspend fun deleteNotification(notificationId: String): NetworkResult<Unit> = safeFirebaseCall {
        firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
            .document(notificationId)
            .delete()
            .await()

        notificationDao.deleteNotificationById(notificationId)
    }

    override suspend fun getUnreadNotificationCount(): NetworkResult<Int> {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return NetworkResult.Error("Not authenticated")

        val remoteResult = safeFirebaseCall {
            firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
                .size()
        }

        return when (remoteResult) {
            is NetworkResult.Success -> remoteResult
            else -> {
                val localCount = notificationDao.getUnreadNotificationCount(currentUserId)
                NetworkResult.Success(localCount)
            }
        }
    }
}