package com.ganderson.exploreni

import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.Itinerary
import com.ganderson.exploreni.entities.api.NiLocation
import com.ganderson.exploreni.ui.activities.MainActivity
import com.jraska.livedata.test
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExploreRepositoryExternalTest {
    private lateinit var location: Location
    private lateinit var itinerary: Itinerary
    private lateinit var openWeatherMapApiKey: String
    private lateinit var googleApiKey: String

    // Used to execute background tasks synchronously.
    @get:Rule val taskRule = InstantTaskExecutorRule()
    @get:Rule val activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun constructLocation() {
        location = Location("")
        location.latitude = 54.597181
        location.longitude = -5.930148
    }

    @Before
    fun constructItinerary() {
        val dbId = ""
        val name = ""
        val niLocation = NiLocation("", "", 0f, "", "54.642748",
            "-5.942263", "", "", "", ArrayList())
        val locationList = ArrayList<NiLocation>()
        locationList.add(niLocation)
        itinerary = Itinerary(dbId, name, locationList)
    }

    @Before
    fun getApiKeys() {
        openWeatherMapApiKey = activityRule.activity.getString(R.string.openweathermap_api_key)
        googleApiKey = activityRule.activity.getString(R.string.google_api_key)
    }

    @Test
    fun getWeatherCelsiusTest() {
        val useFahrenheit = false
        ExploreRepository
            .getWeather(location.latitude, location.longitude, useFahrenheit, openWeatherMapApiKey)
            .test()
            .awaitValue()
            .assertHasValue()
    }

    @Test
    fun getWeatherFahrenheitTest() {
        val useFahrenheit = true
        ExploreRepository
            .getWeather(location.latitude, location.longitude, useFahrenheit, openWeatherMapApiKey)
            .test()
            .awaitValue()
            .assertHasValue()
    }

    @Test
    fun calculateDurationTest() {
        ExploreRepository
            .calculateDuration(itinerary, location, googleApiKey)
            .test()
            .awaitValue()
            .assertHasValue()
    }

    @Test
    fun getItineraryPolylineTest() {
        ExploreRepository
            .getItineraryPolyline(itinerary, location, googleApiKey)
            .test()
            .awaitValue()
            .assertHasValue()
    }
}