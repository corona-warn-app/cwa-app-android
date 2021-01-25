package de.rki.coronawarnapp.statistics.source

import de.rki.coronawarnapp.environment.download.DownloadCDNModule
import de.rki.coronawarnapp.http.HttpModule
import de.rki.coronawarnapp.statistics.StatisticsModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
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

class StatisticsAPIV1Test : BaseIOTest() {

    private lateinit var webServer: MockWebServer
    private lateinit var serverAddress: String

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private val cacheDir = File(testDir, "cacheDir")

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

    private fun createAPI(): StatisticsApiV1 {
        val httpModule = HttpModule()
        val defaultHttpClient = httpModule.defaultHttpClient()
        val gsonConverterFactory = httpModule.provideGSONConverter()

        val cdnHttpClient = DownloadCDNModule()
            .cdnHttpClient(defaultHttpClient)
            .newBuilder()
            .connectionSpecs(listOf(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS))
            .build()

        val cache = StatisticsModule().httpCache(cacheDir)

        return StatisticsModule().api(
            client = cdnHttpClient,
            url = serverAddress,
            gsonConverterFactory = gsonConverterFactory,
            cache = cache
        )
    }

    @Test
    fun `application config download`() {
        val api = createAPI()

        webServer.enqueue(MockResponse().setBody("~look at me, I'm statistics"))

        runBlocking {
            api.getStatistics().apply {
                body()!!.string() shouldBe "~look at me, I'm statistics"
            }
        }

        val request = webServer.takeRequest(5, TimeUnit.SECONDS)!!
        request.method shouldBe "GET"
        request.path shouldBe "/version/v1/stats"
    }
}
