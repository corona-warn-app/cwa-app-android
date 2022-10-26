package de.rki.coronawarnapp.presencetracing.storage.retention

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant

internal class CheckInCleanerTest : BaseTest() {

    @MockK lateinit var checkInRepository: CheckInRepository
    @MockK lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { checkInRepository.deleteCheckIns(any()) } just Runs

        // Now = Jan 16th 2020, 00:00
        // CheckIns should be kept for 15 days, so every check-in with an end date before
        // Jan 1st 2020, 00:00 should get deleted
        every { timeStamper.nowUTC } returns Instant.parse("2020-01-16T00:00:00.000Z")
    }

    private fun createInstance() = CheckInCleaner(checkInRepository, timeStamper)

    @Test
    fun `cleanUp() should do nothing when no check-ins are stored`() = runTest {
        every { checkInRepository.allCheckIns } returns flowOf(emptyList())

        createInstance().cleanUp()

        coVerify(exactly = 1) { checkInRepository.deleteCheckIns(emptyList()) }
    }

    @Test
    fun `cleanUp() should NOT delete check-ins with an check-out date that is not older than 15 days`() =
        runTest {

            val lastValidCheckIn = createCheckIn(Instant.parse("2020-01-01T00:00:00.000Z"))
            every { checkInRepository.allCheckIns } returns flowOf(listOf(lastValidCheckIn))

            createInstance().cleanUp()

            coVerify(exactly = 1) { checkInRepository.deleteCheckIns(emptyList()) }
        }

    @Test
    fun `cleanUp() should delete check-ins that are older than 15 days`() =
        runTest {

            val oldCheckIn = createCheckIn(Instant.parse("2019-12-31T23:59:59.000Z"))
            every { checkInRepository.allCheckIns } returns flowOf(listOf(oldCheckIn))

            createInstance().cleanUp()

            coVerify(exactly = 1) { checkInRepository.deleteCheckIns(listOf(oldCheckIn)) }
        }

    private fun createCheckIn(checkOutDate: Instant) = CheckIn(
        traceLocationId = "traceLocationId1".encode(),
        version = 1,
        type = 1,
        description = "",
        address = "",
        traceLocationStart = null,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = 30,
        cryptographicSeed = "cryptographicSeed".encode(),
        cnPublicKey = "cnPublicKey",
        // checkInStart not relevant for this
        checkInStart = Instant.parse("1970-01-01T00:00:00.000Z"),
        checkInEnd = checkOutDate,
        completed = true,
        createJournalEntry = true
    )
}
