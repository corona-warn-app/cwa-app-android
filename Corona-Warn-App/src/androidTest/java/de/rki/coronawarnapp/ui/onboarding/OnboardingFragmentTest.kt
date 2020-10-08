package de.rki.coronawarnapp.ui.onboarding

import androidx.fragment.app.testing.launchFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest

@RunWith(AndroidJUnit4::class)
class OnboardingFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: OnboardingFragmentViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(object : OnboardingFragmentViewModel.Factory {
            override fun create(): OnboardingFragmentViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment<OnboardingFragment>()
    }
}

@Module
abstract class OnboardingFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun onboardingFragment(): OnboardingFragment
}