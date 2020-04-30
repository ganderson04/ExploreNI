package com.ganderson.exploreni.ui.viewmodels

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.Itinerary

class ItineraryMapViewModel : ViewModel() {
    private val polylineLiveData = MutableLiveData<Triple<Itinerary, Location?, String>>()
    val polyline = polylineLiveData.switchMap { params ->
        ExploreRepository.getItineraryPolyline(
            params.first,
            params.second,
            params.third
        )
    }

    fun setPolylineParams(itinerary: Itinerary, userLocation: Location?, apiKey: String) {
        polylineLiveData.value = Triple(itinerary, userLocation, apiKey)
    }
}
