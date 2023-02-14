package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.ui.eventregistration.organizer.TraceLocationData
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class TraceLocationSelectionFragmentTest : BaseUITest() {

    @MockK private lateinit var checkInsRepository: CheckInRepository
    @MockK private lateinit var traceLocationRepository: TraceLocationRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { checkInsRepository.allCheckIns } returns flowOf(listOf())
        every { traceLocationRepository.traceLocationsWithinRetention } returns flowOf(
            listOf(
                TraceLocationData.traceLocationSameDate,
                TraceLocationData.traceLocationDifferentDate
            )
        )
        setupMockViewModel(
            object : TraceLocationSelectionViewModel.Factory {
                override fun create(): TraceLocationSelectionViewModel {
                    return createViewModel()
                }
            }
        )
    }

    @Screenshot
    @Test
    fun screenshot() {
        launchFragmentInContainer2<TraceLocationSelectionFragment>()
        takeScreenshot<TraceLocationSelectionFragment>()
    }

    private fun createViewModel() = TraceLocationSelectionViewModel(
        checkInsRepository = checkInsRepository,
        traceLocationRepository = traceLocationRepository,
        dispatcherProvider = TestDispatcherProvider()
    )
}
