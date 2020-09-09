package de.rki.coronawarnapp.diagnosiskeys.storage.legacy

import android.content.Context
import android.database.SQLException
import dagger.Lazy
import de.rki.coronawarnapp.util.HashExtensions.hashToMD5
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File
import java.io.IOException

class LegacyKeyCacheMigrationTest : BaseIOTest() {

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var timeStamper: TimeStamper

    @MockK
    lateinit var legacyDao: KeyCacheLegacyDao

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)
    private val legacyDir = File(testDir, "key-export")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true

        every { context.cacheDir } returns testDir
        every { timeStamper.nowUTC } returns Instant.EPOCH

        coEvery { legacyDao.clear() } returns Unit
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    private fun createTool() = LegacyKeyCacheMigration(
        context = context,
        legacyDao = Lazy { legacyDao },
        timeStamper = timeStamper
    )

    @Test
    fun `nothing happens on null checksum`() {
        val tool = createTool()
        runBlocking {
            tool.tryMigration(null, File(testDir, "something"))
        }

        coVerify(exactly = 0) { legacyDao.clear() }
    }

    @Test
    fun `migrate a file successfully`() {
        val legacyFile1 = File(legacyDir, "1234.zip")
        legacyFile1.parentFile!!.mkdirs()
        legacyFile1.writeText("testdata")
        legacyFile1.exists() shouldBe true

        val legacyFile1MD5 = legacyFile1.hashToMD5()
        legacyFile1MD5.isNotEmpty() shouldBe true

        val migrationTarget = File(testDir, "migratedkey.zip")

        val tool = createTool()
        runBlocking {
            tool.tryMigration(legacyFile1MD5, migrationTarget)
        }

        legacyFile1.exists() shouldBe false
        migrationTarget.exists() shouldBe true
        migrationTarget.hashToMD5() shouldBe legacyFile1MD5

        coVerify(exactly = 1) { legacyDao.clear() }
    }

    @Test
    fun `migrating a single file fails gracefully`() {
        val legacyFile1 = File(legacyDir, "1234.zip")
        legacyFile1.parentFile!!.mkdirs()
        legacyFile1.writeText("testdata")
        legacyFile1.exists() shouldBe true

        val legacyFile1MD5 = legacyFile1.hashToMD5()
        legacyFile1MD5.isNotEmpty() shouldBe true

        val migrationTarget = mockk<File>()
        every { migrationTarget.path } throws IOException()

        val tool = createTool()
        runBlocking {
            tool.tryMigration(legacyFile1MD5, migrationTarget)
        }

        legacyFile1.exists() shouldBe false

        coVerify(exactly = 1) { legacyDao.clear() }
    }

    @Test
    fun `legacy app database can crash, we don't care`() {
        val legacyFile1 = File(legacyDir, "1234.zip")
        legacyFile1.parentFile!!.mkdirs()
        legacyFile1.writeText("testdata")
        legacyFile1.exists() shouldBe true

        val legacyFile1MD5 = legacyFile1.hashToMD5()
        legacyFile1MD5.isNotEmpty() shouldBe true

        val migrationTarget = File(testDir, "migratedkey.zip")

        coEvery { legacyDao.clear() } throws SQLException()

        val tool = createTool()
        runBlocking {
            tool.tryMigration(legacyFile1MD5, migrationTarget)
        }

        legacyFile1.exists() shouldBe false
        migrationTarget.exists() shouldBe true
        migrationTarget.hashToMD5() shouldBe legacyFile1MD5

        coVerify(exactly = 1) { legacyDao.clear() }
    }

    @Test
    fun `init failure causes legacy cache to be cleared`() {
        val legacyFile1 = File(legacyDir, "1234.zip")
        legacyFile1.parentFile!!.mkdirs()
        legacyFile1.writeText("testdata")

        val legacyFile1MD5 = legacyFile1.hashToMD5()
        legacyFile1MD5.isNotEmpty() shouldBe true

        legacyFile1.setReadable(false)

        val migrationTarget = File(testDir, "migratedkey.zip")

        val tool = createTool()
        runBlocking {
            tool.tryMigration(legacyFile1MD5, migrationTarget)
        }

        legacyFile1.exists() shouldBe false
        migrationTarget.exists() shouldBe false
    }

    @Test
    fun `stale legacy files (older than 15 days) are cleaned up on init`() {
        val legacyFile1 = File(legacyDir, "1234.zip")
        legacyFile1.parentFile!!.mkdirs()
        legacyFile1.writeText("testdata")

        val legacyFile1MD5 = legacyFile1.hashToMD5()
        legacyFile1MD5.isNotEmpty() shouldBe true

        every { timeStamper.nowUTC } returns Instant.ofEpochMilli(legacyFile1.lastModified())
            .plus(Duration.standardDays(16))

        val migrationTarget = File(testDir, "migratedkey.zip")

        coEvery { legacyDao.clear() } throws SQLException()

        val tool = createTool()
        runBlocking {
            tool.tryMigration(legacyFile1MD5, migrationTarget)
        }

        legacyFile1.exists() shouldBe false
        migrationTarget.exists() shouldBe false
    }
}
