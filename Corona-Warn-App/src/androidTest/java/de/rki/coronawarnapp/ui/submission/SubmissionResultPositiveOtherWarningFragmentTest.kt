package de.rki.coronawarnapp.ui.submission

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.main.MainFragment
import org.junit.Assert.assertNotNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class SubmissionResultPositiveOtherWarningFragmentTest {
    @Rule
    @JvmField
    //Create ActivityTestRule
    var activityTestRule: ActivityTestRule<MainActivity?>? = ActivityTestRule(MainActivity::class.java)
    lateinit var activity: MainActivity

    @Before
    //Setup activity before the test
    fun SetUP() {
        activity = activityTestRule!!.activity!!
    }

    /**
     * Positive result test
     * to be executed in a scenario where test results are positive
     */
    @Test
    fun performPositiveTestResultTest() {
        // Click on the the button of Positive test card
        onView(withId(R.id.submission_status_card_positive_button))
            .check(matches(isDisplayed()))
            .perform(click())

        // Click to continue next to submit keys
        onView(withId(R.id.submission_test_result_button_positive_continue))
            .check(matches(isDisplayed()))
            .perform(click())

        //Click on the next button to submit tests
        onView(withId(R.id.submission_positive_other_warning_button_next))
            .check(matches(isDisplayed()))
            .perform(click())

        //When there are no keys, an error dialog will be shown. we click on the positive button
        onView(withId(android.R.id.button1))
            .check(matches(isDisplayed()))
            .perform(click());
    }

    @After
    fun tearDown() {
        //Checking if MainFragment is loaded
        getInstrumentation().waitForIdleSync()
        var f = activity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment);
        if (f is MainFragment) {
            assertNotNull(f)
        }
    }
}