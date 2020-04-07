package com.ganderson.exploreni

import android.widget.TextView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.activities.SettingsActivity
import org.hamcrest.CoreMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeFragmentTest {
    @get:Rule val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun openEmergencyDialogTest() {
        onView(withId(R.id.tb_emergency))
            .perform(click())

        onView(withText("Emergency"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun openAndCloseEmergencyDialogTest() {
        onView(withId(R.id.tb_emergency))
            .perform(click())

        onView(withText("Close"))
            .perform(click())

        // The toolbar becomes inaccessible while the dialog is showing, even though it does
        // not take over the entire screen. Checking we can access the toolbar title shows that
        // the dialog has closed and focus has returned to the underlying fragment.
        // CoreMatchers#allOf allows multiple Matchers to be combined.
        onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.toolbar))))
            .check(matches(withText("Home")))
    }

    @Test
    fun moreOptionsTest() {
        openActionBarOverflowOrOptionsMenu(getApplicationContext())

        onView(withText("Settings")).check(matches(isDisplayed()))
    }

    @Test
    fun openSettingsTest() {
        // This test opens a new Activity which requires the use of an Intent. It is necessary
        // to call Intents#init in the Espresso library to begin recording Intents.
        Intents.init()
        openActionBarOverflowOrOptionsMenu(getApplicationContext())

        onView(withText("Settings"))
            .perform(click())

        intended(hasComponent(SettingsActivity::class.java.name))
        Intents.release() // Must be called after each test case which uses Intents#init.
    }

    @Test
    fun openAndCloseSettingsTest() {
        Intents.init()
        openActionBarOverflowOrOptionsMenu(getApplicationContext())

        onView(withText("Settings"))
            .perform(click())

        intended(hasComponent(SettingsActivity::class.java.name))

        // Espresso cannot seem to find the back button on the toolbar by its ID
        // "android.R.id.home". It is instead found by its content description "Navigate up".
        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description))
            .perform(click())

        // Check for the title TextView in the toolbar and verify that the text matches what is
        // expected on the Home screen.
        onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.toolbar))))
            .check(matches(withText("Home")))
        Intents.release()
    }

    @Test
    fun openEventsFragmentTest() {
        onView(withId(R.id.cvEvents))
            .perform(click())

        // The loading dialog is shown immediately after opening the Events screen. This means that
        // other views "beneath" it, including the toolbar, are not directly accessible to Espresso.
        // It is necessary to specify the "root" view in which it should look for the view with the
        // specified ID (here "rvEvents" representing the RecyclerView holding the event items).
        // The root here is the "DecorView" which contains the view hierarchy for the window.
        // Using "not(isDialog())" tells Espresso explicitly that it should *not* look for the ID
        // within a Dialog.
        onView(withId(R.id.rvEvents))
            .inRoot(allOf(withDecorView(`is`(activityRule.activity.window.decorView)), not(isDialog())))
            .check(matches(isDisplayed()))
    }
}