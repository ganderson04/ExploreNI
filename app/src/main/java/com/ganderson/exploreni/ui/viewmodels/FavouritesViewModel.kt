package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.data.api.NiLocation

class FavouritesViewModel : ViewModel() {
    val favourites = ExploreRepository.getFavouriteLocations()

    fun removeFromFavourites(niLocation: NiLocation) {
        ExploreRepository.removeFavouriteLocation(niLocation.id)
    }
}