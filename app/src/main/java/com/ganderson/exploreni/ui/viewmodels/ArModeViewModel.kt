package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.ganderson.exploreni.data.ExploreRepository

class ArModeViewModel : ViewModel() {
    // A private LiveData object is kept in the ViewModels to monitor changes to the parameters
    // required for the API calls. "Triple" is an inbuilt Kotlin class (along with "Pair") designed
    // to hold three values of any type.
    private val nearbyParamsLiveData = MutableLiveData<Triple<Double, Double, Int>>()

    // The public LiveData object accessible by the relevant UI class is provided with data by way
    // of the LiveData "switchMap" extension function which is fired when the private LiveData
    // above is updated.
    val nearbyLocations = nearbyParamsLiveData.switchMap { params ->
        ExploreRepository.getNearbyLocations(params.first, params.second, params.third)
    }

    fun updateParameters(lat: Double, lon: Double, radius: Int) {
        // The private LiveData object is updated, causing "switchMap" to fire.
        nearbyParamsLiveData.value = Triple(lat, lon, radius)
    }
}