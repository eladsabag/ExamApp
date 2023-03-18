package com.elad.examapp.data.room

import com.elad.examapp.data.room.LocationDAO
import com.elad.examapp.model.LocationObject
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
    private val locationDAO: LocationDAO
    ) {
    fun getAllLocations(): Flow<List<LocationObject>> = locationDAO.getAllLocations()

    suspend fun getLocationById(id: String): LocationObject = locationDAO.getLocationById(id)

    suspend fun insertLocation(locationObject: LocationObject) = locationDAO.insertLocation(locationObject)

    suspend fun updateLocation(locationObject: LocationObject) = locationDAO.updateLocation(locationObject)

    suspend fun  deleteAllLocations() = locationDAO.deleteAllLocations()

    suspend fun deleteLocation(locationObject: LocationObject) = locationDAO.deleteLocation(locationObject)
}