package de.rki.coronawarnapp.test.risk.storage

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.presencetracing.risk.storage.PresenceTracingRiskRepository
import de.rki.coronawarnapp.risk.EwRiskLevelTaskResult
import de.rki.coronawarnapp.risk.ExposureWindowsFilter
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import de.rki.coronawarnapp.risk.storage.DefaultRiskLevelStorage
import de.rki.coronawarnapp.risk.storage.internal.RiskCombinator
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase
import de.rki.coronawarnapp.risk.storage.internal.riskresults.PersistedRiskLevelResultDao
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class DefaultRiskLevelStorageTest : testhelpers.BaseTest() {

    @MockK lateinit var databaseFactory: RiskResultDatabase.Factory
    @MockK lateinit var database: RiskResultDatabase
    @MockK lateinit var riskResultTables: RiskResultDatabase.RiskResultsDao
    @MockK lateinit var exposureWindowTables: RiskResultDatabase.ExposureWindowsDao
    @MockK lateinit var presenceTracingRiskRepository: PresenceTracingRiskRepository
    @MockK lateinit var ewFilter: ExposureWindowsFilter

    private val testRiskLevelResultDao = PersistedRiskLevelResultDao(
        id = "riskresult-id",
        calculatedAt = Instant.ofEpochMilli(9999L),
        aggregatedRiskResult = PersistedRiskLevelResultDao.PersistedAggregatedRiskResult(
            totalRiskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
            totalMinimumDistinctEncountersWithLowRisk = 1,
            totalMinimumDistinctEncountersWithHighRisk = 2,
            mostRecentDateWithLowRisk = Instant.ofEpochMilli(3),
            mostRecentDateWithHighRisk = Instant.ofEpochMilli(4),
            numberOfDaysWithLowRisk = 5,
            numberOfDaysWithHighRisk = 6
        ),
        failureReason = null
    )
    private val testRisklevelResult = EwRiskLevelTaskResult(
        calculatedAt = Instant.ofEpochMilli(9999L),
        ewAggregatedRiskResult = EwAggregatedRiskResult(
            totalRiskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
            totalMinimumDistinctEncountersWithLowRisk = 1,
            totalMinimumDistinctEncountersWithHighRisk = 2,
            mostRecentDateWithLowRisk = Instant.ofEpochMilli(3),
            mostRecentDateWithHighRisk = Instant.ofEpochMilli(4),
            numberOfDaysWithLowRisk = 5,
            numberOfDaysWithHighRisk = 6
        ),
        failureReason = null,
        exposureWindows = listOf(
            ExposureWindow.Builder().build(),
            ExposureWindow.Builder().build()
        )
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { databaseFactory.create() } returns database
        every { database.riskResults() } returns riskResultTables
        every { database.exposureWindows() } returns exposureWindowTables
        every { database.clearAllTables() } just Runs

        every { riskResultTables.allEntries() } returns flowOf(listOf(testRiskLevelResultDao))
        every { riskResultTables.latestEntries(2) } returns emptyFlow()
        every { riskResultTables.lastSuccessful() } returns emptyFlow()
        coEvery { riskResultTables.insertEntry(any()) } just Runs
        coEvery { riskResultTables.deleteOldest(any()) } returns 7

        every { exposureWindowTables.allEntries() } returns emptyFlow()
        coEvery { exposureWindowTables.insertWindows(any()) } returns listOf(111L, 222L)
        coEvery { exposureWindowTables.insertScanInstances(any()) } just Runs
        coEvery { exposureWindowTables.deleteByRiskResultId(any()) } returns 1

        every { presenceTracingRiskRepository.traceLocationCheckInRiskStates } returns emptyFlow()
        every { presenceTracingRiskRepository.presenceTracingDayRisk } returns emptyFlow()
        every { presenceTracingRiskRepository.allRiskLevelResults } returns emptyFlow()
    }

    private fun createInstance() = DefaultRiskLevelStorage(
        riskResultDatabaseFactory = databaseFactory,
        presenceTracingRiskRepository = presenceTracingRiskRepository,
        riskCombinator = RiskCombinator(TimeStamper()),
        ewFilter = ewFilter,
    )

    @Test
    fun `stored item limit for deviceForTesters`() {
        createInstance().storedResultLimit shouldBe 14 * 6
    }

    @Test
    fun `we are storing and cleaning up exposure windows`() = runTest {
        val instance = createInstance()
        instance.storeResult(testRisklevelResult)

        coVerify {
            riskResultTables.insertEntry(any())
            riskResultTables.deleteOldest(instance.storedResultLimit)

            exposureWindowTables.insertWindows(any())
            exposureWindowTables.insertScanInstances(any())
            exposureWindowTables.deleteByRiskResultId(listOf("riskresult-id"))
        }
    }
}
