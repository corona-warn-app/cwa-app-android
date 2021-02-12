package de.rki.coronawarnapp.datadonation.survey.server

import android.content.Context
import com.google.protobuf.ByteString
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtp
import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtpRequestAndroid
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpacAndroid
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import java.util.UUID

class SurveyServerTest : BaseTest() {

    @MockK lateinit var surveyApi: SurveyApiV1
    @MockK lateinit var context: Context
    @MockK lateinit var attestationResult: DeviceAttestation.Result

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `valid otp`() {
        val otpData = "15cff19f-af26-41bc-94f2-c1a65075e894"
        val build = PpacAndroid.PPACAndroid.newBuilder()
            .setSafetyNetJws("abc")
            .setSalt("def")
            .build()
        every { attestationResult.accessControlProtoBuf } returns build

        runBlocking {
            val server = SurveyServer(surveyApi = { surveyApi }, TestDispatcherProvider())
            coEvery { surveyApi.authOTP(any()) } answers {
                arg<EdusOtpRequestAndroid.EDUSOneTimePasswordRequestAndroid>(0).payload shouldBe
                    EdusOtpRequestAndroid.EDUSOneTimePasswordRequestAndroid.newBuilder()
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
                SurveyApiV1.DataDonationResponse(null)
            }

            server.authOTP(
                OneTimePassword(UUID.fromString(otpData)),
                attestationResult
            ).errorCode shouldBe null

            coVerify { surveyApi.authOTP(any()) }
        }
    }

    @Test
    fun `invalid otp`() {
        val otpData = "15cff19f-af26-41bc-94f2-c1a65075e894"
        val build = PpacAndroid.PPACAndroid.newBuilder()
            .setSafetyNetJws("abc")
            .setSalt("def")
            .build()
        every { attestationResult.accessControlProtoBuf } returns build

        runBlocking {
            val server = SurveyServer(surveyApi = { surveyApi }, TestDispatcherProvider())
            coEvery { surveyApi.authOTP(any()) } answers {
                arg<EdusOtpRequestAndroid.EDUSOneTimePasswordRequestAndroid>(0).payload shouldBe
                    EdusOtpRequestAndroid.EDUSOneTimePasswordRequestAndroid.newBuilder()
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
                SurveyApiV1.DataDonationResponse("API_TOKEN_ALREADY_ISSUED")
            }

            server.authOTP(
                OneTimePassword(UUID.fromString(otpData)),
                attestationResult
            ).errorCode shouldBe "API_TOKEN_ALREADY_ISSUED"

            coVerify { surveyApi.authOTP(any()) }
        }
    }

    @Test
    fun `return code 500`(): Unit = runBlocking {
        val server = SurveyServer(surveyApi = { surveyApi }, TestDispatcherProvider())

        val data = OneTimePassword(UUID.fromString("15cff19f-af26-41bc-94f2-c1a65075e894"))

        shouldThrowAny {
            server.authOTP(data, attestationResult)
        }
    }
}
