package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.duration

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.ui.eventregistration.organizer.TraceLocationData
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.toLocalDateTimeUserTz
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot
import java.time.Duration
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class TraceLocationWarnDurationFragmentTest : BaseUITest() {

    @MockK lateinit var timeStamper: TimeStamper
    private val fragmentArgs = TraceLocationWarnDurationFragmentArgs(TraceLocationData.traceLocationSameDate).toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { timeStamper.nowUTC } returns Instant.parse("2020-11-03T05:35:16.000Z")
    }

    @Screenshot
    @Test
    fun screenshot() {
        launchFragmentInContainer2<TraceLocationWarnDurationFragment>(fragmentArgs)
        takeScreenshot<TraceLocationWarnDurationFragment>()
    }

    private fun createViewModel() = TraceLocationWarnDurationViewModel(
        traceLocation = TraceLocationData.traceLocationSameDate,
        timeStamper = timeStamper,
        dispatcherProvider = TestDispatcherProvider()
    ).apply {
        durationChanged(Duration.ofHours(3))
        dateChanged(TraceLocationData.traceLocationSameDate.startDate!!.toLocalDateTimeUserTz())
    }
}
