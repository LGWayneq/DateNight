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

    fun getCategory(name: String): Flow<List<Category>>? = categoryDao.getByCategoryName(name)

    private fun insertCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.insert(category)
        }
    }

    private fun createNewCategoryEntry(categoryName: String): Category {
        return Category(categoryName = categoryName)
    }

    fun addCategory(categoryName: String) {
        val newCategory = createNewCategoryEntry(categoryName)
        insertCategory(newCategory)
    }

    fun getCategory(id: Int): LiveData<Category> {
        return categoryDao.getByCategoryId(id).asLiveData()
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.delete(category)
        }
    }

    fun doesCategoryExist(categoryName: String) : Boolean = categoryDao.doesCategoryExist(categoryName)
    fun doesIdeaWithCategoryExist(categoryName: String) : Boolean = ideaDao.doesIdeaWithCategoryExist(categoryName)
    fun allIdeas(): Flow<List<Idea>> = ideaDao.getAllIdeas()

    fun getIdea(name: String?): Flow<List<Idea>> = ideaDao.getIdeaByName(name)

    fun getIdeaInCategory(category_name: String): Flow<List<Idea>> = ideaDao.getIdeasByCategory(category_name)

    private fun insertIdea(idea: Idea) {
        viewModelScope.launch {
            ideaDao.insert(idea)
        }
    }

    private fun createNewIdeaEntry(ideaName: String, categoryName: String, ideaDescription: String?, ideaLocation: String?, ideaLatitude: Double?, ideaLongitude: Double?): Idea {
        return Idea(ideaName = ideaName,
            categoryName = categoryName,
            ideaDescription = ideaDescription,
            ideaLocation = ideaLocation,
            ideaLatitude = ideaLatitude,
            ideaLongitude = ideaLongitude)
    }

    fun addIdea(ideaName: String, categoryName: String, ideaDescription: String?, ideaLocation: String?, ideaLatitude: Double?=null, ideaLongitude: Double?=null) {
        val newIdea = createNewIdeaEntry(ideaName, categoryName, ideaDescription, ideaLocation, ideaLatitude, ideaLongitude)
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

    fun getUpdatedIdea(ideaId: Int, ideaName: String, categoryName: String, ideaDescription: String?, ideaLocation: String?, ideaLatitude: Double?=null, ideaLongitude: Double?=null) {
        val updatedIdea = Idea(ideaId, ideaName, categoryName, ideaLocation, ideaDescription, ideaLatitude, ideaLongitude)
        updateIdea(updatedIdea)
    }


    fun generateSuggestion(categoryName: String): LiveData<Idea> {
        return ideaDao.getRandomIdeaInCategory(categoryName).asLiveData()
    }

    fun queryIdea(query: String?, categoryName: String) = ideaDao.searchIdea("%" + query + "%", category_name = categoryName)

    fun getIdeasCount(categoryName: String): Int = ideaDao.getIdeasCount(categoryName)
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