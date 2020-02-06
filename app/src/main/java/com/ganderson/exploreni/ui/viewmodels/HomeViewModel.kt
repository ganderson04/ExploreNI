package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.api.Weather

class HomeViewModel : ViewModel() {

    fun getLocationName(lat: Double, lon: Double, apiKey: String) : LiveData<String> {
        return ExploreRepository.getLocationName(lat, lon, apiKey)
    }

    fun getWeather(lat: Double, lon: Double, useFahrenheit: Boolean, apiKey: String)
            : LiveData<Weather> {
        return ExploreRepository.getWeather(lat, lon, useFahrenheit, apiKey)
    }
}