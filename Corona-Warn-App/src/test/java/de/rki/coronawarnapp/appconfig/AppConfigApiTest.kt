package de.rki.coronawarnapp.appconfig

import android.content.Context
import de.rki.coronawarnapp.environment.download.DownloadCDNModule
import de.rki.coronawarnapp.http.HttpModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import okhttp3.ConnectionSpec
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File
import java.util.concurrent.TimeUnit

class AppConfigApiTest : BaseIOTest() {

    @MockK
    private lateinit var context: Context

    private lateinit var webServer: MockWebServer
    private lateinit var serverAddress: String

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private val cacheFiles = File(testDir, "cache")
    private val privateFiles = File(testDir, "files")
    private val newHttpCacheDir = File(privateFiles, "appconfig_httpstore")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { context.filesDir } returns privateFiles
        every { context.cacheDir } returns cacheFiles

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

        val cdnHttpClient = DownloadCDNModule()
            .cdnHttpClient(defaultHttpClient)
            .newBuilder()
            .connectionSpecs(listOf(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS))
            .build()

        return AppConfigModule().provideAppConfigApi(
            context = context,
            client = cdnHttpClient,
            url = serverAddress,
            gsonConverterFactory = gsonConverterFactory
        )
    }

    @Test
    fun `application config download`() {
        val api = createAPI()

        webServer.enqueue(MockResponse().setBody("~appconfig"))

        runBlocking {
            api.getApplicationConfiguration("DE").string() shouldBe "~appconfig"
        }

        val request = webServer.takeRequest(5, TimeUnit.SECONDS)!!
        request.method shouldBe "GET"
        request.path shouldBe "/version/v1/configuration/country/DE/app_config"
    }

    @Test
    fun `application config download uses cache`() {
        newHttpCacheDir.exists() shouldBe false

        val api = createAPI()

        val configResponse =
            MockResponse().setBody("~appconfig").addHeader("Cache-Control: max-age=2")

        webServer.enqueue(configResponse)
        runBlocking {
            api.getApplicationConfiguration("DE").string() shouldBe "~appconfig"
        }
        newHttpCacheDir.exists() shouldBe true
        newHttpCacheDir.listFiles()!!.size shouldBe 3

        webServer.takeRequest(5, TimeUnit.SECONDS)!!.apply {
            method shouldBe "GET"
            path shouldBe "/version/v1/configuration/country/DE/app_config"
        }

        webServer.enqueue(configResponse)
        runBlocking {
            api.getApplicationConfiguration("DE").string() shouldBe "~appconfig"
        }
        newHttpCacheDir.exists() shouldBe true
        newHttpCacheDir.listFiles()!!.size shouldBe 3

        webServer.takeRequest(2, TimeUnit.SECONDS) shouldBe null

        Thread.sleep(4000) // Let the cache expire

        webServer.enqueue(configResponse)
        runBlocking {
            api.getApplicationConfiguration("DE").string() shouldBe "~appconfig"
        }
        newHttpCacheDir.exists() shouldBe true
        newHttpCacheDir.listFiles()!!.size shouldBe 3

        webServer.takeRequest(5, TimeUnit.SECONDS)!!.apply {
            method shouldBe "GET"
            path shouldBe "/version/v1/configuration/country/DE/app_config"
        }
    }

    @Test
    fun `cache is used when connection is flaky`() {
        newHttpCacheDir.exists() shouldBe false

        val api = createAPI()

        val configResponse =
            MockResponse().setBody("~appconfig").addHeader("Cache-Control: max-age=300")

        webServer.enqueue(configResponse)
        runBlocking {
            api.getApplicationConfiguration("DE").string() shouldBe "~appconfig"
        }
        newHttpCacheDir.exists() shouldBe true
        newHttpCacheDir.listFiles()!!.size shouldBe 3

        webServer.takeRequest(5, TimeUnit.SECONDS)!!.apply {
            method shouldBe "GET"
            path shouldBe "/version/v1/configuration/country/DE/app_config"
        }

        webServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_DURING_REQUEST_BODY))

        runBlocking {
            api.getApplicationConfiguration("DE").string() shouldBe "~appconfig"
        }
    }

    @Test
    fun `fallback to last cached even if stale`() {
        newHttpCacheDir.exists() shouldBe false

        val api = createAPI()

        val configResponse =
            MockResponse().setBody("~appconfig").addHeader("Cache-Control: max-age=2")

        webServer.enqueue(configResponse)
        runBlocking {
            api.getApplicationConfiguration("DE").string() shouldBe "~appconfig"
        }
        newHttpCacheDir.exists() shouldBe true
        newHttpCacheDir.listFiles()!!.size shouldBe 3

        webServer.takeRequest(5, TimeUnit.SECONDS)!!.apply {
            method shouldBe "GET"
            path shouldBe "/version/v1/configuration/country/DE/app_config"
        }

        val errorResponse = MockResponse().setHttp2ErrorCode(500)
        webServer.enqueue(errorResponse)

        runBlocking {
            api.getApplicationConfiguration("DE").string() shouldBe "~appconfig"
        }
        webServer.takeRequest(2, TimeUnit.SECONDS) shouldBe null

        Thread.sleep(4000) // Let the cache expire

        webServer.enqueue(errorResponse)
        runBlocking {
            api.getApplicationConfiguration("DE").string() shouldBe "~appconfig"
        }
        webServer.takeRequest(2, TimeUnit.SECONDS) shouldBe null
    }

    @Test
    fun `timeout and retry behavior`() {
        // TODO 10s timeout, 3 retries
    }
}
