package de.rki.coronawarnapp.ui.onboarding

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.launchFragment2
import testhelpers.launchInEmptyActivity
import testhelpers.takeScreenshot
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class OnboardingPrivacyFragmentTest : BaseUITest() {

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

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
        launchInEmptyActivity<OnboardingPrivacyFragment>()
        takeScreenshot<OnboardingPrivacyFragment>()
    }
}

@Module
abstract class OnboardingPrivacyTestModule {
    @ContributesAndroidInjector
    abstract fun onboardingPrivacyFragment(): OnboardingPrivacyFragment
}
