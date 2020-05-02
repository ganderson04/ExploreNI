package com.ganderson.exploreni.data.api

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ganderson.exploreni.data.api.services.GoogleService
import com.ganderson.exploreni.entities.Itinerary
import com.ganderson.exploreni.entities.data.DataResult
import com.ganderson.exploreni.entities.data.api.Event
import com.ganderson.exploreni.entities.data.api.NiLocation
import com.ganderson.exploreni.entities.data.api.Weather
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiAccessor {
    companion object {
        private const val API_ERROR_CODE = 404

        fun getNearbyLocations(lat: Double, lon: Double, radius:Int)
                : LiveData<DataResult<List<NiLocation>>> {
            // LiveData is used to publish the result of the asynchronous API calls to attached
            // observers.
            val data = MutableLiveData<DataResult<List<NiLocation>>>()

            // Make, enqueue and process the call.
            val call = ApiServices.exploreLocationService.getNearbyLocations(lat, lon, radius)
            call.enqueue(object: Callback<List<NiLocation>> {
                override fun onFailure(call: Call<List<NiLocation>>, t: Throwable) {
                    data.value = DataResult(null, t)
                }

                override fun onResponse(call: Call<List<NiLocation>>,
                                        response: Response<List<NiLocation>>) {
                    if(response.code() != API_ERROR_CODE) {
                        data.value = DataResult(response.body(), null)
                    }
                    else {
                        data.value = DataResult(emptyList(), null)
                    }
                }
            })

            return data
        }

        fun getLocationsByType(type: String) : LiveData<DataResult<List<NiLocation>>> {
            val data = MutableLiveData<DataResult<List<NiLocation>>>()

            val call = ApiServices.exploreLocationService.getLocationsByType(type)
            call.enqueue(object: Callback<List<NiLocation>> {
                override fun onFailure(call: Call<List<NiLocation>>, t: Throwable) {
                    data.value = DataResult(null, t)
                }

                override fun onResponse(call: Call<List<NiLocation>>,
                                        response: Response<List<NiLocation>>) {
                    if(response.code() != API_ERROR_CODE) {
                        data.value = DataResult(response.body(), null)
                    }
                    else {
                        data.value = DataResult(emptyList(), null)
                    }
                }
            })

            return data
        }

        fun getEvents() : LiveData<DataResult<List<Event>>> {
            val data = MutableLiveData<DataResult<List<Event>>>()

            val call = ApiServices.exploreEventService.getEvents()
            call.enqueue(object: Callback<List<Event>> {
                override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                    data.value = DataResult(null, t)
                }

                override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                    if(response.code() != API_ERROR_CODE) {
                        data.value = DataResult(response.body(), null)
                    }
                    else {
                        data.value = DataResult(emptyList(), null)
                    }
                }
            })

            return data
        }

        fun performSearch(query: String) : LiveData<DataResult<List<NiLocation>>> {
            val data = MutableLiveData<DataResult<List<NiLocation>>>()

            val call = ApiServices.exploreLocationService.performSearch(query)
            call.enqueue(object: Callback<List<NiLocation>> {
                override fun onFailure(call: Call<List<NiLocation>>, t: Throwable) {
                    data.value = DataResult(null, t)
                }

                override fun onResponse(call: Call<List<NiLocation>>,
                                        response: Response<List<NiLocation>>) {
                    if(response.code() != API_ERROR_CODE) {
                        data.value = DataResult(response.body(), null)
                    }
                    else {
                        data.value = DataResult(emptyList(), null)
                    }
                }
            })

            return data
        }

        fun getLocationName(lat: Double, lon: Double, apiKey: String) :
                LiveData<DataResult<String>> {
            val data = MutableLiveData<DataResult<String>>()

            // Assemble the required parameters.
            val geocodingData = HashMap<String, String>()
            geocodingData["latlng"] = "$lat,$lon"
            geocodingData["result_type"] = GoogleService.GEOCODING_RESULT_TYPE
            geocodingData["key"] = apiKey

            val geocodingCall = ApiServices.geocodingService.reverseGeocode(geocodingData)
            geocodingCall.enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    data.value = DataResult(null, t)
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    data.value = DataResult(response.body(), null)
                }
            })

            return data
        }

        fun getWeather(lat: Double, lon: Double, useFahrenheit: Boolean, apiKey: String)
                : LiveData<DataResult<Weather>> {
            val data = MutableLiveData<DataResult<Weather>>()

            var units = "metric"
            if(useFahrenheit) {
                units = "imperial"
            }

            val weatherData = HashMap<String, String>()
            weatherData["lat"] = lat.toString()
            weatherData["lon"] = lon.toString()
            weatherData["units"] = units
            weatherData["APPID"] = apiKey

            val weatherCall = ApiServices.weatherService.getCurrentWeather(weatherData)
            weatherCall.enqueue(object : Callback<Weather> {
                override fun onFailure(call: Call<Weather>, t: Throwable) {
                    data.value = DataResult(null, t)
                }

                override fun onResponse(call: Call<Weather>,
                                        response: Response<Weather>) {
                    data.value = DataResult(response.body(), null)
                }
            })

            return data
        }

        fun calculateDuration(itinerary: Itinerary, userLocation: Location?,
                              apiKey: String): LiveData<DataResult<Int>> {
            val itemList = itinerary.itemList
            val data = MutableLiveData<DataResult<Int>>()

            // "beginIndex" represents the index from which to start adding locations to the
            // "origins" parameter in the API call. If the user's location is available, begin at
            // index 0 in the itinerary's location list as it has not been accessed yet, otherwise
            // begin at index 1 as the first location has been used for the first origin.
            var beginIndex = 0
            var origins: String
            if(userLocation != null) {
                origins = "${userLocation.latitude},${userLocation.longitude}"
            }
            else {
                origins = "${itemList[0].lat},${itemList[0].long}"
                beginIndex = 1
            }
            var destinations = "${itemList[beginIndex].lat},${itemList[beginIndex].long}"

            for(i in beginIndex until itemList.size-1) {
                origins += "|" + "${itemList[i].lat},${itemList[i].long}"
            }

            // The first "origin" is not included in the destinations list.
            for(i in beginIndex+1 until itemList.size) {
                destinations += "|" + "${itemList[i].lat},${itemList[i].long}"
            }

            val params = HashMap<String, String>()
            params["origins"] = origins
            params["destinations"] = destinations
            params["key"] = apiKey

            val durationCall = ApiServices.durationService.getItineraryDuration(params)
            durationCall.enqueue(object: Callback<Int> {
                override fun onFailure(call: Call<Int>, t: Throwable) {
                    data.value = DataResult(null, t)
                }

                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    data.value = DataResult(response.body(), null)
                }
            })

            return data
        }

        fun getItineraryPolyline(itinerary: Itinerary, userLocation: Location?,
                                 apiKey: String) : LiveData<DataResult<String>> {
            val itemList = itinerary.itemList
            val data = MutableLiveData<DataResult<String>>()

            // The same principle is used here as above with regards to "beginIndex".
            var beginIndex = 1
            val origin: String
            if(userLocation != null) {
                origin = "${userLocation.latitude},${userLocation.longitude}"
                beginIndex = 0
            }
            else {
                origin = "${itemList[0].lat},${itemList[0].long}"
            }
            val destination = "${itemList.last().lat},${itemList.last().long}"

            var waypoints = ""
            for(i in beginIndex until itemList.size) {
                val loc = itemList[i]
                waypoints += "via:${loc.lat},${loc.long}"
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
                    data.value = DataResult(null, t)
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    data.value = DataResult(response.body(), null)
                }
            })

            return data
        }
    }
}