package com.ganderson.exploreni.data.api

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ganderson.exploreni.data.api.services.GoogleService
import com.ganderson.exploreni.entities.Itinerary
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
            geocodingData["result_type"] = GoogleService.GEOCODING_RESULT_TYPE
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

        fun calculateDuration(itinerary: Itinerary, apiKey: String): LiveData<Int> {
            val itemList = itinerary.itemList
            val data = MutableLiveData<Int>()

            var origins = "${itemList[0].lat},${itemList[0].long}"
            var destinations = "${itemList[1].lat},${itemList[1].long}"

            for(i in 1 until itemList.size-1) {
                origins += "|" + "${itemList[i].lat},${itemList[i].long}"
            }

            for(i in 2 until itemList.size) {
                destinations += "|" + "${itemList[i].lat},${itemList[i].long}"
            }

            val params = HashMap<String, String>()
            params["origins"] = origins
            params["destinations"] = destinations
            params["key"] = apiKey

            val durationCall = ApiServices.durationService.getItineraryDuration(params)
            durationCall.enqueue(object: Callback<Int> {
                override fun onFailure(call: Call<Int>, t: Throwable) {
                    throw t
                }

                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    data.value = response.body()
                }
            })

            return data
        }

        fun getItineraryPolyline(itinerary: Itinerary, apiKey: String) : LiveData<String> {
            val itemList = itinerary.itemList
            val data = MutableLiveData<String>()

            val origin = "${itemList[0].lat},${itemList[0].long}"
            val destination = "${itemList.last().lat},${itemList.last().long}"

            var waypoints = ""
            for(i in 1 until itemList.size) {
                val loc = itemList[i]
                waypoints += "via:$loc"
                if(i < itemList.size-1) waypoints += "|"
            }

            val params = HashMap<String, String>()
            params["origin"] = origin
            params["destination"] = destination
            params["key"] = apiKey
            if(waypoints.isNotBlank()) {
                params["waypoints"] = waypoints
            }
            val polylineCall = ApiServices.polylineService.getPolyline(params)
            polylineCall.enqueue(object: Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    throw t
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    data.value = response.body()
                }
            })

            return data
        }
    }
}