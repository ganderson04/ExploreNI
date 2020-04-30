package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.data.DataResult
import com.ganderson.exploreni.entities.data.api.Weather

class HomeViewModel : ViewModel() {
    private val locationNameLiveData = MutableLiveData<Triple<Double, Double, String>>()
    val locationName = locationNameLiveData.switchMap { params ->
        ExploreRepository.getLocationName(params.first, params.second, params.third)
    }

    private val weatherLiveData = MutableLiveData<HashMap<String, String>>()
    val weather = weatherLiveData.switchMap { params ->
        ExploreRepository.getWeather(
            params["lat"]!!.toDouble(),
            params["lon"]!!.toDouble(),
            params["useFahrenheit"]!!.toBoolean(),
            params["apiKey"]!!
        )
    }

    fun updateLocationParams(lat: Double, lon: Double, apiKey: String) {
        locationNameLiveData.value = Triple(lat, lon, apiKey)
    }

    fun updateWeatherParams(lat: Double, lon: Double, useFahrenheit: Boolean, apiKey: String) {
        val params = HashMap<String, String>().apply {
            put("lat", lat.toString())
            put("lon", lon.toString())
            put("useFahrenheit", useFahrenheit.toString())
            put("apiKey", apiKey)
        }
        weatherLiveData.value = params
    }
}