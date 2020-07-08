package de.rki.coronawarnapp.ui.submission

import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.main.MainFragment
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class SubmissionResultPositiveOtherWarningFragmentTest {
    @Rule
    @JvmField
    //Create ActivityTestRule
    var activityTestRule: ActivityTestRule<MainActivity?>? = ActivityTestRule(MainActivity::class.java)
    lateinit var activity: MainActivity
    companion object {
        private val TAG: String? = SubmissionResultPositiveOtherWarningFragmentTest::class.simpleName
        private val NULL_ASSERT_MESSAGE: String? = "Button is not null"
    }

    @Before
    //Setup activity before the test
    fun TestEnvironmentSetup() {
        activity = activityTestRule!!.activity!!
    }

    /**
     * Positive result test
     *
     * @see SubmissionResultPositiveOtherWarningFragment
     */
    @Test
    fun performPositiveTestResultTest() {
        // Load the SubmissionResultPositiveOtherWarningFragment
        val fragment = SubmissionResultPositiveOtherWarningFragment()
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment, TAG).addToBackStack(TAG).commit();

        getInstrumentation().waitForIdleSync()
        checkFragmentRendered(SubmissionResultPositiveOtherWarningFragment())
                clickButton(R.id.submission_positive_other_warning_button_next)
    }

    /**
     * Perform assertion and Button Click
     *
     * @param Button id
     * @see assertNotNull
     */
    private fun clickButton(id: Int) {
        assertNotNull(NULL_ASSERT_MESSAGE,activity.findViewById<Button>(id))
        onView(withId(id)).perform(click())
    }

    /**
     * Check if the fragment is loaded
     * @see assertNotNull
     */
    private fun checkFragmentRendered(fragmentToCheck: Fragment) {
        getInstrumentation().waitForIdleSync()
        var currentFragment = activity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        if (currentFragment!!.id === fragmentToCheck.id) {
            assertNotNull(fragmentToCheck)
        }
    }

    /**
     * Perform final checks here`
     */
    @After
    fun tearDown() {
        getInstrumentation().waitForIdleSync()
        checkFragmentRendered(MainFragment())
    }
}