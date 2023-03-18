package com.elad.examapp.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.elad.examapp.model.LocationObject

@Database(entities = [LocationObject::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun locationDAO(): LocationDAO
}