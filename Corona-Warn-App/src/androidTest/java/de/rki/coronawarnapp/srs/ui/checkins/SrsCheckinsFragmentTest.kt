package de.rki.coronawarnapp.srs.ui.checkins

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent.SelectableCheckInVH.Item
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class SrsCheckinsFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: SrsCheckinsFragmentViewModel

    private val fragmentArgs = SrsCheckinsFragmentArgs(submissionType = SrsSubmissionType.SRS_OTHER).toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel.apply {
            every { events } returns SingleLiveEvent()
            every { checkIns } returns MutableLiveData(
                listOf(
                    Item(
                        mockCheckIn(
                            checkInId = 1,
                            checkInDescription = "Frisör",
                            checkInAddress = "Berlin"
                        )
                    ) {},
                    Item(
                        mockCheckIn(
                            checkInId = 2,
                            checkInDescription = "Restaurant",
                            checkInAddress = "Berlin"
                        )
                    ) {}
                )
            )
        }

        setupMockViewModel(
            object : SrsCheckinsFragmentViewModel.Factory {
                override fun create(
                    savedState: SavedStateHandle,
                    submissionType: SrsSubmissionType
                ): SrsCheckinsFragmentViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<SrsCheckinsFragment>(
            fragmentArgs = fragmentArgs
        )
        takeScreenshot<SrsCheckinsFragment>()
    }

    private fun mockCheckIn(
        checkInId: Long,
        checkInDescription: String,
        checkInAddress: String
    ) = mockk<CheckIn>().apply {
        every { id } returns checkInId
        every { description } returns checkInDescription
        every { address } returns checkInAddress
        every { hasSubmissionConsent } returns true
        every { checkInStart } returns Instant.ofEpochSecond(1624291200L)
        every { checkInEnd } returns Instant.ofEpochSecond(1624302000L)
    }
}

@Module
abstract class SrsCheckinsFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun srsCheckinsScreen(): SrsCheckinsFragment
}
