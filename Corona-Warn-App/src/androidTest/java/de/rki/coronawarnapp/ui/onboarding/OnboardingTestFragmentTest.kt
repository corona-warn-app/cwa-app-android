package de.rki.coronawarnapp.ui.onboarding

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.launchFragmentInContainer2
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule
import testhelpers.SCREENSHOT_DELAY_TIME
import testhelpers.launchFragment2

@RunWith(AndroidJUnit4::class)
class OnboardingTestFragmentTest : BaseUITest() {

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

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
        unmockkAll()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<OnboardingTestFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<OnboardingTestFragment>()
        Thread.sleep(SCREENSHOT_DELAY_TIME)
        Screengrab.screenshot(OnboardingTestFragment::class.simpleName)
    }
}

@Module
abstract class OnboardingTestFragmentModule {
    @ContributesAndroidInjector
    abstract fun onboardingTestFragment(): OnboardingTestFragment
}
