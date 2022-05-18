package de.rki.coronawarnapp.datadonation.survey.server

import com.google.protobuf.ByteString
import de.rki.coronawarnapp.datadonation.survey.SurveyModule
import de.rki.coronawarnapp.http.HttpModule
import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtp
import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtpRequestAndroid
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpacAndroid
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toJsonResponse
import java.util.concurrent.TimeUnit

class SurveyApiV1Test : BaseTest() {

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

    private fun createAPI(): SurveyApiV1 {
        val httpModule = HttpModule()

        return SurveyModule.let {
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
    fun `test auth`(): Unit = runTest {
        val api = createAPI()
        """
            {
            
            }
        """.toJsonResponse().apply { webServer.enqueue(this) }

        val surveyPayload = EdusOtpRequestAndroid.EDUSOneTimePasswordRequestAndroid.newBuilder()
            .setPayload(
                EdusOtp.EDUSOneTimePassword.newBuilder()
                    .setOtp("15cff19f-af26-41bc-94f2-c1a65075e894")
                    .setOtpBytes(
                        ByteString.copyFrom(
                            "MTVjZmYxOWYtYWYyNi00MWJjLTk0ZjItYzFhNjUwNzVlODk0".decodeBase64()!!.toByteArray()
                        )
                    )
            )
            .setAuthentication(
                PpacAndroid.PPACAndroid.newBuilder()
                    .setSafetyNetJws("abc")
                    .setSalt("def")
            )
            .build()

        api.authOTP(
            requestBody = surveyPayload
        )

        webServer.takeRequest(5, TimeUnit.SECONDS)!!.apply {
            path shouldBe "/version/v1/android/otp"
            body.readByteArray() shouldBe surveyPayload.toByteArray()
        }
    }

    @Test
    fun `server returns 500`(): Unit = runTest {
        val api = createAPI()
        """
            {
            
            }
        """.toJsonResponse().apply { webServer.enqueue(MockResponse().setResponseCode(500)) }

        val surveyPayload = EdusOtpRequestAndroid.EDUSOneTimePasswordRequestAndroid.newBuilder()
            .setPayload(
                EdusOtp.EDUSOneTimePassword.newBuilder()
                    .setOtp("15cff19f-af26-41bc-94f2-c1a65075e894")
                    .setOtpBytes(
                        ByteString.copyFrom(
                            "MTVjZmYxOWYtYWYyNi00MWJjLTk0ZjItYzFhNjUwNzVlODk0".decodeBase64()!!.toByteArray()
                        )
                    )
            )
            .setAuthentication(
                PpacAndroid.PPACAndroid.newBuilder()
                    .setSafetyNetJws("abc")
                    .setSalt("def")
            )
            .build()

        shouldThrowAny {
            api.authOTP(
                requestBody = surveyPayload
            )
        }
    }
}
