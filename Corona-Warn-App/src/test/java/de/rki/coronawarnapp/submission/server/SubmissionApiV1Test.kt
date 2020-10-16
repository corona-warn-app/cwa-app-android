package de.rki.coronawarnapp.submission.server

import android.content.Context
import com.google.protobuf.ByteString
import de.rki.coronawarnapp.http.HttpModule
import de.rki.coronawarnapp.server.protocols.KeyExportFormat
import de.rki.coronawarnapp.submission.SubmissionModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import okhttp3.ConnectionSpec
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.BaseTest
import testhelpers.extensions.toJsonResponse
import java.io.File
import java.util.concurrent.TimeUnit

class SubmissionApiV1Test : BaseTest() {

    @MockK
    private lateinit var context: Context

    private lateinit var webServer: MockWebServer
    private lateinit var serverAddress: String

    private val testDir = File(BaseIOTest.IO_TEST_BASEDIR, this::class.java.simpleName)
    private val cacheDir = File(testDir, "cache")
    private val httpCacheDir = File(cacheDir, "http_submission")

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
        clearAllMocks()
        webServer.shutdown()
    }

    private fun createAPI(): SubmissionApiV1 {
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
    fun `test submitKeys`(): Unit = runBlocking {
        val api = createAPI()

        """
            {
                "tan": "testTan"
            }
        """.toJsonResponse().apply { webServer.enqueue(this) }

        val submissionPayload = KeyExportFormat.SubmissionPayload.newBuilder()
            .setPadding(ByteString.copyFromUtf8("fakeKeyPadding"))
            .build()

        api.submitKeys(
            authCode = "testAuthCode",
            fake = "0",
            headerPadding = "testPadding",
            requestBody = submissionPayload
        )

        webServer.takeRequest(5, TimeUnit.SECONDS)!!.apply {
            headers["cwa-authorization"] shouldBe "testAuthCode"
            headers["cwa-fake"] shouldBe "0"
            headers["cwa-header-padding"] shouldBe "testPadding"
            path shouldBe "/version/v1/diagnosis-keys"
            body.readUtf8() shouldBe """fakeKeyPadding"""
        }

        httpCacheDir.exists() shouldBe true
    }
}
