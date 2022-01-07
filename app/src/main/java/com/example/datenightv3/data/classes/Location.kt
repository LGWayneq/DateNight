package com.example.datenightv3.data.classes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class Location(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "location_name") val locationName: String,
    @ColumnInfo(name = "location_latitude") val locationLatitude: Double,
    @ColumnInfo(name = "location_longitude") val locationLongitude: Double
)
