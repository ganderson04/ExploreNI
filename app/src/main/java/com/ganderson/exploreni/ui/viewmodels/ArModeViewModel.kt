package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.api.NiLocation

class ArModeViewModel : ViewModel() {
    fun getNearbyLocations(lat: Double, lon: Double, radius: Int) : LiveData<List<NiLocation>> {
        return ExploreRepository.getNearbyLocations(lat, lon, radius)
    }
}