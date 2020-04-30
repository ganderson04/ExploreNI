package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.*
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.LocationType

class ExploreViewModel : ViewModel() {
    private val locationTypeLiveData = MutableLiveData<LocationType>()
    val locations = locationTypeLiveData.switchMap { locationType ->
        ExploreRepository.getLocationsByType(locationType.name)
    }

    fun setLocationType(locationType: LocationType) {
        locationTypeLiveData.value = locationType
    }
}