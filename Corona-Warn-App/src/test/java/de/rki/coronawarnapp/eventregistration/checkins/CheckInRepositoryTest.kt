package de.rki.coronawarnapp.eventregistration.checkins

import de.rki.coronawarnapp.eventregistration.storage.TraceLocationDatabase
import de.rki.coronawarnapp.eventregistration.storage.dao.CheckInDao
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationCheckInEntity
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import okio.ByteString.Companion.encode
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CheckInRepositoryTest : BaseTest() {

    @MockK lateinit var factory: TraceLocationDatabase.Factory
    @MockK lateinit var database: TraceLocationDatabase
    @MockK lateinit var checkInDao: CheckInDao
    private val allEntriesFlow = MutableStateFlow(emptyList<TraceLocationCheckInEntity>())

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { factory.create() } returns database
        every { database.eventCheckInDao() } returns checkInDao
        every { checkInDao.allEntries() } returns allEntriesFlow
        coEvery { checkInDao.entryForId(any()) } coAnswers {
            allEntriesFlow.first().singleOrNull { it.id == arg(0) }
        }
    }

    private fun createInstance(scope: CoroutineScope) = CheckInRepository(factory)

    @Test
    fun `new entities should have ID 0`() = runBlockingTest {
        shouldThrow<IllegalArgumentException> {
            val checkIn = CheckIn(
                id = 1L,
                traceLocationId = "41da2115-eba2-49bd-bf17-adb3d635ddaf".encode(),
                traceLocationIdHash = "41da2115-eba2-49bd-bf17-adb3d635ddaf".encode(),
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
            createInstance(scope = this).addCheckIn(checkIn)
        }
    }

    @Test
    fun `add new check in`() {
        coEvery { checkInDao.insert(any()) } returns 0L
        runBlockingTest {
            val time = Instant.ofEpochMilli(1397210400000)
            val end = Instant.ofEpochMilli(1397210400001)
            createInstance(scope = this).addCheckIn(
                CheckIn(
                    id = 0L,
                    traceLocationId = "41da2115-eba2-49bd-bf17-adb3d635ddaf".encode(),
                    traceLocationIdHash = "41da2115-eba2-49bd-bf17-adb3d635ddaf".encode(),
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
                    createJournalEntry = false
                )
            )
            coVerify {
                checkInDao.insert(
                    TraceLocationCheckInEntity(
                        id = 0L,
                        traceLocationIdBase64 = "41da2115-eba2-49bd-bf17-adb3d635ddaf".encode().base64(),
                        traceLocationIdHashBase64 = "41da2115-eba2-49bd-bf17-adb3d635ddaf".encode().base64(),
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
                        createJournalEntry = false
                    )
                )
            }
        }
    }

    @Test
    fun `update new check in`() = runBlockingTest {
        val slot = slot<(CheckIn) -> CheckIn>()
        coEvery { checkInDao.updateEntityById(any(), capture(slot)) } returns Unit

        val checkIn = mockk<CheckIn>()
        createInstance(scope = this).updateCheckIn(1L) {
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
                traceLocationIdHashBase64 = "41da2115-eba2-49bd-bf17-adb3d635ddaf".encode().base64(),
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
                createJournalEntry = false
            )
        )
        runBlockingTest {
            createInstance(scope = this).allCheckIns.first() shouldBe listOf(
                CheckIn(
                    id = 1L,
                    traceLocationId = "41da2115-eba2-49bd-bf17-adb3d635ddaf".encode(),
                    traceLocationIdHash = "41da2115-eba2-49bd-bf17-adb3d635ddaf".encode(),
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
                    createJournalEntry = false
                )
            )
        }
    }
}
