package de.rki.coronawarnapp.appconfig.download

import android.content.Context
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class AppConfigStorageTest : BaseIOTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var timeStamper: TimeStamper

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private val privateFiles = File(testDir, "files")
    private val storageDir = File(privateFiles, "appconfig_storage")
    private val configPath = File(storageDir, "appconfig")
    private val testByteArray = "The Cake Is A Lie".toByteArray()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { context.filesDir } returns privateFiles
        var start = Instant.EPOCH
        every { timeStamper.nowUTC } answers {
            start.also {
                start = start.plus(Duration.standardDays(1))
            }
        }
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    private fun createStorage() = AppConfigStorage(
        context = context,
        timeStamper = timeStamper
    )

    @Test
    fun `simple read and write config`() = runBlockingTest {
        configPath.exists() shouldBe false
        val storage = createStorage()
        configPath.exists() shouldBe false

        storage.setStoredConfig(testByteArray)

        configPath.exists() shouldBe true
        configPath.readBytes() shouldBe testByteArray

        storage.getStoredConfig() shouldBe AppConfigStorage.StoredConfig(
            rawData = testByteArray,
            storedAt = Instant.ofEpochMilli(configPath.lastModified())
        )
    }

    @Test
    fun `simple update causes date to change`() = runBlockingTest {
        val storage = createStorage()
        storage.setStoredConfig(testByteArray)

        val firstConfig = storage.getStoredConfig()
        firstConfig shouldBe AppConfigStorage.StoredConfig(
            rawData = testByteArray,
            storedAt = Instant.ofEpochMilli(configPath.lastModified())
        )

        storage.setStoredConfig("Mock Config".toByteArray())
        val secondConfig = storage.getStoredConfig()
        secondConfig shouldBe AppConfigStorage.StoredConfig(
            rawData = "Mock Config".toByteArray(),
            storedAt = Instant.ofEpochMilli(configPath.lastModified())
        )

        secondConfig!!.storedAt.isAfter(firstConfig!!.storedAt) shouldBe true
    }

    @Test
    fun `nulling and overwriting`() = runBlockingTest {
        val storage = createStorage()
        configPath.exists() shouldBe false

        storage.getStoredConfig() shouldBe null
        storage.setStoredConfig(null)
        configPath.exists() shouldBe false

        storage.getStoredConfig() shouldBe null
        storage.setStoredConfig(testByteArray)
        storage.getStoredConfig() shouldBe AppConfigStorage.StoredConfig(
            rawData = testByteArray,
            storedAt = Instant.ofEpochMilli(configPath.lastModified())
        )
        configPath.exists() shouldBe true
        configPath.readBytes() shouldBe testByteArray

        storage.setStoredConfig(null)
        storage.getStoredConfig() shouldBe null
        configPath.exists() shouldBe false
    }

    @Test
    fun `we use checksum checks to prevent saving the same config twice`() = runBlockingTest {
        val storage = createStorage()
        storage.setStoredConfig(testByteArray)

        val firstConfig = storage.getStoredConfig()

        val secondConfig = storage.getStoredConfig()
        secondConfig shouldBe firstConfig
    }
}
