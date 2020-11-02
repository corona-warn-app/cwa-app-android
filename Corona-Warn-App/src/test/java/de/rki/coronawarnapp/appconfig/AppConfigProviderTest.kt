package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.download.AppConfigServer
import de.rki.coronawarnapp.appconfig.download.AppConfigStorage
import de.rki.coronawarnapp.appconfig.mapping.ConfigParser
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import okio.ByteString.Companion.decodeHex
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.TestDispatcherProvider
import java.io.File
import java.io.IOException

class AppConfigProviderTest : BaseIOTest() {

    @MockK lateinit var configServer: AppConfigServer
    @MockK lateinit var configStorage: AppConfigStorage
    @MockK lateinit var configParser: ConfigParser
    @MockK lateinit var configContainer: ConfigContainerKey

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)

    private var mockConfigStorage: ByteArray? = null

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true

        coEvery { configStorage.getAppConfigRaw() } answers { mockConfigStorage }
        coEvery { configStorage.setAppConfigRaw(any()) } answers { mockConfigStorage = arg(0) }

        every { configParser.parse(APPCONFIG_RAW) } returns configContainer
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    private fun createInstance() = AppConfigProvider(
        server = configServer,
        storage = configStorage,
        parser = configParser,
        dispatcherProvider = TestDispatcherProvider
    )

    @Test
    fun `successful download stores new config`() = runBlockingTest {
        coEvery { configServer.downloadAppConfig() } returns APPCONFIG_RAW

        val downloadServer = createInstance()
        downloadServer.getAppConfig()

        mockConfigStorage shouldBe APPCONFIG_RAW
        coVerify { configStorage.setAppConfigRaw(APPCONFIG_RAW) }
    }

    @Test
    fun `failed download doesn't overwrite valid config`() = runBlockingTest {
        mockConfigStorage = APPCONFIG_RAW
        coEvery { configServer.downloadAppConfig() } throws IOException()

        createInstance().getAppConfig()

        coVerify(exactly = 0) { configStorage.setAppConfigRaw(any()) }
        mockConfigStorage shouldBe APPCONFIG_RAW
    }

    @Test
    fun `fallback to last config if download fails`() = runBlockingTest {
        mockConfigStorage = APPCONFIG_RAW

        coEvery { configServer.downloadAppConfig() } throws Exception()

        createInstance().getAppConfig() shouldBe configContainer
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
