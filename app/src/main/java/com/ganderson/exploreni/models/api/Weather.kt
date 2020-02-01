package com.ganderson.exploreni.models.api

/**
 * Represents the parts of the OpenWeatherMap API response that are used for this app. Through the
 * use of the "SerializedName" annotation, Retrofit can map the fields of the JSON response to the
 * fields of the class.
 */
data class Weather(val desc: String, val temp: Double)