package de.rki.coronawarnapp.ui.main.home

import androidx.fragment.app.testing.launchFragment
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest

@RunWith(AndroidJUnit4::class)
class HomeFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: HomeFragmentViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { viewModel.tracingHeaderState } returns MutableLiveData()
        every { viewModel.tracingCardState } returns MutableLiveData()
        every { viewModel.submissionCardState } returns MutableLiveData()

        setupMockViewModel(object : HomeFragmentViewModel.Factory {
            override fun create(): HomeFragmentViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment<HomeFragment>()

        // ...
    }
}

@Module
abstract class HomeFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun homeScreen(): HomeFragment
}
