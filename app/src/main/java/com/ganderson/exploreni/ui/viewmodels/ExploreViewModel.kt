package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.LocationType
import com.ganderson.exploreni.entities.api.NiLocation

class ExploreViewModel(locationType: LocationType) : ViewModel() {
    var locations: LiveData<List<NiLocation>> =
        ExploreRepository.getLocationsByType(locationType.name)
}