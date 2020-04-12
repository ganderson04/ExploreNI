package com.ganderson.exploreni

import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.fragments.ExploreFragment
import org.hamcrest.CoreMatchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExploreFragmentTest {
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun showFragment() {
        val fragment = ExploreFragment()
        activityRule.activity.displayFragment(fragment)
    }

    @Test
    fun openCategoryTest() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        onView(withId(R.id.cvSee))
            .perform(click())

        onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.toolbar))))
            .check(matches(withText("View Locations")))
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun openAndCloseCategoryTest() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        onView(withId(R.id.cvSee))
            .perform(click())

        onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.toolbar))))
            .check(matches(withText("View Locations")))

        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description))
            .perform(click())

        onView(withId(R.id.cvSee))
            .check(matches(isDisplayed()))
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }
    
    @Test
    fun emptyStringSearchTermTest() {
        onView(withId(R.id.etSearch))
            .perform(typeText(""), pressImeActionButton())

        onView(withText("Please enter a search term."))
            .inRoot(withDecorView(not(activityRule.activity.window.decorView)))
            .check(matches(isDisplayed()))
    }

    @Test
    fun blankStringSearchTermTest() {
        onView(withId(R.id.etSearch))
            .perform(typeText(" "), pressImeActionButton())

        onView(withText("Please enter a search term."))
            .inRoot(withDecorView(not(activityRule.activity.window.decorView)))
            .check(matches(isDisplayed()))
    }

    @Test
    fun validSearchTermTest() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        onView(withId(R.id.etSearch))
            .perform(typeText("Castle"), pressImeActionButton())

        onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.toolbar))))
            .check(matches(withText(containsString("Results for"))))
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }
}