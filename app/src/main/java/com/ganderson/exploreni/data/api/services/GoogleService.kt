package com.ganderson.exploreni.data.api.services

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap
import java.lang.reflect.Type

/**
 * Represents the Google Maps API endpoints used in the application.
 */
interface GoogleService {

    companion object {
        const val BASE_URL = "https://maps.googleapis.com/maps/api/"
        const val GEOCODING_RESULT_TYPE = "postal_town"
    }

    @GET("geocode/json?")
    fun reverseGeocode(@QueryMap params: Map<String, String>) : Call<String>

    @GET("distancematrix/json?units=imperial")
    fun getItineraryDuration(@QueryMap params: Map<String, String>) : Call<Int>

    @GET("directions/json?")
    fun getPolyline(@QueryMap params: Map<String, String>) : Call<String>

    class GeocodingDeserialiser : JsonDeserializer<String> {
        override fun deserialize(json: JsonElement?, typeOfT: Type?,
            context: JsonDeserializationContext?): String? {
            json?.let {
                val geocodingResponse = json.asJsonObject

                val resultsObject = geocodingResponse
                    .getAsJsonArray("results")
                    .get(0)
                    .asJsonObject

                val postalTown = resultsObject
                    .getAsJsonArray("address_components")
                    .get(0)
                    .asJsonObject

                return postalTown.get("long_name")?.asString
            }
            return null
        }
    }

    class DurationDeserialiser : JsonDeserializer<Int> {
        override fun deserialize(json: JsonElement?, typeOfT: Type?,
                                 context: JsonDeserializationContext?): Int? {
            json?.let {
                val durationResponse = it.asJsonObject
                val rows = durationResponse.getAsJsonArray("rows")
                var duration = 0
                for(i in 0 until rows.size()) {
                    val row = rows[i].asJsonObject
                    val elements = row.getAsJsonArray("elements")
                    val element = elements[i].asJsonObject
                    val durationObject = element.getAsJsonObject("duration")
                    duration += durationObject.get("value").asInt
                }
                return duration
            }
            return null
        }
    }

    class PolylineDeserialiser : JsonDeserializer<String> {
        override fun deserialize(json: JsonElement?, typeOfT: Type?,
                                 context: JsonDeserializationContext?): String? {
            json?.let {
                val polylineResponse = it.asJsonObject
                val routesArray = polylineResponse.getAsJsonArray("routes")
                val route = routesArray[0].asJsonObject
                val polylineObject = route.get("overview_polyline").asJsonObject
                return polylineObject.get("points").asString
            }
            return null
        }
    }
}