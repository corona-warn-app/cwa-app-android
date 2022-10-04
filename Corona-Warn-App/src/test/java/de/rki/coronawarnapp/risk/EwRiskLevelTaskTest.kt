package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows.AnalyticsExposureWindowCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsTestResultCollector
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
import de.rki.coronawarnapp.util.toLocalDateTimeUtc
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.time.Duration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testhelpers.BaseTest
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

class EwRiskLevelTaskTest : BaseTest() {
    @MockK lateinit var riskLevels: RiskLevels
    @MockK lateinit var enfClient: ENFClient
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var backgroundModeStatus: BackgroundModeStatus
    @MockK lateinit var riskLevelSettings: RiskLevelSettings
    @MockK lateinit var configData: ConfigData
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var keyCacheRepository: KeyCacheRepository
    @MockK lateinit var analyticsExposureWindowCollector: AnalyticsExposureWindowCollector
    @MockK lateinit var analyticsTestResultCollector: AnalyticsTestResultCollector
    @MockK lateinit var exposureWindow1: ExposureWindow
    @MockK lateinit var exposureWindow2: ExposureWindow
    @MockK lateinit var ewFilter: ExposureWindowsFilter

    private val arguments: Task.Arguments = object : Task.Arguments {}
    private val testTimeNow = LocalDate.parse("2020-12-28").atStartOfDay(ZoneOffset.UTC).toInstant()
    private val testAggregatedResult = mockk<EwAggregatedRiskResult>().apply {
        every { isIncreasedRisk() } returns true
    }
    private val testCachedKey = mockCachedKey(testTimeNow.toLocalDateTimeUtc().minusDays(1))

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(TimeVariables)

        every { timeStamper.nowUTC } returns testTimeNow
        every { backgroundModeStatus.isAutoModeEnabled } returns flowOf(true)
        coEvery { appConfigProvider.getAppConfig() } returns configData

        configData.apply {
            every { identifier } returns "config-identifier"
            every { isDeviceTimeCorrect } returns true
            every { maxEncounterAgeInDays } returns 14
        }

        coEvery { riskLevelSettings.updateLastUsedConfigIdentifier(any()) } just Runs

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
        coEvery { analyticsTestResultCollector.reportRiskResultsPerWindow(any()) } just Runs

        every { ewFilter.filterByAge(any(), any(), any()) } returns emptyList()
    }

    private fun createTask() = EwRiskLevelTask(
        riskLevels = riskLevels,
        enfClient = enfClient,
        timeStamper = timeStamper,
        backgroundModeStatus = backgroundModeStatus,
        riskLevelSettings = riskLevelSettings,
        appConfigProvider = appConfigProvider,
        riskLevelStorage = riskLevelStorage,
        keyCacheRepository = keyCacheRepository,
        analyticsExposureWindowCollector = analyticsExposureWindowCollector,
        analyticsTestResultCollector = analyticsTestResultCollector,
        filter = ewFilter
    )

    private fun mockCachedKey(
        dateTime: LocalDateTime,
        isComplete: Boolean = true,
    ): CachedKey = mockk<CachedKey>().apply {
        every { info } returns createMockCachedKeyInfo(dateTime.toLocalDate(), dateTime.toLocalTime(), isComplete)
    }

    @Test
    fun `last used config ID is set after calculation`() = runTest {
        every { configData.isDeviceTimeCorrect } returns true

        val task = createTask()
        task.run(arguments)

        coVerify { riskLevelSettings.updateLastUsedConfigIdentifier("config-identifier") }
    }

    @Test
    fun `risk calculation is skipped if device time is incorrect`() = runTest {
        every { configData.isDeviceTimeCorrect } returns false
        every { configData.localOffset } returns Duration.ofHours(5)

        val serverTime = testTimeNow.minus(configData.localOffset)

        createTask().run(arguments) shouldBe EwRiskLevelTaskResult(
            calculatedAt = serverTime,
            failureReason = EwRiskLevelResult.FailureReason.INCORRECT_DEVICE_TIME
        )
    }

    @Test
    fun `risk calculation is skipped if tracing is disabled`() = runTest {
        every { enfClient.isTracingEnabled } returns flowOf(false)

        createTask().run(arguments) shouldBe EwRiskLevelTaskResult(
            calculatedAt = testTimeNow,
            failureReason = EwRiskLevelResult.FailureReason.TRACING_OFF
        )
    }

    @Test
    fun `risk calculation is skipped if results are not existing while in background mode`() = runTest {
        coEvery { keyCacheRepository.getAllCachedKeys() } returns listOf()
        every { backgroundModeStatus.isAutoModeEnabled } returns flowOf(true)
        createTask().run(arguments) shouldBe EwRiskLevelTaskResult(
            calculatedAt = testTimeNow,
            failureReason = EwRiskLevelResult.FailureReason.OUTDATED_RESULTS
        )
    }

    @Test
    fun `risk calculation is skipped if results are not existing while no background mode`() = runTest {
        coEvery { keyCacheRepository.getAllCachedKeys() } returns listOf()
        every { backgroundModeStatus.isAutoModeEnabled } returns flowOf(false)
        createTask().run(arguments) shouldBe EwRiskLevelTaskResult(
            calculatedAt = testTimeNow,
            failureReason = EwRiskLevelResult.FailureReason.OUTDATED_RESULTS_MANUAL
        )
    }

    @Test
    fun `risk calculation is skipped if results are outdated while in background mode`() = runTest {
        val cachedKey = mockCachedKey(LocalDate.parse("2020-12-28").atStartOfDay().minusDays(3))
        val now = LocalDate.parse("2020-12-28").atStartOfDay().toInstant(ZoneOffset.UTC)

        coEvery { keyCacheRepository.getAllCachedKeys() } returns listOf(cachedKey)
        every { backgroundModeStatus.isAutoModeEnabled } returns flowOf(true)
        every { timeStamper.nowUTC } returns now

        createTask().run(arguments) shouldBe EwRiskLevelTaskResult(
            calculatedAt = now,
            failureReason = EwRiskLevelResult.FailureReason.OUTDATED_RESULTS
        )
    }

    @Test
    fun `risk calculation is skipped if results are outdated while no background mode`() = runTest {
        val cachedKey = mockCachedKey(LocalDate.parse("2020-12-28").atStartOfDay().minusDays(3))
        val now = LocalDate.parse("2020-12-28").atStartOfDay().toInstant(ZoneOffset.UTC)

        coEvery { keyCacheRepository.getAllCachedKeys() } returns listOf(cachedKey)
        every { backgroundModeStatus.isAutoModeEnabled } returns flowOf(false)
        every { timeStamper.nowUTC } returns now

        createTask().run(arguments) shouldBe EwRiskLevelTaskResult(
            calculatedAt = now,
            failureReason = EwRiskLevelResult.FailureReason.OUTDATED_RESULTS_MANUAL
        )
    }

    @Test
    fun `risk calculation returns aggregated risk result`() = runTest {
        createTask().run(arguments) shouldBe EwRiskLevelTaskResult(
            calculatedAt = testTimeNow,
            failureReason = null,
            ewAggregatedRiskResult = testAggregatedResult,
            listOf()
        )
        coVerify(exactly = 1) {
            analyticsExposureWindowCollector.reportRiskResultsPerWindow(any())
            analyticsTestResultCollector.reportRiskResultsPerWindow(any())
        }
    }

    @Test
    fun `run task throws exception if it is already canceled`() = runTest {
        val task = createTask()
        task.cancel()
        assertThrows<TaskCancellationException> {
            task.run(arguments)
        }
    }

    @Test
    fun `areKeyPkgsOutDated returns true`() = runTest {
        val now = ZonedDateTime.parse("2020-12-28T00:00+00:00").toLocalDateTime()
        val cachedKey = mockCachedKey(now.minusHours(49)) // outdated > 48h

        coEvery { keyCacheRepository.getAllCachedKeys() } returns listOf(cachedKey)

        createTask().areKeyPkgsOutDated(now.toInstant(ZoneOffset.UTC)) shouldBe true
    }

    @Test
    fun `areKeyPkgsOutDated returns false`() = runTest {
        val now = ZonedDateTime.parse("2020-12-28T00:00+00:00").toLocalDateTime()
        val cachedKey = mockCachedKey(now.minusHours(49))
        val cachedKey2 = mockCachedKey(now.minusHours(47)) // not outdated < 48h

        coEvery { keyCacheRepository.getAllCachedKeys() } returns listOf(cachedKey, cachedKey2)

        createTask().areKeyPkgsOutDated(now.toInstant(ZoneOffset.UTC)) shouldBe false
    }

    @Test
    fun `risk calculation applies filter`() = runTest {
        val cachedKey = mockCachedKey(LocalDate.parse("2020-12-28").atStartOfDay().minusDays(1))
        val now = Instant.parse("2020-12-28T00:00:00Z")
        val aggregatedRiskResult = mockk<EwAggregatedRiskResult>().apply {
            every { isIncreasedRisk() } returns true
        }

        every { ewFilter.filterByAge(any(), any(), any()) } returns listOf(exposureWindow2)

        coEvery { keyCacheRepository.getAllCachedKeys() } returns listOf(cachedKey)
        coEvery { enfClient.exposureWindows() } returns listOf(exposureWindow1, exposureWindow2)
        every { riskLevels.calculateRisk(any(), any()) } returns null
        every { riskLevels.aggregateResults(any(), any()) } returns aggregatedRiskResult
        every { timeStamper.nowUTC } returns now

        createTask().run(arguments) shouldBe EwRiskLevelTaskResult(
            calculatedAt = now,
            failureReason = null,
            ewAggregatedRiskResult = aggregatedRiskResult,
            listOf(exposureWindow2)
        )
    }
}
