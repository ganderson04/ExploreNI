package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.Itinerary

class ItineraryViewerViewModel : ViewModel() {
    fun saveItinerary(itinerary: Itinerary) : Boolean {
        return ExploreRepository.saveItinerary(itinerary)
    }

    fun isDuplicateItineraryName(name: String) : Boolean {
        return ExploreRepository.isDuplicateItineraryName(name)
    }

    fun deleteItinerary(dbId: String) {
        ExploreRepository.deleteItinerary(dbId)
    }

    fun calculateDuration(itinerary: Itinerary, apiKey: String) : LiveData<Int> {
        return ExploreRepository.calculateDuration(itinerary, apiKey)
    }
}