package com.example.photonest.data.local.dao

import androidx.room.*
import com.example.photonest.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id IN (:userIds)")
    suspend fun getUsersByIds(userIds: List<String>): List<UserEntity>

    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%' ORDER BY followersCount DESC LIMIT :limit")
    suspend fun searchUsers(query: String, limit: Int = 20): List<UserEntity>

    @Query("SELECT * FROM users ORDER BY followersCount DESC LIMIT :limit")
    suspend fun getPopularUsers(limit: Int = 20): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("DELETE FROM users")
    suspend fun clearAllUsers()
}
