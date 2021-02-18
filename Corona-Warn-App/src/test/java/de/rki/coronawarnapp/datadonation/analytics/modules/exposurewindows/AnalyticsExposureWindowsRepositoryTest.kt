package de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows

import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Days
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class AnalyticsExposureWindowsRepositoryTest : BaseTest() {

    @MockK lateinit var databaseFactory: AnalyticsExposureWindowDatabase.Factory
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var analyticsExposureWindowDao: AnalyticsExposureWindowDao
    @MockK lateinit var analyticsExposureWindowDatabase: AnalyticsExposureWindowDatabase
    @MockK lateinit var analyticsReportedExposureWindowEntity: AnalyticsReportedExposureWindowEntity

    private val analyticsScanInstance = AnalyticsScanInstance(
        1,
        1,
        1,
    )
    private val analyticsExposureWindow = AnalyticsExposureWindow(
        1,
        1L,
        1,
        1,
        listOf(analyticsScanInstance),
        1.0,
        1
    )
    private val now = Instant.now()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns now
        coEvery { analyticsExposureWindowDao.deleteReportedOlderThan(any()) } just Runs
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `stale data clean up`() {
        addDatabase()
        runBlockingTest {
            newInstance().deleteStaleData()
            coVerify {
                analyticsExposureWindowDao.deleteReportedOlderThan(
                    now.minus(Days.days(15).toStandardDuration()).millis
                )
            }
        }
    }

    @Test
    fun `insert if hash not reported`() {
        coEvery { analyticsExposureWindowDao.getReported(any()) } returns null
        coEvery { analyticsExposureWindowDao.insert(any()) } just Runs
        addDatabase()
        runBlockingTest {
            newInstance().addNew(analyticsExposureWindow)
            coVerify { analyticsExposureWindowDao.insert(any()) }
        }
    }

    @Test
    fun `no insert if hash reported`() {
        coEvery { analyticsExposureWindowDao.getReported(any()) } returns analyticsReportedExposureWindowEntity
        addDatabase()
        runBlockingTest {
            newInstance().addNew(analyticsExposureWindow)
            coVerify(exactly = 0) { analyticsExposureWindowDao.insert(any()) }
        }
    }

    private fun addDatabase() {
        every { analyticsExposureWindowDatabase.analyticsExposureWindowDao() } returns analyticsExposureWindowDao
        every { databaseFactory.create() } returns analyticsExposureWindowDatabase
    }

    private fun newInstance() =
        AnalyticsExposureWindowRepository(
            databaseFactory, timeStamper
        )
}
