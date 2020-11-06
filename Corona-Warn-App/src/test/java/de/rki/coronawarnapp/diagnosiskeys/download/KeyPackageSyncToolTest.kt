package de.rki.coronawarnapp.diagnosiskeys.download

import de.rki.coronawarnapp.diagnosiskeys.server.DiagnosisKeyServer
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.network.NetworkStateProvider
import de.rki.coronawarnapp.util.preferences.FlowPreference
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.preferences.mockFlowPreference
import java.io.File

class KeyPackageSyncToolTest : BaseIOTest() {

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)

    @MockK lateinit var keyServer: DiagnosisKeyServer
    @MockK lateinit var keyCache: KeyCacheRepository
    @MockK lateinit var daySyncTool: DaySyncTool
    @MockK lateinit var hourSyncTool: HourSyncTool
    @MockK lateinit var syncSettings: KeyPackageSyncSettings
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var networkStateProvider: NetworkStateProvider
    @MockK lateinit var networkState: NetworkStateProvider.State
    private val lastDownloadHours: FlowPreference<KeyPackageSyncSettings.LastDownload?> = mockFlowPreference(null)
    private val lastDownloadDays: FlowPreference<KeyPackageSyncSettings.LastDownload?> = mockFlowPreference(null)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true

        coEvery { keyServer.getLocationIndex() } returns listOf(LocationCode("EUR"))
        coEvery { keyCache.getAllCachedKeys() } returns listOf()
        coEvery { syncSettings.lastDownloadHours } returns lastDownloadHours
        coEvery { syncSettings.lastDownloadDays } returns lastDownloadDays

        coEvery { daySyncTool.syncMissingDays(any(), any()) } returns true
        coEvery { hourSyncTool.syncMissingHours(any(), any()) } returns true

        every { timeStamper.nowUTC } returns Instant.EPOCH
        every { networkStateProvider.networkState } returns flowOf(networkState)
        every { networkState.isMeteredConnection } returns false
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    fun createInstance(): KeyPackageSyncTool = KeyPackageSyncTool(
        keyServer = keyServer,
        keyCache = keyCache,
        daySyncTool = daySyncTool,
        hourSyncTool = hourSyncTool,
        syncSettings = syncSettings,
        timeStamper = timeStamper,
        networkStateProvider = networkStateProvider
    )

    @Test
    fun `normal call sequence`() = runBlockingTest {
        val instance = createInstance()

        instance.syncKeyFiles()

        coVerifySequence {
            keyServer.getLocationIndex() // wanted-available=target
            keyCache.getAllCachedKeys() // To clean up stale locations

            lastDownloadDays.value
            lastDownloadDays.update(any())
            daySyncTool.syncMissingDays(listOf(LocationCode("EUR")), true)
            lastDownloadDays.update(any())

            networkStateProvider.networkState // Check metered
            lastDownloadHours.value
            lastDownloadHours.update(any())
            hourSyncTool.syncMissingHours(listOf(LocationCode("EUR")), true)
            lastDownloadHours.update(any())

            keyCache.getAllCachedKeys()
        }
    }

    @Test
    fun `last download starts failed, and is set successful after sync completes`() {
        TODO()
        // Both Day- HourSyncTool need to return successful sync for the download to be considered successful
    }

    @Test
    fun `failed day sync is reflected in results property`() {
        TODO()
    }

    @Test
    fun `missing last download causes force sync`() {
        TODO()
    }

    @Test
    fun `failed last download causes force sync`() {
        TODO()
    }

    @Test
    fun `before hour sync is called we check if the connection is metered`() {
        TODO()
    }
}
