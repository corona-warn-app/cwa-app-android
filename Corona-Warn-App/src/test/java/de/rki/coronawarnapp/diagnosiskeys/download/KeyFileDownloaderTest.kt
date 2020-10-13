package de.rki.coronawarnapp.diagnosiskeys.download

import android.database.SQLException
import de.rki.coronawarnapp.diagnosiskeys.server.DiagnosisKeyServer
import de.rki.coronawarnapp.diagnosiskeys.server.DownloadInfo
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo.Type
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.diagnosiskeys.storage.legacy.LegacyKeyCacheMigration
import de.rki.coronawarnapp.storage.DeviceStorage
import de.rki.coronawarnapp.storage.InsufficientStorageException
import de.rki.coronawarnapp.storage.TestSettings
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.TestDispatcherProvider
import timber.log.Timber
import java.io.File
import java.io.IOException
import kotlin.time.ExperimentalTime

/**
 * CachedKeyFileHolder test.
 */
@ExperimentalTime
@ExperimentalCoroutinesApi
class KeyFileDownloaderTest : BaseIOTest() {

    @MockK
    private lateinit var keyCache: KeyCacheRepository

    @MockK
    private lateinit var legacyMigration: LegacyKeyCacheMigration

    @MockK
    private lateinit var server: DiagnosisKeyServer

    @MockK
    private lateinit var deviceStorage: DeviceStorage

    @MockK
    private lateinit var testSettings: TestSettings

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)
    private val keyRepoData = mutableMapOf<String, CachedKeyInfo>()

    private val String.loc get() = LocationCode(this)
    private val String.day get() = LocalDate.parse(this)
    private val String.hour get() = LocalTime.parse(this)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true

        every { testSettings.isHourKeyPkgMode } returns false

        coEvery { server.getCountryIndex() } returns listOf("DE".loc, "NL".loc)
        coEvery { deviceStorage.requireSpacePrivateStorage(any()) } returns mockk<DeviceStorage.CheckResult>().apply {
            every { isSpaceAvailable } returns true
        }

        coEvery { server.getDayIndex("DE".loc) } returns listOf(
            "2020-09-01".day, "2020-09-02".day
        )
        coEvery {
            server.getHourIndex("DE".loc, "2020-09-01".day)
        } returns (0..23).map { "$it".hour }
        coEvery {
            server.getHourIndex("DE".loc, "2020-09-02".day)
        } returns (0..23).map { "$it".hour }
        coEvery {
            server.getHourIndex("DE".loc, "2020-09-03".day)
        } returns (0..12).map { "$it".hour }

        coEvery { server.getDayIndex("NL".loc) } returns listOf(
            "2020-09-01".day, "2020-09-02".day
        )
        coEvery {
            server.getHourIndex("NL".loc, "2020-09-01".day)
        } returns (0..23).map { "$it".hour }
        coEvery {
            server.getHourIndex("NL".loc, "2020-09-02".day)
        } returns (0..23).map { "$it".hour }
        coEvery {
            server.getHourIndex("NL".loc, "2020-09-03".day)
        } returns (0..12).map { "$it".hour }

        coEvery { server.downloadKeyFile(any(), any(), any(), any(), any()) } answers {
            mockDownloadServerDownload(
                locationCode = arg(0),
                day = arg(1),
                hour = arg(2),
                saveTo = arg(3)
            )
        }

        coEvery { keyCache.createCacheEntry(any(), any(), any(), any()) } answers {
            mockKeyCacheCreateEntry(arg(0), arg(1), arg(2), arg(3))
        }
        coEvery { keyCache.markKeyComplete(any(), any()) } answers {
            mockKeyCacheUpdateComplete(arg(0), arg(1))
        }
        coEvery { keyCache.getEntriesForType(any()) } answers {
            val type = arg<Type>(0)
            keyRepoData.values.filter { it.type == type }.map { it to File(testDir, it.id) }
        }
        coEvery { keyCache.getAllCachedKeys() } answers {
            keyRepoData.values.map {
                it to File(testDir, it.id)
            }
        }
        coEvery { keyCache.delete(any()) } answers {
            val keyInfos = arg<List<CachedKeyInfo>>(0)
            keyInfos.forEach {
                keyRepoData.remove(it.id)
            }
        }
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        keyRepoData.clear()
        testDir.deleteRecursively()
    }

    private fun mockKeyCacheCreateEntry(
        type: Type,
        location: LocationCode,
        dayIdentifier: LocalDate,
        hourIdentifier: LocalTime?
    ): Pair<CachedKeyInfo, File> {
        val keyInfo = CachedKeyInfo(
            type = type,
            location = location,
            day = dayIdentifier,
            hour = hourIdentifier,
            createdAt = Instant.now()
        )
        Timber.i("mockKeyCacheCreateEntry(...): %s", keyInfo)
        val file = File(testDir, keyInfo.id)
        keyRepoData[keyInfo.id] = keyInfo
        return keyInfo to file
    }

    private fun mockKeyCacheUpdateComplete(
        keyInfo: CachedKeyInfo,
        checksum: String
    ) {
        keyRepoData[keyInfo.id] = keyInfo.copy(
            isDownloadComplete = true,
            checksumMD5 = checksum
        )
    }

    private fun mockDownloadServerDownload(
        locationCode: LocationCode,
        day: LocalDate,
        hour: LocalTime? = null,
        saveTo: File,
        checksumServerMD5: String? = "serverMD5",
        checksumLocalMD5: String? = "localMD5"
    ): DownloadInfo {
        saveTo.writeText("$locationCode.$day.$hour")
        return mockk<DownloadInfo>().apply {
            every { serverMD5 } returns checksumServerMD5
            every { localMD5 } returns checksumLocalMD5
        }
    }

    private fun mockAddData(
        type: Type,
        location: LocationCode,
        day: LocalDate,
        hour: LocalTime?,
        isCompleted: Boolean
    ): Pair<CachedKeyInfo, File> {
        val (keyInfo, file) = mockKeyCacheCreateEntry(type, location, day, hour)
        if (isCompleted) {
            mockDownloadServerDownload(
                locationCode = location,
                day = day,
                hour = hour,
                saveTo = file
            )
            mockKeyCacheUpdateComplete(keyInfo, "serverMD5")
        }
        return keyRepoData[keyInfo.id]!! to file
    }

    private fun createDownloader(): KeyFileDownloader {
        val downloader = KeyFileDownloader(
            deviceStorage = deviceStorage,
            keyServer = server,
            keyCache = keyCache,
            legacyKeyCache = legacyMigration,
            testSettings = testSettings,
            dispatcherProvider = TestDispatcherProvider
        )
        Timber.i("createDownloader(): %s", downloader)
        return downloader
    }

    @Test
    fun `wanted country list is empty, day mode`() {
        val downloader = createDownloader()
        runBlocking {
            downloader.asyncFetchKeyFiles(emptyList()) shouldBe emptyList()
        }
    }

    @Test
    fun `wanted country list is empty, hour mode`() {
        every { testSettings.isHourKeyPkgMode } returns true

        val downloader = createDownloader()
        runBlocking {
            downloader.asyncFetchKeyFiles(emptyList()) shouldBe emptyList()
        }
    }

    @Test
    fun `fetching is aborted in day if not enough free storage`() {
        coEvery { deviceStorage.requireSpacePrivateStorage(1048576L) } throws InsufficientStorageException(
            mockk(relaxed = true)
        )

        val downloader = createDownloader()

        runBlocking {
            shouldThrow<InsufficientStorageException> {
                downloader.asyncFetchKeyFiles(listOf("DE".loc))
            }
        }
    }

    @Test
    fun `fetching is aborted in hour if not enough free storage`() {
        every { testSettings.isHourKeyPkgMode } returns true

        coEvery { deviceStorage.requireSpacePrivateStorage(540672L) } throws InsufficientStorageException(
            mockk(relaxed = true)
        )

        val downloader = createDownloader()

        runBlocking {
            shouldThrow<InsufficientStorageException> {
                downloader.asyncFetchKeyFiles(listOf("DE".loc))
            }
        }
    }

    @Test
    fun `error during country index fetch`() {
        coEvery { server.getCountryIndex() } throws IOException()

        val downloader = createDownloader()

        runBlocking {
            shouldThrow<IOException> {
                downloader.asyncFetchKeyFiles(listOf("DE".loc))
            }
        }
    }

    @Test
    fun `day fetch without prior data`() {
        val downloader = createDownloader()

        runBlocking {
            downloader.asyncFetchKeyFiles(listOf("DE".loc, "NL".loc)).size shouldBe 4
        }

        coVerify {
            keyCache.createCacheEntry(
                type = Type.COUNTRY_DAY,
                location = "DE".loc,
                dayIdentifier = "2020-09-01".day,
                hourIdentifier = null
            )
            keyCache.createCacheEntry(
                type = Type.COUNTRY_DAY,
                location = "DE".loc,
                dayIdentifier = "2020-09-02".day,
                hourIdentifier = null
            )
            keyCache.createCacheEntry(
                type = Type.COUNTRY_DAY,
                location = "NL".loc,
                dayIdentifier = "2020-09-01".day,
                hourIdentifier = null
            )
            keyCache.createCacheEntry(
                type = Type.COUNTRY_DAY,
                location = "NL".loc,
                dayIdentifier = "2020-09-02".day,
                hourIdentifier = null
            )
        }
        keyRepoData.size shouldBe 4
        keyRepoData.values.forEach { it.isDownloadComplete shouldBe true }
        coVerify { deviceStorage.requireSpacePrivateStorage(2097152L) }
    }

    @Test
    fun `day fetch with existing data`() {
        mockAddData(
            type = Type.COUNTRY_DAY,
            location = "DE".loc,
            day = "2020-09-01".day,
            hour = null,
            isCompleted = true
        )
        mockAddData(
            type = Type.COUNTRY_DAY,
            location = "NL".loc,
            day = "2020-09-02".day,
            hour = null,
            isCompleted = true
        )

        val downloader = createDownloader()

        runBlocking {
            downloader.asyncFetchKeyFiles(listOf("DE".loc, "NL".loc)).size shouldBe 4
        }

        coVerify {
            keyCache.createCacheEntry(
                type = Type.COUNTRY_DAY,
                location = "DE".loc,
                dayIdentifier = "2020-09-02".day,
                hourIdentifier = null
            )
            keyCache.createCacheEntry(
                type = Type.COUNTRY_DAY,
                location = "NL".loc,
                dayIdentifier = "2020-09-01".day,
                hourIdentifier = null
            )
        }

        coVerify(exactly = 2) { keyCache.createCacheEntry(any(), any(), any(), any()) }
        coVerify(exactly = 2) { keyCache.markKeyComplete(any(), any()) }

        coVerify { deviceStorage.requireSpacePrivateStorage(1048576L) }
    }

    @Test
    fun `day fetch deletes stale data`() {
        coEvery { server.getDayIndex("DE".loc) } returns listOf("2020-09-02".day)
        val (staleKeyInfo, _) = mockAddData(
            type = Type.COUNTRY_DAY,
            location = "DE".loc,
            day = "2020-09-01".day,
            hour = null,
            isCompleted = true
        )

        mockAddData(
            type = Type.COUNTRY_DAY,
            location = "NL".loc,
            day = "2020-09-02".day,
            hour = null,
            isCompleted = true
        )

        val downloader = createDownloader()

        runBlocking {
            downloader.asyncFetchKeyFiles(listOf("DE".loc, "NL".loc)).size shouldBe 3
        }

        coVerify {
            keyCache.createCacheEntry(
                type = Type.COUNTRY_DAY,
                location = "DE".loc,
                dayIdentifier = "2020-09-02".day,
                hourIdentifier = null
            )
            keyCache.createCacheEntry(
                type = Type.COUNTRY_DAY,
                location = "NL".loc,
                dayIdentifier = "2020-09-01".day,
                hourIdentifier = null
            )
        }
        coVerify(exactly = 1) { keyCache.delete(listOf(staleKeyInfo)) }
        coVerify(exactly = 2) { keyCache.createCacheEntry(any(), any(), any(), any()) }
        coVerify(exactly = 2) { keyCache.markKeyComplete(any(), any()) }
    }

    @Test
    fun `day fetch skips single download failures`() {
        var dlCounter = 0
        coEvery { server.downloadKeyFile(any(), any(), any(), any(), any()) } answers {
            dlCounter++
            if (dlCounter == 2) throw IOException("Timeout")
            mockDownloadServerDownload(
                locationCode = arg(0),
                day = arg(1),
                hour = arg(2),
                saveTo = arg(3)
            )
        }

        val downloader = createDownloader()

        runBlocking {
            downloader.asyncFetchKeyFiles(listOf("DE".loc, "NL".loc)).size shouldBe 3
        }

        // We delete the entry for the failed download
        coVerify(exactly = 1) { keyCache.delete(any()) }
    }

    @Test
    fun `last3Hours fetch without prior data`() {
        every { testSettings.isHourKeyPkgMode } returns true

        val downloader = createDownloader()

        runBlocking {
            downloader.asyncFetchKeyFiles(listOf("DE".loc, "NL".loc)).size shouldBe 48
        }

        coVerify {
            keyCache.createCacheEntry(
                type = Type.COUNTRY_HOUR,
                location = "DE".loc,
                dayIdentifier = "2020-09-03".day,
                hourIdentifier = "12".hour
            )
            keyCache.createCacheEntry(
                type = Type.COUNTRY_HOUR,
                location = "DE".loc,
                dayIdentifier = "2020-09-03".day,
                hourIdentifier = "11".hour
            )
            keyCache.createCacheEntry(
                type = Type.COUNTRY_HOUR,
                location = "DE".loc,
                dayIdentifier = "2020-09-03".day,
                hourIdentifier = "10".hour
            )

            keyCache.createCacheEntry(
                type = Type.COUNTRY_HOUR,
                location = "NL".loc,
                dayIdentifier = "2020-09-03".day,
                hourIdentifier = "12".hour
            )
            keyCache.createCacheEntry(
                type = Type.COUNTRY_HOUR,
                location = "NL".loc,
                dayIdentifier = "2020-09-03".day,
                hourIdentifier = "11".hour
            )
            keyCache.createCacheEntry(
                type = Type.COUNTRY_HOUR,
                location = "NL".loc,
                dayIdentifier = "2020-09-03".day,
                hourIdentifier = "10".hour
            )
        }
        coVerify(exactly = 48) { keyCache.markKeyComplete(any(), any()) }

        keyRepoData.size shouldBe 48
        keyRepoData.values.forEach { it.isDownloadComplete shouldBe true }

        coVerify { deviceStorage.requireSpacePrivateStorage(1081344L) }
    }

    @Test
    fun `last3Hours fetch with prior data`() {
        every { testSettings.isHourKeyPkgMode } returns true

        mockAddData(
            type = Type.COUNTRY_HOUR,
            location = "DE".loc,
            day = "2020-09-03".day,
            hour = "11".hour,
            isCompleted = true
        )
        mockAddData(
            type = Type.COUNTRY_HOUR,
            location = "NL".loc,
            day = "2020-09-03".day,
            hour = "11".hour,
            isCompleted = true
        )

        val downloader = createDownloader()

        runBlocking {
            downloader.asyncFetchKeyFiles(listOf("DE".loc, "NL".loc)).size shouldBe 48
        }

        coVerify {
            keyCache.createCacheEntry(
                type = Type.COUNTRY_HOUR,
                location = "DE".loc,
                dayIdentifier = "2020-09-03".day,
                hourIdentifier = "12".hour
            )
            keyCache.createCacheEntry(
                type = Type.COUNTRY_HOUR,
                location = "DE".loc,
                dayIdentifier = "2020-09-02".day,
                hourIdentifier = "13".hour
            )

            keyCache.createCacheEntry(
                type = Type.COUNTRY_HOUR,
                location = "NL".loc,
                dayIdentifier = "2020-09-03".day,
                hourIdentifier = "12".hour
            )
            keyCache.createCacheEntry(
                type = Type.COUNTRY_HOUR,
                location = "NL".loc,
                dayIdentifier = "2020-09-02".day,
                hourIdentifier = "13".hour
            )
        }
        coVerify(exactly = 46) {
            server.downloadKeyFile(any(), any(), any(), any(), any())
        }
        coVerify { deviceStorage.requireSpacePrivateStorage(1036288L) }
    }

    @Test
    fun `last3Hours fetch deletes stale data`() {
        every { testSettings.isHourKeyPkgMode } returns true

        val (staleKey1, _) = mockAddData(
            type = Type.COUNTRY_HOUR,
            location = "DE".loc,
            day = "2020-09-02".day,
            hour = "01".hour, // Stale hour
            isCompleted = true
        )

        val (staleKey2, _) = mockAddData(
            type = Type.COUNTRY_HOUR,
            location = "NL".loc,
            day = "2020-09-02".day, // Stale day
            hour = "01".hour,
            isCompleted = true
        )

        mockAddData(
            type = Type.COUNTRY_HOUR,
            location = "DE".loc,
            day = "2020-09-03".day,
            hour = "10".hour,
            isCompleted = true
        )
        mockAddData(
            type = Type.COUNTRY_HOUR,
            location = "NL".loc,
            day = "2020-09-03".day,
            hour = "10".hour,
            isCompleted = true
        )

        val downloader = createDownloader()

        runBlocking {
            downloader.asyncFetchKeyFiles(listOf("DE".loc, "NL".loc)).size shouldBe 48
        }

        coVerify {
            keyCache.createCacheEntry(
                type = Type.COUNTRY_HOUR,
                location = "DE".loc,
                dayIdentifier = "2020-09-03".day,
                hourIdentifier = "12".hour
            )
            keyCache.createCacheEntry(
                type = Type.COUNTRY_HOUR,
                location = "DE".loc,
                dayIdentifier = "2020-09-03".day,
                hourIdentifier = "11".hour
            )

            keyCache.createCacheEntry(
                type = Type.COUNTRY_HOUR,
                location = "NL".loc,
                dayIdentifier = "2020-09-02".day,
                hourIdentifier = "13".hour
            )
            keyCache.createCacheEntry(
                type = Type.COUNTRY_HOUR,
                location = "NL".loc,
                dayIdentifier = "2020-09-02".day,
                hourIdentifier = "13".hour
            )
        }
        coVerify(exactly = 46) {
            server.downloadKeyFile(any(), any(), any(), any(), any())
        }
        coVerify(exactly = 1) { keyCache.delete(listOf(staleKey1, staleKey2)) }
    }

    @Test
    fun `last3Hours fetch skips single download failures`() {
        every { testSettings.isHourKeyPkgMode } returns true

        var dlCounter = 0
        coEvery { server.downloadKeyFile(any(), any(), any(), any(), any()) } answers {
            dlCounter++
            if (dlCounter == 2) throw IOException("Timeout")
            mockDownloadServerDownload(
                locationCode = arg(0),
                day = arg(1),
                hour = arg(2),
                saveTo = arg(3)
            )
        }

        val downloader = createDownloader()

        runBlocking {
            downloader.asyncFetchKeyFiles(listOf("DE".loc, "NL".loc)).size shouldBe 48
        }

        // We delete the entry for the failed download
        coVerify(exactly = 1) { keyCache.delete(any()) }
    }

    @Test
    fun `not completed cache entries are overwritten`() {
        mockAddData(
            type = Type.COUNTRY_DAY,
            location = "DE".loc,
            day = "2020-09-01".day,
            hour = null,
            isCompleted = false
        )

        val downloader = createDownloader()

        runBlocking {
            downloader.asyncFetchKeyFiles(listOf("DE".loc, "NL".loc)).size shouldBe 4
        }

        coVerify {
            keyCache.createCacheEntry(
                type = Type.COUNTRY_DAY,
                location = "DE".loc,
                dayIdentifier = "2020-09-01".day,
                hourIdentifier = null
            )
        }
    }

    @Test
    fun `database errors do not abort the whole process`() {
        var completionCounter = 0
        coEvery { keyCache.markKeyComplete(any(), any()) } answers {
            completionCounter++
            if (completionCounter == 2) throw SQLException(":)")
            mockKeyCacheUpdateComplete(arg(0), arg(1))
        }

        val downloader = createDownloader()

        runBlocking {
            downloader.asyncFetchKeyFiles(listOf("DE".loc, "NL".loc)).size shouldBe 3
        }

        coVerify(exactly = 4) {
            server.downloadKeyFile(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `store server md5`() {
        coEvery { server.getCountryIndex() } returns listOf("DE".loc)
        coEvery { server.getDayIndex("DE".loc) } returns listOf("2020-09-01".day)

        val downloader = createDownloader()

        runBlocking {
            downloader.asyncFetchKeyFiles(listOf("DE".loc)).size shouldBe 1
        }

        coVerify {
            keyCache.createCacheEntry(
                type = Type.COUNTRY_DAY,
                location = "DE".loc,
                dayIdentifier = "2020-09-01".day,
                hourIdentifier = null
            )
        }
        keyRepoData.size shouldBe 1
        keyRepoData.values.forEach {
            it.isDownloadComplete shouldBe true
            it.checksumMD5 shouldBe "serverMD5"
        }
    }

    @Test
    fun `use local MD5 as fallback if there is none available from the server`() {
        coEvery { server.getCountryIndex() } returns listOf("DE".loc)
        coEvery { server.getDayIndex("DE".loc) } returns listOf("2020-09-01".day)
        coEvery { server.downloadKeyFile(any(), any(), any(), any(), any()) } answers {
            mockDownloadServerDownload(
                locationCode = arg(0),
                day = arg(1),
                hour = arg(2),
                saveTo = arg(3),
                checksumServerMD5 = null
            )
        }

        val downloader = createDownloader()

        runBlocking {
            downloader.asyncFetchKeyFiles(listOf("DE".loc)).size shouldBe 1
        }

        coVerify {
            keyCache.createCacheEntry(
                type = Type.COUNTRY_DAY,
                location = "DE".loc,
                dayIdentifier = "2020-09-01".day,
                hourIdentifier = null
            )
        }
        keyRepoData.size shouldBe 1
        keyRepoData.values.forEach {
            it.isDownloadComplete shouldBe true
            it.checksumMD5 shouldBe "localMD5"
        }
    }
}
