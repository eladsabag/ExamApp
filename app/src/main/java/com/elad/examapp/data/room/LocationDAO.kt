package com.elad.examapp.data.room

import androidx.room.*
import com.elad.examapp.model.LocationObject
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDAO {
    @Query(value = "SELECT * FROM location_tbl")
    fun getAllLocations(): Flow<List<LocationObject>>

    @Query(value = "SELECT * FROM location_tbl WHERE id =:id")
    suspend fun getLocationById(id: String): LocationObject

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(locationObject: LocationObject)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateLocation(locationObject: LocationObject)

    @Query(value = "DELETE FROM location_tbl")
    suspend fun deleteAllLocations()

    @Delete
    suspend fun deleteLocation(locationObject: LocationObject)
}