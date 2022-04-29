package de.rki.coronawarnapp.profile.ui.onboarding

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

class ProfileOnboardingFragmentTest : BaseUITest() {
    @MockK lateinit var viewModel: ProfileOnboardingFragmentViewModel

    private val args = ProfileOnboardingFragmentArgs().toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, true)
        setupMockViewModel(
            object : ProfileOnboardingFragmentViewModel.Factory {
                override fun create(): ProfileOnboardingFragmentViewModel = viewModel
            }
        )
    }

    @Test
    fun launch_fragment() {
        launchFragment2<ProfileOnboardingFragment>(fragmentArgs = args)
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<ProfileOnboardingFragment>(fragmentArgs = args)
        takeScreenshot<ProfileOnboardingFragment>()
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }
}

@Module
abstract class ProfileOnboardingFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun profileOnboardingFragment(): ProfileOnboardingFragment
}
