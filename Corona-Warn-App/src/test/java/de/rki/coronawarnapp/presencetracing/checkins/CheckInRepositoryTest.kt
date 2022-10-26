package de.rki.coronawarnapp.presencetracing.checkins

import de.rki.coronawarnapp.presencetracing.storage.TraceLocationDatabase
import de.rki.coronawarnapp.presencetracing.storage.dao.CheckInDao
import de.rki.coronawarnapp.presencetracing.storage.entity.TraceLocationCheckInEntity
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant

class CheckInRepositoryTest : BaseTest() {

    @MockK lateinit var factory: TraceLocationDatabase.Factory
    @MockK lateinit var database: TraceLocationDatabase
    @MockK lateinit var checkInDao: CheckInDao
    @RelaxedMockK lateinit var timeStamper: TimeStamper
    private val allEntriesFlow = MutableStateFlow(emptyList<TraceLocationCheckInEntity>())

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { factory.create() } returns database
        every { database.checkInDao() } returns checkInDao
        every { checkInDao.allEntries() } returns allEntriesFlow
        coEvery { checkInDao.entryForId(any()) } coAnswers {
            allEntriesFlow.first().singleOrNull { it.id == arg(0) }
        }
    }

    private fun createInstance() = CheckInRepository(factory, timeStamper)

    @Test
    fun `new entities should have ID 0`() = runTest {
        shouldThrow<IllegalArgumentException> {
            val checkIn = CheckIn(
                id = 1L,
                traceLocationId = "41da2115-eba2-49bd-bf17-adb3d635ddaf".encode(),
                version = 1,
                type = 2,
                description = "brothers birthday",
                address = "Malibu",
                traceLocationStart = Instant.EPOCH,
                traceLocationEnd = null,
                defaultCheckInLengthInMinutes = null,
                cryptographicSeed = "cryptographicSeed".encode(),
                cnPublicKey = "cnPublicKey",
                checkInStart = Instant.EPOCH,
                checkInEnd = Instant.EPOCH,
                completed = false,
                createJournalEntry = false
            )
            createInstance().addCheckIn(checkIn)
        }
    }

    @Test
    fun `add new check in`() {
        coEvery { checkInDao.insert(any()) } returns 0L
        runTest {
            val time = Instant.ofEpochMilli(1397210400000)
            val end = Instant.ofEpochMilli(1397210400001)
            createInstance().addCheckIn(
                CheckIn(
                    id = 0L,
                    traceLocationId = "41da2115-eba2-49bd-bf17-adb3d635ddaf".encode(),
                    version = 1,
                    type = 2,
                    description = "brothers birthday",
                    address = "Malibu",
                    traceLocationStart = time,
                    traceLocationEnd = null,
                    defaultCheckInLengthInMinutes = null,
                    cryptographicSeed = "cryptographicSeed".encode(),
                    cnPublicKey = "cnPublicKey",
                    checkInStart = time,
                    checkInEnd = end,
                    completed = false,
                    createJournalEntry = false,
                    isSubmitted = true
                )
            )
            coVerify {
                checkInDao.insert(
                    TraceLocationCheckInEntity(
                        id = 0L,
                        traceLocationIdBase64 = "41da2115-eba2-49bd-bf17-adb3d635ddaf".encode().base64(),
                        version = 1,
                        type = 2,
                        description = "brothers birthday",
                        address = "Malibu",
                        traceLocationStart = time,
                        traceLocationEnd = null,
                        defaultCheckInLengthInMinutes = null,
                        cryptographicSeedBase64 = "cryptographicSeed".encode().base64(),
                        cnPublicKey = "cnPublicKey",
                        checkInStart = time,
                        checkInEnd = end,
                        completed = false,
                        createJournalEntry = false,
                        isSubmitted = true,
                        hasSubmissionConsent = false,
                    )
                )
            }
        }
    }

    @Test
    fun `update new check in`() = runTest {
        val slot = slot<(CheckIn) -> CheckIn>()
        coEvery { checkInDao.updateEntityById(any(), capture(slot)) } returns Unit

        val checkIn = mockk<CheckIn>()
        createInstance().updateCheckIn(1L) {
            checkIn
        }

        slot.captured.invoke(mockk()) shouldBe checkIn

        coVerify {
            checkInDao.updateEntityById(1L, any())
        }
    }

    @Test
    fun `get data`() {
        val start = Instant.ofEpochMilli(1615796487)
        val end = Instant.ofEpochMilli(1397210400000)
        allEntriesFlow.value = listOf(
            TraceLocationCheckInEntity(
                id = 1L,
                traceLocationIdBase64 = "41da2115-eba2-49bd-bf17-adb3d635ddaf".encode().base64(),
                version = 1,
                type = 2,
                description = "sisters birthday",
                address = "Long Beach",
                traceLocationStart = start,
                traceLocationEnd = end,
                defaultCheckInLengthInMinutes = null,
                cryptographicSeedBase64 = "cryptographicSeed".encode().base64(),
                cnPublicKey = "cnPublicKey",
                checkInStart = start,
                checkInEnd = end,
                completed = false,
                createJournalEntry = false,
                isSubmitted = true,
                hasSubmissionConsent = true,
            )
        )
        runTest {
            createInstance().allCheckIns.first() shouldBe listOf(
                CheckIn(
                    id = 1L,
                    traceLocationId = "41da2115-eba2-49bd-bf17-adb3d635ddaf".encode(),
                    version = 1,
                    type = 2,
                    description = "sisters birthday",
                    address = "Long Beach",
                    traceLocationStart = start,
                    traceLocationEnd = end,
                    defaultCheckInLengthInMinutes = null,
                    cryptographicSeed = "cryptographicSeed".encode(),
                    cnPublicKey = "cnPublicKey",
                    checkInStart = start,
                    checkInEnd = end,
                    completed = false,
                    createJournalEntry = false,
                    isSubmitted = true,
                    hasSubmissionConsent = true,
                )
            )
        }
    }

    @Test
    fun `checkInsWithinRetention() should filter out stale check-ins`() = runTest {

        // Now = Jan 16th 2020, 00:00
        // CheckIns should be kept for 15 days, so every check-in with an end date before
        // Jan 1st 2020, 00:00 should get deleted
        every { timeStamper.nowUTC } returns Instant.parse("2020-01-16T00:00:00.000Z")

        val checkInWithinRetention = createCheckIn(Instant.parse("2020-01-01T00:00:00.000Z"))

        // should be filtered out
        val staleCheckIn = createCheckIn(Instant.parse("2019-12-31T23:59:59.000Z"))

        every { checkInDao.allEntries() } returns flowOf(
            listOf(
                staleCheckIn.toEntity(),
                checkInWithinRetention.toEntity()
            )
        )

        createInstance().checkInsWithinRetention.first() shouldBe
            listOf(checkInWithinRetention)
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
        checkInStart = Instant.parse("1970-01-01T00:00:00.000Z"),
        checkInEnd = checkOutDate,
        completed = true,
        createJournalEntry = true
    )
}
