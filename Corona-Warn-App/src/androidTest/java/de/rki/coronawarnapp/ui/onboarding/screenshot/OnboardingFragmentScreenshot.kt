package de.rki.coronawarnapp.ui.onboarding.screenshot

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.onboarding.OnboardingFragment
import de.rki.coronawarnapp.ui.onboarding.OnboardingFragmentViewModel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import tools.fastlane.screengrab.DecorViewScreenshotStrategy
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule

@Screenshot
@RunWith(AndroidJUnit4::class)
class OnboardingFragmentScreenshot : BaseUITest() {

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @MockK lateinit var viewModel: OnboardingFragmentViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(object : OnboardingFragmentViewModel.Factory {
            override fun create(): OnboardingFragmentViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun capture_screenshot() {
        launchFragmentInContainer<OnboardingFragment>(themeResId = R.style.AppTheme)
        Thread.sleep(1000)
        Screengrab.screenshot(OnboardingFragment::class.simpleName)
    }
}
