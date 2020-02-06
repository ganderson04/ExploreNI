package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.NiLocation

class LookAroundViewModel : ViewModel() {
    fun getNearbyLocations(lat: Double, lon: Double) : LiveData<List<NiLocation>> {
        return ExploreRepository.getNearbyLocations(lat, lon)
    }
}