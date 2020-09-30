package de.rki.coronawarnapp.verification.server

import android.content.Context
import de.rki.coronawarnapp.http.HttpModule
import de.rki.coronawarnapp.util.headerSizeIgnoringContentLength
import de.rki.coronawarnapp.verification.VerificationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import okhttp3.ConnectionSpec
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class VerificationServerTest : BaseIOTest() {

    @MockK lateinit var verificationApi: VerificationApiV1
    @MockK private lateinit var context: Context

    private lateinit var webServer: MockWebServer
    private lateinit var serverAddress: String

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private val cacheDir = File(testDir, "cache")
    private val httpCacheDir = File(cacheDir, "http_verification")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true
        every { context.cacheDir } returns cacheDir

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

    private fun createServer(
        customApi: VerificationApiV1 = verificationApi
    ) = VerificationServer(verificationAPI = { customApi })

    @Test
    fun `get registration token via GUID`(): Unit = runBlocking {
        val server = createServer()
        coEvery { verificationApi.getRegistrationToken(any(), any(), any()) } answers {
            arg<String>(0) shouldBe "0"
            arg<String>(1) shouldBe ""
            arg<VerificationApiV1.RegistrationTokenRequest>(2).apply {
                keyType shouldBe VerificationKeyType.GUID.name
                key shouldBe "15291f67d99ea7bc578c3544dadfbb991e66fa69cb36ff70fe30e798e111ff5f"
                requestPadding!!.length shouldBe 139
            }
            VerificationApiV1.RegistrationTokenResponse(
                registrationToken = "testRegistrationToken"
            )
        }

        server.retrieveRegistrationToken(
            "testKey", VerificationKeyType.GUID
        ) shouldBe "testRegistrationToken"

        coVerify { verificationApi.getRegistrationToken(any(), any(), any()) }
    }

    @Test
    fun `get registration token via TELETAN`() = runBlocking {
        val server = createServer()
        coEvery { verificationApi.getRegistrationToken(any(), any(), any()) } answers {
            arg<String>(0) shouldBe "0"
            arg<String>(1) shouldBe ""
            arg<VerificationApiV1.RegistrationTokenRequest>(2).apply {
                keyType shouldBe VerificationKeyType.TELETAN.name
                key shouldBe "testKey"
                requestPadding!!.length shouldBe 190
            }
            VerificationApiV1.RegistrationTokenResponse(
                registrationToken = "testRegistrationToken"
            )
        }

        server.retrieveRegistrationToken(
            "testKey", VerificationKeyType.TELETAN
        ) shouldBe "testRegistrationToken"

        coVerify { verificationApi.getRegistrationToken(any(), any(), any()) }
    }

    @Test
    fun `get test result`(): Unit = runBlocking {
        val server = createServer()
        coEvery { verificationApi.getTestResult(any(), any(), any()) } answers {
            arg<String>(0) shouldBe "0"
            arg<String>(1).length shouldBe 7 // Header-padding
            arg<VerificationApiV1.RegistrationRequest>(2).apply {
                registrationToken shouldBe "testRegistrationToken"
                requestPadding!!.length shouldBe 170
            }
            VerificationApiV1.TestResultResponse(testResult = 2)
        }

        server.retrieveTestResults("testRegistrationToken") shouldBe 2

        coVerify { verificationApi.getTestResult(any(), any(), any()) }
    }

    @Test
    fun `get TAN`(): Unit = runBlocking {
        val server = createServer()
        coEvery { verificationApi.getTAN(any(), any(), any()) } answers {
            arg<String>(0) shouldBe "0"
            arg<String>(1).length shouldBe 14 // Header-padding
            arg<VerificationApiV1.TanRequestBody>(2).apply {
                registrationToken shouldBe "testRegistrationToken"
                requestPadding!!.length shouldBe 170
            }
            VerificationApiV1.TanResponse(tan = "testTan")
        }

        server.retrieveTan("testRegistrationToken") shouldBe "testTan"

        coVerify { verificationApi.getTAN(any(), any(), any()) }
    }

    @Test
    fun `get TAN with fake data`(): Unit = runBlocking {
        val server = createServer()
        coEvery { verificationApi.getTAN(any(), any(), any()) } answers {
            arg<String>(0) shouldBe "1"
            arg<String>(1).length shouldBe 14 // Header-padding
            arg<VerificationApiV1.TanRequestBody>(2).apply {
                registrationToken shouldBe "11111111-2222-4444-8888-161616161616"
                requestPadding!!.length shouldBe 170
            }
            VerificationApiV1.TanResponse(tan = "testTan")
        }

        server.retrieveTanFake() shouldBe VerificationApiV1.TanResponse(tan = "testTan")

        coVerify { verificationApi.getTAN(any(), any(), any()) }
    }

    private fun createRealApi(): VerificationApiV1 {
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
    fun `all requests have the same footprint for pleasible deniability`(): Unit = runBlocking {
        val guidExample = "3BF1D4-1C6003DD-733D-41F1-9F30-F85FA7406BF7"
        val teletanExample = "9A3B578UMG"
        val registrationTokenExample = "63b4d3ff-e0de-4bd4-90c1-17c2bb683a2f"

        val api = createServer(createRealApi())
        webServer.enqueue(MockResponse().setBody("{}"))
        api.retrieveRegistrationToken(guidExample, VerificationKeyType.GUID)

        webServer.enqueue(MockResponse().setBody("{}"))
        api.retrieveRegistrationToken(teletanExample, VerificationKeyType.TELETAN)

        webServer.enqueue(MockResponse().setBody("{}"))
        api.retrieveTestResults(registrationTokenExample)

        webServer.enqueue(MockResponse().setBody("{}"))
        api.retrieveTan(registrationTokenExample)

        webServer.enqueue(MockResponse().setBody("{}"))
        api.retrieveTanFake()

        val requests = listOf(
            webServer.takeRequest(),
            webServer.takeRequest(),
            webServer.takeRequest(),
            webServer.takeRequest(),
            webServer.takeRequest()
        )

        // ensure all request have same size (header & body)
        requests.forEach { it.bodySize shouldBe 250L }

        requests.zipWithNext().forEach { (a, b) ->
            a.headerSizeIgnoringContentLength() shouldBe b.headerSizeIgnoringContentLength()
        }
    }
}
