package com.example.datenightv3.viewmodel

import androidx.lifecycle.*
import com.example.datenightv3.data.dao.LocationDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

class LocationViewModel(private val locationDao: LocationDao): ViewModel() {

    fun getAllLocationNames():MutableLiveData<List<String>> {
        val locationNamesList = MutableLiveData<List<String>>()
        viewModelScope.launch {
            locationNamesList.value = locationDao.getAllLocations()
        }
        return locationNamesList
    }

    suspend fun getLocationLatitude(locationName: String?): Double? = locationDao.getLocationLatitude(locationName)

    suspend fun getLocationLongitude(locationName: String?): Double? = locationDao.getLocationLongitude(locationName)

}

class LocationViewModelFactory(
    private val locationDao: LocationDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationViewModel(locationDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}