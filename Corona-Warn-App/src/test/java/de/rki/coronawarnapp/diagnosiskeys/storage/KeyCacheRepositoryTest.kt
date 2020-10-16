package de.rki.coronawarnapp.diagnosiskeys.storage

import android.content.Context
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class KeyCacheRepositoryTest : BaseIOTest() {
    @MockK
    lateinit var context: Context

    @MockK
    lateinit var timeStamper: TimeStamper

    @MockK
    lateinit var databaseFactory: KeyCacheDatabase.Factory

    @MockK
    lateinit var database: KeyCacheDatabase

    @MockK
    lateinit var keyfileDAO: KeyCacheDatabase.CachedKeyFileDao

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true

        every { timeStamper.nowUTC } returns Instant.EPOCH
        every { context.cacheDir } returns File(testDir, "cache")

        every { databaseFactory.create() } returns database
        every { database.cachedKeyFiles() } returns keyfileDAO

        coEvery { keyfileDAO.getAllEntries() } returns emptyList()
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    private fun createRepo(): KeyCacheRepository = KeyCacheRepository(
        context = context,
        databaseFactory = databaseFactory,
        timeStamper = timeStamper
    )

    @Test
    fun `housekeeping runs before data access`() {
        val lostKey = CachedKeyInfo(
            location = LocationCode("DE"),
            day = LocalDate.now(),
            hour = LocalTime.now(),
            type = CachedKeyInfo.Type.COUNTRY_HOUR,
            createdAt = Instant.now()
        ).copy(
            isDownloadComplete = true,
            checksumMD5 = "checksum"
        )

        val existingKey = CachedKeyInfo(
            location = LocationCode("NL"),
            day = LocalDate.now(),
            hour = LocalTime.now(),
            type = CachedKeyInfo.Type.COUNTRY_HOUR,
            createdAt = Instant.now()
        )

        File(testDir, "diagnosis_keys/${existingKey.id}.zip").apply {
            parentFile!!.mkdirs()
            createNewFile()
        }

        coEvery { keyfileDAO.getAllEntries() } returns listOf(lostKey, existingKey)
        coEvery { keyfileDAO.updateDownloadState(any()) } returns Unit
        coEvery { keyfileDAO.deleteEntry(lostKey) } returns Unit

        val repo = createRepo()

        coVerify(exactly = 0) { keyfileDAO.updateDownloadState(any()) }

        runBlocking {
            repo.getAllCachedKeys()
            coVerify(exactly = 2) { keyfileDAO.getAllEntries() }
            coVerify { keyfileDAO.deleteEntry(lostKey) }
        }
    }

    @Test
    fun `insert and retrieve`() {
        val repo = createRepo()

        coEvery { keyfileDAO.insertEntry(any()) } returns Unit

        runBlocking {
            val (keyFile, path) = repo.createCacheEntry(
                location = LocationCode("NL"),
                dayIdentifier = LocalDate.parse("2020-09-09"),
                hourIdentifier = LocalTime.parse("23:00"),
                type = CachedKeyInfo.Type.COUNTRY_HOUR
            )

            path shouldBe File(context.cacheDir, "diagnosis_keys/${keyFile.id}.zip")

            coVerify { keyfileDAO.insertEntry(keyFile) }
        }
    }

    @Test
    fun `update download state`() {
        val repo = createRepo()

        coEvery { keyfileDAO.insertEntry(any()) } returns Unit
        coEvery { keyfileDAO.updateDownloadState(any()) } returns Unit

        runBlocking {
            val (keyFile, _) = repo.createCacheEntry(
                location = LocationCode("NL"),
                dayIdentifier = LocalDate.parse("2020-09-09"),
                hourIdentifier = LocalTime.parse("23:00"),
                type = CachedKeyInfo.Type.COUNTRY_HOUR
            )

            repo.markKeyComplete(keyFile, "checksum")

            coVerify {
                keyfileDAO.insertEntry(keyFile)
                keyfileDAO.updateDownloadState(keyFile.toDownloadUpdate("checksum"))
            }
        }
    }

    @Test
    fun `delete only selected entries`() {
        val repo = createRepo()

        coEvery { keyfileDAO.insertEntry(any()) } returns Unit
        coEvery { keyfileDAO.deleteEntry(any()) } returns Unit

        runBlocking {
            val (keyFile, path) = repo.createCacheEntry(
                location = LocationCode("NL"),
                dayIdentifier = LocalDate.parse("2020-09-09"),
                hourIdentifier = LocalTime.parse("23:00"),
                type = CachedKeyInfo.Type.COUNTRY_HOUR
            )

            path.createNewFile() shouldBe true
            path.exists() shouldBe true

            repo.delete(listOf(keyFile))

            coVerify { keyfileDAO.deleteEntry(keyFile) }

            path.exists() shouldBe false
        }
    }

    @Test
    fun `clear all files`() {
        val repo = createRepo()

        val keyFileToClear = CachedKeyInfo(
            location = LocationCode("DE"),
            day = LocalDate.now(),
            hour = LocalTime.now(),
            type = CachedKeyInfo.Type.COUNTRY_HOUR,
            createdAt = Instant.now()
        )

        coEvery { keyfileDAO.getAllEntries() } returns listOf(keyFileToClear)
        coEvery { keyfileDAO.deleteEntry(any()) } returns Unit

        val keyFilePath = repo.getPathForKey(keyFileToClear)
        keyFilePath.createNewFile() shouldBe true
        keyFilePath.exists() shouldBe true

        runBlocking {
            repo.clear()

            coVerify { keyfileDAO.deleteEntry(keyFileToClear) }

            keyFilePath.exists() shouldBe false
        }
    }
}
