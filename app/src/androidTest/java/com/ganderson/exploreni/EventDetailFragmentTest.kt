package com.ganderson.exploreni

import android.app.Instrumentation
import android.content.Intent
import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.ganderson.exploreni.entities.api.Event
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.fragments.EventDetailFragment
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class EventDetailFragmentTest {
    private val event = Event("", "Test Event", "", LocalDate.now(),
        LocalDate.now(), "", "", "https://www.google.co.uk")

    @get:Rule val activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun showFragment() {
        val fragment = EventDetailFragment(event)
        activityRule.activity.displayFragment(fragment)
    }

    @Test
    fun launchFragmentTest() {
        onView(allOf(instanceOf(TextView::class.java),
            withParent(withId(R.id.toolbar))))
            .check(matches(withText(event.name)))
    }

    @Test
    fun openWebsiteTest() {
        // Espresso testing would stop when the app hands over to the device's default web browser.
        // The Intent which loads the specified URL in the browser is intercepted here instead and
        // its contents examined to prove it contains the expected information.
        // Solution adapted from: https://stackoverflow.com/a/35067831/8100469
        Intents.init()

        val expectedIntent = allOf(hasAction(Intent.ACTION_VIEW), hasData(event.website))
        intending(expectedIntent).respondWith(Instrumentation.ActivityResult(0, null))

        onView(withId(R.id.btnWebsite))
            .perform(click())

        intended(expectedIntent)
        Intents.release()
    }
}