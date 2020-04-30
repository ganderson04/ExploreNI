package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.ganderson.exploreni.data.ExploreRepository

class MyItinerariesViewModel : ViewModel() {
    val itineraries = ExploreRepository.getItineraries()

    fun deleteItinerary(itineraryId: String) : Boolean {
        return ExploreRepository.deleteItinerary(itineraryId)
    }
}