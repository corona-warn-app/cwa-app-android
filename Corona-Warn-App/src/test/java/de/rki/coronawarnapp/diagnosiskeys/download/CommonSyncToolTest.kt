package de.rki.coronawarnapp.diagnosiskeys.download

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.diagnosiskeys.server.DiagnosisKeyServer
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKey
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.storage.DeviceStorage
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import testhelpers.BaseIOTest
import timber.log.Timber
import java.io.File

abstract class CommonSyncToolTest : BaseIOTest() {

    @MockK lateinit var deviceStorage: DeviceStorage
    @MockK lateinit var keyCache: KeyCacheRepository
    @MockK lateinit var keyServer: DiagnosisKeyServer
    @MockK lateinit var downloadTool: KeyDownloadTool
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var configProvider: AppConfigProvider

    @MockK lateinit var downloadConfig: ConfigData

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)

    internal val String.loc get() = LocationCode(this)
    internal val String.day get() = LocalDate.parse(this)
    internal val String.hour get() = LocalTime.parse(this)
    val keyRepoData = mutableMapOf<String, CachedKey>()

    @BeforeEach
    open fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true

        coEvery { configProvider.getAppConfig() } returns downloadConfig

        coEvery { keyCache.getEntriesForType(any()) } answers {
            keyRepoData
                .filter { it.value.info.type == arg(0) }
                .map { it.value }
        }

        coEvery { keyServer.getDayIndex(any()) } returns listOf(
            "2020-01-01".day, "2020-01-02".day, "2020-01-03".day
        )
        coEvery { keyServer.getHourIndex(any(), "2020-01-04".day) } returns listOf(
            "00:00".hour, "01:00".hour, "02:00".hour
        )

        every { timeStamper.nowUTC } returns Instant.parse("2020-01-04T03:15:00.000Z")

        coEvery { deviceStorage.requireSpacePrivateStorage(any()) } returns mockk()

        coEvery {
            keyCache.createCacheEntry(CachedKeyInfo.Type.LOCATION_DAY, any(), any(), null)
        } answers {
            mockCachedDay(arg(1), arg(2))
        }
        coEvery {
            keyCache.createCacheEntry(CachedKeyInfo.Type.LOCATION_HOUR, any(), any(), any())
        } answers {
            mockCachedHour(arg(1), arg(2), arg(3))
        }
        coEvery { keyCache.getAllCachedKeys() } answers { keyRepoData.values.toList() }
        coEvery { keyCache.delete(any()) } answers {
            val toDelete: List<CachedKeyInfo> = arg(0)
            toDelete.forEach {
                keyRepoData.remove(it.id)
            }
            Unit
        }

        coEvery { downloadTool.downloadKeyFile(any(), any()) } answers {
            arg(0)
        }
    }

    @AfterEach
    open fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    internal fun mockCachedDay(
        location: LocationCode,
        dayIdentifier: LocalDate,
        isComplete: Boolean = true
    ): CachedKey = mockCacheEntry(
        location, dayIdentifier, null, isComplete
    )

    internal fun mockCachedHour(
        location: LocationCode,
        dayIdentifier: LocalDate,
        hourIdentifier: LocalTime,
        isComplete: Boolean = true
    ): CachedKey = mockCacheEntry(
        location, dayIdentifier, hourIdentifier, isComplete
    )

    private fun mockCacheEntry(
        location: LocationCode,
        dayIdentifier: LocalDate,
        hourIdentifier: LocalTime?,
        isComplete: Boolean = true
    ): CachedKey {
        var keyInfo = CachedKeyInfo(
            type = when (hourIdentifier) {
                null -> CachedKeyInfo.Type.LOCATION_DAY
                else -> CachedKeyInfo.Type.LOCATION_HOUR
            },
            location = location,
            day = dayIdentifier,
            hour = hourIdentifier,
            createdAt = when (hourIdentifier) {
                null -> dayIdentifier.toLocalDateTime(LocalTime.MIDNIGHT).toDateTime(DateTimeZone.UTC).toInstant()
                else -> dayIdentifier.toLocalDateTime(hourIdentifier).toDateTime(DateTimeZone.UTC).toInstant()
            }
        )
        if (isComplete) {
            keyInfo = keyInfo.copy(
                etag = when (hourIdentifier) {
                    null -> "$location-$dayIdentifier"
                    else -> "$location-$dayIdentifier-$hourIdentifier"
                },
                isDownloadComplete = true
            )
        }
        Timber.i("mockKeyCacheCreateEntry(...): %s", keyInfo)
        val file = File(testDir, keyInfo.id)
        file.createNewFile()
        return CachedKey(keyInfo, file).also {
            keyRepoData[it.info.id] = it
        }
    }
}
