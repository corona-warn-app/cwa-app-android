package de.rki.coronawarnapp.ui.coronatest.rat.profile.create

import androidx.lifecycle.MutableLiveData
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.profile.model.Profile
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.joda.time.format.DateTimeFormatter
import org.junit.After
import org.junit.Before
import org.junit.Test
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

class RATProfileCreateFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: RATProfileCreateFragmentViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        setupMockViewModel(
            object : RATProfileCreateFragmentViewModel.Factory {
                override fun create(formatter: DateTimeFormatter): RATProfileCreateFragmentViewModel = viewModel
            }
        )

        viewModel.apply {
            every { events } returns SingleLiveEvent()
            every { savedProfile } returns SingleLiveEvent()
            every { profile } returns MutableLiveData(Profile())
        }
    }

    @Test
    fun launch_fragment() {
        launchFragment2<RATProfileCreateFragment>()
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<RATProfileCreateFragment>()
        takeScreenshot<RATProfileCreateFragment>()
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }
}

@Module
abstract class RATProfileCreateFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun ratProfileCreateFragment(): RATProfileCreateFragment
}
