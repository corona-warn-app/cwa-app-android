package de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseTestInstrumentation

@RunWith(AndroidJUnit4::class)
class AnalyticsExposureWindowsDatabaseTest : BaseTestInstrumentation() {

    private val database: AnalyticsExposureWindowDatabase =
        Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AnalyticsExposureWindowDatabase::class.java
        )
            .build()

    private val dao = database.analyticsExposureWindowDao()

    @After
    fun teardown() {
        database.clearAllTables()
    }

    @Test
    fun testMoveToReportedAndRollback() = runBlocking {
        // insert new
        val exposureWindowEntity = AnalyticsExposureWindowEntity("hash", 1, 1, 1, 1, 1.0, 1)
        val scanInstance = AnalyticsScanInstanceEntity(null, "hash", 1, 1, 1)
        val wrapper = AnalyticsExposureWindowEntityWrapper(exposureWindowEntity, listOf(scanInstance))
        dao.insert(listOf(wrapper))
        val allNew = dao.getAllNew()
        allNew.size shouldBe 1
        allNew[0].exposureWindowEntity.sha256Hash shouldBe "hash"

        // move to reported
        dao.moveToReported(listOf(wrapper), 999999)
        dao.getAllNew() shouldBe listOf()
        val reported = dao.getReported("hash")
        reported!!.sha256Hash shouldBe "hash"

        // rollback
        dao.rollback(listOf(wrapper), listOf(reported))
        val allNew2 = dao.getAllNew()
        allNew2.size shouldBe 1
        allNew2[0].exposureWindowEntity.sha256Hash shouldBe "hash"
        dao.getReported("hash") shouldBe null
    }

    @Test
    fun testDeleteStaleReported() = runBlocking {
        // insert new
        val exposureWindowEntity = AnalyticsExposureWindowEntity("hash", 1, 1, 1, 1, 1.0, 1)
        val scanInstance = AnalyticsScanInstanceEntity(null, "hash", 1, 1, 1)
        val wrapper = AnalyticsExposureWindowEntityWrapper(exposureWindowEntity, listOf(scanInstance))
        val exposureWindowEntity2 = AnalyticsExposureWindowEntity("hash2", 1, 1, 1, 1, 1.0, 1)
        val scanInstance2 = AnalyticsScanInstanceEntity(null, "hash2", 1, 1, 1)
        val wrapper2 = AnalyticsExposureWindowEntityWrapper(exposureWindowEntity2, listOf(scanInstance2))
        dao.insert(listOf(wrapper, wrapper2))

        // move to reported
        dao.moveToReported(listOf(wrapper), 999990)
        dao.moveToReported(listOf(wrapper2), 999999)

        // delete stale
        dao.deleteReportedOlderThan(999999)
        dao.getReported("hash") shouldBe null
        dao.getReported("hash2")!!.sha256Hash shouldBe "hash2"
    }
}
