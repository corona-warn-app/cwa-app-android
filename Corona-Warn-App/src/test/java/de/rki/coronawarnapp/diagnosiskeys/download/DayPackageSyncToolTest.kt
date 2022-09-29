package de.rki.coronawarnapp.diagnosiskeys.download

import de.rki.coronawarnapp.appconfig.mapping.RevokedKeyPackage
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo.Type
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.TestDispatcherProvider
import java.io.IOException
import java.time.Instant

class DayPackageSyncToolTest : CommonSyncToolTest() {

    @BeforeEach
    override fun setup() {
        super.setup()

        every { downloadConfig.revokedDayPackages } returns emptyList()
    }

    @AfterEach
    override fun teardown() {
        super.teardown()
    }

    fun createInstance() = DayPackageSyncTool(
        deviceStorage = deviceStorage,
        keyServer = keyServer,
        keyCache = keyCache,
        downloadTool = downloadTool,
        timeStamper = timeStamper,
        dispatcherProvider = TestDispatcherProvider(),
        configProvider = configProvider
    )

    @Test
    fun `successful sync`() = runTest {
        // Today is the 4th
        mockCachedDay("EUR".loc, "2020-01-01".day)

        val instance = createInstance()
        instance.syncMissingDayPackages(listOf("EUR".loc), false) shouldBe BaseKeyPackageSyncTool.SyncResult(
            successful = true,
            newPackages = keyRepoData.values.filterNot { it.info.day == "2020-01-01".day }
        )

        coVerifySequence {
            configProvider.getAppConfig()
            keyCache.getEntriesForType(Type.LOCATION_DAY)
            timeStamper.nowUTC
            keyServer.getDayIndex("EUR".loc)
            keyCache.createCacheEntry(Type.LOCATION_DAY, "EUR".loc, "2020-01-02".day, null)
            downloadTool.downloadKeyFile(any(), downloadConfig)
            keyCache.createCacheEntry(Type.LOCATION_DAY, "EUR".loc, "2020-01-03".day, null)
            downloadTool.downloadKeyFile(any(), downloadConfig)
        }
    }

    @Test
    fun `determine missing days checks EXPECT NEW DAYS`() = runTest {
        mockCachedDay("EUR".loc, "2020-01-01".day)
        mockCachedDay("EUR".loc, "2020-01-02".day)

        val instance = createInstance()

        every { timeStamper.nowUTC } returns Instant.parse("2020-01-03T12:12:12.000Z")
        instance.determineMissingDayPackages("EUR".loc, false) shouldBe null
        every { timeStamper.nowUTC } returns Instant.parse("2020-01-04T12:12:12.000Z")
        instance.determineMissingDayPackages("EUR".loc, false) shouldBe LocationDays(
            location = "EUR".loc,
            dayData = listOf("2020-01-03".day)
        )
    }

    @Test
    fun `determine missing days with forcesync ignores EXPECT NEW DAYS`() = runTest {
        mockCachedDay("EUR".loc, "2020-01-01".day)
        mockCachedDay("EUR".loc, "2020-01-02".day)

        val instance = createInstance()

        every { timeStamper.nowUTC } returns Instant.parse("2020-01-02T12:12:12.000Z")
        instance.determineMissingDayPackages("EUR".loc, true) shouldBe LocationDays(
            location = "EUR".loc,
            dayData = listOf("2020-01-03".day)
        )
    }

    @Test
    fun `EXPECT_NEW_DAY_PACKAGES evaluation`() = runTest {
        val cachedKey1 = mockCachedDay("EUR".loc, "2020-10-30".day)
        val cachedKey2 = mockCachedDay("EUR".loc, "2020-10-31".day)

        val instance = createInstance()

        every { timeStamper.nowUTC } returns Instant.parse("2020-11-01T01:02:03.000Z")
        instance.expectNewDayPackages(listOf(cachedKey1)) shouldBe true
        instance.expectNewDayPackages(listOf(cachedKey1, cachedKey2)) shouldBe false

        every { timeStamper.nowUTC } returns Instant.parse("2020-10-31T01:02:03.000Z")
        instance.expectNewDayPackages(listOf(cachedKey1, cachedKey2)) shouldBe true
    }

    @Test
    fun `download errors do not abort the whole sync`() = runTest {
        var counter = 0
        coEvery { downloadTool.downloadKeyFile(any(), any()) } answers {
            if (++counter == 2) throw IOException()
            arg(0)
        }

        val instance = createInstance()
        instance.syncMissingDayPackages(listOf("EUR".loc), false) shouldBe BaseKeyPackageSyncTool.SyncResult(
            successful = false,
            newPackages = keyRepoData.values.filterNot { it.info.day == "2020-01-02".day }
        )

        coVerifySequence {
            configProvider.getAppConfig()
            keyCache.getEntriesForType(Type.LOCATION_DAY)
            timeStamper.nowUTC
            keyServer.getDayIndex("EUR".loc)
            keyCache.createCacheEntry(Type.LOCATION_DAY, "EUR".loc, "2020-01-01".day, null)
            downloadTool.downloadKeyFile(any(), downloadConfig)
            keyCache.createCacheEntry(Type.LOCATION_DAY, "EUR".loc, "2020-01-02".day, null)
            downloadTool.downloadKeyFile(any(), downloadConfig)
            keyCache.createCacheEntry(Type.LOCATION_DAY, "EUR".loc, "2020-01-03".day, null)
            downloadTool.downloadKeyFile(any(), downloadConfig)
        }
    }

    @Test
    fun `app config can invalidate cached days`() = runTest {
        mockCachedDay("EUR".loc, "2020-01-01".day)
        mockCachedDay("EUR".loc, "2020-01-02".day)
        val invalidDay = mockCachedDay("EUR".loc, "2020-01-03".day)

        every { downloadConfig.revokedDayPackages } returns listOf(
            RevokedKeyPackage.Day(
                day = invalidDay.info.day,
                region = invalidDay.info.location,
                etag = invalidDay.info.etag!!
            )
        )

        val instance = createInstance()
        instance.syncMissingDayPackages(listOf("EUR".loc), false) shouldBe BaseKeyPackageSyncTool.SyncResult(
            successful = true,
            newPackages = keyRepoData.values.filter { it.info.day == "2020-01-03".day }
        )

        coVerifySequence {
            configProvider.getAppConfig()

            keyCache.getAllCachedKeys()
            keyCache.deleteInfoAndFile(listOf(invalidDay.info))

            keyCache.getEntriesForType(Type.LOCATION_DAY)
            keyServer.getDayIndex("EUR".loc)

            keyCache.createCacheEntry(Type.LOCATION_DAY, "EUR".loc, "2020-01-03".day, null)
            downloadTool.downloadKeyFile(any(), downloadConfig)
        }
    }

    @Test
    fun `if keys were revoked skip the EXPECT packages check`() = runTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-01-04T12:12:12.000Z")
        mockCachedDay("EUR".loc, "2020-01-01".day)
        mockCachedDay("EUR".loc, "2020-01-02".day)
        mockCachedDay("EUR".loc, "2020-01-03".day).apply {
            every { downloadConfig.revokedDayPackages } returns listOf(
                RevokedKeyPackage.Day(
                    day = info.day,
                    region = info.location,
                    etag = info.etag!!
                )
            )
        }

        createInstance().syncMissingDayPackages(listOf("EUR".loc), false)

        coVerify(exactly = 1) { keyServer.getDayIndex("EUR".loc) }
    }

    @Test
    fun `if force-sync is set we skip the EXPECT packages check`() = runTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-01-04T12:12:12.000Z")
        mockCachedDay("EUR".loc, "2020-01-01".day)
        mockCachedDay("EUR".loc, "2020-01-02".day)
        mockCachedDay("EUR".loc, "2020-01-03".day)
        createInstance().syncMissingDayPackages(listOf("EUR".loc), true)

        coVerify(exactly = 1) { keyServer.getDayIndex("EUR".loc) }
    }

    @Test
    fun `if neither force-sync is set and keys were revoked we check EXPECT NEW PKGS`() = runTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-01-04T12:12:12.000Z")
        mockCachedDay("EUR".loc, "2020-01-01".day)
        mockCachedDay("EUR".loc, "2020-01-02".day)
        mockCachedDay("EUR".loc, "2020-01-03".day)
        createInstance().syncMissingDayPackages(listOf("EUR".loc), false)

        coVerify(exactly = 0) { keyServer.getDayIndex("EUR".loc) }
    }
}
