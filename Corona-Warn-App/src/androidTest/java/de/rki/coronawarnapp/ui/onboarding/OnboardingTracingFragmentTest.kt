package de.rki.coronawarnapp.ui.onboarding

import androidx.fragment.app.testing.launchFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.nearby.TracingPermissionHelper
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.spyk
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.SCREENSHOT_DELAY_TIME
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragmentInContainer2
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class OnboardingTracingFragmentTest : BaseUITest() {

    @MockK lateinit var interopRepo: InteroperabilityRepository
    @MockK lateinit var factory: TracingPermissionHelper.Factory

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        val viewModelSpy = spyk(
            OnboardingTracingFragmentViewModel(
                interoperabilityRepository = interopRepo,
                tracingPermissionHelperFactory = factory,
                dispatcherProvider = TestDispatcherProvider
            )
        )

        every { viewModelSpy.resetTracing() } just Runs
        every { interopRepo.countryList } returns flowOf()

        setupMockViewModel(object : OnboardingTracingFragmentViewModel.Factory {
            override fun create(): OnboardingTracingFragmentViewModel = viewModelSpy
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
        launchFragmentInContainer2<OnboardingTracingFragment>()
        Thread.sleep(SCREENSHOT_DELAY_TIME)
        Screengrab.screenshot(simpleName)
    }
}

@Module
abstract class OnboardingTracingFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun onboardingTracingFragment(): OnboardingTracingFragment
}
