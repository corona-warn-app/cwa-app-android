package de.rki.coronawarnapp.ui.onboarding.screenshot

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.onboarding.OnboardingTracingFragment
import de.rki.coronawarnapp.ui.onboarding.OnboardingTracingFragmentViewModel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule

@Screenshot
@RunWith(AndroidJUnit4::class)
class OnboardingTracingFragmentScreenshot : BaseUITest() {

    @MockK lateinit var viewModel: OnboardingTracingFragmentViewModel

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(object : OnboardingTracingFragmentViewModel.Factory {
            override fun create(): OnboardingTracingFragmentViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun capture_screenshot() {
        launchFragmentInContainer<OnboardingTracingFragment>(themeResId = R.style.AppTheme)
            .onFragment {
                Screengrab.screenshot(OnboardingTracingFragment::class.simpleName)
            }
    }
}
