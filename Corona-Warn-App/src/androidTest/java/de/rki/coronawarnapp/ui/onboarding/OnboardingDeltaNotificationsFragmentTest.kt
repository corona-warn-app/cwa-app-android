package de.rki.coronawarnapp.ui.onboarding

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class OnboardingDeltaNotificationsFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: OnboardingDeltaNotificationsViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(
            object : OnboardingDeltaNotificationsViewModel.Factory {
                override fun create(): OnboardingDeltaNotificationsViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<OnboardingDeltaNotificationsFragment>()
        takeScreenshot<OnboardingDeltaNotificationsFragment>()
    }
}

@Module
abstract class OnboardingDeltaNotificationsFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun onboardingDeltaNotificationsFragment(): OnboardingDeltaNotificationsFragment
}
