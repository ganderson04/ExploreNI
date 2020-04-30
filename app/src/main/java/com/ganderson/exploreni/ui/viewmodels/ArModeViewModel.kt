package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.ganderson.exploreni.data.ExploreRepository

class ArModeViewModel : ViewModel() {
    private val nearbyParamsLiveData = MutableLiveData<Triple<Double, Double, Int>>()
    val nearbyLocations = nearbyParamsLiveData.switchMap { params ->
        ExploreRepository.getNearbyLocations(params.first, params.second, params.third)
    }

    fun updateParameters(lat: Double, lon: Double, radius: Int) {
        nearbyParamsLiveData.value = Triple(lat, lon, radius)
    }
}