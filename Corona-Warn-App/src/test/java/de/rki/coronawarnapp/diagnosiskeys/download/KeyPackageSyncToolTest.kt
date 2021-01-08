package de.rki.coronawarnapp.diagnosiskeys.download

import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKey
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.network.NetworkStateProvider
import de.rki.coronawarnapp.util.preferences.FlowPreference
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.preferences.mockFlowPreference
import java.io.File

class KeyPackageSyncToolTest : BaseIOTest() {

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)

    @MockK lateinit var keyCache: KeyCacheRepository
    @MockK lateinit var dayPackageSyncTool: DayPackageSyncTool
    @MockK lateinit var hourPackageSyncTool: HourPackageSyncTool
    @MockK lateinit var syncSettings: DownloadDiagnosisKeysSettings
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var networkStateProvider: NetworkStateProvider
    @MockK lateinit var networkState: NetworkStateProvider.State
    private val lastDownloadDays: FlowPreference<DownloadDiagnosisKeysSettings.LastDownload?> = mockFlowPreference(
        DownloadDiagnosisKeysSettings.LastDownload(
            startedAt = Instant.EPOCH,
            finishedAt = Instant.EPOCH,
            successful = true
        )
    )
    private val lastDownloadHours: FlowPreference<DownloadDiagnosisKeysSettings.LastDownload?> = mockFlowPreference(
        DownloadDiagnosisKeysSettings.LastDownload(
            startedAt = Instant.EPOCH,
            finishedAt = Instant.EPOCH,
            successful = true
        )
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
        coEvery { keyCache.delete(any()) } just Runs
        coEvery { syncSettings.lastDownloadDays } returns lastDownloadDays
        coEvery { syncSettings.lastDownloadHours } returns lastDownloadHours

        coEvery { dayPackageSyncTool.syncMissingDayPackages(any(), any()) } returns BaseKeyPackageSyncTool.SyncResult(
            successful = true,
            newPackages = listOf(cachedDayKey)
        )
        coEvery { hourPackageSyncTool.syncMissingHourPackages(any(), any()) } returns BaseKeyPackageSyncTool.SyncResult(
            successful = true,
            newPackages = listOf(cachedHourKey)
        )

        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(Duration.standardDays(1))

        every { networkStateProvider.networkState } returns flowOf(networkState)
        every { networkState.isMeteredConnection } returns false
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
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
    fun `normal call sequence`() = runBlockingTest {
        val instance = createInstance()

        instance.syncKeyFiles() shouldBe KeyPackageSyncTool.Result(
            availableKeys = emptyList(),
            newKeys = listOf(cachedDayKey, cachedHourKey),
            wasDaySyncSucccessful = true
        )

        coVerifySequence {
            keyCache.getAllCachedKeys() // To clean up stale locations

            lastDownloadDays.value
            lastDownloadDays.update(any())
            dayPackageSyncTool.syncMissingDayPackages(listOf(LocationCode("EUR")), false)
            lastDownloadDays.update(any())

            networkStateProvider.networkState // Check metered
            lastDownloadHours.value
            lastDownloadHours.update(any())
            hourPackageSyncTool.syncMissingHourPackages(listOf(LocationCode("EUR")), false)
            lastDownloadHours.update(any())

            keyCache.getAllCachedKeys()
        }
    }

    @Test
    fun `failed day sync is reflected in results property`() = runBlockingTest {
        coEvery { dayPackageSyncTool.syncMissingDayPackages(any(), any()) } returns BaseKeyPackageSyncTool.SyncResult(
            successful = false,
            newPackages = listOf(cachedDayKey)
        )
        val instance = createInstance()

        instance.syncKeyFiles() shouldBe KeyPackageSyncTool.Result(
            availableKeys = emptyList(),
            newKeys = listOf(cachedDayKey, cachedHourKey),
            wasDaySyncSucccessful = false
        )

        coVerifySequence {
            keyCache.getAllCachedKeys() // To clean up stale locations

            lastDownloadDays.value
            lastDownloadDays.update(any())
            dayPackageSyncTool.syncMissingDayPackages(listOf(LocationCode("EUR")), false)
            lastDownloadDays.update(any())

            networkStateProvider.networkState // Check metered
            lastDownloadHours.value
            lastDownloadHours.update(any())
            hourPackageSyncTool.syncMissingHourPackages(listOf(LocationCode("EUR")), false)
            lastDownloadHours.update(any())

            keyCache.getAllCachedKeys()
        }
    }

    @Test
    fun `missing last download causes force sync`() = runBlockingTest {
        lastDownloadDays.update { null }
        lastDownloadHours.update { null }

        val instance = createInstance()

        instance.syncKeyFiles() shouldBe KeyPackageSyncTool.Result(
            availableKeys = emptyList(),
            newKeys = listOf(cachedDayKey, cachedHourKey),
            wasDaySyncSucccessful = true
        )

        coVerifySequence {
            // Initial reset
            lastDownloadDays.update(any())
            lastDownloadHours.update(any())

            keyCache.getAllCachedKeys() // To clean up stale locations

            lastDownloadDays.value
            lastDownloadDays.update(any())
            dayPackageSyncTool.syncMissingDayPackages(listOf(LocationCode("EUR")), true)
            lastDownloadDays.update(any())

            networkStateProvider.networkState // Check metered
            lastDownloadHours.value
            lastDownloadHours.update(any())
            hourPackageSyncTool.syncMissingHourPackages(listOf(LocationCode("EUR")), true)
            lastDownloadHours.update(any())

            keyCache.getAllCachedKeys()
        }
    }

    @Test
    fun `failed last download causes force sync`() = runBlockingTest {
        lastDownloadDays.update {
            DownloadDiagnosisKeysSettings.LastDownload(
                startedAt = Instant.EPOCH,
                finishedAt = Instant.EPOCH,
                successful = false
            )
        }
        lastDownloadHours.update {
            DownloadDiagnosisKeysSettings.LastDownload(
                startedAt = Instant.EPOCH,
                finishedAt = Instant.EPOCH,
                successful = false
            )
        }
        val instance = createInstance()

        instance.syncKeyFiles() shouldBe KeyPackageSyncTool.Result(
            availableKeys = emptyList(),
            newKeys = listOf(cachedDayKey, cachedHourKey),
            wasDaySyncSucccessful = true
        )

        coVerifySequence {
            // Initial reset
            lastDownloadDays.update(any())
            lastDownloadHours.update(any())

            keyCache.getAllCachedKeys() // To clean up stale locations

            lastDownloadDays.value
            lastDownloadDays.update(any())
            dayPackageSyncTool.syncMissingDayPackages(listOf(LocationCode("EUR")), true)
            lastDownloadDays.update(any())

            networkStateProvider.networkState // Check metered
            lastDownloadHours.value
            lastDownloadHours.update(any())
            hourPackageSyncTool.syncMissingHourPackages(listOf(LocationCode("EUR")), true)
            lastDownloadHours.update(any())

            keyCache.getAllCachedKeys()
        }
    }

    @Test
    fun `hourly download does not happen on metered connections`() = runBlockingTest {
        every { networkState.isMeteredConnection } returns true
        val instance = createInstance()

        instance.syncKeyFiles() shouldBe KeyPackageSyncTool.Result(
            availableKeys = emptyList(),
            newKeys = listOf(cachedDayKey),
            wasDaySyncSucccessful = true
        )

        coVerifySequence {
            keyCache.getAllCachedKeys() // To clean up stale locations

            lastDownloadDays.value
            lastDownloadDays.update(any())
            dayPackageSyncTool.syncMissingDayPackages(listOf(LocationCode("EUR")), false)
            lastDownloadDays.update(any())

            networkStateProvider.networkState // Check metered

            keyCache.getAllCachedKeys()
        }
    }

    @Test
    fun `we clean up stale location data`() = runBlockingTest {
        val badLocation = CachedKey(
            info = mockk<CachedKeyInfo>().apply {
                every { location } returns LocationCode("NOT-EUR")
                every { isDownloadComplete } returns true
            },
            path = mockk<File>().apply {
                every { exists() } returns true
            }
        )
        val goodLocation = CachedKey(
            info = mockk<CachedKeyInfo>().apply {
                every { location } returns LocationCode("EUR")
                every { isDownloadComplete } returns true
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
            keyCache.delete(listOf(badLocation.info))

            lastDownloadDays.value
            lastDownloadDays.update(any())
            dayPackageSyncTool.syncMissingDayPackages(listOf(LocationCode("EUR")), false)
            lastDownloadDays.update(any())

            networkStateProvider.networkState // Check metered
            lastDownloadHours.value
            lastDownloadHours.update(any())
            hourPackageSyncTool.syncMissingHourPackages(listOf(LocationCode("EUR")), false)
            lastDownloadHours.update(any())

            keyCache.getAllCachedKeys()
        }
    }
}
