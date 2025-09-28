package com.example.photonest.data.local.dao

import androidx.room.*
import com.example.photonest.data.local.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsFlow(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getNotificationsPaged(userId: String, limit: Int, offset: Int): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE id = :notificationId")
    suspend fun getNotificationById(notificationId: String): NotificationEntity?

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    suspend fun getUnreadNotificationCount(userId: String): Int

    @Query("SELECT * FROM notifications WHERE userId = :userId AND isRead = 0 ORDER BY timestamp DESC")
    suspend fun getUnreadNotifications(userId: String): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE userId = :userId AND type = :type ORDER BY timestamp DESC")
    suspend fun getNotificationsByType(userId: String, type: String): List<NotificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)

    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteNotificationById(notificationId: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markNotificationAsRead(notificationId: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsAsRead(userId: String)

    @Query("UPDATE notifications SET isClicked = 1 WHERE id = :notificationId")
    suspend fun markNotificationAsClicked(notificationId: String)

    @Query("DELETE FROM notifications WHERE userId = :userId AND timestamp < :timestamp")
    suspend fun deleteOldNotifications(userId: String, timestamp: Long)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteUserNotifications(userId: String)

    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun getTotalNotificationCount(): Int

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()
}
