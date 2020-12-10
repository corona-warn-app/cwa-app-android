package de.rki.coronawarnapp.appconfig.sources.local

import android.content.Context
import de.rki.coronawarnapp.appconfig.internal.InternalConfigData
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.toByteString
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

    private val testConfigDownload = InternalConfigData(
        rawData = APPCONFIG_RAW,
        serverTime = Instant.parse("2020-11-03T05:35:16.000Z"),
        localOffset = Duration.standardHours(1),
        etag = "I am an ETag :)!",
        cacheValidity = Duration.standardSeconds(123)
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
        baseGson = SerializationModule().baseGson()
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
                "rawData": "$APPCONFIG_BASE64",
                "etag": "I am an ETag :)!",
                "serverTime": 1604381716000,
                "localOffset": 3600000,
                "cacheValidity": 123000
            }
        """.toComparableJson()

        storage.getStoredConfig() shouldBe testConfigDownload
    }

    @Test
    fun `restoring from storage`() = runBlockingTest {
        configPath.parentFile!!.mkdirs()
        configPath.writeText(
            """
            {
                "rawData": "$APPCONFIG_BASE64",
                "etag": "I am an ETag :)!",
                "serverTime": 1604381716000,
                "localOffset": 3600000,
                "cacheValidity": 123000
            }
        """.trimIndent()
        )
        val storage = createStorage()
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
                "rawData": "$APPCONFIG_BASE64",
                "etag": "I am an ETag :)!",
                "serverTime": 1604381716000,
                "localOffset": 3600000,
                "cacheValidity": 123000
            }
        """.toComparableJson()

        storage.setStoredConfig(null)
        storage.getStoredConfig() shouldBe null
        configPath.exists() shouldBe false
    }

    @Test
    fun `nulling deletes legacy config`() = runBlockingTest {
        val storage = createStorage()
        configPath.exists() shouldBe false

        storage.getStoredConfig() shouldBe null
        storage.setStoredConfig(null)
        configPath.exists() shouldBe false

        legacyConfigPath.exists() shouldBe false
        legacyConfigPath.parentFile!!.mkdirs()
        legacyConfigPath.writeBytes(APPCONFIG_RAW)
        legacyConfigPath.exists() shouldBe true

        storage.setStoredConfig(null)
        storage.getStoredConfig() shouldBe null
        configPath.exists() shouldBe false
        legacyConfigPath.exists() shouldBe false
    }

    @Test
    fun `if no fallback exists, but we have a legacy config, use that`() = runBlockingTest {
        configPath.exists() shouldBe false
        legacyConfigPath.exists() shouldBe false

        legacyConfigPath.parentFile!!.mkdirs()
        legacyConfigPath.writeBytes(APPCONFIG_RAW)

        val storage = createStorage()

        storage.getStoredConfig() shouldBe InternalConfigData(
            rawData = APPCONFIG_RAW,
            serverTime = Instant.ofEpochMilli(legacyConfigPath.lastModified()),
            localOffset = Duration.ZERO,
            etag = "I am an ETag :)!",
            cacheValidity = Duration.standardSeconds(0)
        )
    }

    @Test
    fun `writing a new config deletes any legacy configsconfig`() = runBlockingTest {
        legacyConfigPath.parentFile!!.mkdirs()
        legacyConfigPath.writeBytes(APPCONFIG_RAW)
        configPath.exists() shouldBe false

        val storage = createStorage()
        storage.setStoredConfig(testConfigDownload)

        legacyConfigPath.exists() shouldBe false
        configPath.exists() shouldBe true
    }

    @Test
    fun `return null on errors`() = runBlockingTest {
        every { timeStamper.nowUTC } throws Exception()

        val storage = createStorage()
        storage.getStoredConfig() shouldBe null
    }

    @Test
    fun `return null on invalid json and delete config file`() = runBlockingTest {
        configPath.parentFile!!.mkdirs()
        configPath.writeText(
            """
            {
               
            }
        """.trimIndent()
        )
        val storage = createStorage()
        storage.getStoredConfig() shouldBe null

        configPath.exists() shouldBe false
    }

    @Test
    fun `return null on empty file and delete config file`() {
        configPath.parentFile!!.mkdirs()
        configPath.createNewFile()

        val storage = createStorage()

        runBlockingTest {
            storage.getStoredConfig() shouldBe null
        }

        configPath.exists() shouldBe false
    }

    @Test
    fun `catch errors when trying to save the config`() {
        configPath.parentFile!!.mkdirs()
        configPath.createNewFile()

        val storage = createStorage()

        runBlockingTest {
            storage.setStoredConfig(mockk())
        }

        configPath.exists() shouldBe true
    }

    companion object {
        private val APPCONFIG_RAW = (
            "080b124d0a230a034c4f57180f221a68747470733a2f2f777777" +
                "2e636f726f6e617761726e2e6170700a260a0448494748100f1848221a68747470733a2f2f7777772e636f7" +
                "26f6e617761726e2e6170701a640a10080110021803200428053006380740081100000000000049401a0a20" +
                "0128013001380140012100000000000049402a1008051005180520052805300538054005310000000000003" +
                "4403a0e1001180120012801300138014001410000000000004940221c0a040837103f121209000000000000" +
                "f03f11000000000000e03f20192a1a0a0a0a041008180212021005120c0a0408011804120408011804"
            ).decodeHex().toByteArray()

        private val APPCONFIG_BASE64 = APPCONFIG_RAW.toByteString().base64()
    }
}
