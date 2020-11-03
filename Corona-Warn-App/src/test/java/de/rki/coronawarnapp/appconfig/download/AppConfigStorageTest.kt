package de.rki.coronawarnapp.appconfig.download

import android.content.Context
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.serialization.SerializationModule
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
import testhelpers.extensions.toComparableJson
import java.io.File

class AppConfigStorageTest : BaseIOTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var timeStamper: TimeStamper

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private val privateFiles = File(testDir, "files")
    private val storageDir = File(privateFiles, "appconfig_storage")
    private val legacyConfigPath = File(storageDir, "appconfig")
    private val configPath = File(storageDir, "appconfig.json")

    private val testConfigDownload = ConfigDownload(
        rawData = "The Cake Is A Lie".toByteArray(),
        serverTime = Instant.parse("2020-11-03T05:35:16.000Z"),
        localOffset = Duration.standardHours(1)
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { context.filesDir } returns privateFiles

        every { timeStamper.nowUTC } returns Instant.ofEpochMilli(1234)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    private fun createStorage() = AppConfigStorage(
        context = context,
        timeStamper = timeStamper,
        gson = SerializationModule().baseGson()
    )

    @Test
    fun `simple read and write config`() = runBlockingTest {
        configPath.exists() shouldBe false
        val storage = createStorage()
        configPath.exists() shouldBe false

        storage.setStoredConfig(testConfigDownload)

        configPath.exists() shouldBe true
        configPath.readText().toComparableJson() shouldBe """
            {
                "rawData": "The Cake Is A Lie",
                "serverTime": {
                    "iMillis": 1604381716000
                },
                "localOffset": {
                    "iMillis": 3600000
                }
            }
        """.toComparableJson()

        storage.getStoredConfig() shouldBe testConfigDownload
    }

    @Test
    fun `nulling and overwriting`() = runBlockingTest {
        val storage = createStorage()
        configPath.exists() shouldBe false

        storage.getStoredConfig() shouldBe null
        storage.setStoredConfig(null)
        configPath.exists() shouldBe false

        storage.getStoredConfig() shouldBe null
        storage.setStoredConfig(testConfigDownload)
        storage.getStoredConfig() shouldBe testConfigDownload

        configPath.exists() shouldBe true
        configPath.readText().toComparableJson() shouldBe """
            {
                "rawData": "The Cake Is A Lie",
                "serverTime": {
                    "iMillis": 1604381716000
                },
                "localOffset": {
                    "iMillis": 3600000
                }
            }
        """.toComparableJson()

        storage.setStoredConfig(null)
        storage.getStoredConfig() shouldBe null
        configPath.exists() shouldBe false
    }

    @Test
    fun `if no fallback exists, but we have a legacy config, use that`() = runBlockingTest {
        configPath.exists() shouldBe false
        legacyConfigPath.exists() shouldBe false

        legacyConfigPath.parentFile!!.mkdirs()
        legacyConfigPath.writeText("The Cake Is A Lie")

        val storage = createStorage()

        storage.getStoredConfig() shouldBe ConfigDownload(
            rawData = "The Cake Is A Lie".toByteArray(),
            serverTime = Instant.ofEpochMilli(1234),
            localOffset = Duration.ZERO
        )
    }

    @Test
    fun `writing a new config deletes any legacy configsconfig`() = runBlockingTest {
        legacyConfigPath.parentFile!!.mkdirs()
        legacyConfigPath.writeText("The Cake Is A Lie")
        configPath.exists() shouldBe false

        val storage = createStorage()
        storage.setStoredConfig(testConfigDownload)

        legacyConfigPath.exists() shouldBe false
        configPath.exists() shouldBe true
    }
}
