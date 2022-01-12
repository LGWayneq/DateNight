package com.example.datenightv3.data.dao

import androidx.room.*
import com.example.datenightv3.data.classes.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM category ORDER BY category_name ASC")
    fun getAllCategory(): Flow<List<Category>>

    @Query("SELECT * FROM category WHERE category_name = :categoryName ORDER BY category_name ASC")
    fun getByCategoryName(categoryName: String): Flow<List<Category>>

    @Query("SELECT * FROM category WHERE id = :categoryId ORDER BY category_name ASC")
    fun getByCategoryId(categoryId: Int): Flow<Category>

    @Query("SELECT EXISTS(SELECT * FROM category WHERE category_name = :categoryName)")
    fun doesCategoryExist(categoryName: String): Boolean

    @Query("SELECT require_location FROM category WHERE id = :categoryId")
    suspend fun requireLocation(categoryId: Int): Int

    @Query("SELECT category_name FROM category WHERE id = :categoryId")
    fun getCategoryName(categoryId: Int): String

    @Query("SELECT id FROM category WHERE category_name = :categoryName")
    fun getCategoryId(categoryName: String): Int
}