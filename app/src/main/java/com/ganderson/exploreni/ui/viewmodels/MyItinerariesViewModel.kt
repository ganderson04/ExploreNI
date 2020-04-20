package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.Itinerary

class MyItinerariesViewModel : ViewModel() {
    fun getItineraries() : LiveData<List<Itinerary>> {
        return ExploreRepository.getItineraries()
    }

    fun deleteItinerary(itineraryId: String) : Boolean {
        return ExploreRepository.deleteItinerary(itineraryId)
    }
}