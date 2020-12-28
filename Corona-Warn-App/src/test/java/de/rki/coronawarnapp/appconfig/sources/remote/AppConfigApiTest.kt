package de.rki.coronawarnapp.appconfig.sources.remote

import android.content.Context
import de.rki.coronawarnapp.appconfig.AppConfigModule
import de.rki.coronawarnapp.appconfig.download.AppConfigApiV2
import de.rki.coronawarnapp.environment.download.DownloadCDNModule
import de.rki.coronawarnapp.http.HttpModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import okhttp3.ConnectionSpec
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File
import java.util.concurrent.TimeUnit

class AppConfigApiTest : BaseIOTest() {

    @MockK private lateinit var context: Context

    private lateinit var webServer: MockWebServer
    private lateinit var serverAddress: String

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

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

    private fun createAPI(): AppConfigApiV2 {
        val httpModule = HttpModule()
        val defaultHttpClient = httpModule.defaultHttpClient()
        val gsonConverterFactory = httpModule.provideGSONConverter()

        val cdnHttpClient = DownloadCDNModule()
            .cdnHttpClient(defaultHttpClient)
            .newBuilder()
            .connectionSpecs(listOf(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS))
            .build()

        return AppConfigModule().provideAppConfigApi(
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
            api.getApplicationConfiguration().apply {
                body()!!.string() shouldBe "~appconfig"
            }
        }

        val request = webServer.takeRequest(5, TimeUnit.SECONDS)!!
        request.method shouldBe "GET"
        request.path shouldBe "/version/v1/app_config_android"
    }
}
