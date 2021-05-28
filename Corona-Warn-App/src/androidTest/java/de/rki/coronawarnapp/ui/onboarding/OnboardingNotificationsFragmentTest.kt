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
import testhelpers.launchInEmptyActivity
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class OnboardingNotificationsFragmentTest : BaseUITest() {

    @Before
    fun setup() {
        setupMockViewModel(
            object : OnboardingNotificationsViewModel.Factory {
                override fun create(): OnboardingNotificationsViewModel = OnboardingNotificationsViewModel()
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<OnboardingNotificationsFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchInEmptyActivity<OnboardingNotificationsFragment>()
        takeScreenshot<OnboardingNotificationsFragment>()
    }
}

@Module
abstract class OnboardingNotificationsTestModule {
    @ContributesAndroidInjector
    abstract fun onboardingNotificationsFragment(): OnboardingNotificationsFragment
}
