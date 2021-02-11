package de.rki.coronawarnapp.datadonation.survey.server

import android.content.Context
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtp
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.BaseTest
import java.io.File
import java.util.UUID

class SurveyServerTest : BaseTest() {
    @MockK lateinit var surveyApi: SurveyApiV1
    @MockK lateinit var context: Context

    private lateinit var webServer: MockWebServer
    private lateinit var serverAddress: String

    private val testDir = File(BaseIOTest.IO_TEST_BASEDIR, this::class.java.simpleName)
    private val cacheDir = File(testDir, "cache")

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
        customApi: SurveyApiV1 = surveyApi
    ) = SurveyServer(dataDonationApi = { customApi })

    @Test
    fun `valid otp`(): Unit = runBlocking {
        val server = createServer()
        coEvery { surveyApi.authOTP(any()) } answers {
            arg<EdusOtp.EDUSOneTimePassword>(0).apply {
                otp shouldBe "15cff19f-af26-41bc-94f2-c1a65075e894"
            }
            SurveyApiV1.DataDonationResponse(
                "2021-02-16T08:34:00+00:00"
            )
        }

        val data = OneTimePassword(UUID.fromString("15cff19f-af26-41bc-94f2-c1a65075e894"))
        server.authOTP(data).expirationDate shouldBe "2021-02-16T08:34:00+00:00"

        coVerify { surveyApi.authOTP(any()) }
    }

    @Test
    fun `invalid otp`(): Unit = runBlocking {
        val server = createServer()
        coEvery { surveyApi.authOTP(any()) } answers {
            arg<EdusOtp.EDUSOneTimePassword>(0).apply {
                otp shouldBe "15cff19f-af26-41bc-94f2-c1a65075e894"
            }
            SurveyApiV1.DataDonationResponse(null)
        }

        val data = OneTimePassword(UUID.fromString("15cff19f-af26-41bc-94f2-c1a65075e894"))
        server.authOTP(data).expirationDate shouldBe null

        coVerify { surveyApi.authOTP(any()) }
    }
}
