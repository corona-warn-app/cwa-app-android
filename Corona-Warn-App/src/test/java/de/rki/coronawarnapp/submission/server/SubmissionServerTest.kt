package de.rki.coronawarnapp.submission.server

import android.content.Context
import com.google.protobuf.ByteString
import de.rki.coronawarnapp.http.HttpModule
import de.rki.coronawarnapp.server.protocols.external.exposurenotification.TemporaryExposureKeyExportOuterClass
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass
import de.rki.coronawarnapp.submission.SubmissionModule
import de.rki.coronawarnapp.util.headerSizeIgnoringContentLength
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
import testhelpers.BaseTest
import java.io.File

class SubmissionServerTest : BaseTest() {
    @MockK lateinit var submissionApi: SubmissionApiV1
    @MockK lateinit var context: Context

    private lateinit var webServer: MockWebServer
    private lateinit var serverAddress: String

    private val testDir = File(BaseIOTest.IO_TEST_BASEDIR, this::class.java.simpleName)
    private val cacheDir = File(testDir, "cache")
    private val httpCacheDir = File(cacheDir, "http_submission")

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
        customApi: SubmissionApiV1 = submissionApi
    ) = SubmissionServer(submissionApi = { customApi })

    @Test
    fun `normal submission`(): Unit = runBlocking {
        val testKeyData = ByteString.copyFrom("TestKeyDataGoogle", Charsets.UTF_8)

        val server = createServer()
        coEvery { submissionApi.submitKeys(any(), any(), any(), any()) } answers {
            arg<String>(0) shouldBe "testAuthCode"
            arg<String>(1) shouldBe "0"
            arg<String>(2) shouldBe ""
            arg<SubmissionPayloadOuterClass.SubmissionPayload>(3).apply {
                keysList.single().keyData shouldBe testKeyData
                requestPadding.size() shouldBe 364
                hasConsentToFederation() shouldBe true
                visitedCountriesList shouldBe listOf("DE")
            }
            Unit
        }

        val googleKeyList = TemporaryExposureKeyExportOuterClass.TemporaryExposureKey
            .newBuilder()
            .setKeyData(testKeyData)
            .build()

        val submissionData = SubmissionServer.SubmissionData(
            authCode = "testAuthCode",
            keyList = listOf(googleKeyList),
            consentToFederation = true,
            visistedCountries = listOf("DE")
        )
        server.submitKeysToServer(submissionData)

        coVerify { submissionApi.submitKeys(any(), any(), any(), any()) }
    }

    @Test
    fun `fake submission`(): Unit = runBlocking {
        val server = createServer()
        coEvery { submissionApi.submitKeys(any(), any(), any(), any()) } answers {
            arg<String>(0) shouldBe "" // cwa-authorization
            arg<String>(1) shouldBe "1" // cwa-fake
            arg<String>(2).length shouldBe 36 // cwa-header-padding
            arg<SubmissionPayloadOuterClass.SubmissionPayload>(3).apply {
                keysList.size shouldBe 0
                requestPadding.size() shouldBe 392
                hasConsentToFederation() shouldBe false
                visitedCountriesList shouldBe emptyList()
            }
            Unit
        }

        server.submitKeysToServerFake()

        coVerify { submissionApi.submitKeys(any(), any(), any(), any()) }
    }

    private fun createRealApi(): SubmissionApiV1 {
        val httpModule = HttpModule()
        val defaultHttpClient = httpModule.defaultHttpClient()

        return SubmissionModule().let {
            val downloadHttpClient = it.cdnHttpClient(
                defaultHttpClient,
                listOf(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS)
            )
            it.provideSubmissionApi(
                context = context,
                client = downloadHttpClient,
                url = serverAddress,
                gsonConverterFactory = httpModule.provideGSONConverter(),
                protoConverterFactory = httpModule.provideProtoConverter()
            )
        }
    }

    @Test
    fun allRequestHaveSameFootprintForPlausibleDeniability(): Unit = runBlocking {
        val server = createServer(createRealApi())

        val testKeyData = ByteString.copyFrom("TestKeyDataGoogle", Charsets.UTF_8)
        val googleKeyList = TemporaryExposureKeyExportOuterClass.TemporaryExposureKey
            .newBuilder()
            .setKeyData(testKeyData)
            .build()
        val submissionData = SubmissionServer.SubmissionData(
            authCode = "39ec4930-7a1f-4d5d-921f-bfad3b6f1269",
            keyList = listOf(googleKeyList),
            consentToFederation = true,
            visistedCountries = listOf("DE")
        )
        webServer.enqueue(MockResponse().setBody("{}"))
        server.submitKeysToServer(submissionData)

        webServer.enqueue(MockResponse().setBody("{}"))
        server.submitKeysToServerFake()

        val requests = listOf(
            webServer.takeRequest(),
            webServer.takeRequest()
        )

        // ensure all request have same size (header & body)
        requests.zipWithNext().forEach { (a, b) ->
            a.headerSizeIgnoringContentLength() shouldBe b.headerSizeIgnoringContentLength()
        }
    }
}
