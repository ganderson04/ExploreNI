package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.data.api.NiLocation

class AttractionDetailViewModel : ViewModel() {
    fun addFavouriteLocation(location: NiLocation) : Boolean {
        return ExploreRepository.addFavouriteLocation(location)
    }

    fun removeFavouriteLocation(id: String) : Boolean {
        return ExploreRepository.removeFavouriteLocation(id)
    }

    fun isFavouriteLocation(id: String) : Boolean {
        return ExploreRepository.isFavouriteLocation(id)
    }
}