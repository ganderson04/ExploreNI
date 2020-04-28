package com.ganderson.exploreni

import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.Itinerary
import com.ganderson.exploreni.entities.data.api.NiLocation
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.fragments.ItineraryViewerFragment
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ItineraryViewerFragmentTest {
    private val itineraryName = "DbItineraryName"
    private lateinit var savedItinerary: Itinerary

    @get:Rule val activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun showFragment() {
        insertItinerary()
        savedItinerary = ExploreRepository.getItinerary(itineraryName)
        val fragment = ItineraryViewerFragment(false, savedItinerary)
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
        onView(withId(R.id.rvItinerary))
            .check(matches(isDisplayed()))
    }

    @Test
    fun openNameChangeDialogTest() {
        onView(withId(R.id.tvItineraryName))
            .perform(click())

        onView(withText("Change itinerary name"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun removeItemFromItineraryTest() {
        onView(withId(R.id.ibRemoveItem))
            .perform(click())

        // Confirm removal
        onView(withText("Yes"))
            .perform(click())

        // Confirm empty itinerary deletion
        onView(withText("Yes"))
            .perform(click())

        assertFalse(ExploreRepository.isDuplicateItineraryName(savedItinerary.name))
    }

    @Test
    fun openExploreTest() {
        onView(withId(R.id.tb_add_location))
            .perform(click())

        onView(withId(R.id.etSearch))
            .check(matches(isDisplayed()))
    }

    @Test
    fun openAndCloseExploreTest() {
        onView(withId(R.id.tb_add_location))
            .perform(click())

        onView(withId(R.id.etSearch))
            .check(matches(isDisplayed()))

        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description))
            .perform(click())

        onView(withId(R.id.tvItineraryName))
            .check(matches(isDisplayed()))
    }

    @Test
    fun openItineraryMapTest() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        onView(withId(R.id.tb_itinerary_map))
            .perform(click())

        onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.toolbar))))
            .check(matches(withText("Itinerary Map")))
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun openAndCloseItineraryMapTest() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        onView(withId(R.id.tb_itinerary_map))
            .perform(click())

        onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.toolbar))))
            .check(matches(withText("Itinerary Map")))

        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description))
            .perform(click())

        onView(withId(R.id.tvItineraryName))
            .check(matches(isDisplayed()))
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }
}