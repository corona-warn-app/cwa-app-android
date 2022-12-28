package de.rki.coronawarnapp.diagnosiskeys.download

import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKey
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.network.NetworkStateProvider
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File
import java.time.Duration
import java.time.Instant

class KeyPackageSyncToolTest : BaseIOTest() {

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)

    @MockK lateinit var keyCache: KeyCacheRepository
    @MockK lateinit var dayPackageSyncTool: DayPackageSyncTool
    @MockK lateinit var hourPackageSyncTool: HourPackageSyncTool
    @MockK lateinit var syncSettings: DownloadDiagnosisKeysSettings
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var networkStateProvider: NetworkStateProvider
    @MockK lateinit var networkState: NetworkStateProvider.State
    private val lastDownloadDays = DownloadDiagnosisKeysSettings.LastDownload(
        startedAt = Instant.EPOCH,
        finishedAt = Instant.EPOCH,
        successful = true
    )

    private val lastDownloadHours =
        DownloadDiagnosisKeysSettings.LastDownload(
            startedAt = Instant.EPOCH,
            finishedAt = Instant.EPOCH,
            successful = true
        )

    private val cachedDayKey = CachedKey(
        info = mockk(),
        path = mockk()
    )
    private val cachedHourKey = CachedKey(
        info = mockk(),
        path = mockk()
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true

        coEvery { keyCache.getAllCachedKeys() } returns listOf()
        coEvery { keyCache.deleteInfoAndFile(any()) } just Runs
        coEvery { syncSettings.lastDownloadDays } returns flowOf(lastDownloadDays)
        coEvery { syncSettings.lastDownloadHours } returns flowOf(lastDownloadHours)
        coEvery { syncSettings.updateLastVersionCode(any()) } just Runs
        coEvery { syncSettings.updateLastDownloadDays(any()) } just Runs
        coEvery { syncSettings.updateLastDownloadHours(any()) } just Runs

        coEvery { dayPackageSyncTool.syncMissingDayPackages(any(), any()) } returns BaseKeyPackageSyncTool.SyncResult(
            successful = true,
            newPackages = listOf(cachedDayKey)
        )
        coEvery { hourPackageSyncTool.syncMissingHourPackages(any(), any()) } returns BaseKeyPackageSyncTool.SyncResult(
            successful = true,
            newPackages = listOf(cachedHourKey)
        )

        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(Duration.ofDays(1))

        every { networkStateProvider.networkState } returns flowOf(networkState)
        every { networkState.isMeteredConnection } returns false
    }

    @AfterEach
    fun teardown() {
        testDir.deleteRecursively()
    }

    fun createInstance(): KeyPackageSyncTool = KeyPackageSyncTool(
        keyCache = keyCache,
        dayPackageSyncTool = dayPackageSyncTool,
        hourPackageSyncTool = hourPackageSyncTool,
        syncSettings = syncSettings,
        timeStamper = timeStamper,
        networkStateProvider = networkStateProvider
    )

    @Test
    fun `normal call sequence`() = runTest {
        val instance = createInstance()

        instance.syncKeyFiles() shouldBe KeyPackageSyncTool.Result(
            deltaKeys = emptyList(),
            newKeys = listOf(cachedDayKey, cachedHourKey),
            wasDaySyncSucccessful = true
        )

        coVerifySequence {
            keyCache.getAllCachedKeys() // To clean up stale locations

            syncSettings.lastDownloadDays
            syncSettings.updateLastDownloadDays(any())
            dayPackageSyncTool.syncMissingDayPackages(listOf(LocationCode("EUR")), false)
            syncSettings.lastDownloadDays
            syncSettings.updateLastDownloadDays(any())

            networkStateProvider.networkState // Check metered
            syncSettings.lastDownloadHours
            syncSettings.updateLastDownloadHours(any())
            hourPackageSyncTool.syncMissingHourPackages(listOf(LocationCode("EUR")), false)
            syncSettings.lastDownloadHours
            syncSettings.updateLastDownloadHours(any())

            keyCache.getAllCachedKeys()
        }
    }

    @Test
    fun `failed day sync is reflected in results property`() = runTest {
        coEvery { dayPackageSyncTool.syncMissingDayPackages(any(), any()) } returns BaseKeyPackageSyncTool.SyncResult(
            successful = false,
            newPackages = listOf(cachedDayKey)
        )
        val instance = createInstance()

        instance.syncKeyFiles() shouldBe KeyPackageSyncTool.Result(
            deltaKeys = emptyList(),
            newKeys = listOf(cachedDayKey, cachedHourKey),
            wasDaySyncSucccessful = false
        )

        coVerifySequence {
            keyCache.getAllCachedKeys() // To clean up stale locations

            syncSettings.lastDownloadDays
            syncSettings.updateLastDownloadDays(any())
            dayPackageSyncTool.syncMissingDayPackages(listOf(LocationCode("EUR")), false)
            syncSettings.lastDownloadDays
            syncSettings.updateLastDownloadDays(any())

            networkStateProvider.networkState // Check metered
            syncSettings.lastDownloadHours
            syncSettings.updateLastDownloadHours(any())
            hourPackageSyncTool.syncMissingHourPackages(listOf(LocationCode("EUR")), false)
            syncSettings.lastDownloadHours
            syncSettings.updateLastDownloadHours(any())

            keyCache.getAllCachedKeys()
        }
    }

    @Test
    fun `missing last download causes force sync`() = runTest {
        every { syncSettings.lastDownloadDays } returns flowOf(null)
        every { syncSettings.lastDownloadHours } returns flowOf(null)

        val instance = createInstance()

        instance.syncKeyFiles() shouldBe KeyPackageSyncTool.Result(
            deltaKeys = emptyList(),
            newKeys = listOf(cachedDayKey, cachedHourKey),
            wasDaySyncSucccessful = true
        )

        coVerifySequence {
            keyCache.getAllCachedKeys() // To clean up stale locations

            syncSettings.lastDownloadDays
            syncSettings.updateLastDownloadDays(any())
            dayPackageSyncTool.syncMissingDayPackages(listOf(LocationCode("EUR")), true)
            syncSettings.lastDownloadDays
            syncSettings.updateLastDownloadDays(any())

            networkStateProvider.networkState // Check metered
            syncSettings.lastDownloadHours
            syncSettings.updateLastDownloadHours(any())
            hourPackageSyncTool.syncMissingHourPackages(listOf(LocationCode("EUR")), true)
            syncSettings.lastDownloadHours
            syncSettings.updateLastDownloadHours(any())

            keyCache.getAllCachedKeys()
        }
    }

    @Test
    fun `failed last download causes force sync`() = runTest {

        every { syncSettings.lastDownloadDays } returns flowOf(
            DownloadDiagnosisKeysSettings.LastDownload(
                startedAt = Instant.EPOCH,
                finishedAt = Instant.EPOCH,
                successful = false
            )
        )

        every { syncSettings.lastDownloadHours } returns flowOf(
            DownloadDiagnosisKeysSettings.LastDownload(
                startedAt = Instant.EPOCH,
                finishedAt = Instant.EPOCH,
                successful = false
            )
        )

        val instance = createInstance()

        instance.syncKeyFiles() shouldBe KeyPackageSyncTool.Result(
            deltaKeys = emptyList(),
            newKeys = listOf(cachedDayKey, cachedHourKey),
            wasDaySyncSucccessful = true
        )

        coVerifySequence {
            // Initial reset
            keyCache.getAllCachedKeys() // To clean up stale locations

            syncSettings.lastDownloadDays
            syncSettings.updateLastDownloadDays(any())
            dayPackageSyncTool.syncMissingDayPackages(listOf(LocationCode("EUR")), true)
            syncSettings.lastDownloadDays
            syncSettings.updateLastDownloadDays(any())

            networkStateProvider.networkState // Check metered
            syncSettings.lastDownloadHours
            syncSettings.updateLastDownloadHours(any())
            hourPackageSyncTool.syncMissingHourPackages(listOf(LocationCode("EUR")), true)
            syncSettings.lastDownloadHours
            syncSettings.updateLastDownloadHours(any())

            keyCache.getAllCachedKeys()
        }
    }

    @Test
    fun `hourly download does not happen on metered connections`() = runTest {
        every { networkState.isMeteredConnection } returns true
        val instance = createInstance()

        instance.syncKeyFiles() shouldBe KeyPackageSyncTool.Result(
            deltaKeys = emptyList(),
            newKeys = listOf(cachedDayKey),
            wasDaySyncSucccessful = true
        )

        coVerifySequence {
            keyCache.getAllCachedKeys() // To clean up stale locations

            syncSettings.lastDownloadDays
            syncSettings.updateLastDownloadDays(any())
            dayPackageSyncTool.syncMissingDayPackages(listOf(LocationCode("EUR")), false)
            syncSettings.lastDownloadDays
            syncSettings.updateLastDownloadDays(any())

            networkStateProvider.networkState // Check metered

            keyCache.getAllCachedKeys()
        }
    }

    @Test
    fun `we clean up stale location data`() = runTest {
        val badLocation = CachedKey(
            info = mockk<CachedKeyInfo>().apply {
                every { location } returns LocationCode("NOT-EUR")
                every { isDownloadComplete } returns true
                every { checkedForExposures } returns true
            },
            path = mockk<File>().apply {
                every { exists() } returns true
            }
        )
        val goodLocation = CachedKey(
            info = mockk<CachedKeyInfo>().apply {
                every { location } returns LocationCode("EUR")
                every { isDownloadComplete } returns true
                every { checkedForExposures } returns false
            },
            path = mockk<File>().apply {
                every { exists() } returns true
            }
        )
        coEvery { keyCache.getAllCachedKeys() } returns listOf(badLocation, goodLocation)
        val instance = createInstance()

        instance.syncKeyFiles()

        coVerifySequence {
            keyCache.getAllCachedKeys() // To clean up stale locations
            keyCache.deleteInfoAndFile(listOf(badLocation.info))

            syncSettings.lastDownloadDays
            syncSettings.updateLastDownloadDays(any())
            dayPackageSyncTool.syncMissingDayPackages(listOf(LocationCode("EUR")), false)
            syncSettings.lastDownloadDays
            syncSettings.updateLastDownloadDays(any())

            networkStateProvider.networkState // Check metered
            syncSettings.lastDownloadHours
            syncSettings.updateLastDownloadHours(any())
            hourPackageSyncTool.syncMissingHourPackages(listOf(LocationCode("EUR")), false)
            syncSettings.lastDownloadHours
            syncSettings.updateLastDownloadHours(any())

            keyCache.getAllCachedKeys()
        }
    }
}
