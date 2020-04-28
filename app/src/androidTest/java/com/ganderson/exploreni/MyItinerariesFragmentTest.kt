package com.ganderson.exploreni

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.Itinerary
import com.ganderson.exploreni.entities.data.api.NiLocation
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.fragments.MyItinerariesFragment
import org.junit.*
import org.junit.Assert.assertFalse
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MyItinerariesFragmentTest {
    private val itineraryName = "DbItineraryName"
    private lateinit var savedItinerary: Itinerary

    @get:Rule val activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun showFragment() {
        insertItinerary()
        savedItinerary = ExploreRepository.getItinerary(itineraryName)
        val fragment = MyItinerariesFragment()
        activityRule.activity.displayFragment(fragment)
    }

    private fun insertItinerary() {
        val dbId = ""
        val niLocation = NiLocation("123", "DbTest", 0f, "",
            "54.642748", "-5.942263", "", "", "", ArrayList())
        val locationList = ArrayList<NiLocation>()
        locationList.add(niLocation)
        ExploreRepository.saveItinerary(Itinerary(dbId, itineraryName, locationList))
    }

    @After
    fun deleteItinerary() {
        ExploreRepository.deleteItinerary(savedItinerary.dbId)
    }

    @Test
    fun launchFragmentTest() {
        onView(withId(R.id.rvMyItineraries))
            .check(matches(isDisplayed()))
    }

    @Test
    fun itineraryLoadTest() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        onView(withText(savedItinerary.name))
            .check(matches(isDisplayed()))
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun openItineraryViewerTest() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        onView(withId(R.id.rvMyItineraries))
            .perform(RecyclerViewActions.actionOnItemAtPosition
            <MyItinerariesFragment.MyItinerariesAdapter.ItineraryViewHolder>(0, click()))

        onView(withId(R.id.rvItinerary))
            .check(matches(isDisplayed()))
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun openAndCloseItineraryViewerTest() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        onView(withId(R.id.rvMyItineraries))
            .perform(RecyclerViewActions.actionOnItemAtPosition
            <MyItinerariesFragment.MyItinerariesAdapter.ItineraryViewHolder>(0, click()))

        onView(withId(R.id.rvItinerary))
            .check(matches(isDisplayed()))

        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description))
            .perform(click())

        onView(withId(R.id.rvMyItineraries))
            .check(matches(isDisplayed()))
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun deleteItineraryTest() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        onView(withId(R.id.ibRemoveItinerary))
            .perform(click())

        onView(withText("Yes"))
            .perform(click())

        assertFalse(ExploreRepository.isDuplicateItineraryName(savedItinerary.name))
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }
}