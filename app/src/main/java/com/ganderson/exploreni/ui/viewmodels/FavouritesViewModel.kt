package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.data.api.NiLocation

class FavouritesViewModel : ViewModel() {
    fun getFavouriteLocations() : LiveData<List<NiLocation>> {
        return ExploreRepository.getFavouriteLocations()
    }

    fun removeFromFavourites(niLocation: NiLocation) {
        ExploreRepository.removeFavouriteLocation(niLocation.id)
    }
}