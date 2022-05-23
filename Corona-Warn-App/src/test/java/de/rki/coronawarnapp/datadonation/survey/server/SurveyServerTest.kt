package de.rki.coronawarnapp.datadonation.survey.server

import android.content.Context
import com.google.protobuf.ByteString
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtp
import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtpRequestAndroid
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpacAndroid
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import java.io.IOException
import java.util.UUID

class SurveyServerTest : BaseTest() {

    @MockK lateinit var surveyApi: SurveyApiV1
    @MockK lateinit var context: Context
    @MockK lateinit var attestationResult: DeviceAttestation.Result

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { attestationResult.accessControlProtoBuf } returns build
    }

    @Test
    fun `valid otp`() = runTest {
        val server = SurveyServer(surveyApi = { surveyApi }, TestDispatcherProvider())
        coEvery { surveyApi.authOTP(any()) } answers {
            arg<EdusOtpRequestAndroid.EDUSOneTimePasswordRequestAndroid>(0).toString() shouldBe expectedPayload()
            SurveyApiV1.DataDonationResponse(null)
        }

        server.authOTP(
            OneTimePassword(UUID.fromString(otpData)),
            attestationResult
        ).errorCode shouldBe null

        coVerify { surveyApi.authOTP(any()) }
    }

    @Test
    fun `invalid otp`() = runTest {
        val server = SurveyServer(surveyApi = { surveyApi }, TestDispatcherProvider())
        coEvery { surveyApi.authOTP(any()) } answers {
            arg<EdusOtpRequestAndroid.EDUSOneTimePasswordRequestAndroid>(0).toString() shouldBe expectedPayload()
            SurveyApiV1.DataDonationResponse("API_TOKEN_ALREADY_ISSUED")
        }

        server.authOTP(
            OneTimePassword(UUID.fromString(otpData)),
            attestationResult
        ).errorCode shouldBe "API_TOKEN_ALREADY_ISSUED"

        coVerify { surveyApi.authOTP(any()) }
    }

    @Test
    fun `API fails`(): Unit = runTest {
        val server = SurveyServer(surveyApi = { surveyApi }, TestDispatcherProvider())
        coEvery { surveyApi.authOTP(any()) } throws (IOException())

        shouldThrow<IOException> {
            server.authOTP(
                OneTimePassword(UUID.fromString(otpData)),
                attestationResult
            )
        }
    }

    private val otpData = "15cff19f-af26-41bc-94f2-c1a65075e894"
    private val build = PpacAndroid.PPACAndroid.newBuilder()
        .setSafetyNetJws("abc")
        .setSalt("def")
        .build()

    private fun expectedPayload() = EdusOtpRequestAndroid.EDUSOneTimePasswordRequestAndroid.newBuilder()
        .setPayload(
            EdusOtp.EDUSOneTimePassword.newBuilder()
                .setOtp(otpData)
                .setOtpBytes(
                    ByteString.copyFrom(
                        "MTVjZmYxOWYtYWYyNi00MWJjLTk0ZjItYzFhNjUwNzVlODk0".decodeBase64()!!
                            .toByteArray()
                    )
                )
        )
        .setAuthentication(build)
        .build()
        .toString()
}
