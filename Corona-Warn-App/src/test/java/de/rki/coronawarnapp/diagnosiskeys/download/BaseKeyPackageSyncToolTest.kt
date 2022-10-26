package de.rki.coronawarnapp.diagnosiskeys.download

import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKey
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.storage.DeviceStorage
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class BaseKeyPackageSyncToolTest : BaseIOTest() {

    @MockK lateinit var keyCache: KeyCacheRepository
    @MockK lateinit var deviceStorage: DeviceStorage

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)

    private val String.loc get() = LocationCode(this)
    private val String.day get() = LocalDate.parse(this)
    private val String.hour get() = LocalTime.parse(this)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true

        coEvery { deviceStorage.requireSpacePrivateStorage(any()) } returns mockk()
        coEvery { keyCache.deleteInfoAndFile(any()) } just Runs
    }

    @AfterEach
    fun teardown() {
        testDir.deleteRecursively()
    }

    class TestSyncTool(
        keyCache: KeyCacheRepository,
        deviceStorage: DeviceStorage
    ) : BaseKeyPackageSyncTool(
        keyCache = keyCache,
        deviceStorage = deviceStorage,
        "tag"
    ) {
        fun findStaleData(keys: List<CachedKey>, available: List<LocationData>): List<CachedKey> =
            keys.findStaleData(available)
    }

    fun createInstance() = TestSyncTool(
        keyCache = keyCache,
        deviceStorage = deviceStorage
    )

    @Test
    fun `revoke keys based on ETags and return true if something happened`() = runTest {
        val invalidatedDay = mockk<KeyDownloadConfig.RevokedKeyPackage>().apply {
            every { etag } returns "etag-badday"
        }
        val invalidatedHour = mockk<KeyDownloadConfig.RevokedKeyPackage>().apply {
            every { etag } returns "etag-badhour"
        }

        val badDayInfo = mockk<CachedKeyInfo>().apply {
            every { etag } returns "etag-badday"
        }
        val badDay = mockk<CachedKey>().apply {
            every { info } returns badDayInfo
        }
        val goodDay = mockk<CachedKey>().apply {
            every { info } returns mockk<CachedKeyInfo>().apply {
                every { etag } returns "etag-goodday"
            }
        }

        val badHourInfo = mockk<CachedKeyInfo>().apply {
            every { etag } returns "etag-badhour"
        }
        val badHour = mockk<CachedKey>().apply {
            every { info } returns badHourInfo
        }
        val goodHour = mockk<CachedKey>().apply {
            every { info } returns mockk<CachedKeyInfo>().apply {
                every { etag } returns "etag-goodhour"
            }
        }

        coEvery { keyCache.getAllCachedKeys() } returns listOf(badDay, goodDay, badHour, goodHour)

        val instance = createInstance()
        instance.revokeCachedKeys(listOf(invalidatedDay, invalidatedHour)) shouldBe true

        coEvery { keyCache.getAllCachedKeys() } returns emptyList()
        instance.revokeCachedKeys(listOf(invalidatedDay, invalidatedHour)) shouldBe false

        instance.revokeCachedKeys(emptyList()) shouldBe false

        coVerify { keyCache.deleteInfoAndFile(listOf(badDayInfo, badHourInfo)) }
    }

    @Test
    fun `filtering out stale day data`() {
        val staleKey = CachedKey(
            info = CachedKeyInfo(
                type = CachedKeyInfo.Type.LOCATION_DAY,
                location = "EUR".loc,
                day = "2020-09-01".day,
                hour = null,
                createdAt = Instant.EPOCH
            ),
            path = File("")
        )

        val freshKey = CachedKey(
            info = CachedKeyInfo(
                type = CachedKeyInfo.Type.LOCATION_DAY,
                location = "EUR".loc,
                day = "2020-09-02".day,
                hour = null,
                createdAt = Instant.EPOCH
            ),
            path = File("")
        )

        val availableCountryDay = LocationDays(
            LocationCode("EUR"),
            listOf("2020-09-02".day)
        )

        val toFilter = listOf(staleKey, freshKey)
        val availableData = listOf(availableCountryDay)

        val instance = createInstance()
        instance.findStaleData(toFilter, availableData) shouldBe listOf(staleKey)
    }

    @Test
    fun `filtering out stale hour data`() {
        val staleHour = CachedKey(
            info = CachedKeyInfo(
                type = CachedKeyInfo.Type.LOCATION_HOUR,
                location = "EUR".loc,
                day = "2020-09-01".day,
                hour = "01:00".hour,
                createdAt = Instant.EPOCH
            ),
            path = File("")
        )
        val freshHour = CachedKey(
            info = CachedKeyInfo(
                type = CachedKeyInfo.Type.LOCATION_HOUR,
                location = "EUR".loc,
                day = "2020-09-02".day,
                hour = "02:00".hour,
                createdAt = Instant.EPOCH
            ),
            path = File("")
        )
        val availableCountryDay = LocationHours(
            LocationCode("EUR"),
            mapOf("2020-09-02".day to listOf("02:00".hour))
        )

        val toFilter = listOf(freshHour, staleHour)
        val availableData = listOf(availableCountryDay)

        val instance = createInstance()
        instance.findStaleData(toFilter, availableData) shouldBe listOf(staleHour)
    }

    @Test
    fun `filtering out stale mixed data`() {
        val staleHour = CachedKey(
            info = CachedKeyInfo(
                type = CachedKeyInfo.Type.LOCATION_HOUR,
                location = "EUR".loc,
                day = "2020-09-01".day,
                hour = "01:00".hour,
                createdAt = Instant.EPOCH
            ),
            path = File("")
        )
        val staleHourReplacedByDay = CachedKey(
            info = CachedKeyInfo(
                type = CachedKeyInfo.Type.LOCATION_HOUR,
                location = "EUR".loc,
                day = "2020-09-02".day,
                hour = "01:00".hour,
                createdAt = Instant.EPOCH
            ),
            path = File("")
        )
        val freshHour = CachedKey(
            info = CachedKeyInfo(
                type = CachedKeyInfo.Type.LOCATION_HOUR,
                location = "EUR".loc,
                day = "2020-09-01".day,
                hour = "02:00".hour,
                createdAt = Instant.EPOCH
            ),
            path = File("")
        )
        val availableHour = LocationHours(
            LocationCode("EUR"),
            mapOf(
                "2020-09-01".day to listOf("02:00".hour),
                "2020-09-02".day to listOf("01:00".hour)
            )
        )

        val staleDay = CachedKey(
            info = CachedKeyInfo(
                type = CachedKeyInfo.Type.LOCATION_DAY,
                location = "EUR".loc,
                day = "2020-09-01".day,
                hour = null,
                createdAt = Instant.EPOCH
            ),
            path = File("")
        )
        val freshDay = CachedKey(
            info = CachedKeyInfo(
                type = CachedKeyInfo.Type.LOCATION_DAY,
                location = "EUR".loc,
                day = "2020-09-02".day,
                hour = null,
                createdAt = Instant.EPOCH
            ),
            path = File("")
        )
        val availableDay = LocationDays(
            LocationCode("EUR"),
            listOf("2020-09-02".day)
        )

        val toFilter = listOf(freshDay, staleDay, freshHour, staleHour, staleHourReplacedByDay)
        val availableData = listOf(availableDay, availableHour)

        val instance = createInstance()
        instance.findStaleData(toFilter, availableData) shouldBe listOf(staleDay, staleHour, staleHourReplacedByDay)
    }

    @Test
    fun `required storage check`() = runTest {
        val instance = createInstance()
        val countryDay = mockk<LocationDays>().apply {
            every { approximateSizeInBytes } returns 9000L
        }
        val countryHour = mockk<LocationHours>().apply {
            every { approximateSizeInBytes } returns 1337L
        }
        instance.requireStorageSpace(listOf(countryDay, countryHour))

        coVerify { deviceStorage.requireSpacePrivateStorage(10337L) }
    }

    @Test
    fun `getting completed or checked keys`() = runTest {
        // incomplete -> no delta
        val key1 = mockk<CachedKey>().apply {
            every { info } returns mockk<CachedKeyInfo>().apply {
                every { isDownloadComplete } returns false
                every { checkedForExposures } returns false
                every { location } returns LocationCode("EUR")
            }
            every { path } returns mockk<File>().apply { every { exists() } returns true }
        }
        // delta
        val key2 = mockk<CachedKey>().apply {
            every { info } returns mockk<CachedKeyInfo>().apply {
                every { isDownloadComplete } returns true
                every { checkedForExposures } returns true
                every { location } returns LocationCode("EUR")
            }
            every { path } returns mockk<File>().apply { every { exists() } returns false }
        }

        // delta -> not checked but existing
        val key3 = mockk<CachedKey>().apply {
            every { info } returns mockk<CachedKeyInfo>().apply {
                every { isDownloadComplete } returns true
                every { checkedForExposures } returns false
                every { location } returns LocationCode("EUR")
            }
            every { path } returns mockk<File>().apply { every { exists() } returns true }
        }

        //  DE location
        val key4 = mockk<CachedKey>().apply {
            every { info } returns mockk<CachedKeyInfo>().apply {
                every { isDownloadComplete } returns true
                every { checkedForExposures } returns true
                every { location } returns LocationCode("DE")
            }
            every { path } returns mockk<File>().apply { every { exists() } returns true }
        }
        coEvery { keyCache.getEntriesForType(any()) } returns listOf(key1, key2, key3, key4)

        val instance = createInstance()
        instance.getCachedKeys(
            LocationCode("EUR"),
            CachedKeyInfo.Type.LOCATION_DAY
        ) shouldBe listOf(key2, key3)
        coVerify { keyCache.getEntriesForType(CachedKeyInfo.Type.LOCATION_DAY) }

        instance.getCachedKeys(
            LocationCode("EUR"),
            CachedKeyInfo.Type.LOCATION_HOUR
        ) shouldBe listOf(key2, key3)
        coVerify { keyCache.getEntriesForType(CachedKeyInfo.Type.LOCATION_HOUR) }

        instance.getCachedKeys(
            LocationCode("DE"),
            CachedKeyInfo.Type.LOCATION_HOUR
        ) shouldBe listOf(key4)
        coVerify { keyCache.getEntriesForType(CachedKeyInfo.Type.LOCATION_HOUR) }
    }
}
