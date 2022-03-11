package de.rki.coronawarnapp.risk.storage

import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingDayRisk
import de.rki.coronawarnapp.presencetracing.risk.minusDaysAtStartOfDayUtc
import de.rki.coronawarnapp.presencetracing.risk.storage.PresenceTracingRiskRepository
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.ExposureWindowsFilter
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.ewCalculatedAt
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.testAggregatedRiskPerDateResult
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.testExposureWindow
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.testExposureWindowDaoWrapper
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.testPersistedAggregatedRiskPerDateResult
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.testRiskLevelResultDao
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.testRisklevelResult
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.testRisklevelResultWithAggregatedRiskPerDateResult
import de.rki.coronawarnapp.risk.storage.internal.RiskCombinator
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase.AggregatedRiskPerDateResultDao
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase.ExposureWindowsDao
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase.Factory
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase.RiskResultsDao
import de.rki.coronawarnapp.risk.storage.internal.riskresults.PersistedRiskLevelResultDao
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

class BaseRiskLevelStorageTest : BaseTest() {

    @MockK lateinit var databaseFactory: Factory
    @MockK lateinit var database: RiskResultDatabase
    @MockK lateinit var riskResultTables: RiskResultsDao
    @MockK lateinit var exposureWindowTables: ExposureWindowsDao
    @MockK lateinit var aggregatedRiskPerDateResultDao: AggregatedRiskPerDateResultDao
    @MockK lateinit var presenceTracingRiskRepository: PresenceTracingRiskRepository
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var ewFilter: ExposureWindowsFilter

    private lateinit var riskCombinator: RiskCombinator
    private val now = Instant.parse("2021-01-01T12:00:00.000Z")
    private val maxCheckInAgeInDays = 10

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns now
        riskCombinator = RiskCombinator(
            timeStamper = timeStamper
        )

        every { databaseFactory.create() } returns database
        every { database.riskResults() } returns riskResultTables
        every { database.exposureWindows() } returns exposureWindowTables
        every { database.aggregatedRiskPerDate() } returns aggregatedRiskPerDateResultDao
        every { database.clearAllTables() } just Runs

        every { riskResultTables.allEntries() } returns emptyFlow()
        every { riskResultTables.latestEntries(2) } returns emptyFlow()
        every { riskResultTables.latestAndLastSuccessful() } returns emptyFlow()
        coEvery { riskResultTables.insertEntry(any()) } just Runs
        coEvery { riskResultTables.deleteOldest(any()) } returns 7

        every { exposureWindowTables.allEntries() } returns emptyFlow()

        every { aggregatedRiskPerDateResultDao.allEntries() } returns emptyFlow()
        coEvery { aggregatedRiskPerDateResultDao.insertRisk(any()) } just Runs

        // TODO proper tests
        coEvery { presenceTracingRiskRepository.traceLocationCheckInRiskStates } returns emptyFlow()
        coEvery { presenceTracingRiskRepository.presenceTracingDayRisk } returns emptyFlow()
        coEvery { presenceTracingRiskRepository.allRiskLevelResults } returns emptyFlow()
        coEvery { presenceTracingRiskRepository.clearAllTables() } just Runs
        coEvery { presenceTracingRiskRepository.latestRiskLevelResult } returns emptyFlow()
    }

    private fun createInstance(
        scope: CoroutineScope = TestCoroutineScope(),
        storedResultLimit: Int = 10,
        onStoreExposureWindows: (String, EwRiskLevelResult) -> Unit = { _, _ -> },
        onDeletedOrphanedExposureWindows: () -> Unit = {}
    ) = object : BaseRiskLevelStorage(
        scope = scope,
        riskResultDatabaseFactory = databaseFactory,
        presenceTracingRiskRepository = presenceTracingRiskRepository,
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
        val testPersistedAggregatedRiskPerDateResultFlow = flowOf(listOf(testPersistedAggregatedRiskPerDateResult))
        every { aggregatedRiskPerDateResultDao.allEntries() } returns testPersistedAggregatedRiskPerDateResultFlow

        runBlockingTest {
            val instance = createInstance()
            val allEntries = instance.aggregatedRiskPerDateResultTables.allEntries()
            allEntries shouldBe testPersistedAggregatedRiskPerDateResultFlow
            allEntries.first().map { it.toExposureWindowDayRisk() } shouldBe listOf(
                testAggregatedRiskPerDateResult
            )

            val aggregatedRiskPerDateResults = instance.ewDayRiskStates.first()
            aggregatedRiskPerDateResults shouldNotBe listOf(testPersistedAggregatedRiskPerDateResult)
            aggregatedRiskPerDateResults shouldBe listOf(testAggregatedRiskPerDateResult)
        }
    }

    @Test
    fun `ptDayRiskStates are returned from database`() {
        val testPresenceTracingDayRiskFlow = flowOf(listOf(testPresenceTracingDayRisk))
        every { presenceTracingRiskRepository.presenceTracingDayRisk } returns testPresenceTracingDayRiskFlow

        runBlockingTest {
            val instance = createInstance()

            val states = instance.ptDayRiskStates.first()
            states shouldBe listOf(testPresenceTracingDayRisk)
        }
    }

    @Test
    fun `exposureWindows are returned from database and mapped`() {
        val testDaoWrappers = flowOf(listOf(testExposureWindowDaoWrapper))
        every { exposureWindowTables.allEntries() } returns testDaoWrappers

        runBlockingTest {
            val exposureWindowDAOWrappers = createInstance().exposureWindowsTables.allEntries()
            exposureWindowDAOWrappers shouldBe testDaoWrappers
            exposureWindowDAOWrappers.first().map { it.toExposureWindow() } shouldBe listOf(testExposureWindow)
        }
    }

    @Test
    fun `riskLevelResults are returned from database and mapped`() {
        every { riskResultTables.allEntries() } returns flowOf(listOf(testRiskLevelResultDao))
        every { exposureWindowTables.allEntries() } returns flowOf(emptyList())

        runBlockingTest {
            val instance = createInstance()
            instance.allEwRiskLevelResultsWithExposureWindows.first() shouldBe listOf(testRisklevelResult)
        }
    }

    @Test
    fun `riskLevelResults with exposure windows are returned from database and mapped`() {
        every { riskResultTables.allEntries() } returns flowOf(listOf(testRiskLevelResultDao))
        every { exposureWindowTables.allEntries() } returns flowOf(listOf(testExposureWindowDaoWrapper))

        runBlockingTest {
            val instance = createInstance()
            val riskLevelResult = testRisklevelResult.copy(exposureWindows = listOf(testExposureWindow))
            instance.allEwRiskLevelResultsWithExposureWindows.first() shouldBe listOf(riskLevelResult)

            verify {
                riskResultTables.allEntries()
            }
        }
    }

    // This just tests the mapping, the correctness of the SQL statement is validated in an instrumentation test
    @Test
    fun `latestRiskLevelResults with exposure windows are returned from database and mapped`() {
        every { riskResultTables.latestEntries(any()) } returns flowOf(listOf(testRiskLevelResultDao))
        every { exposureWindowTables.getWindowsForResult(any()) } returns flowOf(listOf(testExposureWindowDaoWrapper))

        runBlockingTest2(ignoreActive = true) {
            val instance = createInstance(scope = this)

            val riskLevelResult = testRisklevelResult.copy(exposureWindows = listOf(testExposureWindow))
            instance.allEwRiskLevelResults.first() shouldBe listOf(riskLevelResult)

            verify {
                riskResultTables.latestEntries(2)
                exposureWindowTables.getWindowsForResult(listOf(testRiskLevelResultDao.id))
            }
        }
    }

    @Test
    fun `latestCombinedEwPtRiskLevelResults works when 2 pt result and 1 ew result are available`() {
        every { riskResultTables.latestEntries(any()) } returns flowOf(listOf(testRiskLevelResultDao))
        every { exposureWindowTables.getWindowsForResult(any()) } returns flowOf(listOf(testExposureWindowDaoWrapper))
        val calculatedAt = ewCalculatedAt.plus(6000L)
        runBlockingTest2(ignoreActive = true) {
            val instance = createInstance(scope = this)

            val riskLevelResults = instance.allCombinedEwPtRiskLevelResults.first()
            riskLevelResults.size shouldBe 2

            riskLevelResults[0].calculatedAt shouldBe calculatedAt
            riskLevelResults[0].riskState shouldBe RiskState.CALCULATION_FAILED
            riskLevelResults[1].calculatedAt shouldBe ewCalculatedAt
            riskLevelResults[1].riskState shouldBe RiskState.INCREASED_RISK

            verify {
                riskResultTables.latestEntries(2)
            }
        }
    }

    @Test
    fun `latestCombinedEwPtRiskLevelResults works when only one calc each is available`() {
        every { riskResultTables.latestEntries(any()) } returns flowOf(listOf(testRiskLevelResultDao))
        every { exposureWindowTables.getWindowsForResult(any()) } returns flowOf(listOf(testExposureWindowDaoWrapper))
        runBlockingTest2(ignoreActive = true) {
            val instance = createInstance(scope = this)

            val riskLevelResults = instance.allCombinedEwPtRiskLevelResults.first()
            riskLevelResults.size shouldBe 2

            riskLevelResults[0].calculatedAt shouldBe ewCalculatedAt
            riskLevelResults[0].riskState shouldBe RiskState.INCREASED_RISK

            // result from the combination with initial ew low risk result
            riskLevelResults[1].calculatedAt shouldBe ewCalculatedAt.minus(400L)
            riskLevelResults[1].riskState shouldBe RiskState.LOW_RISK

            verify {
                riskResultTables.latestEntries(2)
            }
        }
    }

    @Test
    fun `latestCombinedEwPtRiskLevelResults works when two calc each are available`() {
        val ewResultDao1 = PersistedRiskLevelResultDao(
            id = "id1",
            calculatedAt = ewCalculatedAt,
            failureReason = null,
            aggregatedRiskResult = PersistedRiskLevelResultDao.PersistedAggregatedRiskResult(
                totalRiskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW,
                totalMinimumDistinctEncountersWithLowRisk = 1,
                totalMinimumDistinctEncountersWithHighRisk = 2,
                mostRecentDateWithLowRisk = Instant.ofEpochMilli(3),
                mostRecentDateWithHighRisk = Instant.ofEpochMilli(4),
                numberOfDaysWithLowRisk = 5,
                numberOfDaysWithHighRisk = 6
            )
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
        every { riskResultTables.latestEntries(any()) } returns flowOf(listOf(ewResultDao1, ewResultDao2))
        every { exposureWindowTables.getWindowsForResult(any()) } returns flowOf(listOf(testExposureWindowDaoWrapper))
        val calculatedAt = ewCalculatedAt.minus(400L)
        runBlockingTest2(ignoreActive = true) {
            val instance = createInstance(scope = this)

            val riskLevelResults = instance.allCombinedEwPtRiskLevelResults.first()
            riskLevelResults.size shouldBe 2

            riskLevelResults[0].calculatedAt shouldBe ewCalculatedAt
            riskLevelResults[0].riskState shouldBe RiskState.INCREASED_RISK
            riskLevelResults[1].calculatedAt shouldBe ewCalculatedAt.minus(200L)
            riskLevelResults[1].riskState shouldBe RiskState.INCREASED_RISK

            verify {
                riskResultTables.latestEntries(2)
            }
        }
    }

    // This just tests the mapping, the correctness of the SQL statement is validated in an instrumentation test
    @Test
    fun `latestAndLastSuccessful with exposure windows are returned from database and mapped`() {
        every { riskResultTables.latestAndLastSuccessful() } returns flowOf(listOf(testRiskLevelResultDao))
        every { exposureWindowTables.getWindowsForResult(any()) } returns flowOf(listOf(testExposureWindowDaoWrapper))

        runBlockingTest2(ignoreActive = true) {
            val instance = createInstance(scope = this)

            val riskLevelResult = testRisklevelResult.copy(exposureWindows = listOf(testExposureWindow))
            instance.latestAndLastSuccessfulEwRiskLevelResult.first() shouldBe listOf(riskLevelResult)

            verify {
                riskResultTables.latestAndLastSuccessful()
                exposureWindowTables.getWindowsForResult(listOf(testRiskLevelResultDao.id))
            }
        }
    }

    @Test
    fun `latestAndLastSuccessfulCombinedEwPtRiskLevelResult are combined`() {
        val calculatedAt = ewCalculatedAt.plus(6000L)
        every { presenceTracingRiskRepository.allRiskLevelResults } returns flowOf(
            listOf(
                PtRiskLevelResult(
                    calculatedAt = calculatedAt,
                    presenceTracingDayRisk = null,
                    riskState = RiskState.CALCULATION_FAILED,
                    calculatedFrom = calculatedAt.minusDaysAtStartOfDayUtc(maxCheckInAgeInDays).toInstant()
                ),
                PtRiskLevelResult(
                    calculatedAt = ewCalculatedAt.minus(1000L),
                    presenceTracingDayRisk = listOf(testPresenceTracingDayRisk),
                    riskState = RiskState.INCREASED_RISK,
                    calculatedFrom = calculatedAt.minus(1000L)
                        .minusDaysAtStartOfDayUtc(maxCheckInAgeInDays).toInstant()
                )
            )
        )
        every { presenceTracingRiskRepository.presenceTracingDayRisk } returns
            flowOf(listOf(testPresenceTracingDayRisk))

        every { riskResultTables.latestAndLastSuccessful() } returns flowOf(listOf(testRiskLevelResultDao))
        every { exposureWindowTables.getWindowsForResult(any()) } returns flowOf(listOf(testExposureWindowDaoWrapper))

        runBlockingTest2(ignoreActive = true) {
            val instance = createInstance(scope = this)

            val riskLevelResult = instance.latestAndLastSuccessfulCombinedEwPtRiskLevelResult.first()

            riskLevelResult.lastCalculated.calculatedAt shouldBe calculatedAt
            riskLevelResult.lastCalculated.riskState shouldBe RiskState.INCREASED_RISK
            riskLevelResult.lastSuccessfullyCalculated.calculatedAt shouldBe calculatedAt
            riskLevelResult.lastSuccessfullyCalculated.riskState shouldBe RiskState.INCREASED_RISK

            verify {
                presenceTracingRiskRepository.allRiskLevelResults
                presenceTracingRiskRepository.presenceTracingDayRisk
            }
        }
    }

    @Test
    fun `latestAndLastSuccessfulCombinedEwPtRiskLevelResult are combined 2`() {
        every { presenceTracingRiskRepository.allRiskLevelResults } returns flowOf(
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
                    presenceTracingDayRisk = listOf(testPresenceTracingDayRisk),
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
        every { presenceTracingRiskRepository.presenceTracingDayRisk } returns
            flowOf(listOf(testPresenceTracingDayRisk))

        every { riskResultTables.latestAndLastSuccessful() } returns flowOf(listOf(ewResultDao1, ewResultDao2))
        every { exposureWindowTables.getWindowsForResult(any()) } returns flowOf(listOf(testExposureWindowDaoWrapper))

        runBlockingTest2(ignoreActive = true) {
            val instance = createInstance(scope = this)

            val riskLevelResult = instance.latestAndLastSuccessfulCombinedEwPtRiskLevelResult.first()

            riskLevelResult.lastCalculated.calculatedAt shouldBe ewCalculatedAt.plus(6000L)
            riskLevelResult.lastCalculated.riskState shouldBe RiskState.CALCULATION_FAILED
            riskLevelResult.lastSuccessfullyCalculated.calculatedAt shouldBe ewCalculatedAt
            riskLevelResult.lastSuccessfullyCalculated.riskState shouldBe RiskState.INCREASED_RISK

            verify {
                presenceTracingRiskRepository.allRiskLevelResults
                presenceTracingRiskRepository.presenceTracingDayRisk
            }
        }
    }

    @Test
    fun `latestAndLastSuccessfulCombinedEwPtRiskLevelResult are combined 3`() {
        every { presenceTracingRiskRepository.allRiskLevelResults } returns flowOf(
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
                    presenceTracingDayRisk = listOf(testPresenceTracingDayRisk),
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
        every { presenceTracingRiskRepository.presenceTracingDayRisk } returns
            flowOf(listOf(testPresenceTracingDayRisk))

        every { riskResultTables.latestAndLastSuccessful() } returns flowOf(listOf(ewResultDao1, ewResultDao2))
        every { exposureWindowTables.getWindowsForResult(any()) } returns flowOf(listOf(testExposureWindowDaoWrapper))

        runBlockingTest2(ignoreActive = true) {
            val instance = createInstance(scope = this)

            val riskLevelResult = instance.latestAndLastSuccessfulCombinedEwPtRiskLevelResult.first()

            riskLevelResult.lastCalculated.calculatedAt shouldBe ewCalculatedAt.plus(6000L)
            riskLevelResult.lastCalculated.riskState shouldBe RiskState.CALCULATION_FAILED
            riskLevelResult.lastSuccessfullyCalculated.calculatedAt shouldBe ewCalculatedAt
            riskLevelResult.lastSuccessfullyCalculated.riskState shouldBe RiskState.LOW_RISK

            verify {
                presenceTracingRiskRepository.allRiskLevelResults
                presenceTracingRiskRepository.presenceTracingDayRisk
            }
        }
    }

    @Test
    fun `latestAndLastSuccessfulCombinedEwPtRiskLevelResult works when no pt result yet`() {

        every { presenceTracingRiskRepository.allRiskLevelResults } returns flowOf(listOf())
        every { presenceTracingRiskRepository.presenceTracingDayRisk } returns flowOf(listOf())

        val ewResultDao1 = PersistedRiskLevelResultDao(
            id = "id1",
            calculatedAt = ewCalculatedAt,
            failureReason = null,
            aggregatedRiskResult = PersistedRiskLevelResultDao.PersistedAggregatedRiskResult(
                totalRiskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW,
                totalMinimumDistinctEncountersWithLowRisk = 1,
                totalMinimumDistinctEncountersWithHighRisk = 2,
                mostRecentDateWithLowRisk = Instant.ofEpochMilli(3),
                mostRecentDateWithHighRisk = Instant.ofEpochMilli(4),
                numberOfDaysWithLowRisk = 5,
                numberOfDaysWithHighRisk = 6
            )
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

        every { riskResultTables.latestAndLastSuccessful() } returns flowOf(listOf(ewResultDao1, ewResultDao2))
        every { exposureWindowTables.getWindowsForResult(any()) } returns flowOf(listOf(testExposureWindowDaoWrapper))

        runBlockingTest2(ignoreActive = true) {
            val instance = createInstance(scope = this)

            val riskLevelResult = instance.latestAndLastSuccessfulCombinedEwPtRiskLevelResult.first()

            riskLevelResult.lastCalculated.calculatedAt shouldBe ewCalculatedAt
            riskLevelResult.lastCalculated.riskState shouldBe RiskState.LOW_RISK
            riskLevelResult.lastSuccessfullyCalculated.calculatedAt shouldBe ewCalculatedAt
            riskLevelResult.lastSuccessfullyCalculated.riskState shouldBe RiskState.LOW_RISK
        }
    }

    @Test
    fun `errors when storing risk level result are rethrown`() = runBlockingTest {
        coEvery { riskResultTables.insertEntry(any()) } throws IllegalStateException("No body expects the...")
        val instance = createInstance()
        shouldThrow<java.lang.IllegalStateException> {
            instance.storeResult(testRisklevelResult)
        }
    }

    @Test
    fun `errors when storing exposure window results are thrown`() = runBlockingTest {
        val instance = createInstance(onStoreExposureWindows = { _, _ -> throw IllegalStateException("Surprise!") })
        shouldThrow<IllegalStateException> {
            instance.storeResult(testRisklevelResult)
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
        instance.storeResult(testRisklevelResult)

        coVerify {
            riskResultTables.insertEntry(any())
            riskResultTables.deleteOldest(instance.storedResultLimit)
            mockStoreWindows.invoke(any(), testRisklevelResult)
            mockDeleteOrphanedWindows.invoke()
        }

        coVerify(exactly = 0) {
            aggregatedRiskPerDateResultDao.insertRisk(any())
        }
    }

    @Test
    fun `storing aggregatedRiskPerDateResults works`() = runBlockingTest {
        val instance = createInstance()
        instance.storeResult(testRisklevelResultWithAggregatedRiskPerDateResult)

        coVerify {
            aggregatedRiskPerDateResultDao.insertRisk(any())
        }
    }

    @Test
    fun `clear works`() = runBlockingTest {
        createInstance().clear()
        coVerify {
            database.clearAllTables()
            presenceTracingRiskRepository.clearAllTables()
        }
    }
}

private val testPresenceTracingDayRisk = PresenceTracingDayRisk(
    Instant.now().toLocalDateUtc(),
    RiskState.INCREASED_RISK
)
