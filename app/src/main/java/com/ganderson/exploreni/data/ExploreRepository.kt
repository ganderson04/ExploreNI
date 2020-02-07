package com.ganderson.exploreni.data

import androidx.lifecycle.LiveData
import com.ganderson.exploreni.data.api.ApiAccessor
import com.ganderson.exploreni.entities.api.NiLocation
import com.ganderson.exploreni.entities.api.Weather

class ExploreRepository {
    companion object {
        fun getNearbyLocations(lat: Double, lon: Double) : LiveData<List<NiLocation>> {
            return ApiAccessor.getNearbyLocations(lat, lon)
        }

        fun getLocationName(lat: Double, lon: Double, apiKey: String) : LiveData<String> {
            return ApiAccessor.getLocationName(lat, lon, apiKey)
        }

        fun getWeather(lat: Double, lon: Double, useFahrenheit: Boolean, apiKey: String)
                : LiveData<Weather> {
            return ApiAccessor.getWeather(lat, lon, useFahrenheit, apiKey)
        }
    }
}