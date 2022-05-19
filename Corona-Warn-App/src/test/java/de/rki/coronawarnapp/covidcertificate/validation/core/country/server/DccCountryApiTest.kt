package de.rki.coronawarnapp.covidcertificate.validation.core.country.server

import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationModule
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountryApi
import de.rki.coronawarnapp.environment.download.DownloadCDNModule
import de.rki.coronawarnapp.http.HttpModule
import de.rki.coronawarnapp.statistics.StatisticsModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.runTest
import okhttp3.ConnectionSpec
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File
import java.util.concurrent.TimeUnit

class DccCountryApiTest : BaseIOTest() {

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
        webServer.shutdown()
        testDir.deleteRecursively()
    }

    private fun createAPI(): DccCountryApi {
        val httpModule = HttpModule()
        val defaultHttpClient = httpModule.defaultHttpClient()

        val cdnHttpClient = DownloadCDNModule()
            .cdnHttpClient(defaultHttpClient)
            .newBuilder()
            .connectionSpecs(listOf(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS))
            .build()

        val cache = StatisticsModule.httpCache(cacheDir)

        return DccValidationModule.countryApi(
            httpClient = cdnHttpClient,
            url = serverAddress,
            cache = cache
        )
    }

    @Test
    fun `application config download`() {
        val api = createAPI()

        webServer.enqueue(MockResponse().setBody("[\"DE\"]"))

        runTest {
            api.onboardedCountries().apply {
                body()!!.string() shouldBe "[\"DE\"]"
            }
        }

        val request = webServer.takeRequest(5, TimeUnit.SECONDS)!!
        request.method shouldBe "GET"
        request.path shouldBe "/version/v1/ehn-dgc/onboarded-countries"
    }
}
