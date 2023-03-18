package com.elad.examapp.ui.screens.map

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elad.examapp.model.LocationObject
import com.elad.examapp.data.room.DatabaseRepository
import com.elad.examapp.utils.Util
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(private val repository: DatabaseRepository) : ViewModel() {
    val locationsListLiveData = MutableLiveData<List<LocationObject>>()

    fun loadLocationsList() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllLocations().distinctUntilChanged().collect { listOfLocations ->
                logLocationList(listOfLocations)
                locationsListLiveData.postValue(listOfLocations)
            }
        }
    }

    fun insertNewLocation(locationObject: LocationObject) {
        viewModelScope.launch {
            repository.insertLocation(locationObject)
        }
    }

    fun deleteLocation(locationObject: LocationObject) {
        viewModelScope.launch {
            repository.deleteLocation(locationObject)
        }
    }

    private fun logLocationList(listOfLocations: List<LocationObject>) {
        Log.i(TAG, " ----- ------ ----- ----- ----- Loaded Locations List ----- ------ ----- ----- -----")
        listOfLocations.forEach { locationObject ->
            Log.i(TAG, Util.formatLatLng(locationObject))
        }
    }

    companion object {
        const val TAG = "MapViewModel"
    }
}