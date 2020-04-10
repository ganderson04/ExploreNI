package com.ganderson.exploreni.data

import android.location.Location
import androidx.lifecycle.LiveData
import com.ganderson.exploreni.data.api.ApiAccessor
import com.ganderson.exploreni.data.db.DbAccessor
import com.ganderson.exploreni.entities.Itinerary
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

        fun performSearch(query: String) : LiveData<List<NiLocation>> {
            return ApiAccessor.performSearch(query)
        }

        fun getWeather(lat: Double, lon: Double, useFahrenheit: Boolean, apiKey: String)
                : LiveData<Weather> {
            return ApiAccessor.getWeather(lat, lon, useFahrenheit, apiKey)
        }

        fun calculateDuration(itinerary: Itinerary, userLocation: Location?,
                              apiKey: String): LiveData<Int> {
            return ApiAccessor.calculateDuration(itinerary, userLocation, apiKey)
        }

        fun getItineraryPolyline(itinerary: Itinerary, userLocation: Location?,
                                 apiKey: String): LiveData<String> {
            return ApiAccessor.getItineraryPolyline(itinerary, userLocation, apiKey)
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

        fun isFavouriteLocation(locationId: String) : Boolean {
            return DbAccessor.isFavouriteLocation(locationId)
        }

        fun saveItinerary(itinerary: Itinerary) : Boolean {
            return DbAccessor.saveItinerary(itinerary)
        }

        fun isDuplicateItineraryName(name: String) : Boolean {
            return DbAccessor.isDuplicateItineraryName(name)
        }

        fun getItineraries(): LiveData<List<Itinerary>> {
            return DbAccessor.getItineraries()
        }

        fun deleteItinerary(dbId: String) : Boolean {
            return DbAccessor.deleteItinerary(dbId)
        }

        fun getInterests() : List<String> {
            return DbAccessor.getInterests()
        }

        fun setInterests(interests: List<String>) : Boolean {
            return DbAccessor.setInterests(interests)
        }
    }
}