package de.rki.coronawarnapp.risk

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows.AnalyticsExposureWindowCollector
import de.rki.coronawarnapp.diagnosiskeys.download.createMockCachedKeyInfo
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKey
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskCancellationException
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testhelpers.BaseTest

class RiskLevelTaskTest : BaseTest() {
    @MockK lateinit var riskLevels: RiskLevels
    @MockK lateinit var enfClient: ENFClient
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var backgroundModeStatus: BackgroundModeStatus
    @MockK lateinit var riskLevelSettings: RiskLevelSettings
    @MockK lateinit var configData: ConfigData
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var keyCacheRepository: KeyCacheRepository
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var analyticsExposureWindowCollector: AnalyticsExposureWindowCollector

    private val arguments: Task.Arguments = object : Task.Arguments {}

    private val coronaTests: MutableStateFlow<Set<CoronaTest>> = MutableStateFlow(
        setOf(
            mockk<CoronaTest>().apply {
                every { isSubmissionAllowed } returns false
                every { isViewed } returns false
            }
        )
    )

    private val testTimeNow = Instant.parse("2020-12-28")
    private val testAggregatedResult = mockk<EwAggregatedRiskResult>().apply {
        every { isIncreasedRisk() } returns true
    }
    private val testCachedKey = mockCachedKey(testTimeNow.toDateTime().minusDays(1))

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(TimeVariables)

        // Mocks setup a happy path, i.e. successful calculation

        every { timeStamper.nowUTC } returns testTimeNow

        every { coronaTestRepository.coronaTests } returns coronaTests
        every { backgroundModeStatus.isAutoModeEnabled } returns flowOf(true)
        coEvery { appConfigProvider.getAppConfig() } returns configData

        configData.apply {
            every { identifier } returns "config-identifier"
            every { isDeviceTimeCorrect } returns true
        }

        every { riskLevelSettings.lastUsedConfigIdentifier = any() } just Runs

        coEvery { riskLevelStorage.storeResult(any()) } just Runs

        coEvery { keyCacheRepository.getAllCachedKeys() } returns listOf(testCachedKey)
        enfClient.apply {
            every { isTracingEnabled } returns flowOf(true)
            coEvery { exposureWindows() } returns listOf()
        }

        riskLevels.apply {
            every { calculateRisk(any(), any()) } returns null
            every { aggregateResults(any(), any()) } returns testAggregatedResult
        }
        coEvery { analyticsExposureWindowCollector.reportRiskResultsPerWindow(any()) } just Runs
    }

    private fun createTask() = RiskLevelTask(
        riskLevels = riskLevels,
        enfClient = enfClient,
        timeStamper = timeStamper,
        backgroundModeStatus = backgroundModeStatus,
        riskLevelSettings = riskLevelSettings,
        appConfigProvider = appConfigProvider,
        riskLevelStorage = riskLevelStorage,
        keyCacheRepository = keyCacheRepository,
        coronaTestRepository = coronaTestRepository,
        analyticsExposureWindowCollector = analyticsExposureWindowCollector,
    )

    private fun mockCachedKey(
        dateTime: DateTime,
        isComplete: Boolean = true,
    ): CachedKey = mockk<CachedKey>().apply {
        every { info } returns createMockCachedKeyInfo(dateTime.toLocalDate(), dateTime.toLocalTime(), isComplete)
    }

    @Test
    fun `last used config ID is set after calculation`() = runBlockingTest {
        every { configData.isDeviceTimeCorrect } returns true

        val task = createTask()
        task.run(arguments)

        verify { riskLevelSettings.lastUsedConfigIdentifier = "config-identifier" }
    }

    @Test
    fun `risk calculation is skipped if device time is incorrect`() = runBlockingTest {
        every { configData.isDeviceTimeCorrect } returns false
        every { configData.localOffset } returns Duration.standardHours(5)

        createTask().run(arguments) shouldBe EwRiskLevelTaskResult(
            calculatedAt = testTimeNow,
            failureReason = EwRiskLevelResult.FailureReason.INCORRECT_DEVICE_TIME
        )
    }

    @Test
    fun `risk calculation is skipped if tracing is disabled`() = runBlockingTest {
        every { enfClient.isTracingEnabled } returns flowOf(false)

        createTask().run(arguments) shouldBe EwRiskLevelTaskResult(
            calculatedAt = testTimeNow,
            failureReason = EwRiskLevelResult.FailureReason.TRACING_OFF
        )
    }

    @Test
    fun `risk calculation is skipped if results are not existing while in background mode`() = runBlockingTest {
        coEvery { keyCacheRepository.getAllCachedKeys() } returns listOf()
        every { backgroundModeStatus.isAutoModeEnabled } returns flowOf(true)
        createTask().run(arguments) shouldBe EwRiskLevelTaskResult(
            calculatedAt = testTimeNow,
            failureReason = EwRiskLevelResult.FailureReason.OUTDATED_RESULTS
        )
    }

    @Test
    fun `risk calculation is skipped if results are not existing while no background mode`() = runBlockingTest {
        coEvery { keyCacheRepository.getAllCachedKeys() } returns listOf()
        every { backgroundModeStatus.isAutoModeEnabled } returns flowOf(false)
        createTask().run(arguments) shouldBe EwRiskLevelTaskResult(
            calculatedAt = testTimeNow,
            failureReason = EwRiskLevelResult.FailureReason.OUTDATED_RESULTS_MANUAL
        )
    }

    @Test
    fun `risk calculation is skipped if results are outdated while in background mode`() = runBlockingTest {
        val cachedKey = mockCachedKey(DateTime.parse("2020-12-28").minusDays(3))
        val now = Instant.parse("2020-12-28")

        coEvery { keyCacheRepository.getAllCachedKeys() } returns listOf(cachedKey)
        every { backgroundModeStatus.isAutoModeEnabled } returns flowOf(true)
        every { timeStamper.nowUTC } returns now

        createTask().run(arguments) shouldBe EwRiskLevelTaskResult(
            calculatedAt = now,
            failureReason = EwRiskLevelResult.FailureReason.OUTDATED_RESULTS
        )
    }

    @Test
    fun `risk calculation is skipped if results are outdated while no background mode`() = runBlockingTest {
        val cachedKey = mockCachedKey(DateTime.parse("2020-12-28").minusDays(3))
        val now = Instant.parse("2020-12-28")

        coEvery { keyCacheRepository.getAllCachedKeys() } returns listOf(cachedKey)
        every { backgroundModeStatus.isAutoModeEnabled } returns flowOf(false)
        every { timeStamper.nowUTC } returns now

        createTask().run(arguments) shouldBe EwRiskLevelTaskResult(
            calculatedAt = now,
            failureReason = EwRiskLevelResult.FailureReason.OUTDATED_RESULTS_MANUAL
        )
    }

    @Test
    fun `risk calculation is skipped if positive test is registered and viewed`() = runBlockingTest {
        val cachedKey = mockCachedKey(DateTime.parse("2020-12-28").minusDays(1))
        val now = Instant.parse("2020-12-28")

        coEvery { keyCacheRepository.getAllCachedKeys() } returns listOf(cachedKey)
        every { backgroundModeStatus.isAutoModeEnabled } returns flowOf(false)
        every { timeStamper.nowUTC } returns now

        coronaTests.value = setOf(
            mockk<CoronaTest>().apply {
                every { isSubmissionAllowed } returns true
                every { isViewed } returns true
            }
        )

        createTask().run(arguments) shouldBe EwRiskLevelTaskResult(
            calculatedAt = now,
            failureReason = EwRiskLevelResult.FailureReason.POSITIVE_TEST_RESULT
        )
    }

    @Test
    fun `risk calculation is not skipped if positive test is registered and not viewed`() = runBlockingTest {
        val cachedKey = mockCachedKey(DateTime.parse("2020-12-28").minusDays(1))
        val now = Instant.parse("2020-12-28")
        val aggregatedRiskResult = mockk<EwAggregatedRiskResult>().apply {
            every { isIncreasedRisk() } returns true
        }

        coEvery { keyCacheRepository.getAllCachedKeys() } returns listOf(cachedKey)
        coEvery { enfClient.exposureWindows() } returns listOf()
        every { riskLevels.calculateRisk(any(), any()) } returns null
        every { riskLevels.aggregateResults(any(), any()) } returns aggregatedRiskResult
        every { timeStamper.nowUTC } returns now
        coEvery { analyticsExposureWindowCollector.reportRiskResultsPerWindow(any()) } just Runs

        coronaTests.value = setOf(
            mockk<CoronaTest>().apply {
                every { isSubmissionAllowed } returns true
                every { isViewed } returns false
            }
        )

        createTask().run(arguments) shouldBe EwRiskLevelTaskResult(
            calculatedAt = now,
            failureReason = null,
            ewAggregatedRiskResult = aggregatedRiskResult,
            listOf()
        )
    }

    @Test
    fun `risk calculation returns aggregated risk result`() = runBlockingTest {
        createTask().run(arguments) shouldBe EwRiskLevelTaskResult(
            calculatedAt = testTimeNow,
            failureReason = null,
            ewAggregatedRiskResult = testAggregatedResult,
            listOf()
        )
    }

    @Test
    fun `run task throws exception if it is already canceled`() = runBlockingTest {
        val task = createTask()
        task.cancel()
        assertThrows<TaskCancellationException> {
            task.run(arguments)
        }
    }
}
