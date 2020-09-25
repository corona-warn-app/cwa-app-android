package de.rki.coronawarnapp.verification.server

import android.content.Context
import de.rki.coronawarnapp.diagnosiskeys.DiagnosisKeysModule
import de.rki.coronawarnapp.http.HttpModule
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import okhttp3.ConnectionSpec
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class VerificationApiTest : BaseIOTest() {

    @MockK
    private lateinit var context: Context

    private lateinit var webServer: MockWebServer
    private lateinit var serverAddress: String

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private val appConfigCacheDir = File(testDir, "http_app-config")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { context.cacheDir } returns testDir

        webServer = MockWebServer()
        webServer.start()
        serverAddress = "http://${webServer.hostName}:${webServer.port}"
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        webServer.shutdown()
        testDir.deleteRecursively()
    }

    private fun createAPI(): AppConfigApiV1 {
        val httpModule = HttpModule()
        val defaultHttpClient = httpModule.defaultHttpClient()
        val gsonConverterFactory = httpModule.provideGSONConverter()

        return DiagnosisKeysModule().let {
            val downloadHttpClient = it.cdnHttpClient(defaultHttpClient)
                .newBuilder()
                .connectionSpecs(listOf(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS))
                .build()
            it.provideAppConfigApi(
                context = context,
                client = downloadHttpClient,
                url = serverAddress,
                gsonConverterFactory = gsonConverterFactory
            )
        }
    }

    @Test
    fun `todo`() {
        TODO()
    }
//
//    @Test
//    fun `application config download uses cache`() {
//        appConfigCacheDir.exists() shouldBe false
//
//        val api = createAPI()
//
//        val configResponse =
//            MockResponse().setBody("~appconfig").addHeader("Cache-Control: max-age=2")
//
//        webServer.enqueue(configResponse)
//        runBlocking {
//            api.getApplicationConfiguration("DE").string() shouldBe "~appconfig"
//        }
//        appConfigCacheDir.exists() shouldBe true
//        appConfigCacheDir.listFiles()!!.size shouldBe 3
//
//        webServer.takeRequest(5, TimeUnit.SECONDS)!!.apply {
//            method shouldBe "GET"
//            path shouldBe "/version/v1/configuration/country/DE/app_config"
//        }
//
//        webServer.enqueue(configResponse)
//        runBlocking {
//            api.getApplicationConfiguration("DE").string() shouldBe "~appconfig"
//        }
//        appConfigCacheDir.exists() shouldBe true
//        appConfigCacheDir.listFiles()!!.size shouldBe 3
//
//        webServer.takeRequest(2, TimeUnit.SECONDS) shouldBe null
//
//        Thread.sleep(4000) // Let the cache expire
//
//        webServer.enqueue(configResponse)
//        runBlocking {
//            api.getApplicationConfiguration("DE").string() shouldBe "~appconfig"
//        }
//        appConfigCacheDir.exists() shouldBe true
//        appConfigCacheDir.listFiles()!!.size shouldBe 3
//
//        webServer.takeRequest(5, TimeUnit.SECONDS)!!.apply {
//            method shouldBe "GET"
//            path shouldBe "/version/v1/configuration/country/DE/app_config"
//        }
//    }

}
