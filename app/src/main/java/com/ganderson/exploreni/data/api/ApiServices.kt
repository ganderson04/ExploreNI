package com.ganderson.exploreni.data.api

import com.ganderson.exploreni.data.api.services.ExploreService
import com.ganderson.exploreni.data.api.services.GoogleService
import com.ganderson.exploreni.data.api.services.WeatherService
import com.ganderson.exploreni.entities.api.Event
import com.ganderson.exploreni.entities.api.NiLocation
import com.ganderson.exploreni.entities.api.Weather
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiServices {
    companion object {
        val exploreLocationService by lazy { constructExploreLocationService() }
        val exploreEventService by lazy { constructExploreEventService() }
        val geocodingService by lazy { constructGeocodingService() }
        val weatherService by lazy { constructWeatherService() }

        private fun constructExploreLocationService() : ExploreService {
            val locationDeserialiser = GsonBuilder()
                .registerTypeAdapter(NiLocation::class.java, ExploreService.LocationDeserialiser())
                .create()

            return Retrofit.Builder()
                .baseUrl(ExploreService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(locationDeserialiser))
                .build()
                .create(ExploreService::class.java)
        }

        private fun constructExploreEventService() : ExploreService {
            val eventDeserialiser = GsonBuilder()
                .registerTypeAdapter(Event::class.java, ExploreService.EventDeserialiser())
                .create()

            return Retrofit.Builder()
                .baseUrl(ExploreService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(eventDeserialiser))
                .build()
                .create(ExploreService::class.java)
        }

        private fun constructGeocodingService() : GoogleService {
            val geocodingDeserialiser = GsonBuilder()
                .registerTypeAdapter(String::class.java, GoogleService.GeocodingDeserialiser())
                .create()

            return Retrofit.Builder()
                .baseUrl(GoogleService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(geocodingDeserialiser))
                .build()
                .create(GoogleService::class.java)
        }

        private fun constructWeatherService() : WeatherService {
            val weatherDeserialiser = GsonBuilder()
                .registerTypeAdapter(Weather::class.java, WeatherService.WeatherDeserialiser())
                .create()

            return Retrofit.Builder()
                .baseUrl(WeatherService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(weatherDeserialiser))
                .build()
                .create(WeatherService::class.java)
        }
    }
}