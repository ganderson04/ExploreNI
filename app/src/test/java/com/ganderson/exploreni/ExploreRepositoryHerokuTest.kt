package com.ganderson.exploreni

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.LocationType
import com.jraska.livedata.test
import org.junit.Rule
import org.junit.Test

// Heroku tests are not instrumented as they do not require access to API keys via the app's
// resources.
class ExploreRepositoryHerokuTest {
    private val latitude = 54.597181
    private val longitude = -5.930148
    private val radius = 5 // miles

    // Used to execute background tasks synchronously.
    @get:Rule val taskRule = InstantTaskExecutorRule()

    @Test
    fun getNearbyLocationsTest() {
        ExploreRepository
            .getNearbyLocations(latitude, longitude, radius)
            .test()
            .awaitValue()
            .assertHasValue()
    }

    @Test
    fun getLocationsByTypeSeeTest() {
        ExploreRepository
            .getLocationsByType(LocationType.SEE.name)
            .test()
            .awaitValue()
            .assertHasValue()
    }

    @Test
    fun getLocationsByTypeDoTest() {
        ExploreRepository
            .getLocationsByType(LocationType.DO.name)
            .test()
            .awaitValue()
            .assertHasValue()
    }

    @Test
    fun getLocationsByTypeStayTest() {
        ExploreRepository
            .getLocationsByType(LocationType.STAY.name)
            .test()
            .awaitValue()
            .assertHasValue()
    }

    @Test
    fun getLocationsByTypeEatTest() {
        ExploreRepository
            .getLocationsByType(LocationType.EAT.name)
            .test()
            .awaitValue()
            .assertHasValue()
    }

    @Test
    fun getEvents() {
        ExploreRepository
            .getEvents()
            .test()
            .awaitValue()
            .assertHasValue()
    }

    @Test
    fun performSearch() {
        ExploreRepository
            .performSearch("Giant's Causeway")
            .test()
            .awaitValue()
            .assertHasValue()
    }
}