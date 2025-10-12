package com.example.photonest.data.local.dao

import androidx.room.*
import com.example.photonest.data.local.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY postsCount DESC LIMIT :limit")
    suspend fun getPopularCategories(limit: Int = 20): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE name = :name")
    suspend fun getCategoryByName(name: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE name LIKE '%' || :query || '%' ORDER BY postsCount DESC")
    suspend fun searchCategories(query: String): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: String)

    @Query("UPDATE categories SET postsCount = postsCount + 1 WHERE name IN (:categoryNames)")
    suspend fun incrementPostsCount(categoryNames: List<String>)

    @Query("UPDATE categories SET postsCount = postsCount - 1 WHERE name IN (:categoryNames) AND postsCount > 0")
    suspend fun decrementPostsCount(categoryNames: List<String>)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Query("DELETE FROM categories")
    suspend fun clearAllCategories()
}
