package de.rki.coronawarnapp.risk.storage

import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.presencetracing.risk.minusDaysAtStartOfDayUtc
import de.rki.coronawarnapp.presencetracing.risk.storage.PresenceTracingRiskRepository
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.ExposureWindowsFilter
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.ewCalculatedAt
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.ewDaoWrapper
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.ewDayRisk
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.ewPersistedAggregatedRiskPerDateResult
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.ewRiskLevelResult
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.ewRiskLevelResultWithAggregatedRiskPerDateResult
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.ewRiskResult1Increased
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.ewRiskResult2Low
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.maxCheckInAgeInDays
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.ptCalculatedAt
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
import de.rki.coronawarnapp.risk.storage.internal.riskresults.PersistedRiskLevelResultDao
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

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
        every { ewRiskResultTables.allEntries() } returns emptyFlow()
        every { ewRiskResultTables.latestEntries(2) } returns emptyFlow()
        every { ewRiskResultTables.latestAndLastSuccessful() } returns emptyFlow()
        coEvery { ewRiskResultTables.insertEntry(any()) } just Runs
        coEvery { ewRiskResultTables.deleteOldest(any()) } returns 7
        every { ewTables.allEntries() } returns emptyFlow()
        every { ewAggregatedRiskPerDateResultDao.allEntries() } returns emptyFlow()
        coEvery { ewAggregatedRiskPerDateResultDao.insertRisk(any()) } just Runs

        // pt data sources
        coEvery { ptRiskRepository.traceLocationCheckInRiskStates } returns emptyFlow()
        coEvery { ptRiskRepository.presenceTracingDayRisk } returns emptyFlow()
        coEvery { ptRiskRepository.allRiskLevelResults } returns emptyFlow()
        coEvery { ptRiskRepository.clearAllTables() } just Runs
        coEvery { ptRiskRepository.latestRiskLevelResult } returns emptyFlow()
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

        runBlockingTest {
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
        every { ptRiskRepository.presenceTracingDayRisk } returns flowOf(listOf(ptDayRisk))

        runBlockingTest {
            val instance = createInstance()
            instance.ptDayRiskStates.first() shouldBe listOf(ptDayRisk)
        }
    }

    @Test
    fun `exposureWindows are returned from database and mapped`() {
        val testDaoWrappers = flowOf(listOf(ewDaoWrapper))
        every { ewTables.allEntries() } returns testDaoWrappers

        runBlockingTest {
            val exposureWindowDAOWrappers = createInstance().exposureWindowsTables.allEntries()
            exposureWindowDAOWrappers shouldBe testDaoWrappers
            exposureWindowDAOWrappers.first().map { it.toExposureWindow() } shouldBe listOf(testExposureWindow)
        }
    }

    @Test
    fun `ewRiskLevelResults are returned from database and mapped`() {
        every { ewRiskResultTables.allEntries() } returns flowOf(listOf(ewRiskResult1Increased))
        every { ewTables.allEntries() } returns flowOf(emptyList())

        runBlockingTest {
            val instance = createInstance()
            instance.allEwRiskLevelResults.first() shouldBe listOf(ewRiskLevelResult)
        }
    }

    @Test
    fun `ewRiskLevelResults with exposure windows are returned from database and mapped`() {
        every { ewRiskResultTables.allEntries() } returns flowOf(listOf(ewRiskResult1Increased))
        every { ewTables.allEntries() } returns flowOf(listOf(ewDaoWrapper))

        runBlockingTest {
            val instance = createInstance()
            val riskLevelResult = ewRiskLevelResult.copy(exposureWindows = listOf(testExposureWindow))
            instance.allEwRiskLevelResultsWithExposureWindows.first() shouldBe listOf(riskLevelResult)

            verify {
                ewRiskResultTables.allEntries()
            }
        }
    }

    @Test
    fun `latestRiskLevelResults with exposure windows are mapped`() {
        every { ewRiskResultTables.latestEntries(any()) } returns flowOf(listOf(ewRiskResult1Increased))
        every { ewTables.getWindowsForResult(any()) } returns flowOf(listOf(ewDaoWrapper))

        runBlockingTest {
            val instance = createInstance()

            val riskLevelResult = ewRiskLevelResult.copy(exposureWindows = listOf(testExposureWindow))
            instance.allEwRiskLevelResultsWithExposureWindows.first() shouldBe listOf(riskLevelResult)

            verify {
                ewRiskResultTables.latestEntries(2)
                ewTables.getWindowsForResult(listOf(ewRiskResult1Increased.id))
            }
        }
    }

    @Test
    fun `latestAndLastSuccessful with exposure windows are mapped`() {
        every { ewRiskResultTables.latestAndLastSuccessful() } returns flowOf(listOf(ewRiskResult1Increased))
        every { ewTables.getWindowsForResult(any()) } returns flowOf(listOf(ewDaoWrapper))

        runBlockingTest {
            val instance = createInstance()

            val riskLevelResult = ewRiskLevelResult.copy(exposureWindows = listOf(testExposureWindow))
            instance.latestAndLastSuccessfulEwRiskLevelResult.first() shouldBe listOf(riskLevelResult)

            verify {
                ewRiskResultTables.latestAndLastSuccessful()
                ewTables.getWindowsForResult(listOf(ewRiskResult1Increased.id))
            }
        }
    }

    @Test
    fun `errors when storing risk level result are rethrown`() = runBlockingTest {
        coEvery { ewRiskResultTables.insertEntry(any()) } throws IllegalStateException("No body expects the...")
        val instance = createInstance()
        shouldThrow<java.lang.IllegalStateException> {
            instance.storeResult(ewRiskLevelResult)
        }
    }

    @Test
    fun `errors when storing exposure window results are thrown`() = runBlockingTest {
        val instance = createInstance(onStoreExposureWindows = { _, _ -> throw IllegalStateException("Surprise!") })
        shouldThrow<IllegalStateException> {
            instance.storeResult(ewRiskLevelResult)
        }
    }

    @Test
    fun `storeResult works`() = runBlockingTest {
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
    fun `storing aggregatedRiskPerDateResults works`() = runBlockingTest {
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
    fun `latestCombinedEwPtRiskLevelResults works when 1 ew result is available`() = runBlockingTest {
        every { ewRiskResultTables.allEntries() } returns flowOf(listOf(ewRiskResult1Increased))
        every { ptRiskRepository.allRiskLevelResults } returns flowOf(listOf(ptResult1Low, ptResult2Failed))

        val riskLevelResults = createInstance().allCombinedEwPtRiskLevelResults.first()
        riskLevelResults.size shouldBe 3
        riskLevelResults[0].calculatedAt shouldBe ptResult1Low.calculatedAt
        riskLevelResults[0].riskState shouldBe RiskState.INCREASED_RISK
        riskLevelResults[1].calculatedAt shouldBe ewRiskResult1Increased.calculatedAt
        riskLevelResults[1].riskState shouldBe RiskState.CALCULATION_FAILED
        riskLevelResults[2].calculatedAt shouldBe ptResult2Failed.calculatedAt
        riskLevelResults[2].riskState shouldBe RiskState.CALCULATION_FAILED

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
    fun `latestCombinedEwPtRiskLevelResults works when only one calc each is available`() = runBlockingTest {
        every { ewRiskResultTables.allEntries() } returns flowOf(listOf(ewRiskResult1Increased))
        every { ptRiskRepository.allRiskLevelResults } returns flowOf(listOf(ptResult1Low))

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
    fun `latestCombinedEwPtRiskLevelResults works when two calc each are available`() = runBlockingTest {

        every { ewRiskResultTables.allEntries() } returns flowOf(listOf(ewRiskResult1Increased, ewRiskResult2Low))
        every { ptRiskRepository.allRiskLevelResults } returns flowOf(listOf(ptResult1Low, ptResult2Failed))

        val riskLevelResults = createInstance().allCombinedEwPtRiskLevelResults.first()
        riskLevelResults.size shouldBe 4
        riskLevelResults[0].calculatedAt shouldBe ptResult1Low.calculatedAt
        riskLevelResults[0].riskState shouldBe RiskState.INCREASED_RISK
        riskLevelResults[1].calculatedAt shouldBe ewRiskResult1Increased.calculatedAt
        riskLevelResults[1].riskState shouldBe RiskState.CALCULATION_FAILED
        riskLevelResults[2].calculatedAt shouldBe ptResult2Failed.calculatedAt
        riskLevelResults[2].riskState shouldBe RiskState.CALCULATION_FAILED
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
        val calculatedAt = ewCalculatedAt.plus(6000L)
        every { ptRiskRepository.allRiskLevelResults } returns flowOf(
            listOf(ptResult1Low, ptResult2Failed)
        )
        every { ptRiskRepository.presenceTracingDayRisk } returns
            flowOf(listOf(ptDayRisk))

        every { ewRiskResultTables.latestAndLastSuccessful() } returns flowOf(listOf(ewRiskResult1Increased))
        every { ewTables.getWindowsForResult(any()) } returns flowOf(listOf(ewDaoWrapper))

        runBlockingTest {
            val instance = createInstance()
            val riskLevelResult = instance.latestAndLastSuccessfulCombinedEwPtRiskLevelResult.first()

            riskLevelResult.lastCalculated.calculatedAt shouldBe calculatedAt
            riskLevelResult.lastCalculated.riskState shouldBe RiskState.INCREASED_RISK
            riskLevelResult.lastSuccessfullyCalculated.calculatedAt shouldBe calculatedAt
            riskLevelResult.lastSuccessfullyCalculated.riskState shouldBe RiskState.INCREASED_RISK

            verify {
                ptRiskRepository.allRiskLevelResults
                ptRiskRepository.presenceTracingDayRisk
            }
        }
    }

    @Test
    fun `latestAndLastSuccessfulCombinedEwPtRiskLevelResult are combined 2`() {
        every { ptRiskRepository.allRiskLevelResults } returns flowOf(
            listOf(
                PtRiskLevelResult(
                    calculatedAt = ewCalculatedAt.plus(6000L),
                    presenceTracingDayRisk = null,
                    riskState = RiskState.CALCULATION_FAILED,
                    calculatedFrom = ewCalculatedAt.plus(6000L)
                        .minusDaysAtStartOfDayUtc(maxCheckInAgeInDays).toInstant()
                ),
                PtRiskLevelResult(
                    calculatedAt = ewCalculatedAt.minus(1000L),
                    presenceTracingDayRisk = listOf(ptDayRisk),
                    riskState = RiskState.INCREASED_RISK,
                    calculatedFrom = ewCalculatedAt.minus(1000L)
                        .minusDaysAtStartOfDayUtc(maxCheckInAgeInDays).toInstant()
                )
            )
        )

        val ewResultDao1 = PersistedRiskLevelResultDao(
            id = "id1",
            calculatedAt = ewCalculatedAt,
            failureReason = EwRiskLevelResult.FailureReason.UNKNOWN,
            aggregatedRiskResult = null
        )
        val ewResultDao2 = PersistedRiskLevelResultDao(
            id = "id2",
            calculatedAt = ewCalculatedAt.minus(2000L),
            failureReason = null,
            aggregatedRiskResult = PersistedRiskLevelResultDao.PersistedAggregatedRiskResult(
                totalRiskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW,
                totalMinimumDistinctEncountersWithLowRisk = 1,
                totalMinimumDistinctEncountersWithHighRisk = 0,
                mostRecentDateWithLowRisk = Instant.ofEpochMilli(3),
                mostRecentDateWithHighRisk = Instant.ofEpochMilli(0),
                numberOfDaysWithLowRisk = 5,
                numberOfDaysWithHighRisk = 6
            )
        )
        every { ptRiskRepository.presenceTracingDayRisk } returns
            flowOf(listOf(ptDayRisk))

        every { ewRiskResultTables.latestAndLastSuccessful() } returns flowOf(listOf(ewResultDao1, ewResultDao2))
        every { ewTables.getWindowsForResult(any()) } returns flowOf(listOf(ewDaoWrapper))

        runBlockingTest {
            val instance = createInstance()

            val riskLevelResult = instance.latestAndLastSuccessfulCombinedEwPtRiskLevelResult.first()

            riskLevelResult.lastCalculated.calculatedAt shouldBe ewCalculatedAt.plus(6000L)
            riskLevelResult.lastCalculated.riskState shouldBe RiskState.CALCULATION_FAILED
            riskLevelResult.lastSuccessfullyCalculated.calculatedAt shouldBe ewCalculatedAt
            riskLevelResult.lastSuccessfullyCalculated.riskState shouldBe RiskState.INCREASED_RISK

            verify {
                ptRiskRepository.allRiskLevelResults
                ptRiskRepository.presenceTracingDayRisk
            }
        }
    }

    @Test
    fun `latestAndLastSuccessfulCombinedEwPtRiskLevelResult are combined 3`() {
        every { ptRiskRepository.allRiskLevelResults } returns flowOf(
            listOf(
                PtRiskLevelResult(
                    calculatedAt = ewCalculatedAt.plus(6000L),
                    presenceTracingDayRisk = null,
                    riskState = RiskState.CALCULATION_FAILED,
                    calculatedFrom = ewCalculatedAt.plus(6000L)
                        .minusDaysAtStartOfDayUtc(maxCheckInAgeInDays).toInstant()
                ),
                PtRiskLevelResult(
                    calculatedAt = ewCalculatedAt.minus(100L),
                    presenceTracingDayRisk = listOf(ptDayRisk),
                    riskState = RiskState.LOW_RISK,
                    calculatedFrom = ewCalculatedAt.minus(100L)
                        .minusDaysAtStartOfDayUtc(maxCheckInAgeInDays).toInstant()
                )
            )
        )

        val ewResultDao1 = PersistedRiskLevelResultDao(
            id = "id1",
            calculatedAt = ewCalculatedAt,
            failureReason = EwRiskLevelResult.FailureReason.UNKNOWN,
            aggregatedRiskResult = null
        )
        val ewResultDao2 = PersistedRiskLevelResultDao(
            id = "id2",
            calculatedAt = ewCalculatedAt.minus(200L),
            failureReason = null,
            aggregatedRiskResult = PersistedRiskLevelResultDao.PersistedAggregatedRiskResult(
                totalRiskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
                totalMinimumDistinctEncountersWithLowRisk = 1,
                totalMinimumDistinctEncountersWithHighRisk = 2,
                mostRecentDateWithLowRisk = Instant.ofEpochMilli(3),
                mostRecentDateWithHighRisk = Instant.ofEpochMilli(4),
                numberOfDaysWithLowRisk = 5,
                numberOfDaysWithHighRisk = 6
            )
        )
        every { ptRiskRepository.presenceTracingDayRisk } returns
            flowOf(listOf(ptDayRisk))

        every { ewRiskResultTables.latestAndLastSuccessful() } returns flowOf(listOf(ewResultDao1, ewResultDao2))
        every { ewTables.getWindowsForResult(any()) } returns flowOf(listOf(ewDaoWrapper))

        runBlockingTest {
            val instance = createInstance()

            val riskLevelResult = instance.latestAndLastSuccessfulCombinedEwPtRiskLevelResult.first()

            riskLevelResult.lastCalculated.calculatedAt shouldBe ewCalculatedAt.plus(6000L)
            riskLevelResult.lastCalculated.riskState shouldBe RiskState.CALCULATION_FAILED
            riskLevelResult.lastSuccessfullyCalculated.calculatedAt shouldBe ewCalculatedAt
            riskLevelResult.lastSuccessfullyCalculated.riskState shouldBe RiskState.LOW_RISK

            verify {
                ptRiskRepository.allRiskLevelResults
                ptRiskRepository.presenceTracingDayRisk
            }
        }
    }

    @Test
    fun `latestAndLastSuccessfulCombinedEwPtRiskLevelResult works when no pt result yet`() {

        every { ptRiskRepository.allRiskLevelResults } returns flowOf(listOf())
        every { ptRiskRepository.presenceTracingDayRisk } returns flowOf(listOf())
        every { ewRiskResultTables.latestAndLastSuccessful() } returns flowOf(listOf(ewRiskResult1Increased, ewRiskResult2Low))
        every { ewTables.getWindowsForResult(any()) } returns flowOf(listOf(ewDaoWrapper))

        runBlockingTest {
            val instance = createInstance()

            val riskLevelResult = instance.latestAndLastSuccessfulCombinedEwPtRiskLevelResult.first()

            riskLevelResult.lastCalculated.calculatedAt shouldBe ewCalculatedAt
            riskLevelResult.lastCalculated.riskState shouldBe RiskState.LOW_RISK
            riskLevelResult.lastSuccessfullyCalculated.calculatedAt shouldBe ewCalculatedAt
            riskLevelResult.lastSuccessfullyCalculated.riskState shouldBe RiskState.LOW_RISK

            riskLevelResult.lastCalculated.ewRiskLevelResult shouldBe RiskState.LOW_RISK
            riskLevelResult.lastCalculated.ptRiskLevelResult shouldBe RiskState.LOW_RISK

            riskLevelResult.lastSuccessfullyCalculated.ewRiskLevelResult shouldBe RiskState.LOW_RISK
            riskLevelResult.lastSuccessfullyCalculated.ptRiskLevelResult shouldBe RiskState.LOW_RISK
        }
    }

    @Test
    fun `latestAndLastSuccessfulCombinedEwPtRiskLevelResult works with both present`() {
        every { ptRiskRepository.allRiskLevelResults } returns flowOf(listOf())
        every { ptRiskRepository.presenceTracingDayRisk } returns flowOf(listOf())
        every { ewRiskResultTables.latestAndLastSuccessful() } returns
            flowOf(listOf(ewRiskResult1Increased, ewRiskResult2Low))
        every { ewTables.getWindowsForResult(any()) } returns flowOf(listOf(ewDaoWrapper))
        every { ptRiskRepository.allRiskLevelResults }  returns flowOf(listOf(ptResult1Low))
        every { ptRiskRepository.latestRiskLevelResult }  returns flowOf(ptResult1Low)

        runBlockingTest {
            val instance = createInstance()

            val riskLevelResult = instance.latestAndLastSuccessfulCombinedEwPtRiskLevelResult.firstOrNull()

            requireNotNull(riskLevelResult)

            riskLevelResult.lastCalculated.calculatedAt shouldBe ptCalculatedAt
            riskLevelResult.lastCalculated.riskState shouldBe RiskState.INCREASED_RISK
            riskLevelResult.lastSuccessfullyCalculated.calculatedAt shouldBe ptCalculatedAt
            riskLevelResult.lastSuccessfullyCalculated.riskState shouldBe RiskState.INCREASED_RISK

            riskLevelResult.lastCalculated.ewRiskLevelResult shouldBe RiskState.LOW_RISK
            riskLevelResult.lastCalculated.ptRiskLevelResult shouldBe RiskState.INCREASED_RISK

            riskLevelResult.lastSuccessfullyCalculated.ewRiskLevelResult shouldBe RiskState.LOW_RISK
            riskLevelResult.lastSuccessfullyCalculated.ptRiskLevelResult shouldBe RiskState.INCREASED_RISK
        }
    }

    @Test
    fun `clear works`() = runBlockingTest {
        createInstance().clear()
        coVerify {
            ewRiskResultDatabase.clearAllTables()
            ptRiskRepository.clearAllTables()
        }
    }
}
