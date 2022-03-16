package de.rki.coronawarnapp.diagnosiskeys.download

import com.google.android.gms.nearby.exposurenotification.DiagnosisKeysDataMapping
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKey
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.modules.detectiontracker.TrackedExposureDetection
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.io.File

class DownloadDiagnosisKeysTaskTest : BaseTest() {

    @MockK lateinit var enfClient: ENFClient
    @MockK lateinit var environmentSetup: EnvironmentSetup
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var keyPackageSyncTool: KeyPackageSyncTool
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var downloadSettings: DownloadDiagnosisKeysSettings

    @MockK lateinit var appConfig: ConfigData
    @MockK lateinit var syncResult: KeyPackageSyncTool.Result
    @MockK lateinit var diagnosisKeyDataMapping: DiagnosisKeysDataMapping

    @MockK lateinit var deltaKey1: CachedKey
    @MockK lateinit var newKey1: CachedKey
    @MockK lateinit var cachedKeyInfo: CachedKeyInfo

    @MockK lateinit var latestTrackedDetection: TrackedExposureDetection
    @MockK lateinit var coronaTestRepository: CoronaTestRepository

    @MockK lateinit var keyCacheRepository: KeyCacheRepository

    private val coronaTests: MutableStateFlow<Set<CoronaTest>> = MutableStateFlow(
        setOf(
            mockk<CoronaTest>().apply { every { isPositive } returns false }
        )
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(BuildConfigWrap)
        every { BuildConfigWrap.VERSION_CODE } returns 1080005

        every { coronaTestRepository.coronaTests } returns coronaTests

        deltaKey1.apply {
            every { path } returns File("availableKey1")
            every { info } returns cachedKeyInfo
        }
        newKey1.apply {
            every { path } returns File("newKey1")
            every { info } returns cachedKeyInfo
        }

        appConfig.apply {
            every { maxExposureDetectionsPerUTCDay } returns 5
            every { minTimeBetweenDetections } returns Duration.standardHours(24 / 6)
            every { diagnosisKeysDataMapping } returns diagnosisKeyDataMapping
            every { isDeviceTimeCorrect } returns true
        }
        coEvery { appConfigProvider.getAppConfig() } returns appConfig

        downloadSettings.apply {
            every { lastVersionCode } returns 1080000
            every { lastVersionCode = any() } just Runs
        }

        every { enfClient.isTracingEnabled } returns flowOf(true)
        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(Duration.standardHours(5))
        every { environmentSetup.useEuropeKeyPackageFiles } returns true

        coEvery { keyPackageSyncTool.syncKeyFiles(any()) } returns syncResult.apply {
            every { deltaKeys } returns listOf(deltaKey1)
            every { newKeys } returns listOf(newKey1)
        }

        enfClient.apply {
            latestTrackedDetection.apply {
                every { startedAt } returns Instant.EPOCH
                every { isSuccessful } returns true
            }
            coEvery { latestTrackedExposureDetection() } returns flowOf(listOf(latestTrackedDetection))
            coEvery { provideDiagnosisKeys(any(), any()) } returns true
        }

        coEvery { keyCacheRepository.markKeyChecked(any()) } just Runs
    }

    fun createInstance() = DownloadDiagnosisKeysTask(
        enfClient = enfClient,
        environmentSetup = environmentSetup,
        appConfigProvider = appConfigProvider,
        keyPackageSyncTool = keyPackageSyncTool,
        timeStamper = timeStamper,
        settings = downloadSettings,
        coronaTestRepository = coronaTestRepository,
        keyCacheRepository = keyCacheRepository,
    )

    @Test
    fun `enf v1 to v2 change flag is checked and set`() = runBlockingTest {
        every { downloadSettings.lastVersionCode } returns -1L

        val task = createInstance()

        task.run(DownloadDiagnosisKeysTask.Arguments())

        coVerifySequence {
            enfClient.isTracingEnabled
            enfClient.latestTrackedExposureDetection()
            enfClient.provideDiagnosisKeys(any(), any())
        }
        verify(exactly = 0) {
            appConfig.minTimeBetweenDetections
            syncResult.newKeys
        }
    }

    @Test
    fun `normal execution on first run`() = runBlockingTest {
        createInstance().run(DownloadDiagnosisKeysTask.Arguments())

        coVerifySequence {
            enfClient.isTracingEnabled
            enfClient.latestTrackedExposureDetection()
            enfClient.provideDiagnosisKeys(any(), any())
            keyCacheRepository.markKeyChecked(listOf(cachedKeyInfo))
        }
    }

    @Test
    fun `not marked as checked if submission fails`() = runBlockingTest {
        coEvery { enfClient.provideDiagnosisKeys(any(), any()) } returns false
        createInstance().run(DownloadDiagnosisKeysTask.Arguments())

        coVerifySequence {
            enfClient.isTracingEnabled
            enfClient.latestTrackedExposureDetection()
            enfClient.provideDiagnosisKeys(any(), any())
        }
        coVerify(exactly = 0) { keyCacheRepository.markKeyChecked(any()) }
    }

    @Test
    fun `execution is skipped if last detection was recent`() = runBlockingTest {
        // Last detection was at T+2h
        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(Duration.standardHours(2))

        createInstance().run(DownloadDiagnosisKeysTask.Arguments())

        coVerifySequence {
            enfClient.isTracingEnabled
            enfClient.latestTrackedExposureDetection()
        }

        coVerify(exactly = 0) {
            enfClient.provideDiagnosisKeys(any(), any())
        }
    }

    @Test
    fun `execution is NOT skipped if last detection is in our future`() = runBlockingTest {
        // Last detection was at T, i.e. our time is now T-1h, so it was in our future.
        every { timeStamper.nowUTC } returns Instant.EPOCH.minus(Duration.standardHours(1).plus(1))

        createInstance().run(DownloadDiagnosisKeysTask.Arguments())

        coVerify {
            enfClient.provideDiagnosisKeys(any(), any())
        }
    }

    @Test
    fun `wasLastDetectionPerformedRecently honors paramters from config`() = runBlockingTest {
        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(Duration.standardHours(4))

        createInstance().run(DownloadDiagnosisKeysTask.Arguments())

        coVerifySequence {
            enfClient.isTracingEnabled
            enfClient.latestTrackedExposureDetection()
            enfClient.provideDiagnosisKeys(any(), any())
        }
    }

    @Test
    fun `hasRecentDetectionAndNoNewFiles checks for new files`() = runBlockingTest {
        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(Duration.standardHours(4))

        every { syncResult.newKeys } returns emptyList()

        createInstance().run(DownloadDiagnosisKeysTask.Arguments())

        coVerifySequence {
            enfClient.isTracingEnabled
            enfClient.latestTrackedExposureDetection()
        }

        coVerify(exactly = 0) {
            enfClient.provideDiagnosisKeys(any(), any())
        }
    }

    @Test
    fun `hasRecentDetectionAndNoNewFiles ignores amount of files if we didn't update for a day`() = runBlockingTest {
        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(Duration.standardHours(25))

        every { syncResult.newKeys } returns emptyList()

        createInstance().run(DownloadDiagnosisKeysTask.Arguments())

        coVerifySequence {
            enfClient.isTracingEnabled
            enfClient.latestTrackedExposureDetection()
            enfClient.provideDiagnosisKeys(any(), any())
        }
    }

    @Test
    fun `we do not submit keys if device time is incorrect`() = runBlockingTest {
        every { appConfig.isDeviceTimeCorrect } returns false
        every { appConfig.localOffset } returns Duration.standardHours(5)

        createInstance().run(DownloadDiagnosisKeysTask.Arguments())

        coVerifySequence {
            enfClient.isTracingEnabled
        }

        coVerify(exactly = 0) {
            enfClient.provideDiagnosisKeys(any(), any())
        }
    }
}
