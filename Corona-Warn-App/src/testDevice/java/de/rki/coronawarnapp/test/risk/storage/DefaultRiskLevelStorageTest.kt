package de.rki.coronawarnapp.test.risk.storage

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.RiskLevelTaskResult
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.risk.storage.DefaultRiskLevelStorage
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase
import de.rki.coronawarnapp.risk.storage.internal.riskresults.PersistedRiskLevelResultDao
import de.rki.coronawarnapp.risk.storage.legacy.RiskLevelResultMigrator
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DefaultRiskLevelStorageTest : BaseTest() {

    @MockK lateinit var databaseFactory: RiskResultDatabase.Factory
    @MockK lateinit var database: RiskResultDatabase
    @MockK lateinit var riskResultTables: RiskResultDatabase.RiskResultsDao
    @MockK lateinit var exposureWindowTables: RiskResultDatabase.ExposureWindowsDao
    @MockK lateinit var riskLevelResultMigrator: RiskLevelResultMigrator

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
    private val testRisklevelResult = RiskLevelTaskResult(
        calculatedAt = Instant.ofEpochMilli(9999L),
        aggregatedRiskResult = AggregatedRiskResult(
            totalRiskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
            totalMinimumDistinctEncountersWithLowRisk = 1,
            totalMinimumDistinctEncountersWithHighRisk = 2,
            mostRecentDateWithLowRisk = Instant.ofEpochMilli(3),
            mostRecentDateWithHighRisk = Instant.ofEpochMilli(4),
            numberOfDaysWithLowRisk = 5,
            numberOfDaysWithHighRisk = 6
        ),
        exposureWindows = listOf(
            ExposureWindow.Builder().build(),
            ExposureWindow.Builder().build()
        ),
        failureReason = null
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { databaseFactory.create() } returns database
        every { database.riskResults() } returns riskResultTables
        every { database.exposureWindows() } returns exposureWindowTables
        every { database.clearAllTables() } just Runs

        coEvery { riskLevelResultMigrator.getLegacyResults() } returns emptyList()

        every { riskResultTables.allEntries() } returns flowOf(listOf(testRiskLevelResultDao))
        every { riskResultTables.latestEntries(2) } returns emptyFlow()
        every { riskResultTables.latestAndLastSuccessful() } returns emptyFlow()
        coEvery { riskResultTables.insertEntry(any()) } just Runs
        coEvery { riskResultTables.deleteOldest(any()) } returns 7

        every { exposureWindowTables.allEntries() } returns emptyFlow()
        coEvery { exposureWindowTables.insertWindows(any()) } returns listOf(111L, 222L)
        coEvery { exposureWindowTables.insertScanInstances(any()) } just Runs
        coEvery { exposureWindowTables.deleteByRiskResultId(any()) } returns 1
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createInstance(
        scope: CoroutineScope = TestCoroutineScope()
    ) = DefaultRiskLevelStorage(
        scope = scope,
        riskResultDatabaseFactory = databaseFactory,
        riskLevelResultMigrator = riskLevelResultMigrator
    )

    @Test
    fun `stored item limit for deviceForTesters`() {
        createInstance().storedResultLimit shouldBe 2 * 6
    }

    @Test
    fun `we are NOT storing or cleaning up exposure windows`() = runBlockingTest {
        val instance = createInstance()
        instance.storeResult(testRisklevelResult)

        coVerify {
            riskResultTables.insertEntry(any())
            riskResultTables.deleteOldest(instance.storedResultLimit)
        }

        coVerify(exactly = 0) {
            exposureWindowTables.insertWindows(any())
            exposureWindowTables.insertScanInstances(any())
            exposureWindowTables.deleteByRiskResultId(listOf("riskresult-id"))
        }
    }
}
