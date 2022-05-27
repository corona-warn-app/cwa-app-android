package de.rki.coronawarnapp.coronatest.server

import android.content.Context
import de.rki.coronawarnapp.coronatest.type.common.DateOfBirthKey
import de.rki.coronawarnapp.http.HttpModule
import de.rki.coronawarnapp.util.PaddingTool
import de.rki.coronawarnapp.util.headerSizeIgnoringContentLength
import de.rki.coronawarnapp.util.requestHeaderWithoutContentLength
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okhttp3.ConnectionSpec
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.time.Duration
import java.time.LocalDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import timber.log.Timber
import java.io.File
import java.security.SecureRandom
import kotlin.random.asKotlinRandom

class VerificationServerTest : BaseIOTest() {

    @MockK lateinit var verificationApi: VerificationApiV1
    @MockK private lateinit var context: Context

    private lateinit var webServer: MockWebServer
    private lateinit var serverAddress: String

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private val cacheDir = File(testDir, "cache")

    private val requestTan = RegistrationRequest(
        "testKeyTan",
        VerificationKeyType.TELETAN
    )

    private val requestGuid = RegistrationRequest(
        key = "testKeyGuid",
        type = VerificationKeyType.GUID,
        dateOfBirthKey = DateOfBirthKey("testKeyGuid", LocalDate.parse("2020-09-11"))
    )

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
        webServer.shutdown()
        testDir.deleteRecursively()
    }

    private fun createServer(
        customApi: VerificationApiV1 = verificationApi
    ) = VerificationServer(
        verificationAPI = { customApi },
        paddingTool = PaddingTool(
            sourceFast = SecureRandom().asKotlinRandom(),
        )
    )

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
    fun `get registration token via GUID - with dobHash`(): Unit = runTest {
        val server = createServer()
        coEvery { verificationApi.getRegistrationToken(any(), any(), any()) } answers {
            arg<String>(0) shouldBe "0"
            arg<String>(1) shouldBe ""
            arg<VerificationApiV1.RegistrationTokenRequest>(2).apply {
                keyType shouldBe VerificationKeyType.GUID
                key shouldBe "7620a19f93374e8d5acff090d3c10d0242a32fe140c50bbd40c95edf3c0af5b7"
                requestPadding!!.length shouldBe 63
                dateOfBirthKey shouldBe requestGuid.dateOfBirthKey!!.key
            }
            VerificationApiV1.RegistrationTokenResponse(
                registrationToken = "testRegistrationToken"
            )
        }

        server.retrieveRegistrationToken(requestGuid) shouldBe "testRegistrationToken"

        coVerify { verificationApi.getRegistrationToken(any(), any(), any()) }
    }

    @Test
    fun `get registration token via GUID - without dobHash`(): Unit = runTest {
        val server = createServer()
        coEvery { verificationApi.getRegistrationToken(any(), any(), any()) } answers {
            arg<String>(0) shouldBe "0"
            arg<String>(1) shouldBe ""
            arg<VerificationApiV1.RegistrationTokenRequest>(2).apply {
                keyType shouldBe VerificationKeyType.GUID
                key shouldBe "7620a19f93374e8d5acff090d3c10d0242a32fe140c50bbd40c95edf3c0af5b7"
                requestPadding!!.length shouldBe 139
                dateOfBirthKey shouldBe null
            }
            VerificationApiV1.RegistrationTokenResponse(
                registrationToken = "testRegistrationToken"
            )
        }

        server.retrieveRegistrationToken(
            requestGuid.copy(dateOfBirthKey = null)
        ) shouldBe "testRegistrationToken"

        coVerify { verificationApi.getRegistrationToken(any(), any(), any()) }
    }

    @Test
    fun `get registration token via TELETAN`() = runTest {
        val server = createServer()
        coEvery { verificationApi.getRegistrationToken(any(), any(), any()) } answers {
            arg<String>(0) shouldBe "0"
            arg<String>(1) shouldBe ""
            arg<VerificationApiV1.RegistrationTokenRequest>(2).apply {
                keyType shouldBe VerificationKeyType.TELETAN
                key shouldBe "testKeyTan"
                requestPadding!!.length shouldBe 190
                dateOfBirthKey shouldBe null
            }
            VerificationApiV1.RegistrationTokenResponse(
                registrationToken = "testRegistrationToken"
            )
        }

        server.retrieveRegistrationToken(requestTan) shouldBe "testRegistrationToken"

        coVerify { verificationApi.getRegistrationToken(any(), any(), any()) }
    }

    @Test
    fun `get test result`(): Unit = runTest {
        val server = createServer()
        coEvery { verificationApi.getTestResult(any(), any(), any()) } answers {
            arg<String>(0) shouldBe "0"
            arg<String>(1).length shouldBe 7 // Header-padding
            arg<VerificationApiV1.RegistrationRequest>(2).apply {
                registrationToken shouldBe "testRegistrationToken"
                requestPadding.length shouldBe 170
            }
            VerificationApiV1.TestResultResponse(testResult = 2, sampleCollectedAt = null, labId = null)
        }

        server.pollTestResult("testRegistrationToken") shouldBe CoronaTestResultResponse(
            coronaTestResult = CoronaTestResult.PCR_POSITIVE,
            sampleCollectedAt = null,
            labId = null,
        )

        coVerify { verificationApi.getTestResult(any(), any(), any()) }
    }

    @Test
    fun `get TAN`(): Unit = runTest {
        val server = createServer()
        coEvery { verificationApi.getTAN(any(), any(), any()) } answers {
            arg<String>(0) shouldBe "0"
            arg<String>(1).length shouldBe 14 // Header-padding
            arg<VerificationApiV1.TanRequestBody>(2).apply {
                registrationToken shouldBe "testRegistrationToken"
                requestPadding.length shouldBe 170
            }
            VerificationApiV1.TanResponse(tan = "testTan")
        }

        server.retrieveTan("testRegistrationToken") shouldBe "testTan"

        coVerify { verificationApi.getTAN(any(), any(), any()) }
    }

    @Test
    fun `get TAN with fake data`(): Unit = runTest {
        val server = createServer()
        coEvery { verificationApi.getTAN(any(), any(), any()) } answers {
            arg<String>(0) shouldBe "1"
            arg<String>(1).length shouldBe 14 // Header-padding
            arg<VerificationApiV1.TanRequestBody>(2).apply {
                registrationToken shouldBe "11111111-2222-4444-8888-161616161616"
                requestPadding.length shouldBe 170
            }
            VerificationApiV1.TanResponse(tan = "testTan")
        }

        server.retrieveTanFake() shouldBe VerificationApiV1.TanResponse(tan = "testTan")

        coVerify { verificationApi.getTAN(any(), any(), any()) }
    }

    @Test
    fun `all requests have the same footprint for pleasible deniability`(): Unit = runTest {
        val registrationTokenExample = "63b4d3ff-e0de-4bd4-90c1-17c2bb683a2f"

        val requests = mutableListOf<RecordedRequest>()

        val api = createServer(createRealApi())

        // Default happy path
        webServer.enqueue(MockResponse().setBody("{}"))
        api.retrieveRegistrationToken(requestGuid)
        webServer.takeRequest().also { requests.add(it) }.bodySize shouldBe 250L

        // No dobHash
        webServer.enqueue(MockResponse().setBody("{}"))
        api.retrieveRegistrationToken(requestGuid)
        webServer.takeRequest().also { requests.add(it) }.bodySize shouldBe 250L

        // Second happy path try
        webServer.enqueue(MockResponse().setBody("{}"))
        api.retrieveRegistrationToken(requestGuid.copy(key = "3BF1D4-1C6003DD-733D-41F1-9F30-F85FA7406BF7"))
        webServer.takeRequest().also { requests.add(it) }.bodySize shouldBe 250L

        // Via tan
        webServer.enqueue(MockResponse().setBody("{}"))
        api.retrieveRegistrationToken(requestTan.copy(key = "9A3B578UMG"))
        webServer.takeRequest().also { requests.add(it) }.bodySize shouldBe 250L

        // Polling for test result
        webServer.enqueue(MockResponse().setBody("{}"))
        api.pollTestResult(registrationTokenExample)
        webServer.takeRequest().also { requests.add(it) }.bodySize shouldBe 250L

        // Submission TAN
        webServer.enqueue(MockResponse().setBody("{}"))
        api.retrieveTan(registrationTokenExample)
        webServer.takeRequest().also { requests.add(it) }.bodySize shouldBe 250L

        // Playbook dummy request
        webServer.enqueue(MockResponse().setBody("{}"))
        api.retrieveTanFake()
        webServer.takeRequest().also { requests.add(it) }.bodySize shouldBe 250L

        requests.zipWithNext().forEach { (a, b) ->
            Timber.i("Header a: %s", a.requestHeaderWithoutContentLength().replace('\n', ' '))
            Timber.i("Header b: %s", b.requestHeaderWithoutContentLength().replace('\n', ' '))
            a.headerSizeIgnoringContentLength() shouldBe b.headerSizeIgnoringContentLength()
        }
    }

    @Test
    fun `test availability constant`() {
        VerificationServer.TestAvailabilityDuration shouldBe Duration.ofDays(60)
    }
}
