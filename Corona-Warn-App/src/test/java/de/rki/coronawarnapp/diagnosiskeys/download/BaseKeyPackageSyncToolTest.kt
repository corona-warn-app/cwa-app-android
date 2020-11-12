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
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

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
        coEvery { keyCache.delete(any()) } just Runs
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
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
    fun `revoke keys based on ETags and return true if something happened`() = runBlockingTest {
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

        coVerify { keyCache.delete(listOf(badDayInfo, badHourInfo)) }
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
                hour = "01".hour,
                createdAt = Instant.EPOCH
            ),
            path = File("")
        )
        val freshHour = CachedKey(
            info = CachedKeyInfo(
                type = CachedKeyInfo.Type.LOCATION_HOUR,
                location = "EUR".loc,
                day = "2020-09-02".day,
                hour = "02".hour,
                createdAt = Instant.EPOCH
            ),
            path = File("")
        )
        val availableCountryDay = LocationHours(
            LocationCode("EUR"),
            mapOf("2020-09-02".day to listOf("02".hour))
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
                hour = "01".hour,
                createdAt = Instant.EPOCH
            ),
            path = File("")
        )
        val staleHourReplacedByDay = CachedKey(
            info = CachedKeyInfo(
                type = CachedKeyInfo.Type.LOCATION_HOUR,
                location = "EUR".loc,
                day = "2020-09-02".day,
                hour = "01".hour,
                createdAt = Instant.EPOCH
            ),
            path = File("")
        )
        val freshHour = CachedKey(
            info = CachedKeyInfo(
                type = CachedKeyInfo.Type.LOCATION_HOUR,
                location = "EUR".loc,
                day = "2020-09-01".day,
                hour = "02".hour,
                createdAt = Instant.EPOCH
            ),
            path = File("")
        )
        val availableHour = LocationHours(
            LocationCode("EUR"),
            mapOf(
                "2020-09-01".day to listOf("02".hour),
                "2020-09-02".day to listOf("01".hour)
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
    fun `required storage check`() = runBlockingTest {
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
    fun `getting completed keys`() = runBlockingTest {
        val key1 = mockk<CachedKey>().apply {
            every { info } returns mockk<CachedKeyInfo>().apply {
                every { isDownloadComplete } returns false
                every { location } returns LocationCode("EUR")
            }
            every { path } returns mockk<File>().apply { every { exists() } returns true }
        }
        val key2 = mockk<CachedKey>().apply {
            every { info } returns mockk<CachedKeyInfo>().apply {
                every { isDownloadComplete } returns true
                every { location } returns LocationCode("EUR")
            }
            every { path } returns mockk<File>().apply { every { exists() } returns false }
        }
        val key3 = mockk<CachedKey>().apply {
            every { info } returns mockk<CachedKeyInfo>().apply {
                every { isDownloadComplete } returns true
                every { location } returns LocationCode("EUR")
            }
            every { path } returns mockk<File>().apply { every { exists() } returns true }
        }
        val key4 = mockk<CachedKey>().apply {
            every { info } returns mockk<CachedKeyInfo>().apply {
                every { isDownloadComplete } returns true
                every { location } returns LocationCode("DE")
            }
            every { path } returns mockk<File>().apply { every { exists() } returns true }
        }
        coEvery { keyCache.getEntriesForType(any()) } returns listOf(key1, key2, key3, key4)

        val instance = createInstance()
        instance.getDownloadedCachedKeys(
            LocationCode("EUR"),
            CachedKeyInfo.Type.LOCATION_DAY
        ) shouldBe listOf(key3)
        coVerify { keyCache.getEntriesForType(CachedKeyInfo.Type.LOCATION_DAY) }

        instance.getDownloadedCachedKeys(
            LocationCode("EUR"),
            CachedKeyInfo.Type.LOCATION_HOUR
        ) shouldBe listOf(key3)
        coVerify { keyCache.getEntriesForType(CachedKeyInfo.Type.LOCATION_HOUR) }
    }
}
