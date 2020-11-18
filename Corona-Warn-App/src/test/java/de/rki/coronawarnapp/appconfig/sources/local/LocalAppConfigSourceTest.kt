package de.rki.coronawarnapp.appconfig.sources.local

import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.internal.ConfigDataContainer
import de.rki.coronawarnapp.appconfig.internal.InternalConfigData
import de.rki.coronawarnapp.appconfig.mapping.ConfigParser
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import okio.ByteString.Companion.decodeHex
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runBlockingTest2
import java.io.File

class LocalAppConfigSourceTest : BaseIOTest() {

    @MockK lateinit var configStorage: AppConfigStorage
    @MockK lateinit var configParser: ConfigParser
    @MockK lateinit var configData: ConfigData
    @MockK lateinit var timeStamper: TimeStamper

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)

    private var expectedData = InternalConfigData(
        rawData = APPCONFIG_RAW,
        serverTime = Instant.parse("2020-11-03T05:35:16.000Z"),
        localOffset = Duration.standardHours(1),
        etag = "etag",
        cacheValidity = Duration.standardMinutes(5)
    )

    private var mockConfigStorage: InternalConfigData? = null

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true

        coEvery { configStorage.getStoredConfig() } answers { mockConfigStorage }
        coEvery { configStorage.setStoredConfig(any()) } answers {
            mockConfigStorage = arg(0)
        }

        every { configParser.parse(APPCONFIG_RAW) } returns configData

        every { timeStamper.nowUTC } returns Instant.parse("2020-11-03T05:35:16.000Z")
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    private fun createInstance() = LocalAppConfigSource(
        storage = configStorage,
        parser = configParser,
        dispatcherProvider = TestDispatcherProvider
    )

    @Test
    fun `local app config source returns null if storage is empty`() = runBlockingTest {
        coEvery { configStorage.getStoredConfig() } returns null

        val instance = createInstance()

        instance.getConfigData() shouldBe null

        coVerifyOrder { configStorage.getStoredConfig() }
    }

    @Test
    fun `local default config is loaded from storage`() = runBlockingTest {
        coEvery { configStorage.getStoredConfig() } returns expectedData

        val instance = createInstance()

        instance.getConfigData() shouldBe ConfigDataContainer(
            serverTime = expectedData.serverTime,
            localOffset = expectedData.localOffset,
            mappedConfig = configData,
            configType = ConfigData.Type.LAST_RETRIEVED,
            identifier = expectedData.etag,
            cacheValidity = Duration.standardMinutes(5)
        )

        coVerifyOrder { configStorage.getStoredConfig() }
    }

    @Test
    fun `local app config source returns null if there is any exception`() = runBlockingTest {
        coEvery { configStorage.getStoredConfig() } returns expectedData.copy(
            rawData = "I'm not valid protobuf".toByteArray()
        )

        val instance = createInstance()

        instance.getConfigData() shouldBe null

        coVerifyOrder { configStorage.getStoredConfig() }
    }

    @Test
    fun `clear clears caches`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance()

        instance.clear()

        advanceUntilIdle()

        coVerifyOrder {
            configStorage.setStoredConfig(null)
        }
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
    }
}
