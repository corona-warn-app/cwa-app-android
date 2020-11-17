package de.rki.coronawarnapp.appconfig.sources.remote

import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.internal.ConfigDataContainer
import de.rki.coronawarnapp.appconfig.internal.InternalConfigData
import de.rki.coronawarnapp.appconfig.mapping.ConfigParser
import de.rki.coronawarnapp.appconfig.sources.local.AppConfigStorage
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
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
import java.io.IOException

class RemoteAppConfigSourceTest : BaseIOTest() {

    @MockK lateinit var configServer: AppConfigServer
    @MockK lateinit var configStorage: AppConfigStorage
    @MockK lateinit var configParser: ConfigParser
    @MockK lateinit var configData: ConfigData
    @MockK lateinit var timeStamper: TimeStamper

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)

    private var dataFromServer = InternalConfigData(
        rawData = APPCONFIG_RAW,
        serverTime = Instant.parse("2020-11-03T05:35:16.000Z"),
        localOffset = Duration.standardHours(1),
        etag = "etag",
        cacheValidity = Duration.standardSeconds(420)
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

        coEvery { configServer.downloadAppConfig() } returns dataFromServer
        every { configServer.clearCache() } just Runs

        every { configParser.parse(APPCONFIG_RAW) } returns configData

        every { timeStamper.nowUTC } returns Instant.parse("2020-11-03T05:35:16.000Z")
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    private fun createInstance() = RemoteAppConfigSource(
        server = configServer,
        storage = configStorage,
        parser = configParser,
        dispatcherProvider = TestDispatcherProvider
    )

    @Test
    fun `successful download stores new config`() = runBlockingTest2(ignoreActive = true) {
        val source = createInstance()
        source.getConfigData() shouldBe ConfigDataContainer(
            serverTime = mockConfigStorage!!.serverTime,
            localOffset = mockConfigStorage!!.localOffset,
            mappedConfig = configData,
            configType = ConfigData.Type.FROM_SERVER,
            identifier = "etag",
            cacheValidity = Duration.standardSeconds(420)
        )

        mockConfigStorage shouldBe dataFromServer

        coVerify { configStorage.setStoredConfig(dataFromServer) }
    }

    @Test
    fun `failed download doesn't overwrite valid config`() = runBlockingTest2(ignoreActive = true) {
        mockConfigStorage = dataFromServer
        coEvery { configServer.downloadAppConfig() } throws IOException()

        createInstance().getConfigData()

        mockConfigStorage shouldBe dataFromServer

        coVerify(exactly = 0) { configStorage.setStoredConfig(any()) }
    }

    @Test
    fun `clear clears caches`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance()

        instance.clear()

        advanceUntilIdle()

        coVerifyOrder {
            configServer.clearCache()
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
