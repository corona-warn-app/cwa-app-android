package de.rki.coronawarnapp.ui.onboarding

import android.Manifest
import androidx.fragment.app.testing.launchFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragmentInContainer2
import tools.fastlane.screengrab.locale.LocaleTestRule
import testhelpers.SCREENSHOT_DELAY_TIME
import testhelpers.ScreenShotter

@RunWith(AndroidJUnit4::class)
class OnboardingDeltaInteroperabilityFragmentTest : BaseUITest() {

    @MockK lateinit var interopRepo: InteroperabilityRepository

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(object : OnboardingDeltaInteroperabilityFragmentViewModel.Factory {
            override fun create(): OnboardingDeltaInteroperabilityFragmentViewModel =
                OnboardingDeltaInteroperabilityFragmentViewModel(
                    interopRepo = interopRepo,
                    dispatcherProvider = TestDispatcherProvider
                )
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment<OnboardingDeltaInteroperabilityFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<OnboardingDeltaInteroperabilityFragment>()
        Thread.sleep(SCREENSHOT_DELAY_TIME)
        ScreenShotter.capture<OnboardingDeltaInteroperabilityFragment>()
    }
}

@Module
abstract class OnboardingDeltaInteroperabilityFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun onboardingDeltaInteroperabilityFragment(): OnboardingDeltaInteroperabilityFragment
}
