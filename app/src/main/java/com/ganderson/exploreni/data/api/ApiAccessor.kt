package com.ganderson.exploreni.data.api

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ganderson.exploreni.data.api.services.ExploreService
import com.ganderson.exploreni.data.api.services.GeocodingService
import com.ganderson.exploreni.data.api.services.WeatherService
import com.ganderson.exploreni.entities.api.Event
import com.ganderson.exploreni.entities.api.NiLocation
import com.ganderson.exploreni.entities.api.Weather
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiAccessor {
    companion object {
        private const val API_ERROR_CODE = 404
        private val exploreLocationService by lazy { constructExploreLocationService() }
        private val exploreEventService by lazy { constructExploreEventService() }
        private val geocodingService by lazy { constructGeocodingService() }
        private val weatherService by lazy { constructWeatherService() }

        fun getNearbyLocations(lat: Double, lon: Double, radius:Int)
                : LiveData<List<NiLocation>> {
            val data = MutableLiveData<List<NiLocation>>()

            val call = exploreLocationService.getNearbyLocations(lat, lon, radius)
            call.enqueue(object: Callback<List<NiLocation>> {
                override fun onFailure(call: Call<List<NiLocation>>, t: Throwable) {
                    throw t
                }

                override fun onResponse(call: Call<List<NiLocation>>,
                                        response: Response<List<NiLocation>>) {
                    if(response.code() != API_ERROR_CODE) {
                        data.value = response.body()
                    }
                    else {
                        data.value = emptyList()
                    }
                }
            })

            return data
        }

        fun getLocationsByType(type: String) : LiveData<List<NiLocation>> {
            val data = MutableLiveData<List<NiLocation>>()

            val call = exploreLocationService.getLocationsByType(type)
            call.enqueue(object: Callback<List<NiLocation>> {
                override fun onFailure(call: Call<List<NiLocation>>, t: Throwable) {
                    throw t
                }

                override fun onResponse(call: Call<List<NiLocation>>,
                                        response: Response<List<NiLocation>>) {
                    if(response.code() != API_ERROR_CODE) {
                        data.value = response.body()
                    }
                    else {
                        data.value = emptyList()
                    }
                }
            })

            return data
        }

        fun getEvents() : LiveData<List<Event>> {
            val data = MutableLiveData<List<Event>>()

            val call = exploreEventService.getEvents()
            call.enqueue(object: Callback<List<Event>> {
                override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                    throw t
                }

                override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                    if(response.code() != API_ERROR_CODE) {
                        data.value = response.body()
                    }
                    else {
                        data.value = emptyList()
                    }
                }

            })

            return data
        }

        fun performSearch(query: String) : LiveData<List<NiLocation>> {
            val data = MutableLiveData<List<NiLocation>>()

            val call = exploreLocationService.performSearch(query)
            call.enqueue(object: Callback<List<NiLocation>> {
                override fun onFailure(call: Call<List<NiLocation>>, t: Throwable) {
                    throw t
                }

                override fun onResponse(call: Call<List<NiLocation>>,
                                        response: Response<List<NiLocation>>) {
                    if(response.code() != API_ERROR_CODE) {
                        data.value = response.body()
                    }
                    else {
                        data.value = emptyList()
                    }
                }
            })

            return data
        }

        fun getLocationName(lat: Double, lon: Double, apiKey: String) : LiveData<String> {
            val data = MutableLiveData<String>()

            val geocodingData = HashMap<String, String>()
            geocodingData["latlng"] = "$lat,$lon"
            geocodingData["result_type"] = GeocodingService.RESULT_TYPE
            geocodingData["key"] = apiKey

            val geocodingCall = geocodingService.reverseGeocode(geocodingData)
            geocodingCall.enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    throw t
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    data.value = response.body()
                }
            })

            return data
        }

        fun getWeather(lat: Double, lon: Double, useFahrenheit: Boolean, apiKey: String)
                : LiveData<Weather> {
            val data = MutableLiveData<Weather>()

            var units = "metric"
            if(useFahrenheit) {
                units = "imperial"
            }

            // Assemble parameters for OpenWeatherMap API call.
            val weatherData = HashMap<String, String>()
            weatherData["lat"] = lat.toString()
            weatherData["lon"] = lon.toString()
            weatherData["units"] = units
            weatherData["APPID"] = apiKey

            // Make, enqueue and process the call.
            val weatherCall = weatherService.getCurrentWeather(weatherData)
            weatherCall.enqueue(object : Callback<Weather> {
                override fun onFailure(call: Call<Weather>, t: Throwable) {
                    throw t
                }

                override fun onResponse(call: Call<Weather>,
                                        response: Response<Weather>) {
                    data.value = response.body()
                }
            })

            return data
        }

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

        private fun constructGeocodingService() : GeocodingService {
            val geocodingDeserialiser = GsonBuilder()
                .registerTypeAdapter(String::class.java, GeocodingService.GeocodingDeserialiser())
                .create()

            return Retrofit.Builder()
                .baseUrl(GeocodingService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(geocodingDeserialiser))
                .build()
                .create(GeocodingService::class.java)
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