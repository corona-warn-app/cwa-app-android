package de.rki.coronawarnapp.ui.coronatest.rat.profile.onboarding

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

class RATProfileOnboardingFragmentTest : BaseUITest() {
    @MockK lateinit var viewModel: RATProfileOnboardingFragmentViewModel

    private val args = RATProfileOnboardingFragmentArgs().toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, true)
        setupMockViewModel(
            object : RATProfileOnboardingFragmentViewModel.Factory {
                override fun create(): RATProfileOnboardingFragmentViewModel = viewModel
            }
        )
    }

    @Test
    fun launch_fragment() {
        launchFragment2<RATProfileOnboardingFragment>(fragmentArgs = args)
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<RATProfileOnboardingFragment>(fragmentArgs = args)
        takeScreenshot<RATProfileOnboardingFragment>()
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }
}

@Module
abstract class RATProfileOnboardingFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun ratProfileOnboardingFragment(): RATProfileOnboardingFragment
}
