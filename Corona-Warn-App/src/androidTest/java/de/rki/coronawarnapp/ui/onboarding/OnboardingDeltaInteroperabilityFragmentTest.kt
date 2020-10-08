package de.rki.coronawarnapp.ui.onboarding

import androidx.fragment.app.testing.launchFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest

@RunWith(AndroidJUnit4::class)
class OnboardingDeltaInteroperabilityFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: OnboardingDeltaInteroperabilityFragmentViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(object : OnboardingDeltaInteroperabilityFragmentViewModel.Factory {
            override fun create(): OnboardingDeltaInteroperabilityFragmentViewModel = viewModel
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
}

@Module
abstract class OnboardingDeltaInteroperabilityFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun onboardingDeltaInteroperabilityFragment(): OnboardingDeltaInteroperabilityFragment
}