package com.ganderson.exploreni

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.ganderson.exploreni.entities.LocationType
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.components.adapters.LocationAdapter
import com.ganderson.exploreni.ui.fragments.ExploreCategoryFragment
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExploreCategoryFragmentTest {
    @get:Rule val activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun showFragment() {
        val fragment = ExploreCategoryFragment(LocationType.SEE)
        activityRule.activity.displayFragment(fragment)
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun launchFragmentTest() {
        onView(withId(R.id.rvLocations))
            .check(matches(isDisplayed()))
    }

    @Test
    fun openAttractionDetailTest() {
        onView(withId(R.id.rvLocations))
            .perform(RecyclerViewActions
                .actionOnItemAtPosition<LocationAdapter.LocationViewHolder>(0, click()))

        onView(withId(R.id.ivAttraction))
            .check(matches(isDisplayed()))
    }

    @Test
    fun openAndCloseAttractionDetailTest() {
        onView(withId(R.id.rvLocations))
            .perform(RecyclerViewActions
                .actionOnItemAtPosition<LocationAdapter.LocationViewHolder>(0, click()))

        onView(withId(R.id.ivAttraction))
            .check(matches(isDisplayed()))

        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description))
            .perform(click())

        onView(withId(R.id.rvLocations))
            .check(matches(isDisplayed()))
    }

    @Test
    fun openSortDialogTest() {
        onView(withId(R.id.tb_sort))
            .perform(click())

        onView(withText("Sort"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun openAndCloseSortDialogTest() {
        onView(withId(R.id.tb_sort))
            .perform(click())

        onView(withText("Sort"))
            .check(matches(isDisplayed()))

        onView(withText("A-Z"))
            .perform(click())

        // "is" is a reserved keyword in Kotlin, so backticks are used to refer to the "is" method
        // in CoreMatchers.
        onView(withId(R.id.rvLocations))
            .inRoot(allOf(withDecorView(`is`(activityRule.activity.window.decorView)), not(isDialog())))
            .check(matches(isDisplayed()))
    }

    @Test
    fun openFilterDialogTest() {
        onView(withId(R.id.tb_filter))
            .perform(click())

        onView(withText("Filter"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun openAndCloseFilterDialogTest() {
        onView(withId(R.id.tb_filter))
            .perform(click())

        onView(withText("Filter"))
            .check(matches(isDisplayed()))

        onView(withText("Close"))
            .perform(click())

        onView(withId(R.id.rvLocations))
            .inRoot(allOf(withDecorView(`is`(activityRule.activity.window.decorView)), not(isDialog())))
            .check(matches(isDisplayed()))
    }
}