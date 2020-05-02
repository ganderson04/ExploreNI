package com.ganderson.exploreni

import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.data.api.NiLocation
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.components.adapters.FavouritesAdapter
import com.ganderson.exploreni.ui.fragments.FavouritesFragment
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavouritesFragmentTest {
    private val niLocation = NiLocation("123", "FragTest", 0f, "",
        "54.642748", "-5.942263", "", "", "", "", ArrayList())

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun showFragment() {
        val fragment = FavouritesFragment()
        activityRule.activity.displayFragment(fragment)
    }

    @Before
    fun addFavourite() {
        ExploreRepository.addFavouriteLocation(niLocation)
    }

    @Test
    fun launchFragmentTest() {
        onView(withId(R.id.rvFavourites))
            .check(matches(isDisplayed()))

        // Removal is not placed in an @After method as it would cause the app to crash after
        // testing the "Delete" functionality below.
        ExploreRepository.removeFavouriteLocation(niLocation.id)
    }

    @Test
    fun openAttractionDetailTest() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        onView(withId(R.id.rvFavourites))
            .perform(RecyclerViewActions
                .actionOnItemAtPosition<FavouritesAdapter.FavouritesViewHolder>(0, click()))

        onView(allOf(instanceOf(TextView::class.java),
            withParent(withId(R.id.toolbar))))
            .check(matches(withText(niLocation.name)))
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)

        ExploreRepository.removeFavouriteLocation(niLocation.id)
    }

    @Test
    fun openAndCloseAttractionDetailTest() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        onView(withId(R.id.rvFavourites))
            .perform(RecyclerViewActions
                .actionOnItemAtPosition<FavouritesAdapter.FavouritesViewHolder>(0, click()))

        onView(allOf(instanceOf(TextView::class.java),
            withParent(withId(R.id.toolbar))))
            .check(matches(withText(niLocation.name)))

        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description))
            .perform(click())

        onView(withId(R.id.rvFavourites))
            .check(matches(isDisplayed()))
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)

        ExploreRepository.removeFavouriteLocation(niLocation.id)
    }

    @Test
    fun deleteFavouriteTest() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        onView(withId(R.id.ibRemove))
            .perform(click())

        // Click "Yes" on removal dialog.
        onView(withText("Yes"))
            .perform(click())

        assertFalse(ExploreRepository.isFavouriteLocation(niLocation.id))
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }
}