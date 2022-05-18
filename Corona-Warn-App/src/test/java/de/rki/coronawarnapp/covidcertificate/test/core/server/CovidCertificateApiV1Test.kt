package de.rki.coronawarnapp.covidcertificate.test.core.server

import de.rki.coronawarnapp.http.HttpModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toJsonResponse
import java.util.concurrent.TimeUnit

class CovidCertificateApiV1Test : BaseTest() {

    private lateinit var webServer: MockWebServer
    private lateinit var serverAddress: String

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
    }

    private fun createAPI(): TestCertificateApiV1 {
        val httpModule = HttpModule()

        return TestCertificateServerModule().let {
            val downloadHttpClient = httpModule.defaultHttpClient()
            it.apiV1(
                httpClient = downloadHttpClient,
                url = serverAddress,
                gsonConverterFactory = httpModule.provideGSONConverter()
            )
        }
    }

    @Test
    fun `test upload`(): Unit = runTest {
        val api = createAPI()
        """
            {
            
            }
        """.toJsonResponse().apply { webServer.enqueue(this) }

        api.sendPublicKey(
            TestCertificateApiV1.PublicKeyUploadRequest(
                registrationToken = "token1",
                publicKey = "key1"
            )
        )

        webServer.takeRequest(5, TimeUnit.SECONDS)!!.apply {
            path shouldBe "/version/v1/publicKey"
        }
    }
}
