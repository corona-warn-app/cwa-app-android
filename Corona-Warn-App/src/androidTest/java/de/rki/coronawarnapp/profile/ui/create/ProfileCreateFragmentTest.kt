package de.rki.coronawarnapp.profile.ui.create

import androidx.lifecycle.MutableLiveData
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.profile.model.Profile
import de.rki.coronawarnapp.profile.model.ProfileId
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot
import java.time.format.DateTimeFormatter

class ProfileCreateFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: ProfileCreateFragmentViewModel

    private val args = ProfileCreateFragmentArgs(id = 1).toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        setupMockViewModel(
            object : ProfileCreateFragmentViewModel.Factory {
                override fun create(formatter: DateTimeFormatter, profileId: ProfileId?): ProfileCreateFragmentViewModel =
                    viewModel
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
        launchFragment2<ProfileCreateFragment>(
            fragmentArgs = args
        )
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<ProfileCreateFragment>(
            fragmentArgs = args
        )
        takeScreenshot<ProfileCreateFragment>()
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }
}

@Module
abstract class ProfileCreateFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun profileCreateFragment(): ProfileCreateFragment
}
