package com.ganderson.exploreni.data

import androidx.lifecycle.LiveData
import com.couchbase.lite.ResultSet
import com.ganderson.exploreni.data.api.ApiAccessor
import com.ganderson.exploreni.data.db.DbAccessor
import com.ganderson.exploreni.entities.api.Event
import com.ganderson.exploreni.entities.api.NiLocation
import com.ganderson.exploreni.entities.api.Weather

class ExploreRepository {
    companion object {
        fun getNearbyLocations(lat: Double, lon: Double, radius: Int) : LiveData<List<NiLocation>> {
            return ApiAccessor.getNearbyLocations(lat, lon, radius)
        }

        fun getLocationsByType(type: String) : LiveData<List<NiLocation>> {
            return ApiAccessor.getLocationsByType(type)
        }

        fun getLocationName(lat: Double, lon: Double, apiKey: String) : LiveData<String> {
            return ApiAccessor.getLocationName(lat, lon, apiKey)
        }

        fun getEvents() : LiveData<List<Event>> {
            return ApiAccessor.getEvents()
        }

        fun getWeather(lat: Double, lon: Double, useFahrenheit: Boolean, apiKey: String)
                : LiveData<Weather> {
            return ApiAccessor.getWeather(lat, lon, useFahrenheit, apiKey)
        }

        fun getFavouriteLocations() : LiveData<List<NiLocation>> {
            return DbAccessor.getFavouriteLocations()
        }

        fun addFavouriteLocation(niLocation: NiLocation) : Boolean {
            return DbAccessor.addFavouriteLocation(niLocation)
        }

        fun removeFavouriteLocation(locationId: String) : Boolean {
            return DbAccessor.removeFavouriteLocation(locationId)
        }

        fun isFavouriteLocation(locationId: String): Boolean {
            return DbAccessor.isFavouriteLocation(locationId)
        }
    }
}