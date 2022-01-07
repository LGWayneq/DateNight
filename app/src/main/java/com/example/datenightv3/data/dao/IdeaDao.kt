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

    @Query("SELECT * FROM ideas ORDER BY idea_name ASC")
    fun getAllIdeas(): Flow<List<Idea>>

    @Query("SELECT * FROM ideas WHERE idea_name = :idea_name")
    fun getIdeaByName(idea_name: String?): Flow<List<Idea>>

    @Query("SELECT * FROM ideas WHERE id = :id")
    fun getIdeaById(id: Int): Flow<Idea>

    @Query("SELECT * FROM ideas WHERE category_name = :category_name ORDER BY idea_name ASC")
    fun getIdeasByCategory(category_name: String): Flow<List<Idea>>

    @Query("SELECT * FROM ideas WHERE category_name = :category_name ORDER BY RANDOM() LIMIT 1")
    fun getRandomIdeaInCategory(category_name: String): Flow<Idea>

    @Query("SELECT EXISTS(SELECT * FROM ideas WHERE category_name = :category_name)")
    fun doesIdeaWithCategoryExist(category_name: String): Boolean

    @Query("SELECT * FROM ideas WHERE idea_name LIKE :query AND category_name = :category_name ORDER BY idea_name ASC")
    fun searchIdea(query: String?, category_name: String): Flow<List<Idea>>
}