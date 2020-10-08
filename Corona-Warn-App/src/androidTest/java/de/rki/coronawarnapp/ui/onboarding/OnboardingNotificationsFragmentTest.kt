package de.rki.coronawarnapp.ui.onboarding

import androidx.fragment.app.testing.launchFragment
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

@RunWith(AndroidJUnit4::class)
class OnboardingNotificationsFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: OnboardingNotificationsViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(object : OnboardingNotificationsViewModel.Factory {
            override fun create(): OnboardingNotificationsViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment<OnboardingNotificationsFragment>()
    }
}

@Module
abstract class OnboardingNotificationsTestModule {
    @ContributesAndroidInjector
    abstract fun onboardingNotificationsFragment(): OnboardingNotificationsFragment
}