package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.download.AppConfigServer
import de.rki.coronawarnapp.appconfig.download.AppConfigStorage
import de.rki.coronawarnapp.appconfig.download.ConfigDownload
import de.rki.coronawarnapp.appconfig.mapping.ConfigMapping
import de.rki.coronawarnapp.appconfig.mapping.ConfigParser
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import okio.ByteString.Companion.decodeHex
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runBlockingTest2
import testhelpers.coroutines.test
import java.io.File
import java.io.IOException

class AppConfigProviderTest : BaseIOTest() {

    @MockK lateinit var configServer: AppConfigServer
    @MockK lateinit var configStorage: AppConfigStorage
    @MockK lateinit var configParser: ConfigParser
    @MockK lateinit var configData: ConfigData
    @MockK lateinit var timeStamper: TimeStamper

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)

    private var testConfigDownload = ConfigDownload(
        rawData = APPCONFIG_RAW,
        serverTime = Instant.parse("2020-11-03T05:35:16.000Z"),
        localOffset = Duration.standardHours(1)
    )

    private var mockConfigStorage: ConfigDownload? = null

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true

        coEvery { configStorage.getStoredConfig() } answers { mockConfigStorage }
        coEvery { configStorage.setStoredConfig(any()) } answers {
            mockConfigStorage = arg(0)
        }

        coEvery { configServer.downloadAppConfig() } returns testConfigDownload
        every { configServer.clearCache() } just Runs

        every { configParser.parse(APPCONFIG_RAW) } returns configData
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    private fun createInstance(scope: CoroutineScope) = AppConfigProvider(
        server = configServer,
        storage = configStorage,
        parser = configParser,
        dispatcherProvider = TestDispatcherProvider,
        scope = scope
    )

    @Test
    fun `successful download stores new config`() = runBlockingTest2(ignoreActive = true) {
        val provider = createInstance(this)
        provider.getAppConfig() shouldBe DefaultConfigData(
            serverTime = mockConfigStorage!!.serverTime,
            localOffset = mockConfigStorage!!.localOffset,
            mappedConfig = configData,
            isFallback = false
        )

        mockConfigStorage shouldBe testConfigDownload


        coVerify { configStorage.setStoredConfig(testConfigDownload) }
    }

    @Test
    fun `fallback to last config if download fails`() = runBlockingTest2(ignoreActive = true) {
        coEvery { configServer.downloadAppConfig() } throws Exception()

        createInstance(this).getAppConfig() shouldBe DefaultConfigData(
            serverTime = mockConfigStorage!!.serverTime,
            localOffset = mockConfigStorage!!.localOffset,
            mappedConfig = configData,
            isFallback = true
        )
    }

    @Test
    fun `failed download doesn't overwrite valid config`() = runBlockingTest2(ignoreActive = true) {
        coEvery { configServer.downloadAppConfig() } throws IOException()

        createInstance(this).getAppConfig()

        coVerify(exactly = 0) { configStorage.setStoredConfig(any()) }
    }

    @Test
    fun `force update clears caches`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)

        val testCollector = instance.currentConfig.test(startOnScope = this)

        instance.forceUpdate()

        advanceUntilIdle()

        coVerifyOrder {
            configStorage.setStoredConfig(null)
            configServer.clearCache()
        }

        testCollector.cancel()
    }

    @Test
    fun `appConfig is observable`() = runBlockingTest2(ignoreActive = true) {
        // If all observers unsubscribe, the next subscriber triggers an update
        var counter = 0
        val generatedConfigDownloads = mutableListOf<ConfigDownload>()
        coEvery { configServer.downloadAppConfig() } answers {
            ConfigDownload(
                rawData = "${++counter}".toByteArray(),
                serverTime = Instant.EPOCH,
                localOffset = Duration.ZERO
            ).also { generatedConfigDownloads.add(it) }
        }
        val mockConfig1 = mockk<ConfigMapping>()
        every { configParser.parse("1".toByteArray()) } returns mockConfig1

        val mockConfig2 = mockk<ConfigMapping>()
        every { configParser.parse("2".toByteArray()) } returns mockConfig2

        val instance = createInstance(this)

        val testCollector = instance.currentConfig.test(startOnScope = this)

        advanceUntilIdle()

        instance.forceUpdate()

        testCollector.latestValues.size shouldBe 2
        testCollector.latestValues[0] shouldNotBe testCollector.latestValues[1]

        coVerifySequence {
            configServer.downloadAppConfig()
            configParser.parse("1".toByteArray())
            configStorage.setStoredConfig(generatedConfigDownloads[0])

            configStorage.setStoredConfig(null)
            configServer.clearCache()

            configServer.downloadAppConfig()
            configParser.parse("2".toByteArray())
            configStorage.setStoredConfig(generatedConfigDownloads[1])
        }
    }

    @Test
    fun `observed config uses WHILE_SUBSCRIBED`() = runBlockingTest2(ignoreActive = true) {
        coVerify(exactly = 0) { configServer.downloadAppConfig() }
        val instance = createInstance(this)

        instance.currentConfig.test(startOnScope = this).cancel()
        instance.currentConfig.test(startOnScope = this).cancel()

        coVerify(exactly = 2) { configServer.downloadAppConfig() }
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
