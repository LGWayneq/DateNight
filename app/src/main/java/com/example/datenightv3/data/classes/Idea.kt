package com.example.datenightv3.data.classes

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ideas")
data class Idea (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    @NonNull @ColumnInfo(name = "category_id") val categoryId: Int,
    @ColumnInfo(name = "location_id") val locationId: Int?,
    val description: String?,
    val latitude: Double?,
    val longitude: Double?,
)

