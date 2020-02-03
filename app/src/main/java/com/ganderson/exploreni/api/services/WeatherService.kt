package com.ganderson.exploreni.api.services

import com.ganderson.exploreni.entities.api.Weather
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap
import java.lang.reflect.Type

/**
 * Represents the API endpoints required from the OpenWeatherMap service. This is used by
 * Retrofit.
 */
interface WeatherService {

    companion object {
        const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    }

    @GET("weather?")
    fun getCurrentWeather(@QueryMap params: Map<String, String>) : Call<Weather>

    class WeatherDeserialiser : JsonDeserializer<Weather> {
        override fun deserialize(json: JsonElement?, typeOfT: Type?,
                                 context: JsonDeserializationContext?): Weather? {
            json?.let {
                val weatherResponse = it.asJsonObject

                val mainObject = weatherResponse.getAsJsonObject("main")
                val temp = mainObject.get("temp").asDouble

                val weatherArray = weatherResponse.getAsJsonArray("weather")
                val desc = weatherArray.get(0).asJsonObject.get("main").asString

                return Weather(desc, temp)
            }
            return null
        }

    }
}