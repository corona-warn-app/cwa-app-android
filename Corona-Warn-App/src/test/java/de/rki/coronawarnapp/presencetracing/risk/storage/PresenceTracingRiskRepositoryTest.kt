package de.rki.coronawarnapp.presencetracing.risk.storage

import de.rki.coronawarnapp.presencetracing.risk.calculation.CheckInWarningOverlap
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingRiskCalculator
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class PresenceTracingRiskRepositoryTest : BaseTest() {

    @MockK lateinit var presenceTracingRiskCalculator: PresenceTracingRiskCalculator
    @MockK lateinit var databaseFactory: PresenceTracingRiskDatabase.Factory
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var traceTimeIntervalMatchDao: TraceTimeIntervalMatchDao
    @MockK lateinit var riskLevelResultDao: PresenceTracingRiskLevelResultDao
    @MockK lateinit var database: PresenceTracingRiskDatabase

    val now = Instant.ofEpochMilli(9999999)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { databaseFactory.create() } returns database
        every { database.traceTimeIntervalMatchDao() } returns traceTimeIntervalMatchDao
        every { database.presenceTracingRiskLevelResultDao() } returns riskLevelResultDao
        every { timeStamper.nowUTC } returns now
        every { traceTimeIntervalMatchDao.allMatches() } returns flowOf(emptyList())
        coEvery { traceTimeIntervalMatchDao.insert(any()) } just Runs
        every { riskLevelResultDao.insert(any()) } just Runs
        coEvery { traceTimeIntervalMatchDao.deleteMatchesForPackage(any()) } just Runs
        coEvery { presenceTracingRiskCalculator.calculateNormalizedTime(any()) } returns listOf()
        coEvery { presenceTracingRiskCalculator.calculateTotalRisk(any()) } returns RiskState.LOW_RISK
    }

    @Test
    fun `matchesOfLast14DaysPlusToday works`() {
        runBlockingTest {
            createInstance()
        }
    }

    @Test
    fun `traceLocationCheckInRiskStates works`() {
        runBlockingTest {
            createInstance()
        }
    }

    @Test
    fun `presenceTracingDayRisk works`() {
        runBlockingTest {
            createInstance()
        }
    }

    @Test
    fun `deleteStaleData works`() {
        runBlockingTest {
            createInstance()
        }
    }

    @Test
    fun `deleteAllMatches works`() {
        runBlockingTest {
            createInstance()
        }
    }

    @Test
    fun `latestEntries works`() {
        runBlockingTest {
            createInstance()
        }
    }

    @Test
    fun `report successful calculation works`() {
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
            riskState = RiskState.LOW_RISK
        )
        runBlockingTest {
            createInstance().reportCalculation(
                successful = true,
                overlaps = listOf(overlap)
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
            riskState = RiskState.CALCULATION_FAILED
        )
        runBlockingTest {
            createInstance().reportCalculation(
                successful = false,
                overlaps = listOf(overlap)
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
        timeStamper
    )
}
