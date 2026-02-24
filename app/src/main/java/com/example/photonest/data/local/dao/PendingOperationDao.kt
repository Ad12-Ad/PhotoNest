package com.example.photonest.data.local.dao

import androidx.room.*
import com.example.photonest.data.local.entities.PendingOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOperationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(operation: PendingOperationEntity): Long

    @Query("SELECT * FROM pending_operations ORDER BY createdAt ASC")
    fun getAllPending(): Flow<List<PendingOperationEntity>>

    @Query("SELECT * FROM pending_operations ORDER BY createdAt ASC")
    suspend fun getAllPendingNow(): List<PendingOperationEntity>

    @Delete
    suspend fun delete(operation: PendingOperationEntity)

    @Query("DELETE FROM pending_operations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE pending_operations SET retryCount = retryCount + 1, lastAttemptAt = :now WHERE id = :id")
    suspend fun incrementRetry(id: Long, now: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM pending_operations")
    fun getPendingCount(): Flow<Int>
}
