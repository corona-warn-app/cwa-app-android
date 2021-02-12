package de.rki.coronawarnapp.datadonation.survey.server

import android.content.Context
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtp
import io.kotest.assertions.throwables.shouldThrowAny
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

    @Test
    fun `valid otp`(): Unit = runBlocking {
        val server = SurveyServer(surveyApi = { surveyApi }, TestDispatcherProvider())
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
        val server = SurveyServer(surveyApi = { surveyApi }, TestDispatcherProvider())
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
        val server = SurveyServer(surveyApi = { surveyApi }, TestDispatcherProvider())

        val data = OneTimePassword(UUID.fromString("15cff19f-af26-41bc-94f2-c1a65075e894"))

        shouldThrowAny {
            server.authOTP(data, mockk())
        }
    }
}
