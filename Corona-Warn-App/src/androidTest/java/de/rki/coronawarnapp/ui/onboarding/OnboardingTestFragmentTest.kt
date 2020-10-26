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
class OnboardingTestFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: OnboardingTestViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(object : OnboardingTestViewModel.Factory {
            override fun create(): OnboardingTestViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment<OnboardingTestFragment>()
    }
}

@Module
abstract class OnboardingTestFragmentModule {
    @ContributesAndroidInjector
    abstract fun onboardingTestFragment(): OnboardingTestFragment
}
