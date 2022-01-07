package com.example.datenightv3.data

import android.app.Application
import com.example.datenightv3.data.database.CategoryDatabase
import com.example.datenightv3.data.database.IdeaDatabase
import com.example.datenightv3.data.database.LocationDatabase

class DatabaseApplication: Application() {
    val categoryDatabase: CategoryDatabase by lazy { CategoryDatabase.getDatabase(this) }
    val ideaDatabase: IdeaDatabase by lazy { IdeaDatabase.getDatabase(this) }
    val locationDatabase: LocationDatabase by lazy {LocationDatabase.getDatabase(this)}
}