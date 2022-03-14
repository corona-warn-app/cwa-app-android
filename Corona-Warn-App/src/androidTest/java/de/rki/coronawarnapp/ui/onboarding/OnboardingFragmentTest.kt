package de.rki.coronawarnapp.ui.onboarding

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class OnboardingFragmentTest : BaseUITest() {

    @MockK lateinit var appConfigProvider: AppConfigProvider

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        every { appConfigProvider.currentConfig } returns flowOf(
            mockk<ConfigData>().apply {
                every { maxEncounterAgeInDays } returns 14
            }
        )

        setupMockViewModel(
            object : OnboardingViewModel.Factory {
                override fun create(): OnboardingViewModel = OnboardingViewModel(appConfigProvider)
            }
        )
    }

    @Test
    fun launch_fragment() {
        launchFragment2<OnboardingFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<OnboardingFragment>()
        takeScreenshot<OnboardingFragment>()

        if (showEasyLanguageLink()) {
            onView(withId(R.id.onboarding_easy_language)).perform(scrollTo())
            takeScreenshot<OnboardingFragment>("2")
        }
    }
}

@Module
abstract class OnboardingFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun onboardingFragment(): OnboardingFragment
}
