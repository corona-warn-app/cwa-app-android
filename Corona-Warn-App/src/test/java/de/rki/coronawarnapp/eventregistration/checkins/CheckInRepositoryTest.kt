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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
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
                id = 0L,
                guid = "41da2115-eba2-49bd-bf17-adb3d635ddaf",
                version = 1,
                type = 2,
                description = "brothers birthday",
                address = "Malibu",
                traceLocationStart = Instant.EPOCH,
                traceLocationEnd = null,
                defaultCheckInLengthInMinutes = null,
                signature = "abc",
                checkInStart = Instant.EPOCH,
                checkInEnd = null,
                targetCheckInEnd = null,
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
            createInstance(scope = this).addCheckIn(
                CheckIn(
                    id = 1L,
                    guid = "41da2115-eba2-49bd-bf17-adb3d635ddaf",
                    version = 1,
                    type = 2,
                    description = "brothers birthday",
                    address = "Malibu",
                    traceLocationStart = time,
                    traceLocationEnd = null,
                    defaultCheckInLengthInMinutes = null,
                    signature = "abc",
                    checkInStart = time,
                    checkInEnd = null,
                    targetCheckInEnd = null,
                    createJournalEntry = false
                )
            )
            coVerify {
                checkInDao.insert(
                    TraceLocationCheckInEntity(
                        id = 1L,
                        guid = "41da2115-eba2-49bd-bf17-adb3d635ddaf",
                        version = 1,
                        type = 2,
                        description = "brothers birthday",
                        address = "Malibu",
                        traceLocationStart = time,
                        traceLocationEnd = null,
                        defaultCheckInLengthInMinutes = null,
                        signature = "abc",
                        checkInStart = time,
                        checkInEnd = null,
                        targetCheckInEnd = null,
                        createJournalEntry = false
                    )
                )
            }
        }
    }

    @Test
    fun `update new check in`() {
        coEvery { checkInDao.update(any()) } returns Unit
        runBlockingTest {
            val start = Instant.ofEpochMilli(1397210400000)
            val end = Instant.ofEpochMilli(1615796487)
            createInstance(scope = this).updateCheckIn(0L) {
                CheckIn(
                    id = 0L,
                    guid = "6e5530ce-1afc-4695-a4fc-572e6443eacd",
                    version = 1,
                    type = 2,
                    description = "sisters birthday",
                    address = "Long Beach",
                    traceLocationStart = start,
                    traceLocationEnd = end,
                    defaultCheckInLengthInMinutes = null,
                    signature = "efg",
                    checkInStart = start,
                    checkInEnd = end,
                    targetCheckInEnd = end,
                    createJournalEntry = false
                )
            }
            coVerify {
                checkInDao.update(
                    TraceLocationCheckInEntity(
                        id = 0L,
                        guid = "6e5530ce-1afc-4695-a4fc-572e6443eacd",
                        version = 1,
                        type = 2,
                        description = "sisters birthday",
                        address = "Long Beach",
                        traceLocationStart = start,
                        traceLocationEnd = end,
                        defaultCheckInLengthInMinutes = null,
                        signature = "efg",
                        checkInStart = start,
                        checkInEnd = end,
                        targetCheckInEnd = end,
                        createJournalEntry = false
                    )
                )
            }
        }
    }

    @Test
    fun `get data`() {
        val start = Instant.ofEpochMilli(1615796487)
        val end = Instant.ofEpochMilli(1397210400000)
        allEntriesFlow.value = listOf(
            TraceLocationCheckInEntity(
                id = 0L,
                guid = "6e5530ce-1afc-4695-a4fc-572e6443eacd",
                version = 1,
                type = 2,
                description = "sisters birthday",
                address = "Long Beach",
                traceLocationStart = start,
                traceLocationEnd = end,
                defaultCheckInLengthInMinutes = null,
                signature = "efg",
                checkInStart = start,
                checkInEnd = end,
                targetCheckInEnd = end,
                createJournalEntry = false
            )
        )
        runBlockingTest {
            createInstance(scope = this).allCheckIns.first() shouldBe listOf(
                CheckIn(
                    id = 0L,
                    guid = "6e5530ce-1afc-4695-a4fc-572e6443eacd",
                    version = 1,
                    type = 2,
                    description = "sisters birthday",
                    address = "Long Beach",
                    traceLocationStart = start,
                    traceLocationEnd = end,
                    defaultCheckInLengthInMinutes = null,
                    signature = "efg",
                    checkInStart = start,
                    checkInEnd = end,
                    targetCheckInEnd = end,
                    createJournalEntry = false
                )
            )
        }
    }
}
