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
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.io.File
import java.time.Duration
import java.time.Instant

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

    @MockK lateinit var keyCacheRepository: KeyCacheRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(BuildConfigWrap)
        every { BuildConfigWrap.VERSION_CODE } returns 1080005

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
            every { minTimeBetweenDetections } returns Duration.ofHours(24 / 6)
            every { diagnosisKeysDataMapping } returns diagnosisKeyDataMapping
            every { isDeviceTimeCorrect } returns true
        }
        coEvery { appConfigProvider.getAppConfig() } returns appConfig

        downloadSettings.apply {
            every { lastVersionCode } returns 1080000
            every { lastVersionCode = any() } just Runs
        }

        every { enfClient.isTracingEnabled } returns flowOf(true)
        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(Duration.ofHours(5))
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
        keyCacheRepository = keyCacheRepository,
    )

    @Test
    fun `enf v1 to v2 change flag is checked and set`() = runTest {
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
    fun `normal execution on first run`() = runTest {
        createInstance().run(DownloadDiagnosisKeysTask.Arguments())

        coVerifySequence {
            enfClient.isTracingEnabled
            enfClient.latestTrackedExposureDetection()
            enfClient.provideDiagnosisKeys(any(), any())
            keyCacheRepository.markKeyChecked(listOf(cachedKeyInfo))
        }
    }

    @Test
    fun `not marked as checked if submission fails`() = runTest {
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
    fun `execution is skipped if last detection was recent`() = runTest {
        // Last detection was at T+2h
        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(Duration.ofHours(2))

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
    fun `execution is NOT skipped if last detection is in our future`() = runTest {
        // Last detection was at T, i.e. our time is now T-1h, so it was in our future.
        every { timeStamper.nowUTC } returns Instant.EPOCH.minus(Duration.ofHours(1).plusMillis(1))

        createInstance().run(DownloadDiagnosisKeysTask.Arguments())

        coVerify {
            enfClient.provideDiagnosisKeys(any(), any())
        }
    }

    @Test
    fun `wasLastDetectionPerformedRecently honors paramters from config`() = runTest {
        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(Duration.ofHours(4))

        createInstance().run(DownloadDiagnosisKeysTask.Arguments())

        coVerifySequence {
            enfClient.isTracingEnabled
            enfClient.latestTrackedExposureDetection()
            enfClient.provideDiagnosisKeys(any(), any())
        }
    }

    @Test
    fun `hasRecentDetectionAndNoNewFiles checks for new files`() = runTest {
        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(Duration.ofHours(4))

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
    fun `hasRecentDetectionAndNoNewFiles ignores amount of files if we didn't update for a day`() = runTest {
        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(Duration.ofHours(25))

        every { syncResult.newKeys } returns emptyList()

        createInstance().run(DownloadDiagnosisKeysTask.Arguments())

        coVerifySequence {
            enfClient.isTracingEnabled
            enfClient.latestTrackedExposureDetection()
            enfClient.provideDiagnosisKeys(any(), any())
        }
    }

    @Test
    fun `we do not submit keys if device time is incorrect`() = runTest {
        every { appConfig.isDeviceTimeCorrect } returns false
        every { appConfig.localOffset } returns Duration.ofHours(5)

        createInstance().run(DownloadDiagnosisKeysTask.Arguments())

        coVerifySequence {
            enfClient.isTracingEnabled
        }

        coVerify(exactly = 0) {
            enfClient.provideDiagnosisKeys(any(), any())
        }
    }
}
