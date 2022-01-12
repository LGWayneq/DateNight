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

    suspend fun getLocationLatitude(locationId: Int?): Double? = locationDao.getLocationLatitude(locationId)

    suspend fun getLocationLongitude(locationId: Int?): Double? = locationDao.getLocationLongitude(locationId)

    suspend fun getLocationName(locationId: Int?): String = locationDao.getLocationName(locationId)

    suspend fun getLocationId(locationName: String?): Int = locationDao.getLocationId(locationName)

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