package de.rki.coronawarnapp.diagnosiskeys.download

import android.database.SQLException
import de.rki.coronawarnapp.diagnosiskeys.server.DiagnosisKeyServer
import de.rki.coronawarnapp.diagnosiskeys.server.DownloadInfo
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.diagnosiskeys.storage.legacy.LegacyKeyCacheMigration
import de.rki.coronawarnapp.storage.AppSettings
import de.rki.coronawarnapp.storage.DeviceStorage
import de.rki.coronawarnapp.storage.InsufficientStorageException
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
    private lateinit var diagnosisKeyServer: DiagnosisKeyServer

    @MockK
    private lateinit var deviceStorage: DeviceStorage

    @MockK
    private lateinit var settings: AppSettings

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)
    private val keyRepoData = mutableMapOf<String, CachedKeyInfo>()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true

        every { settings.isLast3HourModeEnabled } returns false

        coEvery { diagnosisKeyServer.getCountryIndex() } returns listOf(
            LocationCode("DE"),
            LocationCode("NL")
        )
        coEvery { deviceStorage.requireSpacePrivateStorage(any()) } returns mockk<DeviceStorage.CheckResult>().apply {
            every { isSpaceAvailable } returns true
        }

        coEvery { diagnosisKeyServer.getCountryIndex() } returns listOf(
            LocationCode("DE"), LocationCode("NL")
        )
        coEvery { diagnosisKeyServer.getDayIndex(LocationCode("DE")) } returns listOf(
            LocalDate.parse("2020-09-01"), LocalDate.parse("2020-09-02")
        )
        coEvery {
            diagnosisKeyServer.getHourIndex(LocationCode("DE"), LocalDate.parse("2020-09-01"))
        } returns listOf(
            LocalTime.parse("18"), LocalTime.parse("19"), LocalTime.parse("20")
        )
        coEvery {
            diagnosisKeyServer.getHourIndex(LocationCode("DE"), LocalDate.parse("2020-09-02"))
        } returns listOf(
            LocalTime.parse("20"), LocalTime.parse("21")
        )
        coEvery { diagnosisKeyServer.getDayIndex(LocationCode("NL")) } returns listOf(
            LocalDate.parse("2020-09-02"), LocalDate.parse("2020-09-03")
        )
        coEvery {
            diagnosisKeyServer.getHourIndex(LocationCode("NL"), LocalDate.parse("2020-09-02"))
        } returns listOf(
            LocalTime.parse("20"), LocalTime.parse("21"), LocalTime.parse("22")
        )
        coEvery {
            diagnosisKeyServer.getHourIndex(LocationCode("NL"), LocalDate.parse("2020-09-03"))
        } returns listOf(
            LocalTime.parse("22"), LocalTime.parse("23")
        )
        coEvery { diagnosisKeyServer.downloadKeyFile(any(), any(), any(), any(), any()) } answers {
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
            val type = arg<CachedKeyInfo.Type>(0)
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
        type: CachedKeyInfo.Type,
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
            isDownloadComplete = true, checksumMD5 = checksum
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
        type: CachedKeyInfo.Type,
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
            keyServer = diagnosisKeyServer,
            keyCache = keyCache,
            legacyKeyCache = legacyMigration,
            settings = settings,
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
        every { settings.isLast3HourModeEnabled } returns true

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
                downloader.asyncFetchKeyFiles(listOf(LocationCode("DE")))
            }
        }
    }

    @Test
    fun `fetching is aborted in hour if not enough free storage`() {
        every { settings.isLast3HourModeEnabled } returns true

        coEvery { deviceStorage.requireSpacePrivateStorage(67584L) } throws InsufficientStorageException(
            mockk(relaxed = true)
        )

        val downloader = createDownloader()

        runBlocking {
            shouldThrow<InsufficientStorageException> {
                downloader.asyncFetchKeyFiles(listOf(LocationCode("DE")))
            }
        }
    }

    @Test
    fun `error during country index fetch`() {
        coEvery { diagnosisKeyServer.getCountryIndex() } throws IOException()

        val downloader = createDownloader()

        runBlocking {
            shouldThrow<IOException> {
                downloader.asyncFetchKeyFiles(listOf(LocationCode("DE")))
            }
        }
    }

    @Test
    fun `day fetch without prior data`() {
        val downloader = createDownloader()

        runBlocking {
            downloader.asyncFetchKeyFiles(
                listOf(LocationCode("DE"), LocationCode("NL"))
            ).size shouldBe 4
        }

        coVerify {
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_DAY,
                location = LocationCode("DE"),
                dayIdentifier = LocalDate.parse("2020-09-01"),
                hourIdentifier = null
            )
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_DAY,
                location = LocationCode("DE"),
                dayIdentifier = LocalDate.parse("2020-09-02"),
                hourIdentifier = null
            )
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_DAY,
                location = LocationCode("NL"),
                dayIdentifier = LocalDate.parse("2020-09-02"),
                hourIdentifier = null
            )
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_DAY,
                location = LocationCode("NL"),
                dayIdentifier = LocalDate.parse("2020-09-03"),
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
            type = CachedKeyInfo.Type.COUNTRY_DAY,
            location = LocationCode("DE"),
            day = LocalDate.parse("2020-09-01"),
            hour = null,
            isCompleted = true
        )

        mockAddData(
            type = CachedKeyInfo.Type.COUNTRY_DAY,
            location = LocationCode("NL"),
            day = LocalDate.parse("2020-09-02"),
            hour = null,
            isCompleted = true
        )

        val downloader = createDownloader()

        runBlocking {
            downloader.asyncFetchKeyFiles(
                listOf(LocationCode("DE"), LocationCode("NL"))
            ).size shouldBe 4
        }

        coVerify {
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_DAY,
                location = LocationCode("DE"),
                dayIdentifier = LocalDate.parse("2020-09-02"),
                hourIdentifier = null
            )
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_DAY,
                location = LocationCode("NL"),
                dayIdentifier = LocalDate.parse("2020-09-03"),
                hourIdentifier = null
            )
        }

        coVerify(exactly = 2) { keyCache.createCacheEntry(any(), any(), any(), any()) }
        coVerify(exactly = 2) { keyCache.markKeyComplete(any(), any()) }

        coVerify { deviceStorage.requireSpacePrivateStorage(1048576L) }
    }

    @Test
    fun `day fetch deletes stale data`() {
        coEvery { diagnosisKeyServer.getDayIndex(LocationCode("DE")) } returns listOf(
            LocalDate.parse("2020-09-02")
        )
        val (staleKeyInfo, _) = mockAddData(
            type = CachedKeyInfo.Type.COUNTRY_DAY,
            location = LocationCode("DE"),
            day = LocalDate.parse("2020-09-01"),
            hour = null,
            isCompleted = true
        )

        mockAddData(
            type = CachedKeyInfo.Type.COUNTRY_DAY,
            location = LocationCode("NL"),
            day = LocalDate.parse("2020-09-02"),
            hour = null,
            isCompleted = true
        )

        val downloader = createDownloader()

        runBlocking {
            downloader.asyncFetchKeyFiles(
                listOf(LocationCode("DE"), LocationCode("NL"))
            ).size shouldBe 3
        }

        coVerify {
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_DAY,
                location = LocationCode("DE"),
                dayIdentifier = LocalDate.parse("2020-09-02"),
                hourIdentifier = null
            )
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_DAY,
                location = LocationCode("NL"),
                dayIdentifier = LocalDate.parse("2020-09-03"),
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
        coEvery { diagnosisKeyServer.downloadKeyFile(any(), any(), any(), any(), any()) } answers {
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
            downloader.asyncFetchKeyFiles(
                listOf(LocationCode("DE"), LocationCode("NL"))
            ).size shouldBe 3
        }

        // We delete the entry for the failed download
        coVerify(exactly = 1) { keyCache.delete(any()) }
    }

    @Test
    fun `last3Hours fetch without prior data`() {
        every { settings.isLast3HourModeEnabled } returns true

        val downloader = createDownloader()

        runBlocking {
            downloader.asyncFetchKeyFiles(
                listOf(LocationCode("DE"), LocationCode("NL"))
            ).size shouldBe 6
        }

        coVerify {
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_HOUR,
                location = LocationCode("DE"),
                dayIdentifier = LocalDate.parse("2020-09-02"),
                hourIdentifier = LocalTime.parse("21")
            )
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_HOUR,
                location = LocationCode("DE"),
                dayIdentifier = LocalDate.parse("2020-09-02"),
                hourIdentifier = LocalTime.parse("20")
            )
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_HOUR,
                location = LocationCode("DE"),
                dayIdentifier = LocalDate.parse("2020-09-01"),
                hourIdentifier = LocalTime.parse("20")
            )

            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_HOUR,
                location = LocationCode("NL"),
                dayIdentifier = LocalDate.parse("2020-09-03"),
                hourIdentifier = LocalTime.parse("23")
            )
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_HOUR,
                location = LocationCode("NL"),
                dayIdentifier = LocalDate.parse("2020-09-03"),
                hourIdentifier = LocalTime.parse("22")
            )
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_HOUR,
                location = LocationCode("NL"),
                dayIdentifier = LocalDate.parse("2020-09-02"),
                hourIdentifier = LocalTime.parse("22")
            )
        }
        coVerify(exactly = 6) { keyCache.markKeyComplete(any(), any()) }

        keyRepoData.size shouldBe 6
        keyRepoData.values.forEach { it.isDownloadComplete shouldBe true }

        coVerify { deviceStorage.requireSpacePrivateStorage(135168L) }
    }

    @Test
    fun `last3Hours fetch with prior data`() {
        every { settings.isLast3HourModeEnabled } returns true

        mockAddData(
            type = CachedKeyInfo.Type.COUNTRY_HOUR,
            location = LocationCode("DE"),
            day = LocalDate.parse("2020-09-01"),
            hour = LocalTime.parse("20"),
            isCompleted = true
        )
        mockAddData(
            type = CachedKeyInfo.Type.COUNTRY_HOUR,
            location = LocationCode("NL"),
            day = LocalDate.parse("2020-09-02"),
            hour = LocalTime.parse("22"),
            isCompleted = true
        )

        val downloader = createDownloader()

        runBlocking {
            downloader.asyncFetchKeyFiles(
                listOf(LocationCode("DE"), LocationCode("NL"))
            ).size shouldBe 6
        }

        coVerify {
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_HOUR,
                location = LocationCode("DE"),
                dayIdentifier = LocalDate.parse("2020-09-02"),
                hourIdentifier = LocalTime.parse("21")
            )
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_HOUR,
                location = LocationCode("DE"),
                dayIdentifier = LocalDate.parse("2020-09-02"),
                hourIdentifier = LocalTime.parse("20")
            )

            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_HOUR,
                location = LocationCode("NL"),
                dayIdentifier = LocalDate.parse("2020-09-03"),
                hourIdentifier = LocalTime.parse("23")
            )
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_HOUR,
                location = LocationCode("NL"),
                dayIdentifier = LocalDate.parse("2020-09-03"),
                hourIdentifier = LocalTime.parse("22")
            )
        }
        coVerify(exactly = 4) {
            diagnosisKeyServer.downloadKeyFile(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        }
        coVerify { deviceStorage.requireSpacePrivateStorage(90112L) }
    }

    @Test
    fun `last3Hours fetch deletes stale data`() {
        every { settings.isLast3HourModeEnabled } returns true

        val (staleKey1, _) = mockAddData(
            type = CachedKeyInfo.Type.COUNTRY_HOUR,
            location = LocationCode("NL"),
            day = LocalDate.parse("2020-09-02"),
            hour = LocalTime.parse("12"), // Stale hour
            isCompleted = true
        )

        val (staleKey2, _) = mockAddData(
            type = CachedKeyInfo.Type.COUNTRY_HOUR,
            location = LocationCode("NL"),
            day = LocalDate.parse("2020-09-01"), // Stale day
            hour = LocalTime.parse("22"),
            isCompleted = true
        )

        mockAddData(
            type = CachedKeyInfo.Type.COUNTRY_HOUR,
            location = LocationCode("DE"),
            day = LocalDate.parse("2020-09-01"),
            hour = LocalTime.parse("20"),
            isCompleted = true
        )
        mockAddData(
            type = CachedKeyInfo.Type.COUNTRY_HOUR,
            location = LocationCode("NL"),
            day = LocalDate.parse("2020-09-02"),
            hour = LocalTime.parse("22"),
            isCompleted = true
        )

        val downloader = createDownloader()

        runBlocking {
            downloader.asyncFetchKeyFiles(
                listOf(LocationCode("DE"), LocationCode("NL"))
            ).size shouldBe 6
        }

        coVerify {
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_HOUR,
                location = LocationCode("DE"),
                dayIdentifier = LocalDate.parse("2020-09-02"),
                hourIdentifier = LocalTime.parse("21")
            )
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_HOUR,
                location = LocationCode("DE"),
                dayIdentifier = LocalDate.parse("2020-09-02"),
                hourIdentifier = LocalTime.parse("20")
            )

            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_HOUR,
                location = LocationCode("NL"),
                dayIdentifier = LocalDate.parse("2020-09-03"),
                hourIdentifier = LocalTime.parse("23")
            )
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_HOUR,
                location = LocationCode("NL"),
                dayIdentifier = LocalDate.parse("2020-09-03"),
                hourIdentifier = LocalTime.parse("22")
            )
        }
        coVerify(exactly = 4) {
            diagnosisKeyServer.downloadKeyFile(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        }
        coVerify(exactly = 1) { keyCache.delete(listOf(staleKey1, staleKey2)) }
    }

    @Test
    fun `last3Hours fetch skips single download failures`() {
        every { settings.isLast3HourModeEnabled } returns true

        var dlCounter = 0
        coEvery { diagnosisKeyServer.downloadKeyFile(any(), any(), any(), any(), any()) } answers {
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
            downloader.asyncFetchKeyFiles(
                listOf(LocationCode("DE"), LocationCode("NL"))
            ).size shouldBe 5
        }

        // We delete the entry for the failed download
        coVerify(exactly = 1) { keyCache.delete(any()) }
    }

    @Test
    fun `not completed cache entries are overwritten`() {
        mockAddData(
            type = CachedKeyInfo.Type.COUNTRY_DAY,
            location = LocationCode("DE"),
            day = LocalDate.parse("2020-09-01"),
            hour = null,
            isCompleted = false
        )

        val downloader = createDownloader()

        runBlocking {
            downloader.asyncFetchKeyFiles(
                listOf(LocationCode("DE"), LocationCode("NL"))
            ).size shouldBe 4
        }

        coVerify {
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_DAY,
                location = LocationCode("DE"),
                dayIdentifier = LocalDate.parse("2020-09-01"),
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
            downloader.asyncFetchKeyFiles(
                listOf(LocationCode("DE"), LocationCode("NL"))
            ).size shouldBe 3
        }

        coVerify(exactly = 4) {
            diagnosisKeyServer.downloadKeyFile(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `store server md5`() {
        coEvery { diagnosisKeyServer.getCountryIndex() } returns listOf(LocationCode("DE"))
        coEvery { diagnosisKeyServer.getDayIndex(LocationCode("DE")) } returns listOf(
            LocalDate.parse("2020-09-01")
        )

        val downloader = createDownloader()

        runBlocking {
            downloader.asyncFetchKeyFiles(
                listOf(LocationCode("DE"))
            ).size shouldBe 1
        }

        coVerify {
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_DAY,
                location = LocationCode("DE"),
                dayIdentifier = LocalDate.parse("2020-09-01"),
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
        coEvery { diagnosisKeyServer.getCountryIndex() } returns listOf(LocationCode("DE"))
        coEvery { diagnosisKeyServer.getDayIndex(LocationCode("DE")) } returns listOf(
            LocalDate.parse("2020-09-01")
        )
        coEvery { diagnosisKeyServer.downloadKeyFile(any(), any(), any(), any(), any()) } answers {
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
            downloader.asyncFetchKeyFiles(
                listOf(LocationCode("DE"))
            ).size shouldBe 1
        }

        coVerify {
            keyCache.createCacheEntry(
                type = CachedKeyInfo.Type.COUNTRY_DAY,
                location = LocationCode("DE"),
                dayIdentifier = LocalDate.parse("2020-09-01"),
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
