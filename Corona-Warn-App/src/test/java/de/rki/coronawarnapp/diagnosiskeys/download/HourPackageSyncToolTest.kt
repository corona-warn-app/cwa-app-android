package de.rki.coronawarnapp.diagnosiskeys.download

import de.rki.coronawarnapp.appconfig.mapping.RevokedKeyPackage
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKey
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo.Type
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.TestDispatcherProvider
import java.io.IOException

class HourPackageSyncToolTest : CommonSyncToolTest() {

    @BeforeEach
    override fun setup() {
        super.setup()

        every { downloadConfig.revokedHourPackages } returns emptyList()
    }

    @AfterEach
    override fun teardown() {
        super.teardown()
    }

    fun createInstance() = HourPackageSyncTool(
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
        instance.syncMissingHourPackages(listOf("EUR".loc), false) shouldBe BaseKeyPackageSyncTool.SyncResult(
            successful = true,
            newPackages = keyRepoData.values.filter { it.info.type == Type.LOCATION_HOUR && it.info.hour != "01:00".hour }
        )

        coVerifySequence {
            configProvider.getAppConfig()
            keyCache.getEntriesForType(Type.LOCATION_HOUR) // Get all cached hours
            timeStamper.nowUTC // Timestamp for `expectNewHourPackages` and server index
            keyServer.getHourIndex("EUR".loc, "2020-01-04".day)

            keyCache.getEntriesForType(Type.LOCATION_DAY) // Which hours are covered by days already

            keyCache.delete(listOf(staleHour.info))

            keyCache.createCacheEntry(Type.LOCATION_HOUR, "EUR".loc, "2020-01-04".day, "00:00".hour)
            downloadTool.downloadKeyFile(any(), downloadConfig)
            keyCache.createCacheEntry(Type.LOCATION_HOUR, "EUR".loc, "2020-01-04".day, "02:00".hour)
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

        every { downloadConfig.revokedHourPackages } returns listOf(
            RevokedKeyPackage.Hour(
                day = invalidHour.info.day,
                hour = invalidHour.info.hour!!,
                region = invalidHour.info.location,
                etag = invalidHour.info.etag!!
            )
        )

        val instance = createInstance()
        instance.syncMissingHourPackages(listOf("EUR".loc), false) shouldBe BaseKeyPackageSyncTool.SyncResult(
            successful = true,
            newPackages = keyRepoData.values.filter { it.info.type == Type.LOCATION_HOUR && it.info.hour != "01:00".hour }
        )

        coVerifySequence {
            configProvider.getAppConfig()

            keyCache.getAllCachedKeys()
            keyCache.delete(listOf(invalidHour.info))

            keyCache.getEntriesForType(Type.LOCATION_HOUR) // Get all cached hours
            timeStamper.nowUTC // Timestamp for `expectNewHourPackages` and server index
            keyServer.getHourIndex("EUR".loc, "2020-01-04".day)

            keyCache.getEntriesForType(Type.LOCATION_DAY) // Which hours are covered by days already

            keyCache.createCacheEntry(Type.LOCATION_HOUR, "EUR".loc, "2020-01-04".day, "00:00".hour)
            downloadTool.downloadKeyFile(any(), downloadConfig)
            keyCache.createCacheEntry(Type.LOCATION_HOUR, "EUR".loc, "2020-01-04".day, "02:00".hour)
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
    fun `determine missing hours with forcesync ignores EXPECT NEW HOURS`() = runBlockingTest {
        mockCachedHour("EUR".loc, "2020-01-04".day, "00:00".hour)
        mockCachedHour("EUR".loc, "2020-01-04".day, "01:00".hour)

        val instance = createInstance()

        every { timeStamper.nowUTC } returns Instant.parse("2020-01-04T02:00:00.000Z")
        instance.determineMissingHours("EUR".loc, forceIndexLookup = true) shouldBe LocationHours(
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
        instance.syncMissingHourPackages(listOf("EUR".loc), false) shouldBe BaseKeyPackageSyncTool.SyncResult(
            successful = false,
            newPackages = keyRepoData.values.filter { it.info.type == Type.LOCATION_HOUR && it.info.hour != "01:00".hour }
        )

        coVerifySequence {
            configProvider.getAppConfig()
            keyCache.getEntriesForType(Type.LOCATION_HOUR)
            timeStamper.nowUTC
            keyServer.getHourIndex("EUR".loc, "2020-01-04".day)

            keyCache.getEntriesForType(Type.LOCATION_DAY)

            keyCache.createCacheEntry(Type.LOCATION_HOUR, "EUR".loc, "2020-01-04".day, "00:00".hour)
            downloadTool.downloadKeyFile(any(), downloadConfig)
            keyCache.createCacheEntry(Type.LOCATION_HOUR, "EUR".loc, "2020-01-04".day, "01:00".hour)
            downloadTool.downloadKeyFile(any(), downloadConfig)
            keyCache.createCacheEntry(Type.LOCATION_HOUR, "EUR".loc, "2020-01-04".day, "02:00".hour)
            downloadTool.downloadKeyFile(any(), downloadConfig)
        }
    }

    @Test
    fun `EXPECT_NEW_HOUR_PACKAGES evaluation`() = runBlockingTest {
        val cachedKey1 = mockk<CachedKey>().apply {
            every { info } returns mockk<CachedKeyInfo>().apply {
                every { toDateTime() } returns Instant.parse("2020-01-01T00:00:03.000Z").toDateTime(DateTimeZone.UTC)
            }
        }
        val cachedKey2 = mockk<CachedKey>().apply {
            every { info } returns mockk<CachedKeyInfo>().apply {
                every { toDateTime() } returns Instant.parse("2020-01-01T01:00:03.000Z").toDateTime(DateTimeZone.UTC)
            }
        }

        val instance = createInstance()

        var now = Instant.parse("2020-01-01T02:00:03.000Z")
        instance.expectNewHourPackages(listOf(cachedKey1), now) shouldBe true
        instance.expectNewHourPackages(listOf(cachedKey1, cachedKey2), now) shouldBe false

        now = Instant.parse("2020-01-01T03:00:03.000Z")
        instance.expectNewHourPackages(listOf(cachedKey1, cachedKey2), now) shouldBe true
    }

    @Test
    fun `EXPECT_NEW_HOUR_PACKAGES does not get confused by same hour on next day`() = runBlockingTest {
        val cachedKey1 = mockk<CachedKey>().apply {
            every { info } returns mockk<CachedKeyInfo>().apply {
                every { toDateTime() } returns Instant.parse("2020-01-01T00:00:03.000Z").toDateTime(DateTimeZone.UTC)
            }
        }

        val instance = createInstance()

        val now = Instant.parse("2020-01-02T01:00:03.000Z")
        instance.expectNewHourPackages(listOf(cachedKey1), now) shouldBe true
    }

    @Test
    fun `if keys were revoked skip the EXPECT packages check`() = runBlockingTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-01-04T02:00:00.000Z")
        mockCachedHour("EUR".loc, "2020-01-04".day, "00:00".hour)
        mockCachedHour("EUR".loc, "2020-01-04".day, "01:00".hour)
        mockCachedHour("EUR".loc, "2020-01-04".day, "02:00".hour).apply {
            every { downloadConfig.revokedHourPackages } returns listOf(
                RevokedKeyPackage.Hour(
                    region = info.location,
                    etag = info.etag!!,
                    day = info.day,
                    hour = info.hour!!
                )
            )
        }

        createInstance().syncMissingHourPackages(listOf("EUR".loc), false)

        coVerify(exactly = 1) { keyServer.getHourIndex("EUR".loc, "2020-01-04".day) }
    }

    @Test
    fun `if force-sync is set we skip the EXPECT packages check`() = runBlockingTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-01-04T02:00:00.000Z")
        mockCachedHour("EUR".loc, "2020-01-04".day, "00:00".hour)
        mockCachedHour("EUR".loc, "2020-01-04".day, "01:00".hour)
        createInstance().syncMissingHourPackages(listOf("EUR".loc), true)

        coVerify(exactly = 1) { keyServer.getHourIndex("EUR".loc, "2020-01-04".day) }
    }

    @Test
    fun `if neither force-sync is set and keys were revoked we check EXPECT NEW PKGS`() = runBlockingTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-01-04T02:00:00.000Z")
        mockCachedHour("EUR".loc, "2020-01-04".day, "00:00".hour)
        mockCachedHour("EUR".loc, "2020-01-04".day, "01:00".hour)
        createInstance().syncMissingHourPackages(listOf("EUR".loc), false)

        coVerify(exactly = 0) { keyServer.getHourIndex("EUR".loc, "2020-01-04".day) }
    }

    @Test
    fun `network connection time out does not clear the cache and returns an unsuccessful result`() = runBlockingTest {
        coEvery { keyServer.getHourIndex(any(), any()) } throws NetworkConnectTimeoutException()

        val instance = createInstance()
        instance.syncMissingHourPackages(listOf("EUR".loc), false) shouldBe BaseKeyPackageSyncTool.SyncResult(
            successful = false,
            newPackages = emptyList()
        )

        coVerify(exactly = 1) { keyServer.getHourIndex("EUR".loc, "2020-01-04".day) }
        coVerify(exactly = 0) { keyCache.delete(any()) }
    }
}
