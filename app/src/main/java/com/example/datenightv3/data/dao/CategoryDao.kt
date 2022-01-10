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

    @Query("SELECT * FROM category WHERE category_name = :category_name ORDER BY category_name ASC")
    fun getByCategoryName(category_name: String): Flow<List<Category>>

    @Query("SELECT * FROM category WHERE id = :id ORDER BY category_name ASC")
    fun getByCategoryId(id: Int): Flow<Category>

    @Query("SELECT EXISTS(SELECT * FROM category WHERE category_name = :category_name)")
    fun doesCategoryExist(category_name: String): Boolean

    @Query("SELECT require_location FROM category WHERE category_name = :category_name")
    suspend fun requireLocation(category_name: String): Int
}