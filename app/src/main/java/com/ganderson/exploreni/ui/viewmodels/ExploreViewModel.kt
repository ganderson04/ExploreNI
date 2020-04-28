package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.LocationType
import com.ganderson.exploreni.entities.data.DataResult
import com.ganderson.exploreni.entities.data.api.NiLocation

class ExploreViewModel(locationType: LocationType) : ViewModel() {
    var locations: LiveData<DataResult<List<NiLocation>>> =
        ExploreRepository.getLocationsByType(locationType.name)
}