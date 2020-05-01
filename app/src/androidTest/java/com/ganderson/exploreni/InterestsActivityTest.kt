package com.ganderson.exploreni

import android.view.View
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.ui.activities.SettingsActivity
import com.ganderson.exploreni.ui.components.adapters.InterestsAdapter
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InterestsActivityTest {
    @get:Rule val activityRule = ActivityTestRule(SettingsActivity::class.java)
    private lateinit var interestsArray: Array<String>

    /**
     * The InterestsActivity is accessed through the SettingsActivity so that, once the back
     * button is clicked on the InterestsActivity screen, the app has something to go back to once
     * the InterestsActivity runs "finish()". This means the test will remain active so interest
     * updates can be verified.
     */
    @Before
    fun startInterestsActivity() {
        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("Select interests")), click()))
    }

    @Before
    fun loadAvailableInterests() {
        interestsArray = activityRule.activity.resources.getStringArray(R.array.interests)
    }

    /**
     * Reset interests after the test AND also before, in the event that interests had previously
     * been selected before beginning the testing.
     */
    @Before
    @After
    fun resetInterests() {
        ExploreRepository.setInterests(emptyList())
    }

    @Test
    fun launchActivityTest() {
        onView(withId(R.id.rvInterests))
            .check(matches(isDisplayed()))
    }

    @Test
    fun selectInterestTest() {
        onView(withId(R.id.rvInterests))
            .perform(RecyclerViewActions.actionOnItemAtPosition
            <InterestsAdapter.InterestsViewHolder>(0, clickOnCheckbox()))

        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description))
            .perform(click())

        val selectedInterests = ExploreRepository.getInterests()
        assertTrue(selectedInterests.contains(interestsArray[0]))
    }

    @Test
    fun selectAndDeselectInterestTest() {
        onView(withId(R.id.rvInterests))
            .perform(RecyclerViewActions.actionOnItemAtPosition
            <InterestsAdapter.InterestsViewHolder>(0, clickOnCheckbox()))

        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description))
            .perform(click())

        val firstSelectedInterests = ExploreRepository.getInterests()
        assertTrue(firstSelectedInterests.contains(interestsArray[0]))

        // Re-access InterestsActivity from SettingsActivity.
        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("Select interests")), click()))

        onView(withId(R.id.rvInterests))
            .perform(RecyclerViewActions.actionOnItemAtPosition
            <InterestsAdapter.InterestsViewHolder>(0, clickOnCheckbox()))

        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description))
            .perform(click())

        val secondSelectedInterests = ExploreRepository.getInterests()
        assertFalse(secondSelectedInterests.contains(interestsArray[0]))
    }

    /**
     * Returns a custom ViewAction to click the checkboxes within the individual rows of the
     * interests list.
     */
    private fun clickOnCheckbox() = object: ViewAction {
        override fun getDescription(): String = "Click on the CheckBox in an interest row"

        override fun getConstraints(): Matcher<View>? = null

        override fun perform(uiController: UiController?, view: View?) {
            if(view != null) {
                val checkbox = view.findViewById<CheckBox>(R.id.cbInterest)
                checkbox.performClick()
            }
        }

    }
}