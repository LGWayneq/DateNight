package com.example.datenightv3.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.datenightv3.data.classes.Idea
import com.example.datenightv3.data.dao.IdeaDao

@Database(entities = [Idea::class], version = 13)
abstract class IdeaDatabase: RoomDatabase() {
    abstract fun ideaDao(): IdeaDao

    companion object {
        @Volatile
        private var INSTANCE: IdeaDatabase? = null

        fun getDatabase(context: Context): IdeaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IdeaDatabase::class.java,
                    "idea_database")
                    .createFromAsset("database/ideas.db")
                    //.fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance

                return instance
            }
        }
    }
}