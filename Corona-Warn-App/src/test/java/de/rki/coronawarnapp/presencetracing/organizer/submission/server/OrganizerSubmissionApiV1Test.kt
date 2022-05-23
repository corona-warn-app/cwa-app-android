package de.rki.coronawarnapp.presencetracing.organizer.submission.server

import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.http.HttpModule
import de.rki.coronawarnapp.presencetracing.organizer.submission.OrganizerSubmissionModule
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testhelpers.BaseTest
import java.util.concurrent.TimeUnit

class OrganizerSubmissionApiV1Test : BaseTest() {

    private val uploadTan = "TestUploadTan"
    private val requestBody = SubmissionPayloadOuterClass.SubmissionPayload.newBuilder().build()

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
    fun tearDown() {
        webServer.shutdown()
    }

    private fun createApi(): OrganizerSubmissionApiV1 = OrganizerSubmissionModule().let {
        val httpModule = HttpModule()
        val client = httpModule.defaultHttpClient()
        val protoConverter = httpModule.provideProtoConverter()

        it.provideOrganizerSubmissionApiV1(
            client = client,
            url = serverAddress,
            protoConverterFactory = protoConverter
        )
    }

    @Test
    fun `check submission request`() = runTest {
        webServer.enqueue(MockResponse())

        createApi().submitCheckInsOnBehalf(authCode = uploadTan, requestBody = requestBody)

        webServer.takeRequest(5, TimeUnit.SECONDS)!!.run {
            headers["cwa-authorization"] shouldBe uploadTan
            path shouldBe "/version/v1/submission-on-behalf"
            body.readUtf8() shouldBe """"""
        }
    }

    @Test
    fun `delivers response codes`(): Unit = runTest {
        val apiV1 = createApi()

        // Successful submission doesn't throw
        webServer.enqueue(MockResponse().setResponseCode(200))
        apiV1.submitCheckInsOnBehalf(authCode = uploadTan, requestBody = requestBody)

        assertThrows<CwaClientError> {
            webServer.enqueue(MockResponse().setResponseCode(400))
            apiV1.submitCheckInsOnBehalf(authCode = uploadTan, requestBody = requestBody)
        }

        assertThrows<CwaClientError> {
            webServer.enqueue(MockResponse().setResponseCode(403))
            apiV1.submitCheckInsOnBehalf(authCode = uploadTan, requestBody = requestBody)
        }

        assertThrows<CwaServerError> {
            webServer.enqueue(MockResponse().setResponseCode(500))
            apiV1.submitCheckInsOnBehalf(authCode = uploadTan, requestBody = requestBody)
        }
    }
}
