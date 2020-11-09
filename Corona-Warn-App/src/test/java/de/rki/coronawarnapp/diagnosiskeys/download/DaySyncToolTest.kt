package de.rki.coronawarnapp.diagnosiskeys.download

import de.rki.coronawarnapp.appconfig.mapping.DownloadConfigMapper
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKey
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.TestDispatcherProvider
import java.io.IOException

class DaySyncToolTest : CommonSyncToolTest() {

    @BeforeEach
    override fun setup() {
        super.setup()

        every { downloadConfig.invalidDayETags } returns emptyList()
    }

    @AfterEach
    override fun teardown() {
        super.teardown()
    }

    fun createInstance() = DaySyncTool(
        deviceStorage = deviceStorage,
        keyServer = keyServer,
        keyCache = keyCache,
        downloadTool = downloadTool,
        timeStamper = timeStamper,
        dispatcherProvider = TestDispatcherProvider,
        configProvider = configProvider
    )

    @Test
    fun `successful sync`() = runBlockingTest {
        // Today is the 4th
        mockCachedDay("EUR".loc, "2020-01-01".day)

        val instance = createInstance()
        instance.syncMissingDays(listOf("EUR".loc), false) shouldBe true

        coVerifySequence {
            configProvider.getAppConfig()
            keyCache.getEntriesForType(CachedKeyInfo.Type.LOCATION_DAY)
            timeStamper.nowUTC
            keyServer.getDayIndex("EUR".loc)
            keyCache.createCacheEntry(CachedKeyInfo.Type.LOCATION_DAY, "EUR".loc, "2020-01-02".day, null)
            downloadTool.downloadKeyFile(any(), downloadConfig)
            keyCache.createCacheEntry(CachedKeyInfo.Type.LOCATION_DAY, "EUR".loc, "2020-01-03".day, null)
            downloadTool.downloadKeyFile(any(), downloadConfig)
        }
    }

    @Test
    fun `determine missing days checks EXPECT NEW DAYS`() = runBlockingTest {
        mockCachedDay("EUR".loc, "2020-01-01".day)
        mockCachedDay("EUR".loc, "2020-01-02".day)

        val instance = createInstance()

        every { timeStamper.nowUTC } returns Instant.parse("2020-01-03T12:12:12.000Z")
        instance.determineMissingDays("EUR".loc, false) shouldBe null
        every { timeStamper.nowUTC } returns Instant.parse("2020-01-04T12:12:12.000Z")
        instance.determineMissingDays("EUR".loc, false) shouldBe LocationDays(
            location = "EUR".loc,
            dayData = listOf("2020-01-03".day)
        )
    }

    @Test
    fun `determine missing days forcesync ignores EXPECT NEW DAYS`() = runBlockingTest {
        mockCachedDay("EUR".loc, "2020-01-01".day)
        mockCachedDay("EUR".loc, "2020-01-02".day)

        val instance = createInstance()

        every { timeStamper.nowUTC } returns Instant.parse("2020-01-02T12:12:12.000Z")
        instance.determineMissingDays("EUR".loc, true) shouldBe LocationDays(
            location = "EUR".loc,
            dayData = listOf("2020-01-03".day)
        )
    }

    @Test
    fun `EXPECT_NEW_DAY_PACKAGES evaluation`() = runBlockingTest {
        val cachedKey1 = mockk<CachedKey>().apply {
            every { info } returns mockk<CachedKeyInfo>().apply {
                every { createdAt } returns Instant.parse("2020-10-30T01:02:03.000Z")
            }

        }
        val cachedKey2 = mockk<CachedKey>().apply {
            every { info } returns mockk<CachedKeyInfo>().apply {
                every { createdAt } returns Instant.parse("2020-10-31T01:02:03.000Z")
            }
        }

        val instance = createInstance()

        every { timeStamper.nowUTC } returns Instant.parse("2020-11-01T01:02:03.000Z")
        instance.expectNewDayPackages(listOf(cachedKey1)) shouldBe true
        instance.expectNewDayPackages(listOf(cachedKey1, cachedKey2)) shouldBe false

        every { timeStamper.nowUTC } returns Instant.parse("2020-10-31T01:02:03.000Z")
        instance.expectNewDayPackages(listOf(cachedKey1, cachedKey2)) shouldBe true
    }

    @Test
    fun `download errors do not abort the whole sync`() = runBlockingTest {
        var counter = 0
        coEvery { downloadTool.downloadKeyFile(any(), any()) } answers {
            if (++counter == 2) throw IOException()
            arg(0)
        }

        val instance = createInstance()
        instance.syncMissingDays(listOf("EUR".loc), false) shouldBe false

        coVerifySequence {
            configProvider.getAppConfig()
            keyCache.getEntriesForType(CachedKeyInfo.Type.LOCATION_DAY)
            timeStamper.nowUTC
            keyServer.getDayIndex("EUR".loc)
            keyCache.createCacheEntry(CachedKeyInfo.Type.LOCATION_DAY, "EUR".loc, "2020-01-01".day, null)
            downloadTool.downloadKeyFile(any(), downloadConfig)
            keyCache.createCacheEntry(CachedKeyInfo.Type.LOCATION_DAY, "EUR".loc, "2020-01-02".day, null)
            downloadTool.downloadKeyFile(any(), downloadConfig)
            keyCache.createCacheEntry(CachedKeyInfo.Type.LOCATION_DAY, "EUR".loc, "2020-01-03".day, null)
            downloadTool.downloadKeyFile(any(), downloadConfig)
        }
    }

    @Test
    fun `app config can invalidate cached days`() = runBlockingTest {
        mockCachedDay("EUR".loc, "2020-01-01".day)
        mockCachedDay("EUR".loc, "2020-01-02".day)
        val invalidDay = mockCachedDay("EUR".loc, "2020-01-03".day)

        every { downloadConfig.invalidDayETags } returns listOf(
            DownloadConfigMapper.InvalidatedKeyFile.Day(
                day = invalidDay.info.day,
                region = invalidDay.info.location,
                etag = invalidDay.info.etag!!
            )
        )

        val instance = createInstance()
        instance.syncMissingDays(listOf("EUR".loc), false) shouldBe true

        coVerifySequence {
            configProvider.getAppConfig()

            keyCache.getAllCachedKeys()
            keyCache.delete(listOf(invalidDay.info))

            keyCache.getEntriesForType(CachedKeyInfo.Type.LOCATION_DAY)
            timeStamper.nowUTC
            keyServer.getDayIndex("EUR".loc)

            keyCache.createCacheEntry(CachedKeyInfo.Type.LOCATION_DAY, "EUR".loc, "2020-01-03".day, null)
            downloadTool.downloadKeyFile(any(), downloadConfig)
        }
    }
}
