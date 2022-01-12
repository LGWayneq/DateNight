package com.example.datenightv3.data.dao

import androidx.room.*
import com.example.datenightv3.data.classes.Category
import com.example.datenightv3.data.classes.Location
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(location: Location)

    @Update
    suspend fun update(location: Location)

    @Delete
    suspend fun delete(location: Location)

    @Query("SELECT location_name FROM locations ORDER BY location_name ASC")
    suspend fun getAllLocations(): List<String>

    @Query("SELECT location_latitude FROM locations where id = :locationId")
    suspend fun getLocationLatitude(locationId: Int?): Double

    @Query("SELECT location_longitude FROM locations where id = :locationId")
    suspend fun getLocationLongitude(locationId: Int?): Double?

    @Query("SELECT location_name FROM locations WHERE id = :locationId")
    suspend fun getLocationName(locationId: Int?): String

    @Query("SELECT id FROM locations WHERE location_name = :locationName")
    suspend fun getLocationId(locationName: String?): Int
}