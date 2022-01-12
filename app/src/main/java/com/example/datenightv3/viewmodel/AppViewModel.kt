package com.example.datenightv3.viewmodel

import androidx.lifecycle.*
import com.example.datenightv3.data.classes.Category
import com.example.datenightv3.data.dao.CategoryDao
import com.example.datenightv3.data.classes.Idea
import com.example.datenightv3.data.dao.IdeaDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AppViewModel(private val categoryDao: CategoryDao, private val ideaDao: IdeaDao): ViewModel() {

    fun allCategory(): Flow<List<Category>> = categoryDao.getAllCategory()

    fun getCategoryName(categoryId: Int): String = categoryDao.getCategoryName(categoryId)

    fun getCategoryId(categoryName: String): Int = categoryDao.getCategoryId(categoryName)

    private fun insertCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.insert(category)
        }
    }

    private fun createNewCategoryEntry(categoryName: String, requireLocation: Int): Category {
        return Category(categoryName = categoryName, requireLocation = requireLocation)
    }

    fun addCategory(categoryName: String, requireLocation: Int) {
        val newCategory = createNewCategoryEntry(categoryName, requireLocation)
        insertCategory(newCategory)
    }

    fun getCategory(id: Int): LiveData<Category> {
        return categoryDao.getByCategoryId(id).asLiveData()
    }

    fun getUpdatedCategory(categoryId: Int, categoryName: String, requireLocation: Int) {
        val updatedCategory = Category(categoryId, categoryName, requireLocation)
        updatedCategory(updatedCategory)
    }

    private fun updatedCategory(updatedCategory: Category) {
        viewModelScope.launch {
            categoryDao.update(updatedCategory)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.delete(category)
        }
    }

    suspend fun requireLocation(categoryId: Int): Boolean {
        val boolInt = categoryDao.requireLocation(categoryId)
        if (boolInt == 1) return true
        return false
    }

    fun doesCategoryExist(categoryName: String) : Boolean = categoryDao.doesCategoryExist(categoryName)
    fun doesIdeaWithCategoryExist(categoryId: Int) : Boolean = ideaDao.doesIdeaWithCategoryExist(categoryId)
    fun allIdeas(): Flow<List<Idea>> = ideaDao.getAllIdeas()

    fun getIdeaInCategory(categoryId: Int): Flow<List<Idea>> = ideaDao.getIdeasByCategory(categoryId)

    private fun insertIdea(idea: Idea) {
        viewModelScope.launch {
            ideaDao.insert(idea)
        }
    }

    private fun createNewIdeaEntry(ideaName: String,
                                   categoryId: Int,
                                   ideaDescription: String?,
                                   ideaLocationId: Int?,
                                   ideaLatitude: Double?,
                                   ideaLongitude: Double?): Idea {
        return Idea(name = ideaName,
            categoryId = categoryId,
            description = ideaDescription,
            locationId = ideaLocationId,
            latitude = ideaLatitude,
            longitude = ideaLongitude)
    }

    fun addIdea(ideaName: String,
                categoryId: Int,
                ideaDescription: String?,
                ideaLocationId: Int?,
                ideaLatitude: Double?=null,
                ideaLongitude: Double?=null) {
        val newIdea = createNewIdeaEntry(ideaName, categoryId, ideaDescription, ideaLocationId, ideaLatitude, ideaLongitude)
        insertIdea(newIdea)
    }

    fun getIdea(id: Int): LiveData<Idea> {
        return ideaDao.getIdeaById(id).asLiveData()
    }

    private fun updateIdea(idea: Idea){
        viewModelScope.launch {
            ideaDao.update(idea)
        }
    }

    fun deleteIdea(idea: Idea) {
        viewModelScope.launch {
            ideaDao.delete(idea)
        }
    }

    fun getUpdatedIdea(ideaId: Int,
                       ideaName: String,
                       categoryId: Int,
                       ideaDescription: String?,
                       ideaLocationId: Int?,
                       ideaLatitude: Double?=null,
                       ideaLongitude: Double?=null) {
        val updatedIdea = Idea(ideaId, ideaName, categoryId, ideaLocationId, ideaDescription, ideaLatitude, ideaLongitude)
        updateIdea(updatedIdea)
    }


    fun generateSuggestion(categoryId: Int): LiveData<Idea> {
        return ideaDao.getRandomIdeaInCategory(categoryId).asLiveData()
    }

    fun queryIdea(query: String?, categoryId: Int) = ideaDao.searchIdea("%" + query + "%", categoryId = categoryId)

    fun getIdeasCount(categoryId: Int): Int = ideaDao.getIdeasCount(categoryId)
}

class AppViewModelFactory(
    private val categoryDao: CategoryDao, private val ideaDao: IdeaDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(categoryDao, ideaDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}