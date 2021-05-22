package de.rki.coronawarnapp.ui.onboarding

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class OnboardingFragmentTest : BaseUITest() {

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @Test
    fun launch_fragment() {
        launchFragment2<OnboardingFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<OnboardingFragment>()
        takeScreenshot<OnboardingFragment>()

        if (showEasyLanguageLink()) {
            onView(withId(R.id.onboarding_easy_language)).perform(scrollTo())
            takeScreenshot<OnboardingFragment>("2")
        }
    }
}

@Module
abstract class OnboardingFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun onboardingFragment(): OnboardingFragment
}
