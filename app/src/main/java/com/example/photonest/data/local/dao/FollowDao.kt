package com.example.photonest.data.local.dao

import androidx.room.*
import com.example.photonest.data.local.entities.FollowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowDao {

    @Query("SELECT * FROM follows WHERE followerId = :userId")
    suspend fun getFollowing(userId: String): List<FollowEntity>

    @Query("SELECT * FROM follows WHERE followingId = :userId")
    suspend fun getFollowers(userId: String): List<FollowEntity>

    @Query("SELECT * FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun getFollow(followerId: String, followingId: String): FollowEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM follows WHERE followerId = :followerId AND followingId = :followingId AND isAccepted = 1)")
    suspend fun isFollowing(followerId: String, followingId: String): Boolean

    @Query("SELECT COUNT(*) FROM follows WHERE followingId = :userId AND isAccepted = 1")
    suspend fun getFollowerCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM follows WHERE followerId = :userId AND isAccepted = 1")
    suspend fun getFollowingCount(userId: String): Int

    @Query("SELECT * FROM follows WHERE followingId = :userId AND isAccepted = 0")
    suspend fun getPendingFollowRequests(userId: String): List<FollowEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollow(follow: FollowEntity)

    @Update
    suspend fun updateFollow(follow: FollowEntity)

    @Delete
    suspend fun deleteFollow(follow: FollowEntity)

    @Query("DELETE FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun unfollow(followerId: String, followingId: String)

    @Query("UPDATE follows SET isAccepted = 1 WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun acceptFollowRequest(followerId: String, followingId: String)

    @Query("DELETE FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun rejectFollowRequest(followerId: String, followingId: String)

    @Query("SELECT followingId FROM follows WHERE followerId = :userId AND isAccepted = 1")
    suspend fun getFollowingIds(userId: String): List<String>

    @Query("SELECT followerId FROM follows WHERE followingId = :userId AND isAccepted = 1")
    suspend fun getFollowerIds(userId: String): List<String>

    @Query("DELETE FROM follows")
    suspend fun clearAllFollows()
}
