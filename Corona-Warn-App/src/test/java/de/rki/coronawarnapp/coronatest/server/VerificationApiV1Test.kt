package de.rki.coronawarnapp.coronatest.server

import android.content.Context
import de.rki.coronawarnapp.coronatest.type.common.DateOfBirthKey
import de.rki.coronawarnapp.http.HttpModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okhttp3.ConnectionSpec
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.extensions.toComparableJson
import testhelpers.extensions.toJsonResponse
import java.io.File
import java.util.concurrent.TimeUnit

class VerificationApiV1Test : BaseIOTest() {

    @MockK private lateinit var context: Context

    private lateinit var webServer: MockWebServer
    private lateinit var serverAddress: String

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private val cacheDir = File(testDir, "cache")
    private val httpCacheDir = File(cacheDir, "http_verification")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { context.cacheDir } returns cacheDir

        webServer = MockWebServer()
        webServer.start()
        serverAddress = "http://${webServer.hostName}:${webServer.port}"
    }

    @AfterEach
    fun teardown() {
        webServer.shutdown()
        testDir.deleteRecursively()
    }

    private fun createAPI(): VerificationApiV1 {
        val httpModule = HttpModule()
        val defaultHttpClient = httpModule.defaultHttpClient()
        val gsonConverterFactory = httpModule.provideGSONConverter()

        return VerificationModule().let {
            val downloadHttpClient = it.cdnHttpClient(
                defaultHttpClient,
                listOf(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS)
            )
            it.provideVerificationApi(
                context = context,
                client = downloadHttpClient,
                url = serverAddress,
                gsonConverterFactory = gsonConverterFactory
            )
        }
    }

    @Test
    fun `test getRegistrationToken - GUID`(): Unit = runTest {
        val api = createAPI()

        """
            {
                "registrationToken": "testRegistrationToken"
            }
        """.toJsonResponse().apply { webServer.enqueue(this) }

        val requestBody = VerificationApiV1.RegistrationTokenRequest(
            keyType = VerificationKeyType.GUID,
            key = "testKey",
            dateOfBirthKey = DateOfBirthKey("testKeyGuid", LocalDate.parse("2020-09-11")).key,
            requestPadding = "testRequestPadding"
        )

        api.getRegistrationToken(
            fake = "0",
            headerPadding = "testPadding",
            requestBody
        ) shouldBe VerificationApiV1.RegistrationTokenResponse(
            registrationToken = "testRegistrationToken"
        )

        webServer.takeRequest(5, TimeUnit.SECONDS)!!.apply {
            headers["cwa-fake"] shouldBe "0"
            headers["cwa-header-padding"] shouldBe "testPadding"
            path shouldBe "/version/v1/registrationToken"
            body.readUtf8() shouldBe """
                {
                    "keyType": "GUID",
                    "key": "testKey",
                    "keyDob": "x9acafb78b330522e32b4bf4c90a3ebb7a4d20d8af8cc32018c550ea86a38cc1",
                    "requestPadding": "testRequestPadding"
                }
            """.toComparableJson()
        }

        httpCacheDir.exists() shouldBe true
    }

    @Test
    fun `test getRegistrationToken - TAN`(): Unit = runTest {
        val api = createAPI()

        """
            {
                "registrationToken": "testRegistrationToken"
            }
        """.toJsonResponse().apply { webServer.enqueue(this) }

        val requestBody = VerificationApiV1.RegistrationTokenRequest(
            keyType = VerificationKeyType.TELETAN,
            key = "testKey",
            requestPadding = "testRequestPadding",
        )

        api.getRegistrationToken(
            fake = "0",
            headerPadding = "testPadding",
            requestBody
        ) shouldBe VerificationApiV1.RegistrationTokenResponse(
            registrationToken = "testRegistrationToken"
        )

        webServer.takeRequest(5, TimeUnit.SECONDS)!!.apply {
            headers["cwa-fake"] shouldBe "0"
            headers["cwa-header-padding"] shouldBe "testPadding"
            path shouldBe "/version/v1/registrationToken"
            body.readUtf8() shouldBe """
                {
                    "keyType": "TELETAN",
                    "key": "testKey",
                    "requestPadding": "testRequestPadding"
                }
            """.toComparableJson()
        }

        httpCacheDir.exists() shouldBe true
    }

    @Test
    fun `test getTestResult`(): Unit = runTest {
        val api = createAPI()

        """
            {
                "testResult": 1
            }
        """.toJsonResponse().apply { webServer.enqueue(this) }

        val requestBody = VerificationApiV1.RegistrationRequest(
            registrationToken = "testRegistrationToken",
            requestPadding = "testRequestPadding"
        )

        api.getTestResult(
            fake = "0",
            headerPadding = "testPadding",
            requestBody
        ) shouldBe VerificationApiV1.TestResultResponse(
            testResult = 1,
            sampleCollectedAt = null,
            labId = null,
        )

        webServer.takeRequest(5, TimeUnit.SECONDS)!!.apply {
            headers["cwa-fake"] shouldBe "0"
            headers["cwa-header-padding"] shouldBe "testPadding"
            path shouldBe "/version/v1/testresult"
            body.readUtf8() shouldBe """
                {
                    "registrationToken": "testRegistrationToken",
                    "requestPadding": "testRequestPadding"
                }
            """.toComparableJson()
        }
    }

    @Test
    fun `test getTAN`(): Unit = runTest {
        val api = createAPI()

        """
            {
                "tan": "testTan"
            }
        """.toJsonResponse().apply { webServer.enqueue(this) }

        val requestBody = VerificationApiV1.TanRequestBody(
            registrationToken = "testRegistrationToken",
            requestPadding = "testRequestPadding"
        )

        api.getTAN(
            fake = "0",
            headerPadding = "testPadding",
            requestBody
        ) shouldBe VerificationApiV1.TanResponse(
            tan = "testTan"
        )

        webServer.takeRequest(5, TimeUnit.SECONDS)!!.apply {
            headers["cwa-fake"] shouldBe "0"
            headers["cwa-header-padding"] shouldBe "testPadding"
            path shouldBe "/version/v1/tan"
            body.readUtf8() shouldBe """
                {
                    "registrationToken": "testRegistrationToken",
                    "requestPadding": "testRequestPadding"
                }
            """.toComparableJson()
        }
    }
}
