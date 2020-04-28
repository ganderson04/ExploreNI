package com.ganderson.exploreni

import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.data.api.NiLocation
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.fragments.AttractionDetailFragment
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AttractionDetailFragmentTest {
    private val niLocation = NiLocation("123", "FragTest", 0f, "",
        "54.642748", "-5.942263", "", "", "", ArrayList())

    @get:Rule val activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun showFragment() {
        val fragment = AttractionDetailFragment(niLocation, false)
        activityRule.activity.displayFragment(fragment)
    }

    @Test
    fun launchFragmentTest() {
        onView(allOf(instanceOf(TextView::class.java),
            withParent(withId(R.id.toolbar))))
            .check(matches(withText(niLocation.name)))
    }

    @Test
    fun addFavouriteTest() {
        onView(withId(R.id.tb_favourite))
            .perform(click())

        assertTrue(ExploreRepository.isFavouriteLocation(niLocation.id))
        ExploreRepository.removeFavouriteLocation(niLocation.id)
    }

    @Test
    fun removeFavouriteTest() {
        ExploreRepository.addFavouriteLocation(niLocation)

        onView(withId(R.id.tb_favourite))
            .perform(click())

        assertFalse(ExploreRepository.isFavouriteLocation(niLocation.id))
    }

    @Test
    fun openMapViewTest() {
        onView(withId(R.id.tb_map))
            .perform(click())

        onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.toolbar))))
            .check(matches(withText("Attraction Location")))
    }

    @Test
    fun openAndCloseMapViewTest() {
        onView(withId(R.id.tb_map))
            .perform(click())

        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description))
            .perform(click())

        onView(allOf(instanceOf(TextView::class.java),
            withParent(withId(R.id.toolbar))))
            .check(matches(withText(niLocation.name)))
    }
}