package com.ganderson.exploreni

import android.view.View
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onData
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
import org.hamcrest.CoreMatchers.anything
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Matcher
import org.junit.After
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
        onData(withId(R.id.rvInterests))
            .atPosition(0)
            .onChildView(withClassName(containsString("CheckBox")))
            .perform(click())

        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description))
            .perform(click())

        val selectedInterests = ExploreRepository.getInterests()
        assert(selectedInterests.contains(interestsArray[0]))
    }

    private fun clickOnCheckbox() = object: ViewAction {
        override fun getDescription(): String {
            TODO("Not yet implemented")
        }

        override fun getConstraints(): Matcher<View> {
            TODO("Not yet implemented")
        }

        override fun perform(uiController: UiController?, view: View?) {
            if(uiController != null && view != null) {
                click().perform(uiController, view.findViewById<CheckBox>(R.id.cbInterest))
            }
        }

    }
}