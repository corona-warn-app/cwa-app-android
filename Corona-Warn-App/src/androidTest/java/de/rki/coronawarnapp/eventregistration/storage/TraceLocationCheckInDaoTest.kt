package de.rki.coronawarnapp.eventregistration.storage

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.rki.coronawarnapp.eventregistration.storage.CheckInDatabaseData.testCheckIn
import de.rki.coronawarnapp.eventregistration.storage.CheckInDatabaseData.testCheckInWithoutCheckOutTime
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.joda.time.Instant
import org.junit.After
import org.junit.Test

class TraceLocationCheckInDaoTest {

    private val traceLocationDatabase = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        TraceLocationDatabase::class.java
    ).build()

    private val checkInDao = traceLocationDatabase.eventCheckInDao()

    @After
    fun tearDown() {
        traceLocationDatabase.clearAllTables()
    }

    @Test
    fun traceLocationCheckInDaoShouldReturnNoEntriesInitially() = runBlocking {
        val checkInsFlow = checkInDao.allEntries()

        checkInsFlow.first() shouldBe emptyList()
    }

    @Test
    fun traceLocationCheckInDaoShouldSuccessfullyInsertCheckIn() = runBlocking {
        val checkInsFlow = checkInDao.allEntries()

        val generatedId = checkInDao.insert(testCheckIn)

        checkInsFlow.first() shouldBe listOf(testCheckIn.copy(id = generatedId))
    }

    @Test
    fun traceLocationCheckInDaoShouldSuccessfullyInsertMultipleCheckIns() = runBlocking {
        val checkInsFlow = checkInDao.allEntries()

        val testCheckInGeneratedId = checkInDao.insert(testCheckIn)
        val testCheckInWithoutCheckOutTimeGeneratedId = checkInDao.insert(testCheckInWithoutCheckOutTime)

        checkInsFlow.first() shouldBe listOf(
            testCheckIn.copy(id = testCheckInGeneratedId),
            testCheckInWithoutCheckOutTime.copy(id = testCheckInWithoutCheckOutTimeGeneratedId)
        )
    }

    @Test
    fun traceLocationCheckInDaoShouldSuccessfullyUpdateCheckIn() = runBlocking {
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
    fun traceLocationCheckInDaoShouldSuccessfullyDeleteAllCheckIns() = runBlocking {
        val checkInsFlow = checkInDao.allEntries()

        checkInDao.insert(testCheckIn)
        checkInDao.insert(testCheckInWithoutCheckOutTime)

        checkInDao.deleteAll()

        checkInsFlow.first() shouldBe emptyList()
    }
}
