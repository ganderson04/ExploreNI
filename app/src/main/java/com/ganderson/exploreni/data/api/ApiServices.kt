package com.ganderson.exploreni.data.api

import com.ganderson.exploreni.data.api.services.ExploreService
import com.ganderson.exploreni.data.api.services.GoogleService
import com.ganderson.exploreni.data.api.services.WeatherService
import com.ganderson.exploreni.entities.data.api.Event
import com.ganderson.exploreni.entities.data.api.NiLocation
import com.ganderson.exploreni.entities.data.api.Weather
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Provides implementations of the defined API services combined with the appropriate deserialisers.
 * As some services have multiple deserialisers, they appear here multiple times.
 */
class ApiServices {
    companion object {
        // "by lazy" creates the object when it is first needed by the application during runtime.
        val exploreLocationService by lazy { constructExploreLocationService() }
        val exploreEventService by lazy { constructExploreEventService() }
        val geocodingService by lazy { constructGeocodingService() }
        val durationService by lazy { constructDurationService() }
        val polylineService by lazy { constructPolylineService() }
        val weatherService by lazy { constructWeatherService() }

        private fun constructExploreLocationService() : ExploreService {
            // Construct the appropriate deserialiser for this iteration of the service.
            val locationDeserialiser = GsonBuilder()
                .registerTypeAdapter(NiLocation::class.java, ExploreService.LocationDeserialiser())
                .create()

            // Create an implementation of the service with the deserialiser.
            // Classes in Kotlin are implementations of "KClass". "class.java" returns the
            // equivalent Java "Class".
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

        private fun constructDurationService() : GoogleService {
            val durationDeserialiser = GsonBuilder()
                .registerTypeAdapter(Integer::class.java, GoogleService.DurationDeserialiser())
                .create()

            return Retrofit.Builder()
                .baseUrl(GoogleService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(durationDeserialiser))
                .build()
                .create(GoogleService::class.java)
        }

        private fun constructPolylineService() : GoogleService {
            val polylineDeserialiser = GsonBuilder()
                .registerTypeAdapter(String::class.java, GoogleService.PolylineDeserialiser())
                .create()

            return Retrofit.Builder()
                .baseUrl(GoogleService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(polylineDeserialiser))
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