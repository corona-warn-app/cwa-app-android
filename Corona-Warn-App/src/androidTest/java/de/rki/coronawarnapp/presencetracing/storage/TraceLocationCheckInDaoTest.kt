package de.rki.coronawarnapp.presencetracing.storage

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.rki.coronawarnapp.presencetracing.storage.CheckInDatabaseData.testCheckIn
import de.rki.coronawarnapp.presencetracing.storage.CheckInDatabaseData.testCheckInWithoutCheckOutTime
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.joda.time.Instant
import org.junit.After
import org.junit.Test
import testhelpers.BaseTestInstrumentation

class TraceLocationCheckInDaoTest : BaseTestInstrumentation() {

    private val traceLocationDatabase = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        TraceLocationDatabase::class.java
    ).build()

    private val checkInDao = traceLocationDatabase.checkInDao()

    @After
    fun tearDown() {
        traceLocationDatabase.clearAllTables()
    }

    @Test
    fun traceLocationCheckInDaoShouldReturnNoEntriesInitially() = runTest {
        val checkInsFlow = checkInDao.allEntries()

        checkInsFlow.first() shouldBe emptyList()
    }

    @Test
    fun traceLocationCheckInDaoShouldSuccessfullyInsertCheckIn() = runTest {
        val checkInsFlow = checkInDao.allEntries()

        val generatedId = checkInDao.insert(testCheckIn)

        checkInsFlow.first() shouldBe listOf(testCheckIn.copy(id = generatedId))
    }

    @Test
    fun traceLocationCheckInDaoShouldSuccessfullyInsertMultipleCheckIns() = runTest {
        val checkInsFlow = checkInDao.allEntries()

        val testCheckInGeneratedId = checkInDao.insert(testCheckIn)
        val testCheckInWithoutCheckOutTimeGeneratedId = checkInDao.insert(testCheckInWithoutCheckOutTime)

        checkInsFlow.first() shouldBe listOf(
            testCheckIn.copy(id = testCheckInGeneratedId),
            testCheckInWithoutCheckOutTime.copy(id = testCheckInWithoutCheckOutTimeGeneratedId)
        )
    }

    @Test
    fun traceLocationCheckInDaoShouldSuccessfullyUpdateCheckIn() = runTest {
        val checkInsFlow = checkInDao.allEntries()

        val testCheckInGeneratedId = checkInDao.insert(testCheckInWithoutCheckOutTime)

        val updatedCheckIn = testCheckInWithoutCheckOutTime.copy(
            id = testCheckInGeneratedId,
            checkInEnd = Instant.parse("2021-01-01T14:00:00.000Z")
        )

        checkInDao.update(updatedCheckIn)

        checkInsFlow.first() shouldBe listOf(updatedCheckIn)
    }

    @Test
    fun traceLocationCheckInDaoShouldSuccessfullyDeleteAllCheckIns() = runTest {
        val checkInsFlow = checkInDao.allEntries()

        checkInDao.insert(testCheckIn)
        checkInDao.insert(testCheckInWithoutCheckOutTime)

        checkInDao.deleteAll()

        checkInsFlow.first() shouldBe emptyList()
    }

    @Test
    fun traceLocationCheckInDaoRetrieveById() = runTest {
        val generatedId1 = checkInDao.insert(testCheckIn)
        val generatedId2 = checkInDao.insert(testCheckIn)

        checkInDao.entryForId(generatedId1)!!.id shouldBe generatedId1
        checkInDao.entryForId(generatedId2)!!.id shouldBe generatedId2
    }

    @Test
    fun traceLocationCheckInDaoDeleteById() = runTest {
        val generatedId1 = checkInDao.insert(testCheckIn)
        val generatedId2 = checkInDao.insert(testCheckIn)

        checkInDao.deleteByIds(listOf(generatedId1))
        checkInDao.entryForId(generatedId1) shouldBe null
        checkInDao.entryForId(generatedId2) shouldNotBe null
    }

    @Test
    fun traceLocationCheckInDaoUpdateById() = runTest {
        val generatedId1 = checkInDao.insert(testCheckIn)

        checkInDao.updateEntityById(generatedId1) {
            it.copy(address = "test")
        }
        checkInDao.entryForId(generatedId1)!!.address shouldBe "test"
    }

    @Test
    fun traceLocationCheckInDaoUpdateById_raceCondition1(): Unit = runTest {
        shouldThrow<IllegalStateException> {
            checkInDao.updateEntityById(123) {
                mockk()
            }
        }
    }
}
