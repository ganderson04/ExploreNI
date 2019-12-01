package com.ganderson.exploreni.api

import com.ganderson.exploreni.models.api.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface WeatherService {

    companion object {
        const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    }

    @GET("weather?")
    fun getCurrentWeather(@QueryMap params: Map<String, String>) : Call<WeatherResponse>
}