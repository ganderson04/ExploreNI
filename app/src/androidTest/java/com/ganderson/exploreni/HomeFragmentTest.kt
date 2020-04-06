package com.ganderson.exploreni

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.activities.SettingsActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeFragmentTest {
    @get:Rule val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun emergencyDialogTest() {
        onView(withId(R.id.tb_emergency))
            .perform(click())

        onView(withText("Emergency"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun moreOptionsTest() {
        openActionBarOverflowOrOptionsMenu(activityRule.activity)

        onView(withText("Settings")).check(matches(isDisplayed()))
    }

    @Test
    fun openSettingsTest() {
        // This test opens a new Activity which requires the use of an Intent. It is necessary
        // to call Intents#init in the Espresso library to begin recording Intents.
        Intents.init()
        openActionBarOverflowOrOptionsMenu(activityRule.activity)

        onView(withText("Settings"))
            .perform(click())

        intended(hasComponent(SettingsActivity::class.java.name))
        Intents.release() // Must be called after each test case which uses Intents#init.
    }

    @Test
    fun openAndCloseSettingsTest() {
        Intents.init()
        openActionBarOverflowOrOptionsMenu(activityRule.activity)

        onView(withText("Settings"))
            .perform(click())

        intended(hasComponent(SettingsActivity::class.java.name))

        // Espresso cannot seem to find the back button on the toolbar by its ID
        // "android.R.id.home". It is instead found by its content description "Navigate up".
        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description))
            .perform(click())

        // If the weather information is visible, we are back on the Home screen.
        onView(withId(R.id.tvWeatherTown))
            .check(matches(isDisplayed()))
        Intents.release()
    }

    @Test
    fun openEventsFragmentTest() {
        onView(withId(R.id.cvEvents))
            .perform(click())

        onView(withId(R.id.tvLoadingMessage))
            .check(matches(isDisplayed()))
    }
}