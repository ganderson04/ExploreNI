package com.ganderson.exploreni

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.Itinerary
import com.ganderson.exploreni.entities.api.NiLocation
import com.ganderson.exploreni.ui.activities.MainActivity
import com.jraska.livedata.test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExploreRepositoryDbTest {
    private val niLocation = NiLocation("123", "DbTest", 0f, "", "54.642748",
        "-5.942263", "", "", "", ArrayList())
    private val itineraryName = "DbItineraryName"
    private lateinit var itinerary: Itinerary
    private val interests = ArrayList<String>().apply {
        add("Castles")
        add("Mythology")
    }

    // Used to execute background tasks synchronously.
    @get:Rule val taskRule = InstantTaskExecutorRule()

    // Instance of MainActivity created as the Couchbase DB is initialised within.
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun constructItinerary() {
        val dbId = ""
        val niLocation = NiLocation("", "", 0f, "", "54.642748",
            "-5.942263", "", "", "", ArrayList())
        val locationList = ArrayList<NiLocation>()
        locationList.add(niLocation)
        itinerary = Itinerary(dbId, itineraryName, locationList)
    }

    @Test
    fun addFavouriteLocationTest() {
        assertTrue(ExploreRepository.addFavouriteLocation(niLocation))
        assertTrue(ExploreRepository.isFavouriteLocation("123"))
        ExploreRepository.removeFavouriteLocation("123")
    }

    @Test
    fun removeFavouriteLocationTest() {
        ExploreRepository.addFavouriteLocation(niLocation)
        assertTrue(ExploreRepository.removeFavouriteLocation("123"))
        assertFalse(ExploreRepository.isFavouriteLocation("123"))
    }

    @Test
    fun saveItineraryTest() {
        assertTrue(ExploreRepository.saveItinerary(itinerary))

        val savedItinerary = ExploreRepository.getItineraries()
            .test()
            .awaitValue()
            .value()[0]
        assertTrue(savedItinerary.name == itineraryName)
        ExploreRepository.deleteItinerary(savedItinerary.dbId)
    }

    @Test
    fun deleteItineraryTest() {
        assertTrue(ExploreRepository.saveItinerary(itinerary))

        val savedItinerary = ExploreRepository.getItineraries()
            .test()
            .awaitValue()
            .value()[0]
        assertTrue(savedItinerary.name == itineraryName)
        assertTrue(ExploreRepository.deleteItinerary(savedItinerary.dbId))
    }

    @Test
    fun setInterests() {
        assertTrue(ExploreRepository.setInterests(interests))
        ExploreRepository.setInterests(ArrayList())
    }

    @Test
    fun getInterests() {
        assertTrue(ExploreRepository.setInterests(interests))
        val savedInterests = ExploreRepository.getInterests()
        assertTrue(savedInterests[0] == interests[0] &&
                savedInterests[1] == interests[1])
        ExploreRepository.setInterests((ArrayList()))
    }
}