package de.rki.coronawarnapp.ui.onboarding

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragment2
import testhelpers.launchInEmptyActivity
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class OnboardingDeltaInteroperabilityFragmentTest : BaseUITest() {

    @MockK lateinit var interopRepo: InteroperabilityRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(
            object : OnboardingDeltaInteroperabilityFragmentViewModel.Factory {
                override fun create(): OnboardingDeltaInteroperabilityFragmentViewModel =
                    OnboardingDeltaInteroperabilityFragmentViewModel(
                        interopRepo = interopRepo,
                        dispatcherProvider = TestDispatcherProvider()
                    )
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<OnboardingDeltaInteroperabilityFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchInEmptyActivity<OnboardingDeltaInteroperabilityFragment>()
        takeScreenshot<OnboardingDeltaInteroperabilityFragment>()
    }
}

@Module
abstract class OnboardingDeltaInteroperabilityFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun onboardingDeltaInteroperabilityFragment(): OnboardingDeltaInteroperabilityFragment
}
