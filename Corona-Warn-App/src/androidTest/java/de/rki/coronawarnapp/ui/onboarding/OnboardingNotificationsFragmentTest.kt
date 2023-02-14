package de.rki.coronawarnapp.ui.onboarding

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.main.CWASettings
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
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
class OnboardingNotificationsFragmentTest : BaseUITest() {

    @MockK lateinit var cwaSettings: CWASettings

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        setupMockViewModel(
            object : OnboardingNotificationsViewModel.Factory {
                override fun create(): OnboardingNotificationsViewModel = OnboardingNotificationsViewModel(cwaSettings)
            }
        )
    }



    @Test
    fun launch_fragment() {
        launchFragment2<OnboardingNotificationsFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<OnboardingNotificationsFragment>()
        takeScreenshot<OnboardingNotificationsFragment>()
    }
}
