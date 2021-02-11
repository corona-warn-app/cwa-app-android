package de.rki.coronawarnapp.datadonation.survey.server

import android.content.Context
import com.google.protobuf.ByteString
import de.rki.coronawarnapp.datadonation.survey.SurveyModule
import de.rki.coronawarnapp.http.HttpModule
import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtp
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toJsonResponse
import java.util.concurrent.TimeUnit

class SurveyApiV1Test : BaseTest() {

    @MockK
    private lateinit var context: Context

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
        clearAllMocks()
        webServer.shutdown()
    }

    private fun createAPI(): SurveyApiV1 {
        val httpModule = HttpModule()

        return SurveyModule().let {
            val downloadHttpClient = httpModule.defaultHttpClient()
            it.provideSurveyApi(
                client = downloadHttpClient,
                url = serverAddress,
                gsonConverterFactory = httpModule.provideGSONConverter(),
                protoConverterFactory = httpModule.provideProtoConverter()
            )
        }
    }

    @Test
    fun `test auth`(): Unit = runBlocking {
        val api = createAPI()

        """
            {
            }
        """.toJsonResponse().apply { webServer.enqueue(this) }

        val dataDonationPayload = EdusOtp.EDUSOneTimePassword.newBuilder()
            .setOtp("15cff19f-af26-41bc-94f2-c1a65075e894")
            .setOtpBytes(
                ByteString.copyFrom(
                    "MTVjZmYxOWYtYWYyNi00MWJjLTk0ZjItYzFhNjUwNzVlODk0".decodeBase64()!!.toByteArray()
                )
            )
            .build()

        api.authOTP(
            requestBody = dataDonationPayload
        )

        webServer.takeRequest(5, TimeUnit.SECONDS)!!.apply {
            path shouldBe "/version/v1/android/otp"
            body.readUtf8() shouldBe "15cff19f-af26-41bc-94f2-c1a65075e894"
        }
    }
}
