package de.rki.coronawarnapp.bugreporting.debuglog.upload.server

import de.rki.coronawarnapp.bugreporting.BugReportingSharedModule
import de.rki.coronawarnapp.environment.download.DownloadCDNModule
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.http.HttpModule
import de.rki.coronawarnapp.util.files.determineMimeType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.runBlocking
import okhttp3.ConnectionSpec
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.extensions.toJsonResponse
import java.io.File
import java.util.concurrent.TimeUnit

class LogUploadApiTest : BaseIOTest() {

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private lateinit var webServer: MockWebServer
    private lateinit var serverAddress: String
    private val otp = "15cff19f-af26-41bc-94f2-c1a65075e894"
    private val expectedId = "11111111-af26-41bc-94f2-000000000000"
    private val expectedHash = "22222222-af26-41bc-94f2-000000000000"
    private val testSnapshotFile = File(testDir, "snapshot.zip")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        webServer = MockWebServer()
        webServer.start()
        serverAddress = "http://${webServer.hostName}:${webServer.port}"

        testDir.mkdirs()
        testSnapshotFile.writeText("We needed this months ago.")
        testSnapshotFile.exists() shouldBe true
    }

    @AfterEach
    fun teardown() {
        webServer.shutdown()
        testDir.deleteRecursively()
    }

    private fun createAPI(): LogUploadApiV1 {
        val httpModule = HttpModule()
        val defaultHttpClient = httpModule.defaultHttpClient()
        val gsonConverterFactory = httpModule.provideGSONConverter()
        val protoConverterFactory = httpModule.provideProtoConverter()

        val cdnHttpClient = DownloadCDNModule()
            .cdnHttpClient(defaultHttpClient)
            .newBuilder()
            .connectionSpecs(listOf(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS))
            .build()

        return BugReportingSharedModule.logUploadApi(
            client = cdnHttpClient,
            url = serverAddress,
            gsonConverterFactory = gsonConverterFactory,
            protoConverterFactory = protoConverterFactory
        )
    }

    @Test
    fun `happy upload`(): Unit = runBlocking {
        """
            {
                id :  "$expectedId",
                hash: "$expectedHash"
            }
        """.toJsonResponse().apply { webServer.enqueue(this) }

        val api = createAPI()
        api.uploadLog(
            otp = otp,
            logZip = MultipartBody.Part.createFormData(
                name = "file",
                filename = testSnapshotFile.name,
                body = testSnapshotFile.asRequestBody(testSnapshotFile.determineMimeType().toMediaType())
            )
        )

        webServer.takeRequest(5, TimeUnit.SECONDS)!!.apply {
            path shouldBe "/api/logs"
            val boundary = this.headers["Content-Type"]!!.replace("multipart/form-data; boundary=", "")
            body.readUtf8().replace("\r\n", "\n") shouldBe """
                --$boundary
                Content-Disposition: form-data; name="file"; filename="snapshot.zip"
                Content-Type: application/zip
                Content-Length: 26
                
                We needed this months ago.
                --$boundary--
                
            """.trimIndent()
        }
    }

    @Test
    fun `server returns 500`(): Unit = runBlocking {
        """
            {
                id :  "$expectedId",
                hash: "$expectedHash"
            }
        """.toJsonResponse().apply { webServer.enqueue(MockResponse().setResponseCode(500)) }

        val api = createAPI()

        shouldThrow<CwaWebException> {
            api.uploadLog(
                otp = otp,
                logZip = MultipartBody.Part.createFormData(
                    name = "file",
                    filename = testSnapshotFile.name,
                    body = testSnapshotFile.asRequestBody(testSnapshotFile.determineMimeType().toMediaType())
                )
            )
        }
    }
}
