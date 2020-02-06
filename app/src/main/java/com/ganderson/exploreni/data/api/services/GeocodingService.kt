package com.ganderson.exploreni.data.api.services

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap
import java.lang.reflect.Type

interface GeocodingService {

    companion object {
        const val BASE_URL = "https://maps.googleapis.com/maps/api/geocode/"
        const val RESULT_TYPE = "postal_town"
    }

    @GET("json?")
    fun reverseGeocode(@QueryMap params: Map<String, String>) : Call<String>

    @GET("json?")
    suspend fun reverseGeocode1(@QueryMap params: Map<String, String>) : String

    class GeocodingDeserialiser : JsonDeserializer<String> {
        override fun deserialize(json: JsonElement?, typeOfT: Type?,
            context: JsonDeserializationContext?): String? {
            val geocodingResponse = json?.asJsonObject

            val resultsObject = geocodingResponse
                ?.getAsJsonArray("results")
                ?.get(0)
                ?.asJsonObject

            val postalTown = resultsObject
                ?.getAsJsonArray("address_components")
                ?.get(0)
                ?.asJsonObject

            return postalTown?.get("long_name")?.asString
        }

    }
}