package de.rki.coronawarnapp.ui.onboarding

import android.app.Activity
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.doubleClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.ActivityTestRule
import de.rki.coronawarnapp.R
import junit.framework.TestCase.assertNotNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class OnboardingFragmentTest {

    private val TAG: String? = OnboardingFragmentTest::class.simpleName

    lateinit var onboardingActivity: OnboardingActivity
    lateinit var onboardingFragment: OnboardingFragment

    @Rule
    @JvmField
    var activityActivityTestRule = ActivityTestRule(
        OnboardingActivity::class.java
    )

    @Before
    fun setup() {
        onboardingActivity = activityActivityTestRule.activity
        onboardingFragment = OnboardingFragment()
        activityActivityTestRule.activity
            .supportFragmentManager.beginTransaction()
            .add(R.id.onboarding_fragment_container, onboardingFragment, TAG)
    }

    @Test
    fun checkEasyLanguageLink() {
        assertNotNull(onboardingFragment)
        if (onboardingFragment.view != null) {
            var easyLanguageText =
                onboardingFragment.view!!.findViewById<View>(R.id.onboarding_easy_language)
            assertNotNull(easyLanguageText)
            onView(withText(R.string.onboarding_tracing_easy_language_explanation)).check(
                matches(isDisplayed())).perform(doubleClick())
            //TODO: need to check if the browser opens the link
        }
    }

    @After
    fun teardown() {
    activityActivityTestRule.finishActivity()
    }

}