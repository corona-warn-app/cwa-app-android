package de.rki.coronawarnapp.ui.onboarding

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.TracingPermissionHelper
import de.rki.coronawarnapp.storage.TracingSettings
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
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class OnboardingTracingFragmentTest : BaseUITest() {

    @MockK lateinit var interopRepo: InteroperabilityRepository
    @MockK lateinit var factory: TracingPermissionHelper.Factory
    @MockK lateinit var tracingSettings: TracingSettings
    @MockK lateinit var enfClient: ENFClient

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        val viewModelSpy = spyk(
            OnboardingTracingFragmentViewModel(
                interoperabilityRepository = interopRepo,
                tracingPermissionHelperFactory = factory,
                dispatcherProvider = TestDispatcherProvider(),
                tracingSettings = tracingSettings,
                enfClient = enfClient,
            )
        )

        every { viewModelSpy.disableTracingIfEnabled() } just Runs
        every { interopRepo.countryList } returns flowOf()
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
