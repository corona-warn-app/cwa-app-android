package de.rki.coronawarnapp.ui.onboarding

import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.setViewVisibility
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class OnboardingPrivacyFragmentTest : BaseUITest() {

    @Before
    fun setup() {
        setupMockViewModel(
            object : OnboardingPrivacyViewModel.Factory {
                override fun create(): OnboardingPrivacyViewModel = OnboardingPrivacyViewModel()
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<OnboardingPrivacyFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<OnboardingPrivacyFragment>()
        Espresso.onView(ViewMatchers.withId(R.id.onboarding_privacy_container)).perform(setViewVisibility(true))
        takeScreenshot<OnboardingPrivacyFragment>()
    }
}

@Module
abstract class OnboardingPrivacyTestModule {
    @ContributesAndroidInjector
    abstract fun onboardingPrivacyFragment(): OnboardingPrivacyFragment
}
