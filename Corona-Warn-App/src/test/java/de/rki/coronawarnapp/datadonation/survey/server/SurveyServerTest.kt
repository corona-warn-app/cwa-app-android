package de.rki.coronawarnapp.datadonation.survey.server

import android.content.Context
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtp
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import java.io.File
import java.util.UUID

class SurveyServerTest : BaseTest() {
    @MockK lateinit var surveyApi: SurveyApiV1
    @MockK lateinit var context: Context

    private val testDir = File(BaseIOTest.IO_TEST_BASEDIR, this::class.java.simpleName)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    private fun createServer(
        customApi: SurveyApiV1 = surveyApi
    ) = SurveyServer(surveyApi = { customApi }, TestDispatcherProvider())

    @Test
    fun `valid otp`(): Unit = runBlocking {
        val server = createServer()
        coEvery { surveyApi.authOTP(any()) } answers {
            arg<EdusOtp.EDUSOneTimePassword>(0).apply {
                otp shouldBe "15cff19f-af26-41bc-94f2-c1a65075e894"
            }
            SurveyApiV1.DataDonationResponse(null)
        }

        val data = OneTimePassword(UUID.fromString("15cff19f-af26-41bc-94f2-c1a65075e894"))
        server.authOTP(data, mockk()).errorCode shouldBe null

        coVerify { surveyApi.authOTP(any()) }
    }

    @Test
    fun `invalid otp`(): Unit = runBlocking {
        val server = createServer()
        coEvery { surveyApi.authOTP(any()) } answers {
            arg<EdusOtp.EDUSOneTimePassword>(0).apply {
                otp shouldBe "15cff19f-af26-41bc-94f2-c1a65075e894"
            }
            SurveyApiV1.DataDonationResponse("API_TOKEN_ALREADY_ISSUED")
        }

        val data = OneTimePassword(UUID.fromString("15cff19f-af26-41bc-94f2-c1a65075e894"))
        server.authOTP(data, mockk()).errorCode shouldBe "API_TOKEN_ALREADY_ISSUED"

        coVerify { surveyApi.authOTP(any()) }
    }

    @Test
    fun `return code 500`(): Unit = runBlocking {
        val server = createServer()
        coEvery { surveyApi.authOTP(any()) } answers {
            arg<EdusOtp.EDUSOneTimePassword>(0).apply {
                otp shouldBe "15cff19f-af26-41bc-94f2-c1a65075e894"
            }
            SurveyApiV1.DataDonationResponse("API_TOKEN_ALREADY_ISSUED")
        }

        val data = OneTimePassword(UUID.fromString("15cff19f-af26-41bc-94f2-c1a65075e894"))
        server.authOTP(data, mockk()).errorCode shouldBe "API_TOKEN_ALREADY_ISSUED"

        coVerify { surveyApi.authOTP(any()) }
    }
}
