package de.rki.coronawarnapp.presencetracing.locations.server

import de.rki.coronawarnapp.environment.presencetracing.qrcodeposter.QrCodePosterTemplateModule
import de.rki.coronawarnapp.http.HttpModule
import de.rki.coronawarnapp.presencetracing.locations.server.qrcodepostertemplate.QrCodePosterTemplateApiV1
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File
import java.util.concurrent.TimeUnit

class QrCodePosterTemplateApiV1Test : BaseIOTest() {

    private lateinit var webServer: MockWebServer
    private lateinit var serverAddress: String

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private val cacheDir = File(testDir, "cache")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        webServer = MockWebServer().apply { start() }
        serverAddress = "http://${webServer.hostName}:${webServer.port}"
    }

    @AfterEach
    fun teardown() {
        webServer.shutdown()
        cacheDir.deleteRecursively()
    }

    private fun createAPI(): QrCodePosterTemplateApiV1 {
        val defaultHttpClient = HttpModule().defaultHttpClient()
        val templateModule = QrCodePosterTemplateModule()
        return templateModule.api(
            defaultHttpClient,
            url = serverAddress,
            cache = templateModule.httpCache(cacheDir)
        )
    }

    @Test
    fun `should perform request as specified`() {

        webServer.enqueue(MockResponse().setBody("QR-Code Poster Template"))

        runTest {
            createAPI().getQrCodePosterTemplate().apply {
                body()!!.string() shouldBe "QR-Code Poster Template"
            }
        }

        webServer.takeRequest(5, TimeUnit.SECONDS)!!.apply {
            method shouldBe "GET"
            path shouldBe "/version/v1/qr_code_poster_template_android"
        }
    }

    @Test
    fun `should set ETag header of previously received response and return cached response`() {

        // first mocked response returns a body and ETag
        webServer.enqueue(
            MockResponse()
                .setBody("Poster Template")
                .setResponseCode(200)
                .setHeader("ETag", "ETAG_OF_MOCKED_RESPONSE")
        )

        runTest {
            createAPI().getQrCodePosterTemplate().apply {
                // we should receive the body and ETag
                code() shouldBe 200
                body()!!.string() shouldBe "Poster Template"
                headers()["ETag"] shouldBe "ETAG_OF_MOCKED_RESPONSE"
            }
        }

        webServer.takeRequest(5, TimeUnit.SECONDS)!!.apply {
            // Our first request should not contain any ETag in the 'If-None-Match' header
            headers["If-None-Match"] shouldBe null
        }

        // second mocked response returns 304 and no body (client already has latest poster)
        webServer.enqueue(
            MockResponse()
                .setResponseCode(304)
                .setHeader("ETag", "ETAG_OF_MOCKED_RESPONSE")
        )

        runTest {
            createAPI().getQrCodePosterTemplate().apply {
                code() shouldBe 200
                raw().cacheResponse shouldNotBe null
                raw().networkResponse!!.code shouldBe 304
                // cached poster template should be returned
                body()!!.string() shouldBe "Poster Template"
                headers()["ETag"] shouldBe "ETAG_OF_MOCKED_RESPONSE"
            }
        }

        webServer.takeRequest(5, TimeUnit.SECONDS)!!.apply {
            method shouldBe "GET"
            path shouldBe "/version/v1/qr_code_poster_template_android"
            // Our first request should not contain an ETag
            headers["If-None-Match"] shouldBe "ETAG_OF_MOCKED_RESPONSE"
        }
    }

    @Test
    fun `should return cached response when backend returns unsuccessful response`() {

        // Backend response contains Cache-Control header "public,max-age=300", therefore, okhttp should
        // serve the cached response in case the backend returns an unsuccessful response for a subsequent request

        webServer.enqueue(
            MockResponse()
                .setBody("Poster Template")
                .setResponseCode(200)
                .setHeader("ETag", "ETAG_OF_MOCKED_RESPONSE")
                .setHeader("Cache-Control", "public,max-age=300")
        )

        runTest {
            createAPI().getQrCodePosterTemplate().apply {
                // we should receive the body and ETag
                code() shouldBe 200
                body()!!.string() shouldBe "Poster Template"
                headers()["ETag"] shouldBe "ETAG_OF_MOCKED_RESPONSE"
                headers()["Cache-Control"] shouldBe "public,max-age=300"
            }
        }

        webServer.takeRequest(5, TimeUnit.SECONDS)

        // Second response is unsuccessful ...
        webServer.enqueue(
            MockResponse()
                .setResponseCode(500)
        )

        // ... and in this case, okhttp should serve the cached response
        runTest {
            createAPI().getQrCodePosterTemplate().apply {
                code() shouldBe 200
                raw().cacheResponse shouldNotBe null
                // cached poster template should be returned
                body()!!.string() shouldBe "Poster Template"
                headers()["ETag"] shouldBe "ETAG_OF_MOCKED_RESPONSE"
                headers()["Cache-Control"] shouldBe "public,max-age=300"
            }
        }
    }
}
