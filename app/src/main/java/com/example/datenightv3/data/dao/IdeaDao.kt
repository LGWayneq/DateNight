package com.example.datenightv3.data.dao

import androidx.room.*
import com.example.datenightv3.data.classes.Idea
import kotlinx.coroutines.flow.Flow

@Dao
interface IdeaDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(idea: Idea)

    @Update
    suspend fun update(idea: Idea)

    @Delete
    suspend fun delete(idea: Idea)

    @Query("SELECT * FROM ideas ORDER BY name ASC")
    fun getAllIdeas(): Flow<List<Idea>>

    @Query("SELECT * FROM ideas WHERE name = :idea_name")
    fun getIdeaByName(idea_name: String?): Flow<List<Idea>>

    @Query("SELECT * FROM ideas WHERE id = :id")
    fun getIdeaById(id: Int): Flow<Idea>

    @Query("SELECT * FROM ideas WHERE category_id = :categoryId ORDER BY name ASC")
    fun getIdeasByCategory(categoryId: Int): Flow<List<Idea>>

    @Query("SELECT * FROM ideas WHERE category_id = :categoryId ORDER BY RANDOM() LIMIT 1")
    fun getRandomIdeaInCategory(categoryId: Int): Flow<Idea>

    @Query("SELECT EXISTS(SELECT * FROM ideas WHERE category_id = :categoryId)")
    fun doesIdeaWithCategoryExist(categoryId: Int): Boolean

    @Query("SELECT * FROM ideas WHERE (name LIKE :query OR description LIKE :query)  AND category_id = :categoryId ORDER BY name ASC")
    fun searchIdea(query: String?, categoryId: Int): Flow<List<Idea>>

    @Query("SELECT COUNT(*) FROM ideas WHERE category_id = :categoryId")
    fun getIdeasCount(categoryId: Int): Int
}