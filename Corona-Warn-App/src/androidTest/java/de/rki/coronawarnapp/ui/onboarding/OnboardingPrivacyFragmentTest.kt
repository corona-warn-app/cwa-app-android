package de.rki.coronawarnapp.ui.onboarding

import android.Manifest
import androidx.fragment.app.testing.launchFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.SCREENSHOT_DELAY_TIME
import testhelpers.ScreenShotter
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.launchFragmentInContainer2
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class OnboardingPrivacyFragmentTest : BaseUITest() {

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @Before
    fun setup() {
        setupMockViewModel(object : OnboardingPrivacyViewModel.Factory {
            override fun create(): OnboardingPrivacyViewModel = OnboardingPrivacyViewModel()
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment<OnboardingPrivacyFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<OnboardingPrivacyFragment>()
        Thread.sleep(SCREENSHOT_DELAY_TIME)
        ScreenShotter.capture<OnboardingPrivacyFragment>()
    }
}

@Module
abstract class OnboardingPrivacyTestModule {
    @ContributesAndroidInjector
    abstract fun onboardingPrivacyFragment(): OnboardingPrivacyFragment
}
