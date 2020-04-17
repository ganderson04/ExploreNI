package com.ganderson.exploreni

import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.ganderson.exploreni.ui.activities.InterestsActivity
import com.ganderson.exploreni.ui.activities.SettingsActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsActivityTest {
    @get:Rule
    val activityRule = ActivityTestRule(SettingsActivity::class.java)

    @Test
    fun openInterestsTest() {
        Intents.init()
        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("Select interests")),
                click()))

        intended(hasComponent(InterestsActivity::class.java.name))
        Intents.release()
    }

    @Test
    fun openAndCloseInterestsTest() {
        Intents.init()
        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("Select interests")),
                click()))

        intended(hasComponent(InterestsActivity::class.java.name))

        // Espresso cannot seem to find the back button on the toolbar by its ID
        // "android.R.id.home". It is instead found by its content description "Navigate up".
        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description))
            .perform(click())

        // Check for the RecyclerView used by PreferenceFragmentCompat to display the preferences,
        // ensuring we are back on the Settings screen.
        onView(withId(androidx.preference.R.id.recycler_view))
            .check(matches(isDisplayed()))
        Intents.release()
    }

    @Test
    fun setDistancePreferenceTest() {
        val preferenceBefore = PreferenceManager.getDefaultSharedPreferences(activityRule.activity)
            .getBoolean("measurement_distance", false)

        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("Use metric for distances")),
                click()))

        val preferenceAfter = PreferenceManager.getDefaultSharedPreferences(activityRule.activity)
            .getBoolean("measurement_distance", false)

        assert(preferenceBefore == preferenceAfter)
    }

    @Test
    fun setTemperaturePreferenceTest() {
        val preferenceBefore = PreferenceManager.getDefaultSharedPreferences(activityRule.activity)
            .getBoolean("measurement_temperature", false)

        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("Use Fahrenheit")),
                click()))

        val preferenceAfter = PreferenceManager.getDefaultSharedPreferences(activityRule.activity)
            .getBoolean("measurement_temperature", false)

        assert(preferenceBefore == preferenceAfter)
    }
}