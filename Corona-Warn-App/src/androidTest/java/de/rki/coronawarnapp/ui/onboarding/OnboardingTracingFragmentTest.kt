package de.rki.coronawarnapp.ui.onboarding

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
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.takeScreenshot
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
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
                dispatcherProvider = TestDispatcherProvider()
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
        unmockkAll()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<OnboardingTracingFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<OnboardingTracingFragment>()
        takeScreenshot<OnboardingTracingFragment>()
    }
}

@Module
abstract class OnboardingTracingFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun onboardingTracingFragment(): OnboardingTracingFragment
}
