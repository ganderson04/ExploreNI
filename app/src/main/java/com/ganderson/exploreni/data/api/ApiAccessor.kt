package com.ganderson.exploreni.data.api

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ganderson.exploreni.data.api.services.GeocodingService
import com.ganderson.exploreni.entities.api.Event
import com.ganderson.exploreni.entities.api.NiLocation
import com.ganderson.exploreni.entities.api.Weather
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiAccessor {
    companion object {
        private const val API_ERROR_CODE = 404

        fun getNearbyLocations(lat: Double, lon: Double, radius:Int)
                : LiveData<List<NiLocation>> {
            val data = MutableLiveData<List<NiLocation>>()

            val call = ApiServices.exploreLocationService.getNearbyLocations(lat, lon, radius)
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

            val call = ApiServices.exploreLocationService.getLocationsByType(type)
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

            val call = ApiServices.exploreEventService.getEvents()
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

            val call = ApiServices.exploreLocationService.performSearch(query)
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

            val geocodingCall = ApiServices.geocodingService.reverseGeocode(geocodingData)
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
            val weatherCall = ApiServices.weatherService.getCurrentWeather(weatherData)
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
    }
}