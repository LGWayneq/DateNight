package com.example.datenightv3.data.classes

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ideas")
data class Idea (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "idea_name") val ideaName: String,
    @NonNull @ColumnInfo(name = "category_name") val categoryName: String,
    @ColumnInfo(name = "idea_location") val ideaLocation: String?,
    @ColumnInfo(name = "idea_description") val ideaDescription: String?,
    @ColumnInfo(name = "idea_location_distance") val ideaLocationDistance: Double?,
)

