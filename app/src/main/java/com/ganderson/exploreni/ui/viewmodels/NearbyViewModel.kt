package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.data.DataResult
import com.ganderson.exploreni.entities.data.api.NiLocation

class NearbyViewModel : ViewModel() {

    fun getNearbyLocations(lat: Double, lon: Double, radius: Int) :
            LiveData<DataResult<List<NiLocation>>> {
        return ExploreRepository.getNearbyLocations(lat, lon, radius)
    }
}