package com.example.photonest.data.local.dao

import androidx.room.*
import com.example.photonest.data.local.entities.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPostsFlow(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getPosts(limit: Int, offset: Int): List<PostEntity>

    @Query("SELECT * FROM posts WHERE id = :postId")
    suspend fun getPostById(postId: String): PostEntity?

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getPostsByUser(userId: String): List<PostEntity>

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getPostsByUserPaged(userId: String, limit: Int, offset: Int): List<PostEntity>

    @Query("SELECT * FROM posts WHERE userId IN (:userIds) ORDER BY timestamp DESC")
    suspend fun getPostsByUsers(userIds: List<String>): List<PostEntity>

    @Query("SELECT * FROM posts WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    suspend fun getBookmarkedPosts(): List<PostEntity>

    @Query("SELECT * FROM posts WHERE caption LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY likeCount DESC")
    suspend fun searchPosts(query: String): List<PostEntity>

    @Query("SELECT * FROM posts WHERE category LIKE '%' || :category || '%' ORDER BY timestamp DESC")
    suspend fun getPostsByCategory(category: String): List<PostEntity>

    @Query("SELECT * FROM posts ORDER BY likeCount DESC LIMIT :limit")
    suspend fun getTrendingPosts(limit: Int = 20): List<PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Delete
    suspend fun deletePost(post: PostEntity)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePostById(postId: String)

    @Query("UPDATE posts SET isLiked = :isLiked, likeCount = :likeCount WHERE id = :postId")
    suspend fun updatePostLike(postId: String, isLiked: Boolean, likeCount: Int)

    @Query("UPDATE posts SET isBookmarked = :isBookmarked WHERE id = :postId")
    suspend fun updatePostBookmark(postId: String, isBookmarked: Boolean)

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun getPostCount(): Int

    @Query("SELECT COUNT(*) FROM posts WHERE userId = :userId")
    suspend fun getPostCountByUser(userId: String): Int

    @Query("DELETE FROM posts WHERE userId = :userId")
    suspend fun deletePostsByUser(userId: String)

    @Query("DELETE FROM posts")
    suspend fun clearAllPosts()

    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    suspend fun getAllPosts(): List<PostEntity>
}
