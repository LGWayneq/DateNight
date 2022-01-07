package com.example.datenightv3.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.datenightv3.data.classes.Location
import com.example.datenightv3.data.dao.LocationDao

@Database(entities = [Location::class], version = 4)
abstract class LocationDatabase: RoomDatabase() {
    abstract fun LocationDao(): LocationDao

    companion object {
        @Volatile
        private var INSTANCE: LocationDatabase? = null

        fun getDatabase(context: Context): LocationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocationDatabase::class.java,
                    "Location_database")
                    .createFromAsset("database/locations.db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance

                return instance
            }
        }
    }
}