package de.rki.coronawarnapp.presencetracing.storage.retention

import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant

internal class TraceLocationCleanerTest : BaseTest() {

    @MockK lateinit var traceLocationRepository: TraceLocationRepository
    @MockK lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { traceLocationRepository.deleteTraceLocation(any()) } just Runs

        // Now = Jan 16th 2020, 00:00
        // TraceLocations should be kept for 15 days, so every trace location with and end date before
        // Jan 1st 2020, 00:00 should get deleted
        every { timeStamper.nowUTC } returns Instant.parse("2020-01-16T00:00:00.000Z")
    }

    private fun createInstance() = TraceLocationCleaner(traceLocationRepository, timeStamper)

    @Test
    fun `cleanUp() should do nothing when no trace locations are stored`() = runTest {
        every { traceLocationRepository.allTraceLocations } returns flowOf(emptyList())

        createInstance().cleanUp()

        verify(exactly = 0) { traceLocationRepository.deleteTraceLocation(any()) }
    }

    @Test
    fun `cleanUp() should NOT delete trace locations with an end date that is not older than 15 days`() =
        runTest {

            val lastValidTraceLocation = createTraceLocationWithEndDate(Instant.parse("2020-01-01T00:00:00.000Z"))

            every { traceLocationRepository.allTraceLocations } returns flowOf(listOf(lastValidTraceLocation))

            createInstance().cleanUp()
            verify(exactly = 0) { traceLocationRepository.deleteTraceLocation(any()) }
        }

    @Test
    fun `cleanUp() should NOT delete trace locations without end dates`() = runTest {

        val permanentTraceLocation = createTraceLocationWithEndDate(null)

        every { traceLocationRepository.allTraceLocations } returns flowOf(listOf(permanentTraceLocation))

        createInstance().cleanUp()
        verify(exactly = 0) { traceLocationRepository.deleteTraceLocation(any()) }
    }

    @Test
    fun `cleanUp() should delete trace locations that are older than 15 days`() =
        runTest {

            val oldTraceLocation = createTraceLocationWithEndDate(Instant.parse("2019-12-31T23:59:59.000Z"))

            every { traceLocationRepository.allTraceLocations } returns flowOf(listOf(oldTraceLocation))

            createInstance().cleanUp()
            verify(exactly = 1) { traceLocationRepository.deleteTraceLocation(oldTraceLocation) }
        }

    private fun createTraceLocationWithEndDate(endDate: Instant?) = TraceLocation(
        id = 1,
        type = TraceLocationOuterClass.TraceLocationType.UNRECOGNIZED,
        description = "",
        address = "",
        startDate = null,
        endDate = endDate,
        defaultCheckInLengthInMinutes = 30,
        cryptographicSeed = "seed byte array".encode(),
        cnPublicKey = "cnPublicKey"
    )
}
