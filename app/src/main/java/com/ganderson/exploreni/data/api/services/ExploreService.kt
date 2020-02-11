package com.ganderson.exploreni.data.api.services

import com.ganderson.exploreni.entities.api.Event
import com.ganderson.exploreni.entities.api.NiLocation
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Represents the endpoints used to obtain information stored in the Explore NI database accessible
 * through the Heroku API service.
 */
interface ExploreService {
    companion object {
        const val BASE_URL = "https://explore-ni-api.herokuapp.com/api/"
        val eventDateFormat = DateTimeFormatter.ofPattern("E, d MMM yyyy H:m:s z")
    }

    @POST("locations/nearby")
    @FormUrlEncoded
    fun getNearbyLocations(@Field("lat") lat: Double,
                           @Field("lon") lon: Double) : Call<List<NiLocation>>

    @GET("events")
    fun getEvents() : Call<List<Event>>

    class LocationDeserialiser : JsonDeserializer<NiLocation> {
        override fun deserialize(json: JsonElement?, typeOfT: Type?,
                                 context: JsonDeserializationContext?): NiLocation? {
            json?.let {
                val locationResponse = it.asJsonObject

                val id = locationResponse.get("_id").asString
                val name = locationResponse.get("name").asString
                val elevation = locationResponse.get("elevation").asFloat
                val town = locationResponse.get("town").asString
                val desc = locationResponse.get("desc").asString
                val imgUrl = locationResponse.get("imgUrl").asString
                val imgAttr = locationResponse.get("imgAttr").asString

                // MongoDB requires the coordinates of a location to be stored in an array within
                // an inner JSON object in order to conduct geoqueries.
                val coordObject = locationResponse.getAsJsonObject("location")
                val coordArray = coordObject.getAsJsonArray("coordinates")

                // While coordinates are typically expressed as "latitude,longitude", MongoDB
                // requires the longitude to come first in the array.
                val lat = coordArray[1].asString
                val long = coordArray[0].asString

                return NiLocation(
                    id,
                    name,
                    elevation,
                    town,
                    lat,
                    long,
                    desc,
                    imgUrl,
                    imgAttr
                )
            }
            return null
        }
    }

    class EventDeserialiser : JsonDeserializer<Event> {
        override fun deserialize(json: JsonElement?, typeOfT: Type?,
                                 context: JsonDeserializationContext?): Event? {
            json?.let {
                val eventResponse = it.asJsonObject

                val id = eventResponse.get("_id").asString
                val name = eventResponse.get("name").asString
                val desc = eventResponse.get("desc").asString
                val startDate = LocalDate
                    .parse(eventResponse.get("startDate").asString,
                        eventDateFormat)
                val endDate = LocalDate
                    .parse(eventResponse.get("endDate").asString,
                        eventDateFormat)
                val imgUrl = eventResponse.get("imgUrl").asString
                val imgAttr = eventResponse.get("imgAttr").asString
                val website = eventResponse.get("website").asString

                return Event(
                    id,
                    name,
                    desc,
                    startDate,
                    endDate,
                    imgUrl,
                    imgAttr,
                    website
                )
            }
            return null
        }

    }
}