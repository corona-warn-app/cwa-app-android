package de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows

import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.test.runTest
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Duration

class AnalyticsExposureWindowsRepositoryTest : BaseTest() {

    @MockK lateinit var databaseFactory: AnalyticsExposureWindowDatabase.Factory
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var analyticsExposureWindowDao: AnalyticsExposureWindowDao
    @MockK lateinit var analyticsExposureWindowDatabase: AnalyticsExposureWindowDatabase
    @MockK lateinit var analyticsReportedExposureWindowEntity: AnalyticsReportedExposureWindowEntity
    @MockK lateinit var analyticsExposureWindowEntity: AnalyticsExposureWindowEntity

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

    @Test
    fun `stale data clean up`() {
        addDatabase()
        runTest {
            newInstance().deleteStaleData()
            coVerify {
                analyticsExposureWindowDao.deleteReportedOlderThan(
                    now.minus(Duration.ofDays(15)).toEpochMilli()
                )
            }
        }
    }

    @Test
    fun `insert if hash not reported or new`() {
        coEvery { analyticsExposureWindowDao.getReported(any()) } returns null
        coEvery { analyticsExposureWindowDao.getNew(any()) } returns null
        coEvery { analyticsExposureWindowDao.insert(any()) } just Runs
        addDatabase()
        runTest {
            newInstance().addNew(analyticsExposureWindow)
            coVerify { analyticsExposureWindowDao.insert(any()) }
        }
    }

    @Test
    fun `no insert if hash reported`() {
        coEvery { analyticsExposureWindowDao.getReported(any()) } returns analyticsReportedExposureWindowEntity
        coEvery { analyticsExposureWindowDao.getNew(any()) } returns analyticsExposureWindowEntity
        addDatabase()
        runTest {
            newInstance().addNew(analyticsExposureWindow)
            coVerify(exactly = 0) { analyticsExposureWindowDao.insert(any()) }
        }
    }

    @Test
    fun `no insert if hash in new`() {
        coEvery { analyticsExposureWindowDao.getReported(any()) } returns analyticsReportedExposureWindowEntity
        coEvery { analyticsExposureWindowDao.getNew(any()) } returns analyticsExposureWindowEntity
        addDatabase()
        runTest {
            newInstance().addNew(analyticsExposureWindow)
            coVerify(exactly = 0) { analyticsExposureWindowDao.insert(any()) }
        }
    }

    @Test
    fun `hash value equal for two instances with same data`() {
        val copy = analyticsExposureWindow.copy()
        copy.sha256Hash() shouldBe analyticsExposureWindow.sha256Hash()
    }

    @Test
    fun `hash value not equal for two instances with different data`() {
        val copy = analyticsExposureWindow.copy(dateMillis = 9999)
        copy.sha256Hash() shouldNotBe analyticsExposureWindow.sha256Hash()
    }

    private fun addDatabase() {
        every { analyticsExposureWindowDatabase.analyticsExposureWindowDao() } returns analyticsExposureWindowDao
        every { databaseFactory.create() } returns analyticsExposureWindowDatabase
    }

    private fun newInstance() =
        AnalyticsExposureWindowRepository(
            databaseFactory,
            timeStamper
        )
}
