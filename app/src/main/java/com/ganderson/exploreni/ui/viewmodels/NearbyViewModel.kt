package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.ganderson.exploreni.data.ExploreRepository

class NearbyViewModel : ViewModel() {
    private val nearbyLiveData = MutableLiveData<Triple<Double, Double, Int>>()
    val nearbyLocations = nearbyLiveData.switchMap { params ->
        ExploreRepository.getNearbyLocations(
            params.first,
            params.second,
            params.third
        )
    }

    fun setNearbyParams(lat: Double, lon: Double, radius: Int) {
        nearbyLiveData.value = Triple(lat, lon, radius)
    }
}