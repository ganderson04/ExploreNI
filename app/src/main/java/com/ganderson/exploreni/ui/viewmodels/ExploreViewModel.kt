package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.LocationType
import com.ganderson.exploreni.entities.api.NiLocation

class ExploreViewModel : ViewModel() {
    fun getLocations(locationType: LocationType) : LiveData<List<NiLocation>> {
        return ExploreRepository.getLocationsByType(locationType.name)
    }
}