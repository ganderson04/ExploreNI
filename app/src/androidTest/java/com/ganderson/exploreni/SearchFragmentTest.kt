package com.ganderson.exploreni

import android.widget.TextView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.components.adapters.LocationAdapter
import com.ganderson.exploreni.ui.fragments.SearchFragment
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchFragmentTest {
    private val query = "Castle"

    @get:Rule val activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun showFragment() {
        val fragment = SearchFragment(query)
        activityRule.activity.displayFragment(fragment)
    }

    @Before
    fun registerIdlingResource() {
        // IdlingResources for a test must be registered and unregistered each time.
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun launchFragmentTest() {
        onView(withId(R.id.rvSearchResults))
            .check(matches(isDisplayed()))

        onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.toolbar))))
            .check(matches(withText("Results for \"$query\"")))
    }

    @Test
    fun openAttractionDetailTest() {
        onView(withId(R.id.rvSearchResults))
            .perform(RecyclerViewActions
                .actionOnItemAtPosition<LocationAdapter.LocationViewHolder>(0, click()))

        onView(withId(R.id.ivAttraction))
            .check(matches(isDisplayed()))
    }

    @Test
    fun openAndCloseAttractionDetailTest() {
        onView(withId(R.id.rvSearchResults))
            .perform(RecyclerViewActions
                .actionOnItemAtPosition<LocationAdapter.LocationViewHolder>(0, click()))

        onView(withId(R.id.ivAttraction))
            .check(matches(isDisplayed()))

        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description))
            .perform(click())

        onView(withId(R.id.rvSearchResults))
            .check(matches(isDisplayed()))
    }
}