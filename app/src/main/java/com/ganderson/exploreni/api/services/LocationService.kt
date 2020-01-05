package com.ganderson.exploreni.api.services

import com.ganderson.exploreni.models.Location
import retrofit2.Call
import retrofit2.http.GET

/**
 * Represents the endpoints used to obtain information on locations stored in the Explore NI
 * database accessible through the Heroku API service.
 */
interface LocationService {
    companion object {
        const val BASE_URL = "https://explore-ni-api.herokuapp.com/api/v1.0/"
    }

    @GET("locations")
    fun getAllLocations() : Call<List<Location>>
}