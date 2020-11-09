package de.rki.coronawarnapp.diagnosiskeys.download

import de.rki.coronawarnapp.appconfig.mapping.DownloadConfigMapper
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.TestDispatcherProvider
import java.io.IOException

class HourSyncToolTest : CommonSyncToolTest() {

    @BeforeEach
    override fun setup() {
        super.setup()

        every { downloadConfig.invalidHourEtags } returns emptyList()
    }

    @AfterEach
    override fun teardown() {
        super.teardown()
    }

    fun createInstance() = HourSyncTool(
        deviceStorage = deviceStorage,
        keyServer = keyServer,
        keyCache = keyCache,
        downloadTool = downloadTool,
        timeStamper = timeStamper,
        configProvider = configProvider,
        dispatcherProvider = TestDispatcherProvider
    )

    @Test
    fun `successful sync`() = runBlockingTest {
        // Today is the 4th, 02:15:00
        mockCachedDay("EUR".loc, "2020-01-01".day)
        mockCachedDay("EUR".loc, "2020-01-02".day)
        mockCachedDay("EUR".loc, "2020-01-03".day)

        val staleHour = mockCachedHour("EUR".loc, "2020-01-03".day, "01:00".hour)
        mockCachedHour("EUR".loc, "2020-01-04".day, "01:00".hour)

        val instance = createInstance()
        instance.syncMissingHours(listOf("EUR".loc), false) shouldBe true

        coVerifySequence {
            configProvider.getAppConfig()
            keyCache.getEntriesForType(CachedKeyInfo.Type.LOCATION_HOUR) // Get all cached hours
            timeStamper.nowUTC // Timestamp for `expectNewHourPackages` and server index
            keyServer.getHourIndex("EUR".loc, "2020-01-04".day)

            keyCache.getEntriesForType(CachedKeyInfo.Type.LOCATION_DAY) // Which hours are covered by days already

            keyCache.delete(listOf(staleHour.info))

            keyCache.createCacheEntry(CachedKeyInfo.Type.LOCATION_HOUR, "EUR".loc, "2020-01-04".day, "00:00".hour)
            downloadTool.downloadKeyFile(any(), downloadConfig)
            keyCache.createCacheEntry(CachedKeyInfo.Type.LOCATION_HOUR, "EUR".loc, "2020-01-04".day, "02:00".hour)
            downloadTool.downloadKeyFile(any(), downloadConfig)
        }
    }

    @Test
    fun `app config can invalidate cached hours`() = runBlockingTest {
        // Today is the 4th, 02:15:00
        mockCachedDay("EUR".loc, "2020-01-01".day)
        mockCachedDay("EUR".loc, "2020-01-02".day)
        mockCachedDay("EUR".loc, "2020-01-03".day)

        val invalidHour = mockCachedHour("EUR".loc, "2020-01-04".day, "00:00".hour)
        mockCachedHour("EUR".loc, "2020-01-04".day, "01:00".hour)

        every { downloadConfig.invalidHourEtags } returns listOf(
            DownloadConfigMapper.InvalidatedKeyFile.Hour(
                day = invalidHour.info.day,
                hour = invalidHour.info.hour!!,
                region = invalidHour.info.location,
                etag = invalidHour.info.etag!!
            )
        )

        val instance = createInstance()
        instance.syncMissingHours(listOf("EUR".loc), false) shouldBe true

        coVerifySequence {
            configProvider.getAppConfig()

            keyCache.getAllCachedKeys()
            keyCache.delete(listOf(invalidHour.info))

            keyCache.getEntriesForType(CachedKeyInfo.Type.LOCATION_HOUR) // Get all cached hours
            timeStamper.nowUTC // Timestamp for `expectNewHourPackages` and server index
            keyServer.getHourIndex("EUR".loc, "2020-01-04".day)

            keyCache.getEntriesForType(CachedKeyInfo.Type.LOCATION_DAY) // Which hours are covered by days already

            keyCache.createCacheEntry(CachedKeyInfo.Type.LOCATION_HOUR, "EUR".loc, "2020-01-04".day, "00:00".hour)
            downloadTool.downloadKeyFile(any(), downloadConfig)
            keyCache.createCacheEntry(CachedKeyInfo.Type.LOCATION_HOUR, "EUR".loc, "2020-01-04".day, "02:00".hour)
            downloadTool.downloadKeyFile(any(), downloadConfig)
        }
    }

    @Test
    fun `determine missing hours checks EXPECT NEW HOURS`() = runBlockingTest {
        mockCachedHour("EUR".loc, "2020-01-04".day, "00:00".hour)
        mockCachedHour("EUR".loc, "2020-01-04".day, "01:00".hour)

        val instance = createInstance()

        every { timeStamper.nowUTC } returns Instant.parse("2020-01-04T02:00:00.000Z")
        instance.determineMissingHours("EUR".loc, false) shouldBe null
        every { timeStamper.nowUTC } returns Instant.parse("2020-01-04T03:00:00.000Z")
        instance.determineMissingHours("EUR".loc, false) shouldBe LocationHours(
            location = "EUR".loc,
            hourData = mapOf("2020-01-04".day to listOf("02:00".hour))
        )
    }

    @Test
    fun `determine missing hours forcesync ignores EXPECT NEW HOURS`() = runBlockingTest {
        mockCachedHour("EUR".loc, "2020-01-04".day, "00:00".hour)
        mockCachedHour("EUR".loc, "2020-01-04".day, "01:00".hour)

        val instance = createInstance()

        every { timeStamper.nowUTC } returns Instant.parse("2020-01-04T02:00:00.000Z")
        instance.determineMissingHours("EUR".loc, true) shouldBe LocationHours(
            location = "EUR".loc,
            hourData = mapOf("2020-01-04".day to listOf("02:00".hour))
        )
    }

    @Test
    fun `download errors do not abort the whole sync`() = runBlockingTest {
        var counter = 0
        coEvery { downloadTool.downloadKeyFile(any(), any()) } answers {
            if (++counter == 2) throw IOException()
            arg(0)
        }

        val instance = createInstance()
        instance.syncMissingHours(listOf("EUR".loc), false) shouldBe false

        coVerifySequence {
            configProvider.getAppConfig()
            keyCache.getEntriesForType(CachedKeyInfo.Type.LOCATION_HOUR)
            timeStamper.nowUTC
            keyServer.getHourIndex("EUR".loc, "2020-01-04".day)

            keyCache.getEntriesForType(CachedKeyInfo.Type.LOCATION_DAY)

            keyCache.createCacheEntry(CachedKeyInfo.Type.LOCATION_HOUR, "EUR".loc, "2020-01-04".day, "00:00".hour)
            downloadTool.downloadKeyFile(any(), downloadConfig)
            keyCache.createCacheEntry(CachedKeyInfo.Type.LOCATION_HOUR, "EUR".loc, "2020-01-04".day, "01:00".hour)
            downloadTool.downloadKeyFile(any(), downloadConfig)
            keyCache.createCacheEntry(CachedKeyInfo.Type.LOCATION_HOUR, "EUR".loc, "2020-01-04".day, "02:00".hour)
            downloadTool.downloadKeyFile(any(), downloadConfig)
        }
    }
}
