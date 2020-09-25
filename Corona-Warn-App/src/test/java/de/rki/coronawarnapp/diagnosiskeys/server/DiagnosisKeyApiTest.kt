package de.rki.coronawarnapp.diagnosiskeys.server

import de.rki.coronawarnapp.diagnosiskeys.DiagnosisKeysModule
import de.rki.coronawarnapp.http.HttpModule
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import okhttp3.ConnectionSpec
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.util.concurrent.TimeUnit

class DiagnosisKeyApiTest : BaseIOTest() {

    lateinit var webServer: MockWebServer
    lateinit var serverAddress: String

    @BeforeEach
    fun setup() {
        webServer = MockWebServer()
        webServer.start()
        serverAddress = "http://${webServer.hostName}:${webServer.port}"
    }

    @AfterEach
    fun teardown() {
        webServer.shutdown()
    }

    private fun createAPI(): DiagnosisKeyApiV1 {
        val httpModule = HttpModule()
        val defaultHttpClient = httpModule.defaultHttpClient()
        val gsonConverterFactory = httpModule.provideGSONConverter()

        return DiagnosisKeysModule().let {
            val downloadHttpClient = it.cdnHttpClient(defaultHttpClient)
                .newBuilder()
                .connectionSpecs(listOf(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS))
                .build()
            it.provideDiagnosisKeyApi(
                client = downloadHttpClient,
                url = serverAddress,
                gsonConverterFactory = gsonConverterFactory
            )
        }
    }

    @Test
    fun `download country index`() {
        val api = createAPI()

        webServer.enqueue(MockResponse().setBody("[\"DE\",\"NL\"]"))

        runBlocking {
            api.getCountryIndex() shouldBe listOf("DE", "NL")
        }

        val request = webServer.takeRequest(5, TimeUnit.SECONDS)!!
        request.method shouldBe "GET"
        request.path shouldBe "/version/v1/diagnosis-keys/country"
    }

    @Test
    fun `download day index for country`() {
        val api = createAPI()

        webServer.enqueue(MockResponse().setBody("[\"2020-08-19\",\"2020-08-20\"]"))

        runBlocking {
            api.getDayIndex("DE") shouldBe listOf("2020-08-19", "2020-08-20")
        }

        val request = webServer.takeRequest(5, TimeUnit.SECONDS)!!
        request.method shouldBe "GET"
        request.path shouldBe "/version/v1/diagnosis-keys/country/DE/date"
    }

    @Test
    fun `download hour index for country and day`() {
        val api = createAPI()

        webServer.enqueue(MockResponse().setBody("[22,23]"))

        runBlocking {
            api.getHourIndex("DE", "2020-08-19") shouldBe listOf("22", "23")
        }

        val request = webServer.takeRequest(5, TimeUnit.SECONDS)!!
        request.method shouldBe "GET"
        request.path shouldBe "/version/v1/diagnosis-keys/country/DE/date/2020-08-19/hour"
    }

    @Test
    fun `download key files for day`() {
        val api = createAPI()

        webServer.enqueue(MockResponse().setBody("~daykeyfile"))

        runBlocking {
            api.downloadKeyFileForDay("DE", "2020-09-09").body()!!.string() shouldBe "~daykeyfile"
        }

        val request = webServer.takeRequest(5, TimeUnit.SECONDS)!!
        request.method shouldBe "GET"
        request.path shouldBe "/version/v1/diagnosis-keys/country/DE/date/2020-09-09"
    }

    @Test
    fun `download key files for hour`() {
        val api = createAPI()

        webServer.enqueue(MockResponse().setBody("~hourkeyfile"))

        runBlocking {
            api.downloadKeyFileForHour("DE", "2020-09-09", "23").body()!!
                .string() shouldBe "~hourkeyfile"
        }

        val request = webServer.takeRequest(5, TimeUnit.SECONDS)!!
        request.method shouldBe "GET"
        request.path shouldBe "/version/v1/diagnosis-keys/country/DE/date/2020-09-09/hour/23"
    }
}
