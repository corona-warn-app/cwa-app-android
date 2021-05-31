package de.rki.coronawarnapp.ui.onboarding

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class OnboardingTestFragmentTest : BaseUITest() {

    @Before
    fun setup() {
        setupMockViewModel(
            object : OnboardingTestViewModel.Factory {
                override fun create(): OnboardingTestViewModel = OnboardingTestViewModel()
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<OnboardingTestFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<OnboardingTestFragment>()
        takeScreenshot<OnboardingTestFragment>()
    }
}

@Module
abstract class OnboardingTestFragmentModule {
    @ContributesAndroidInjector
    abstract fun onboardingTestFragment(): OnboardingTestFragment
}
