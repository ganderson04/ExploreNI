package com.ganderson.exploreni.api.services

import com.ganderson.exploreni.models.api.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

/**
 * Represents the API endpoints required from the OpenWeatherMap service. This is used by
 * Retrofit.
 */
interface WeatherService {

    companion object {
        const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    }

    @GET("weather?")
    fun getCurrentWeather(@QueryMap params: Map<String, String>) : Call<WeatherResponse>
}