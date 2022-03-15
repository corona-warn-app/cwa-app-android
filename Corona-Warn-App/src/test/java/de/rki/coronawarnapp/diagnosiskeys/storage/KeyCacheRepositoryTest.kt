package de.rki.coronawarnapp.diagnosiskeys.storage

import android.content.Context
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
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

    private val cacheDir = File(IO_TEST_BASEDIR)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        cacheDir.mkdirs()
        cacheDir.exists() shouldBe true

        every { timeStamper.nowUTC } returns Instant.EPOCH
        every { context.cacheDir } returns cacheDir

        every { databaseFactory.create() } returns database
        every { database.cachedKeyFiles() } returns keyfileDAO

        coEvery { keyfileDAO.allEntries() } returns flowOf(emptyList())
    }

    @AfterEach
    fun teardown() {
        cacheDir.deleteRecursively()
    }

    private fun createRepo(): KeyCacheRepository = KeyCacheRepository(
        context = context,
        databaseFactory = databaseFactory,
        timeStamper = timeStamper
    )

    @Test
    fun `housekeeping runs before data access`() {
        val lostKeyFile = CachedKeyInfo(
            location = LocationCode("DE"),
            day = LocalDate.parse("2022-03-20"),
            hour = LocalTime.parse("21:00"),
            type = CachedKeyInfo.Type.LOCATION_HOUR,
            createdAt = Instant.parse("2022-03-20T21:59:00Z")
        ).copy(
            isDownloadComplete = true,
            etag = "lostKeyFile"
        )

        val existingKeyFileNotChecked = CachedKeyInfo(
            location = LocationCode("NL"),
            day = LocalDate.parse("2022-03-20"),
            hour = LocalTime.parse("22:00"),
            type = CachedKeyInfo.Type.LOCATION_HOUR,
            createdAt = Instant.parse("2022-03-20T22:59:00Z")
        ).copy(
            isDownloadComplete = true,
            etag = "existingKeyFileNotChecked"
        )

        val existingKeyFileChecked = CachedKeyInfo(
            location = LocationCode("NL"),
            day = LocalDate.parse("2022-03-20"),
            hour = LocalTime.parse("23:00"),
            type = CachedKeyInfo.Type.LOCATION_HOUR,
            createdAt = Instant.parse("2022-03-20T23:59:00Z"),
        ).copy(
            isDownloadComplete = true,
            etag = "existingKeyFileChecked",
            checkedForExposures = true
        )

        val diagnosisDir = File(cacheDir, "diagnosis_keys").apply {
            mkdirs()
        }
        val fileNotChecked = File(diagnosisDir, existingKeyFileNotChecked.fileName).apply {
            createNewFile()
        }

        val fileChecked = File(diagnosisDir, existingKeyFileChecked.fileName).apply {
            createNewFile()
        }

        coEvery { keyfileDAO.allEntries() } returns
            flowOf(listOf(lostKeyFile, existingKeyFileNotChecked, existingKeyFileChecked))
        coEvery { keyfileDAO.updateDownloadState(any()) } returns Unit
        coEvery { keyfileDAO.deleteEntry(lostKeyFile) } returns Unit

        val repo = createRepo()

        coVerify(exactly = 0) { keyfileDAO.updateDownloadState(any()) }

        runBlocking {
            repo.getAllCachedKeys()
            coVerify(exactly = 2) { keyfileDAO.allEntries() }
            coVerify(exactly = 1){ keyfileDAO.deleteEntry(lostKeyFile) }
            coVerify(exactly = 0){ keyfileDAO.deleteEntry(existingKeyFileChecked) }
            coVerify(exactly = 0){ keyfileDAO.deleteEntry(existingKeyFileNotChecked) }
            fileChecked.exists() shouldBe false
            fileNotChecked.exists() shouldBe true
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
                type = CachedKeyInfo.Type.LOCATION_HOUR
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
                type = CachedKeyInfo.Type.LOCATION_HOUR
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
                type = CachedKeyInfo.Type.LOCATION_HOUR
            )

            path.createNewFile() shouldBe true
            path.exists() shouldBe true

            repo.deleteInfoAndFile(listOf(keyFile))

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
            type = CachedKeyInfo.Type.LOCATION_HOUR,
            createdAt = Instant.now()
        )

        coEvery { keyfileDAO.allEntries() } returns flowOf(listOf(keyFileToClear))
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
