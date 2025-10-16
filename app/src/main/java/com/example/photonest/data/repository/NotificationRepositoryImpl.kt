package com.example.photonest.data.repository

import android.util.Log
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
            val currentUserId = firebaseAuth.currentUser?.uid

            Log.d("NotificationRepo", "====== START LOADING NOTIFICATIONS ======")
            Log.d("NotificationRepo", "Current User ID: $currentUserId")

            if (currentUserId == null) {
                Log.e("NotificationRepo", "User not authenticated!")
                emit(Resource.Error("User not authenticated"))
                return@flow
            }

            // Query Firestore
            val query = firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)

            Log.d("NotificationRepo", "Querying collection: ${Constants.NOTIFICATIONS_COLLECTION}")
            Log.d("NotificationRepo", "Filtering by userId: $currentUserId")

            val snapshot = query.get().await()

            Log.d("NotificationRepo", "Query returned ${snapshot.size()} documents")

            if (snapshot.isEmpty) {
                Log.w("NotificationRepo", "No notification documents found for user: $currentUserId")
                emit(Resource.Success(emptyList()))
                return@flow
            }

            // Parse each document
            val notifications = mutableListOf<Notification>()
            snapshot.documents.forEachIndexed { index, doc ->
                Log.d("NotificationRepo", "--- Document $index (ID: ${doc.id}) ---")
                Log.d("NotificationRepo", "Document data: ${doc.data}")

                try {
                    val notification = doc.toObject(Notification::class.java)

                    if (notification == null) {
                        Log.e("NotificationRepo", "Failed to parse document ${doc.id} - toObject returned null")
                        Log.e("NotificationRepo", "   Raw data: ${doc.data}")
                    } else {
                        val notificationWithId = notification.copy(id = doc.id)
                        notifications.add(notificationWithId)
                        Log.d("NotificationRepo", "Successfully parsed notification:")
                        Log.d("NotificationRepo", "   Type: ${notificationWithId.type}")
                        Log.d("NotificationRepo", "   From: ${notificationWithId.fromUsername}")
                        Log.d("NotificationRepo", "   Message: ${notificationWithId.message}")
                    }
                } catch (e: Exception) {
                    Log.e("NotificationRepo", "Exception parsing document ${doc.id}", e)
                    Log.e("NotificationRepo", "   Document data: ${doc.data}")
                }
            }

            Log.d("NotificationRepo", "====== FINISHED PARSING ======")
            Log.d("NotificationRepo", "Total notifications parsed: ${notifications.size}")
            Log.d("NotificationRepo", "Emitting Success with ${notifications.size} notifications")

            // Save to local database
            try {
                notificationDao.insertNotifications(notifications.map { it.toEntity() })
                Log.d("NotificationRepo", "Saved to local database")
            } catch (e: Exception) {
                Log.e("NotificationRepo", "Failed to save to local DB", e)
            }

            emit(Resource.Success(notifications))
            Log.d("NotificationRepo", "====== END LOADING NOTIFICATIONS ======")

        } catch (e: Exception) {
            Log.e("NotificationRepo", "Fatal error loading notifications", e)
            Log.e("NotificationRepo", "Error message: ${e.message}")
            Log.e("NotificationRepo", "Error cause: ${e.cause}")
            e.printStackTrace()

            // Try loading from local database as fallback
            try {
                val currentUserId = firebaseAuth.currentUser?.uid
                if (currentUserId != null) {
                    val localNotifications = notificationDao.getUnreadNotifications(currentUserId)
                        .map { it.toNotification() }
                    Log.d("NotificationRepo", "Loaded ${localNotifications.size} notifications from local DB")
                    emit(Resource.Success(localNotifications))
                } else {
                    emit(Resource.Error(e.message ?: "Failed to load notifications"))
                }
            } catch (localError: Exception) {
                Log.e("NotificationRepo", "Also failed to load from local DB", localError)
                emit(Resource.Error(e.message ?: "Failed to load notifications"))
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