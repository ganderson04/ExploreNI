package com.ganderson.exploreni.models.api

import com.google.gson.annotations.SerializedName

/**
 * Represents the parts of the OpenWeatherMap API response that are used for this app. Through the
 * use of the "SerializedName" annotation, Retrofit can map the fields of the JSON response to the
 * fields of the class.
 */
data class WeatherResponse(
    @SerializedName("main") val main: Main,
    @SerializedName("weather") val weather: List<Weather> = ArrayList()
)

/**
 * Represents the JSON object called "main" within the response. Uses only the "temp" field.
 */
data class Main(
    @SerializedName("temp") val temp: Double
)

/**
 * Represents the JSON array called "weather" within the response. Uses only the "main" field (this
 * is different to the "main" above). It contains the summarised weather description (e.g. "Clear").
 */
data class Weather(
    @SerializedName("main") val main: String
)