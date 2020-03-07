package com.ganderson.exploreni.ui.viewmodels

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
}