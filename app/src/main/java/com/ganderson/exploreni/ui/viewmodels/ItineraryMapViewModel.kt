package com.ganderson.exploreni.ui.viewmodels

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.Itinerary
import com.ganderson.exploreni.entities.data.DataResult

class ItineraryMapViewModel : ViewModel() {
    fun getItineraryPolyline(itinerary: Itinerary, userLocation: Location?,
                             apiKey: String) : LiveData<DataResult<String>> {
        return ExploreRepository.getItineraryPolyline(itinerary, userLocation, apiKey)
    }
}
