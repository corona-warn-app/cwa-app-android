package de.rki.coronawarnapp.presencetracing.risk.storage

import de.rki.coronawarnapp.presencetracing.risk.CheckInsFilter
import de.rki.coronawarnapp.presencetracing.risk.calculation.CheckInNormalizedTime
import de.rki.coronawarnapp.presencetracing.risk.calculation.CheckInRiskPerDay
import de.rki.coronawarnapp.presencetracing.risk.calculation.CheckInWarningOverlap
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingDayRisk
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingRiskCalculator
import de.rki.coronawarnapp.presencetracing.risk.minusDaysAtStartOfDayUtc
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Days
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider

class PresenceTracingRiskRepositoryTest : BaseTest() {

    @MockK lateinit var presenceTracingRiskCalculator: PresenceTracingRiskCalculator
    @MockK lateinit var databaseFactory: PresenceTracingRiskDatabase.Factory
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var traceTimeIntervalMatchDao: TraceTimeIntervalMatchDao
    @MockK lateinit var riskLevelResultDao: PresenceTracingRiskLevelResultDao
    @MockK lateinit var database: PresenceTracingRiskDatabase
    @MockK lateinit var checkInsFilter: CheckInsFilter
    val dispatcherProvider = TestDispatcherProvider()

    private val now = Instant.parse("2022-02-02T11:59:59Z")
    private val fifteenDaysAgo = now.minus(Days.days(15).toStandardDuration())
    private val maxCheckInAgeInDays = 10

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns now

        every { traceTimeIntervalMatchDao.allMatches() } returns flowOf(emptyList())
        coEvery { traceTimeIntervalMatchDao.insert(any()) } just Runs
        coEvery { traceTimeIntervalMatchDao.deleteMatchesForPackage(any()) } just Runs
        coEvery { traceTimeIntervalMatchDao.deleteAll() } just Runs
        coEvery { traceTimeIntervalMatchDao.deleteOlderThan(any()) } just Runs

        every { riskLevelResultDao.insert(any()) } just Runs
        coEvery { riskLevelResultDao.deleteOlderThan(any()) } just Runs

        coEvery { databaseFactory.create() } returns database
        every { database.traceTimeIntervalMatchDao() } returns traceTimeIntervalMatchDao
        every { database.presenceTracingRiskLevelResultDao() } returns riskLevelResultDao

        coEvery { presenceTracingRiskCalculator.calculateNormalizedTime(any()) } returns listOf()
        coEvery { presenceTracingRiskCalculator.calculateTotalRisk(any()) } returns RiskState.LOW_RISK

        coEvery { checkInsFilter.filterCheckInWarningsByAge(any(), any()) } returns emptyList()
        coEvery { riskLevelResultDao.allEntries() } returns flowOf(emptyList())
    }

    @Test
    fun `all overlaps works`() {
        val entity = TraceTimeIntervalMatchEntity(
            checkInId = 1L,
            traceWarningPackageId = "traceWarningPackageId",
            transmissionRiskLevel = 1,
            startTimeMillis = fifteenDaysAgo.minus(100000).millis,
            endTimeMillis = fifteenDaysAgo.millis
        )
        val entity2 = TraceTimeIntervalMatchEntity(
            checkInId = 2L,
            traceWarningPackageId = "traceWarningPackageId",
            transmissionRiskLevel = 1,
            startTimeMillis = now.minus(100000).millis,
            endTimeMillis = now.minus(80000).millis
        )
        every { traceTimeIntervalMatchDao.allMatches() } returns flowOf(listOf(entity, entity2))
        runBlockingTest {
            val overlaps = createInstance().allOverlaps.first()
            overlaps.size shouldBe 2
            overlaps[0].checkInId shouldBe 1L
            overlaps[1].checkInId shouldBe 2L
        }
    }

    @Test
    fun `traceLocationCheckInRiskStates works`() {
        val entity = TraceTimeIntervalMatchEntity(
            checkInId = 2L,
            traceWarningPackageId = "traceWarningPackageId",
            transmissionRiskLevel = 1,
            startTimeMillis = now.minus(100000).millis,
            endTimeMillis = now.minus(80000).millis
        )
        every { traceTimeIntervalMatchDao.allMatches() } returns flowOf(listOf(entity))
        coEvery { checkInsFilter.filterCheckInWarningsByAge(any(), any()) } returns
            listOf(entity.toCheckInWarningOverlap())
        val time = CheckInNormalizedTime(
            checkInId = 2L,
            localDateUtc = now.minus(100000).toLocalDateUtc(),
            normalizedTime = 20.0
        )
        val riskPerDay = CheckInRiskPerDay(
            checkInId = 2L,
            localDateUtc = now.minus(100000).toLocalDateUtc(),
            riskState = RiskState.LOW_RISK
        )
        coEvery {
            presenceTracingRiskCalculator.calculateNormalizedTime(listOf(entity.toCheckInWarningOverlap()))
        } returns listOf(time)

        coEvery { presenceTracingRiskCalculator.calculateCheckInRiskPerDay(listOf(time)) } returns listOf(riskPerDay)
        runBlockingTest {
            val riskStates = createInstance().traceLocationCheckInRiskStates.first()
            riskStates.size shouldBe 1
            riskStates[0].checkInId shouldBe 2L
            riskStates[0].riskState shouldBe RiskState.LOW_RISK
        }
    }

    @Test
    fun `presenceTracingDayRisk works`() {
        val dayRisk = PresenceTracingDayRisk(
            localDateUtc = now.minus(100000).toLocalDateUtc(),
            riskState = RiskState.LOW_RISK
        )
        coEvery { presenceTracingRiskCalculator.calculateDayRisk(any()) } returns listOf(dayRisk)
        runBlockingTest {
            val risks = createInstance().presenceTracingDayRisk.first()
            risks.size shouldBe 1
            risks[0].riskState shouldBe RiskState.LOW_RISK
        }
    }

    @Test
    fun `deleteStaleData works`() {
        runBlockingTest {
            createInstance().deleteStaleData()
            coVerify {
                traceTimeIntervalMatchDao.deleteOlderThan(fifteenDaysAgo.millis)
                riskLevelResultDao.deleteOlderThan(fifteenDaysAgo.millis)
            }
        }
    }

    @Test
    fun `deleteAllMatches works`() {
        runBlockingTest {
            createInstance().deleteAllMatches()
            coVerify { traceTimeIntervalMatchDao.deleteAll() }
        }
    }

    @Test
    fun `report successful calculation works`() {
        val deadline = now.minusDaysAtStartOfDayUtc(maxCheckInAgeInDays).toInstant()
        coEvery { checkInsFilter.calculateDeadline(any()) } returns deadline
        val traceWarningPackageId = "traceWarningPackageId"
        val overlap = CheckInWarningOverlap(
            checkInId = 1L,
            transmissionRiskLevel = 1,
            traceWarningPackageId = traceWarningPackageId,
            startTime = Instant.ofEpochMilli(9991000),
            endTime = Instant.ofEpochMilli(9997000)
        )

        val result = PresenceTracingRiskLevelResultEntity(
            calculatedAtMillis = now.millis,
            riskState = RiskState.LOW_RISK,
            calculatedFromMillis = deadline.millis
        )
        runBlockingTest {
            createInstance().reportCalculation(
                successful = true,
                newOverlaps = listOf(overlap)
            )

            coVerify {
                traceTimeIntervalMatchDao.deleteMatchesForPackage(traceWarningPackageId)
                traceTimeIntervalMatchDao.insert(listOf(overlap.toTraceTimeIntervalMatchEntity()))
                riskLevelResultDao.insert(result)
            }
        }
    }

    @Test
    fun `report failed calculation works`() {
        val deadline = now.minusDaysAtStartOfDayUtc(maxCheckInAgeInDays).toInstant()
        coEvery { checkInsFilter.calculateDeadline(any()) } returns deadline
        val traceWarningPackageId = "traceWarningPackageId"
        val overlap = CheckInWarningOverlap(
            checkInId = 1L,
            transmissionRiskLevel = 1,
            traceWarningPackageId = traceWarningPackageId,
            startTime = Instant.ofEpochMilli(9991000),
            endTime = Instant.ofEpochMilli(9997000)
        )

        val result = PresenceTracingRiskLevelResultEntity(
            calculatedAtMillis = now.millis,
            riskState = RiskState.CALCULATION_FAILED,
            calculatedFromMillis = deadline.millis
        )
        runBlockingTest {
            createInstance().reportCalculation(
                successful = false,
                newOverlaps = listOf(overlap)
            )

            coVerify {
                traceTimeIntervalMatchDao.deleteMatchesForPackage(traceWarningPackageId)
                traceTimeIntervalMatchDao.insert(any())
                riskLevelResultDao.insert(result)
            }
        }
    }

    private fun createInstance() = PresenceTracingRiskRepository(
        presenceTracingRiskCalculator,
        databaseFactory,
        timeStamper,
        checkInsFilter,
        TestCoroutineScope(),
        dispatcherProvider
    )
}
