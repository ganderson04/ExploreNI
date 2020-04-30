package com.ganderson.exploreni.ui.viewmodels

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.Itinerary

class ItineraryViewerViewModel : ViewModel() {
    private val durationLiveData = MutableLiveData<Triple<Itinerary, Location?, String>>()
    val duration = durationLiveData.switchMap { params ->
        ExploreRepository.calculateDuration(
            params.first,
            params.second,
            params.third
        )
    }

    fun saveItinerary(itinerary: Itinerary) : Boolean {
        return ExploreRepository.saveItinerary(itinerary)
    }

    fun isDuplicateItineraryName(name: String) : Boolean {
        return ExploreRepository.isDuplicateItineraryName(name)
    }

    fun deleteItinerary(dbId: String) {
        ExploreRepository.deleteItinerary(dbId)
    }

    fun setDurationParams(itinerary: Itinerary, userLocation: Location?, apiKey: String) {
        durationLiveData.value = Triple(itinerary, userLocation, apiKey)
    }
}