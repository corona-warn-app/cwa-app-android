package de.rki.coronawarnapp.risk.storage

import de.rki.coronawarnapp.presencetracing.risk.storage.PresenceTracingRiskRepository
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.ExposureWindowsFilter
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.ewDaoWrapper
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.ewDayRisk
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.ewPersistedAggregatedRiskPerDateResult
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.ewRiskLevelResult
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.ewRiskLevelResultWithAggregatedRiskPerDateResult
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.ewRiskResult1Increased
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.ewRiskResult2Low
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.ptDayRisk
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.ptResult1Low
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.ptResult2Failed
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.testExposureWindow
import de.rki.coronawarnapp.risk.storage.internal.RiskCombinator
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase.AggregatedRiskPerDateResultDao
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase.ExposureWindowsDao
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase.Factory
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase.RiskResultsDao
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant

class BaseRiskLevelStorageTest : BaseTest() {

    @MockK lateinit var databaseFactory: Factory
    @MockK lateinit var ewRiskResultDatabase: RiskResultDatabase
    @MockK lateinit var ewRiskResultTables: RiskResultsDao
    @MockK lateinit var ewTables: ExposureWindowsDao
    @MockK lateinit var ewAggregatedRiskPerDateResultDao: AggregatedRiskPerDateResultDao
    @MockK lateinit var ptRiskRepository: PresenceTracingRiskRepository
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var ewFilter: ExposureWindowsFilter

    private lateinit var riskCombinator: RiskCombinator
    private val now = Instant.parse("2021-01-01T12:00:00.000Z")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns now
        riskCombinator = RiskCombinator(
            timeStamper = timeStamper
        )

        // ew data sources
        every { databaseFactory.create() } returns ewRiskResultDatabase
        every { ewRiskResultDatabase.riskResults() } returns ewRiskResultTables
        every { ewRiskResultDatabase.exposureWindows() } returns ewTables
        every { ewRiskResultDatabase.aggregatedRiskPerDate() } returns ewAggregatedRiskPerDateResultDao
        every { ewRiskResultDatabase.clearAllTables() } just Runs
        every { ewRiskResultTables.allEntries() } returns flowOf(listOf(ewRiskResult1Increased, ewRiskResult2Low))
        every { ewRiskResultTables.latestEntries(1) } returns flowOf(listOf(ewRiskResult1Increased))
        every { ewRiskResultTables.lastSuccessful() } returns
            flowOf(listOf(ewRiskResult1Increased))
        coEvery { ewRiskResultTables.insertEntry(any()) } just Runs
        coEvery { ewRiskResultTables.deleteOldest(any()) } returns 7
        every { ewTables.allEntries() } returns flowOf(listOf(ewDaoWrapper))
        every { ewAggregatedRiskPerDateResultDao.allEntries() } returns
            flowOf(listOf(ewPersistedAggregatedRiskPerDateResult))
        coEvery { ewAggregatedRiskPerDateResultDao.insertRisk(any()) } just Runs
        coEvery { ewFilter.filterDayRisksByAge(any(), any()) } returns listOf(ewDayRisk)
        every { ewTables.getWindowsForResult(any()) } returns flowOf(listOf(ewDaoWrapper))

        // pt data sources
        coEvery { ptRiskRepository.traceLocationCheckInRiskStates } returns emptyFlow()
        coEvery { ptRiskRepository.presenceTracingDayRisk } returns flowOf(listOf(ptDayRisk))
        coEvery { ptRiskRepository.allRiskLevelResults } returns flowOf(listOf(ptResult1Low, ptResult2Failed))
        coEvery { ptRiskRepository.clearAllTables() } just Runs
        coEvery { ptRiskRepository.latestRiskLevelResult } returns flowOf(ptResult1Low)
    }

    private fun createInstance(
        storedResultLimit: Int = 10,
        onStoreExposureWindows: (String, EwRiskLevelResult) -> Unit = { _, _ -> },
        onDeletedOrphanedExposureWindows: () -> Unit = {}
    ) = object : BaseRiskLevelStorage(
        riskResultDatabaseFactory = databaseFactory,
        presenceTracingRiskRepository = ptRiskRepository,
        riskCombinator = riskCombinator,
        ewFilter = ewFilter,
    ) {
        override val storedResultLimit: Int = storedResultLimit

        override suspend fun storeExposureWindows(storedResultId: String, resultEw: EwRiskLevelResult) {
            onStoreExposureWindows(storedResultId, resultEw)
        }

        override suspend fun deletedOrphanedExposureWindows() {
            onDeletedOrphanedExposureWindows()
        }
    }

    @Test
    fun `aggregatedRiskPerDateResults are returned from database and mapped`() {
        val testPersistedAggregatedRiskPerDateResultFlow = flowOf(listOf(ewPersistedAggregatedRiskPerDateResult))
        every { ewAggregatedRiskPerDateResultDao.allEntries() } returns testPersistedAggregatedRiskPerDateResultFlow
        runTest {
            val instance = createInstance()
            val allEntries = instance.aggregatedRiskPerDateResultTables.allEntries()
            allEntries shouldBe testPersistedAggregatedRiskPerDateResultFlow
            allEntries.first().map { it.toExposureWindowDayRisk() } shouldBe listOf(
                ewDayRisk
            )

            val aggregatedRiskPerDateResults = instance.ewDayRiskStates.first()
            aggregatedRiskPerDateResults shouldNotBe listOf(ewPersistedAggregatedRiskPerDateResult)
            aggregatedRiskPerDateResults shouldBe listOf(ewDayRisk)
        }
    }

    @Test
    fun `ptDayRiskStates are returned from database`() {
        runTest {
            val instance = createInstance()
            instance.ptDayRiskStates.first() shouldBe listOf(ptDayRisk)
        }
    }

    @Test
    fun `exposureWindows are returned from database and mapped`() {
        runTest {
            val exposureWindowDAOWrappers = createInstance().exposureWindowsTables.allEntries()
            exposureWindowDAOWrappers.first() shouldBe listOf(ewDaoWrapper)
            exposureWindowDAOWrappers.first().map { it.toExposureWindow() } shouldBe listOf(testExposureWindow)
        }
    }

    @Test
    fun `ewRiskLevelResults are returned from database and mapped`() {
        every { ewRiskResultTables.allEntries() } returns flowOf(listOf(ewRiskResult1Increased))
        every { ewTables.allEntries() } returns flowOf(emptyList())

        runTest {
            val instance = createInstance()
            instance.allEwRiskLevelResults.first() shouldBe listOf(ewRiskLevelResult)
        }
    }

    @Test
    fun `ewRiskLevelResults with exposure windows are returned from database and mapped`() {
        every { ewRiskResultTables.allEntries() } returns flowOf(listOf(ewRiskResult1Increased))
        every { ewTables.allEntries() } returns flowOf(listOf(ewDaoWrapper))

        runTest {
            val instance = createInstance()
            val riskLevelResult = ewRiskLevelResult.copy(exposureWindows = listOf(testExposureWindow))
            instance.allEwRiskLevelResultsWithExposureWindows.first() shouldBe listOf(riskLevelResult)

            verify {
                ewRiskResultTables.allEntries()
            }
        }
    }

    @Test
    fun `allEwRiskLevelResultsWithExposureWindows are mapped`() {
        every { ewRiskResultTables.allEntries() } returns flowOf(listOf(ewRiskResult1Increased))
        every { ewTables.allEntries() } returns flowOf(listOf(ewDaoWrapper))
        runTest {
            val instance = createInstance()

            val riskLevelResult = ewRiskLevelResult.copy(exposureWindows = listOf(testExposureWindow))
            instance.allEwRiskLevelResultsWithExposureWindows.first() shouldBe listOf(riskLevelResult)

            verify {
                ewRiskResultTables.allEntries()
                ewTables.getWindowsForResult(listOf(ewRiskResult1Increased.id))
            }
        }
    }

    @Test
    fun `errors when storing risk level result are rethrown`() = runTest {
        coEvery { ewRiskResultTables.insertEntry(any()) } throws IllegalStateException("No body expects the...")
        val instance = createInstance()
        shouldThrow<java.lang.IllegalStateException> {
            instance.storeResult(ewRiskLevelResult)
        }
    }

    @Test
    fun `errors when storing exposure window results are thrown`() = runTest {
        val instance = createInstance(onStoreExposureWindows = { _, _ -> throw IllegalStateException("Surprise!") })
        shouldThrow<IllegalStateException> {
            instance.storeResult(ewRiskLevelResult)
        }
    }

    @Test
    fun `storeResult works`() = runTest {
        val mockStoreWindows: (String, EwRiskLevelResult) -> Unit = spyk()
        val mockDeleteOrphanedWindows: () -> Unit = spyk()

        val instance = createInstance(
            onStoreExposureWindows = mockStoreWindows,
            onDeletedOrphanedExposureWindows = mockDeleteOrphanedWindows
        )
        instance.storeResult(ewRiskLevelResult)

        coVerify {
            ewRiskResultTables.insertEntry(any())
            ewRiskResultTables.deleteOldest(instance.storedResultLimit)
            mockStoreWindows.invoke(any(), ewRiskLevelResult)
            mockDeleteOrphanedWindows.invoke()
        }

        coVerify(exactly = 0) {
            ewAggregatedRiskPerDateResultDao.insertRisk(any())
        }
    }

    @Test
    fun `storing aggregatedRiskPerDateResults works`() = runTest {
        val instance = createInstance()
        instance.storeResult(ewRiskLevelResultWithAggregatedRiskPerDateResult)

        coVerify {
            ewAggregatedRiskPerDateResultDao.insertRisk(any())
        }
    }

    /*
    * pt and combined risk
    *
    * */

    @Test
    fun `allCombinedEwPtRiskLevelResults works no pt result is available`() = runTest {
        every { ptRiskRepository.allRiskLevelResults } returns flowOf(listOf())

        val riskLevelResults = createInstance().allCombinedEwPtRiskLevelResults.first()
        riskLevelResults.size shouldBe 2
        riskLevelResults[0].calculatedAt shouldBe ewRiskResult1Increased.calculatedAt
        riskLevelResults[0].riskState shouldBe RiskState.INCREASED_RISK

        verify {
            ewRiskResultTables.allEntries()
            ptRiskRepository.allRiskLevelResults
            riskCombinator.combineEwPtRiskLevelResults(
                listOf(ptResult1Low, ptResult2Failed),
                listOf(ewRiskResult1Increased.toRiskResult())
            )
        }
    }

    @Test
    fun `allCombinedEwPtRiskLevelResults works when only one calc each is available`() = runTest {
        every { ptRiskRepository.allRiskLevelResults } returns flowOf(listOf(ptResult1Low))
        every { ewRiskResultTables.allEntries() } returns flowOf(listOf(ewRiskResult1Increased))
        val riskLevelResults = createInstance().allCombinedEwPtRiskLevelResults.first()

        riskLevelResults.size shouldBe 2
        riskLevelResults[0].calculatedAt shouldBe ptResult1Low.calculatedAt
        riskLevelResults[0].riskState shouldBe RiskState.INCREASED_RISK
        riskLevelResults[1].calculatedAt shouldBe ewRiskResult1Increased.calculatedAt
        riskLevelResults[1].riskState shouldBe RiskState.INCREASED_RISK

        verify {
            ewRiskResultTables.allEntries()
            ptRiskRepository.allRiskLevelResults
            riskCombinator.combineEwPtRiskLevelResults(
                listOf(ptResult1Low),
                listOf(ewRiskResult1Increased.toRiskResult())
            )
        }
    }

    @Test
    fun `allCombinedEwPtRiskLevelResults works when two calc each are available`() = runTest {

        every { ewRiskResultTables.allEntries() } returns flowOf(listOf(ewRiskResult1Increased, ewRiskResult2Low))
        every { ptRiskRepository.allRiskLevelResults } returns flowOf(listOf(ptResult1Low, ptResult2Failed))

        val riskLevelResults = createInstance().allCombinedEwPtRiskLevelResults.first()
        riskLevelResults.size shouldBe 4
        riskLevelResults[0].calculatedAt shouldBe ptResult1Low.calculatedAt
        riskLevelResults[0].riskState shouldBe RiskState.INCREASED_RISK

        riskLevelResults[1].calculatedAt shouldBe ewRiskResult1Increased.calculatedAt
        riskLevelResults[1].riskState shouldBe RiskState.INCREASED_RISK

        riskLevelResults[2].calculatedAt shouldBe ewRiskResult2Low.calculatedAt
        riskLevelResults[2].riskState shouldBe RiskState.LOW_RISK

        riskLevelResults[3].calculatedAt shouldBe ewRiskResult2Low.calculatedAt
        riskLevelResults[3].riskState shouldBe RiskState.LOW_RISK

        verify {
            ewRiskResultTables.allEntries()
            ptRiskRepository.allRiskLevelResults
            riskCombinator.combineEwPtRiskLevelResults(
                listOf(ptResult1Low, ptResult2Failed),
                listOf(ewRiskResult1Increased.toRiskResult(), ewRiskResult2Low.toRiskResult())
            )
        }
    }

    @Test
    fun `latestAndLastSuccessfulCombinedEwPtRiskLevelResult are combined`() {
        runTest {
            val riskLevelResult = createInstance().latestAndLastSuccessfulCombinedEwPtRiskLevelResult.first()

            riskLevelResult.lastCalculated.calculatedAt shouldBe ptResult1Low.calculatedAt
            riskLevelResult.lastCalculated.riskState shouldBe RiskState.INCREASED_RISK

            riskLevelResult.lastSuccessfullyCalculatedRiskState shouldBe RiskState.INCREASED_RISK

            verify {
                ptRiskRepository.allRiskLevelResults
                ptRiskRepository.presenceTracingDayRisk
            }
        }
    }

    @Test
    fun `latestAndLastSuccessfulCombinedEwPtRiskLevelResult works when no results yet`() {
        every { ptRiskRepository.allRiskLevelResults } returns flowOf(listOf())
        every { ptRiskRepository.presenceTracingDayRisk } returns flowOf(listOf())
        every { ewRiskResultTables.allEntries() } returns flowOf(listOf())

        runTest {
            val riskLevelResult = createInstance().latestAndLastSuccessfulCombinedEwPtRiskLevelResult.first()
            riskLevelResult.lastCalculated.calculatedAt shouldBe Instant.EPOCH
            riskLevelResult.lastCalculated.riskState shouldBe RiskState.LOW_RISK
            riskLevelResult.lastSuccessfullyCalculatedRiskState shouldBe RiskState.LOW_RISK
        }
    }

    @Test
    fun `clear works`() = runTest {
        createInstance().reset()
        coVerify {
            ewRiskResultDatabase.clearAllTables()
            ptRiskRepository.clearAllTables()
        }
    }
}
