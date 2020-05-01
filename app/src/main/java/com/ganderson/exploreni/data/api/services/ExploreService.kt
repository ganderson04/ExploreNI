package com.ganderson.exploreni.data.api.services

import com.ganderson.exploreni.entities.data.api.Event
import com.ganderson.exploreni.entities.data.api.NiLocation
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import java.lang.reflect.Type
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

/**
 * Represents the endpoints used to obtain information stored in the Explore NI database accessible
 * through the Heroku API service.
 */
interface ExploreService {
    companion object {
        const val BASE_URL = "https://explore-ni-api.herokuapp.com/api/"
        val eventDateFormat = SimpleDateFormat("E, d MMM yyyy H:m:s z", Locale.UK)
    }

    @POST("locations/nearby")
    @FormUrlEncoded
    fun getNearbyLocations(@Field("lat") lat: Double,
                           @Field("lon") lon: Double,
                           @Field("radius") radius: Int) : Call<List<NiLocation>>

    @POST("locations")
    @FormUrlEncoded
    fun getLocationsByType(@Field("type") type: String) : Call<List<NiLocation>>

    @GET("events")
    fun getEvents() : Call<List<Event>>

    @POST("search")
    @FormUrlEncoded
    fun performSearch(@Field("query") query: String) : Call<List<NiLocation>>

    class LocationDeserialiser : JsonDeserializer<NiLocation> {
        override fun deserialize(json: JsonElement?, typeOfT: Type?,
                                 context: JsonDeserializationContext?): NiLocation? {
            json?.let {
                val locationResponse = it.asJsonObject
                if(!locationResponse.has("error")) {

                    val id = locationResponse.get("_id").asString
                    val name = locationResponse.get("name").asString
                    val elevation = locationResponse.get("elevation").asFloat
                    val town = locationResponse.get("town").asString
                    val desc = locationResponse.get("desc").asString
                    val imgUrl = locationResponse.get("imgUrl").asString
                    val imgAttr = locationResponse.get("imgAttr").asString
                    val website = locationResponse.get("website").asString
                    val locTags = Gson().fromJson<List<String>>(locationResponse["locTags"],
                        object: TypeToken<List<String>>(){}.type)

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
                        imgAttr,
                        website,
                        locTags
                    )
                }
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
                val startDate = eventDateFormat.parse(eventResponse.get("startDate").asString)
                val endDate = eventDateFormat.parse(eventResponse.get("endDate").asString)
                val imgUrl = eventResponse.get("imgUrl").asString
                val imgAttr = eventResponse.get("imgAttr").asString
                val website = eventResponse.get("website").asString

                return Event(id, name, desc, startDate, endDate, imgUrl, imgAttr, website)
            }
            return null
        }

    }
}