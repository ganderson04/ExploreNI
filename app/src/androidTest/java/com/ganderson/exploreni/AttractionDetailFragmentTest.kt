package com.ganderson.exploreni

import android.app.Instrumentation
import android.content.Intent
import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
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
        "54.642748", "-5.942263", "", "", "", "https://google.co.uk",
        ArrayList())

    // Used to obtain a reference to the Activity onto which the Fragment under consideration will
    // be placed.
    // When using JUnit Rules in Kotlin, it is necessary to mark them with the "@get" annotation
    // as opposed to "@Rule" which would be used in Java. "@get" instructs the JVM to generate
    // a getter for a specific property and is sometimes required for Java-Kotlin
    // interoperability. Here "@get:Rule" is used to apply the "@Rule" annotation to the property
    // so that JUnit can find it.
    // Ref: https://kotlinlang.org/docs/reference/annotations.html#java-annotations
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

    @Test
    fun openWebsiteTest() {
        // Espresso testing would stop when the app hands over to the device's default web browser.
        // The Intent which loads the specified URL in the browser is intercepted here instead and
        // its contents examined to prove it contains the expected information.
        // Solution adapted from: https://stackoverflow.com/a/35067831/8100469
        Intents.init()

        val expectedIntent = allOf(
            IntentMatchers.hasAction(Intent.ACTION_VIEW),
            IntentMatchers.hasData(niLocation.website)
        )
        Intents.intending(expectedIntent).respondWith(Instrumentation.ActivityResult(0, null))

        onView(withId(R.id.btnWebsite))
            .perform(click())

        Intents.intended(expectedIntent)
        Intents.release()
    }
}