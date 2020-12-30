package de.rki.coronawarnapp.ui.onboarding

import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
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

@RunWith(AndroidJUnit4::class)
class OnboardingTracingFragmentTest : BaseUITest() {

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
    fun launch_fragment() {
        launchFragment<OnboardingTracingFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        val simpleName = OnboardingTracingFragment::class.simpleName
        launchFragmentInContainer<OnboardingTracingFragment>(themeResId = R.style.AppTheme)
        // Check any view to make sure screenshot is not blank
        onView(withId(R.id.onboarding_button_next)).check(matches(isDisplayed()))
        Screengrab.screenshot(simpleName)
    }
}

@Module
abstract class OnboardingTracingFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun onboardingTracingFragment(): OnboardingTracingFragment
}
