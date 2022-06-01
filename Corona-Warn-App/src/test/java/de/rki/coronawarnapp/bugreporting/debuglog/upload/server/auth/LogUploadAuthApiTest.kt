package de.rki.coronawarnapp.bugreporting.debuglog.upload.server.auth

import de.rki.coronawarnapp.bugreporting.BugReportingSharedModule
import de.rki.coronawarnapp.environment.download.DownloadCDNModule
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.http.HttpModule
import de.rki.coronawarnapp.server.protocols.internal.ppdd.ElsOtp
import de.rki.coronawarnapp.server.protocols.internal.ppdd.ElsOtpRequestAndroid
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpacAndroid
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.runTest
import okhttp3.ConnectionSpec
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toJsonResponse
import java.util.concurrent.TimeUnit

class LogUploadAuthApiTest : BaseTest() {

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

    private fun createAPI(): LogUploadAuthApiV1 {
        val httpModule = HttpModule()
        val defaultHttpClient = httpModule.defaultHttpClient()
        val gsonConverterFactory = httpModule.provideGSONConverter()
        val protoConverterFactory = httpModule.provideProtoConverter()

        val cdnHttpClient = DownloadCDNModule()
            .cdnHttpClient(defaultHttpClient)
            .newBuilder()
            .connectionSpecs(listOf(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS))
            .build()

        return BugReportingSharedModule.logUploadAuthApi(
            client = cdnHttpClient,
            url = serverAddress,
            gsonConverterFactory = gsonConverterFactory,
            protoConverterFactory = protoConverterFactory
        )
    }

    @Test
    fun `test auth`(): Unit = runTest {
        """
            {
                expirationDate : "2020-08-20T14:00:00.000Z"
            }
        """.toJsonResponse().apply { webServer.enqueue(this) }

        val elsPayload = ElsOtpRequestAndroid.ELSOneTimePasswordRequestAndroid.newBuilder()
            .setPayload(ElsOtp.ELSOneTimePassword.newBuilder().setOtp("15cff19f-af26-41bc-94f2-c1a65075e894"))
            .setAuthentication(PpacAndroid.PPACAndroid.newBuilder().setSafetyNetJws("abc").setSalt("def"))
            .build()

        val api = createAPI()
        api.authOTP(requestBody = elsPayload)

        webServer.takeRequest(5, TimeUnit.SECONDS)!!.apply {
            path shouldBe "/version/v1/android/els"
            body.readByteArray() shouldBe elsPayload.toByteArray()
        }
    }

    @Test
    fun `server returns 500`(): Unit = runTest {
        """
            {
                errorCode: "Nope"
            }
        """.toJsonResponse().apply { webServer.enqueue(MockResponse().setResponseCode(500)) }

        val elsPayload = ElsOtpRequestAndroid.ELSOneTimePasswordRequestAndroid.newBuilder()
            .setPayload(ElsOtp.ELSOneTimePassword.newBuilder().setOtp("15cff19f-af26-41bc-94f2-c1a65075e894"))
            .setAuthentication(PpacAndroid.PPACAndroid.newBuilder().setSafetyNetJws("abc").setSalt("def"))
            .build()

        val api = createAPI()

        shouldThrow<CwaWebException> {
            api.authOTP(requestBody = elsPayload)
        }
    }
}
