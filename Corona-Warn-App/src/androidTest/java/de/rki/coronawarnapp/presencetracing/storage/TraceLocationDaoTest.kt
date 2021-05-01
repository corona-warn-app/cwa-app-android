package de.rki.coronawarnapp.presencetracing.storage

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.rki.coronawarnapp.presencetracing.storage.TraceLocationDatabaseData.testTraceLocation1
import de.rki.coronawarnapp.presencetracing.storage.TraceLocationDatabaseData.testTraceLocation2
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import testhelpers.BaseTestInstrumentation

class TraceLocationDaoTest : BaseTestInstrumentation() {

    private val traceLocationDatabase = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        TraceLocationDatabase::class.java
    ).build()

    private val traceLocationDao = traceLocationDatabase.traceLocationDao()

    @After
    fun tearDown() {
        traceLocationDatabase.clearAllTables()
    }

    @Test
    fun traceLocationDaoShouldReturnNoEntriesInitially() = runBlocking {
        val traceLocationsFlow = traceLocationDao.allEntries()

        traceLocationsFlow.first() shouldBe emptyList()
    }

    @Test
    fun traceLocationDaoShouldSuccessfullyInsertTraceLocation() = runBlocking {
        val traceLocationsFlow = traceLocationDao.allEntries()

        traceLocationDao.insert(testTraceLocation1)

        traceLocationsFlow.first() shouldBe listOf(testTraceLocation1)
    }

    @Test
    fun traceLocationDaoShouldSuccessfullyInsertMultipleTraceLocations() = runBlocking {
        val traceLocationsFlow = traceLocationDao.allEntries()

        traceLocationDao.insert(testTraceLocation1)
        traceLocationDao.insert(testTraceLocation2)

        traceLocationsFlow.first() shouldBe listOf(testTraceLocation1, testTraceLocation2)
    }

    @Test
    fun traceLocationDaoShouldSuccessfullyDeleteSingleTraceLocation() = runBlocking {
        val traceLocationsFlow = traceLocationDao.allEntries()

        traceLocationDao.insert(testTraceLocation1)
        traceLocationDao.delete(testTraceLocation1)
        traceLocationsFlow.first() shouldBe emptyList()
    }

    @Test
    fun traceLocationDaoShouldSuccessfullyDeleteAllTraceLocations() = runBlocking {
        val traceLocationsFlow = traceLocationDao.allEntries()

        traceLocationDao.insert(testTraceLocation1)
        traceLocationDao.insert(testTraceLocation2)
        traceLocationDao.deleteAll()
        traceLocationsFlow.first() shouldBe emptyList()
    }

    @Test
    fun entryForIdShouldReturnCorrectEntry() = runBlocking {
        traceLocationDao.insert(testTraceLocation1)
        traceLocationDao.insert(testTraceLocation2)

        traceLocationDao.entityForId(0) shouldBe null
        traceLocationDao.entityForId(1) shouldBe testTraceLocation1
        traceLocationDao.entityForId(2) shouldBe testTraceLocation2
    }
}
