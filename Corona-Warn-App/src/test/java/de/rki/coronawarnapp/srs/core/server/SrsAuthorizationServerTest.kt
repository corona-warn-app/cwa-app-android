package de.rki.coronawarnapp.srs.core.server

import com.google.protobuf.ByteString
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.SelfReportSubmissionConfigContainer
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import de.rki.coronawarnapp.exception.http.NetworkReadTimeoutException
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException.ErrorCode
import de.rki.coronawarnapp.srs.core.model.SrsAuthorizationRequest
import de.rki.coronawarnapp.srs.core.model.SrsOtp
import de.rki.coronawarnapp.srs.core.storage.SrsDevSettings
import de.rki.coronawarnapp.util.PaddingTool
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.toInstant
import java.time.DateTimeException
import kotlin.random.Random

internal class SrsAuthorizationServerTest : BaseTest() {

    @MockK lateinit var srsAuthorizationApi: SrsAuthorizationApi
    @MockK lateinit var srsDevSettings: SrsDevSettings
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var configData: ConfigData
    private val paddingTool = PaddingTool(Random)

    private val request = SrsAuthorizationRequest(
        srsOtp = SrsOtp(),
        safetyNetJws = "wwrr",
        salt = "salt",
        androidId = ByteString.EMPTY
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { srsAuthorizationApi.authenticate(any(), any()) } returns Response.success(
            """
             {
               "expirationDate": "2023-05-16T08:34:00+00:00"
             }
            """.trimIndent().toResponseBody()
        )
        coEvery { srsDevSettings.forceAndroidIdAcceptance() } returns false
        every { configData.selfReportSubmission } returns SelfReportSubmissionConfigContainer.DEFAULT
        every { appConfigProvider.currentConfig } returns flowOf(configData)
    }

    @Test
    fun `authorize pass`() = runTest {
        instance().authorize(request) shouldBe "2023-05-16T08:34:00+00:00".toInstant()
    }

    @Test
    fun `force accept android id - on`() = runTest {
        val headers = mapOf(
            "Content-Type" to "application/x-protobuf",
            "cwa-ppac-android-accept-android-id" to "1"
        )
        coEvery { srsDevSettings.forceAndroidIdAcceptance() } returns true
        instance().authorize(request) shouldBe "2023-05-16T08:34:00+00:00".toInstant()
        coVerify { srsAuthorizationApi.authenticate(headers, any()) }
    }

    @Test
    fun `force accept android id - off`() = runTest {
        val headers = mapOf("Content-Type" to "application/x-protobuf")
        instance().authorize(request) shouldBe "2023-05-16T08:34:00+00:00".toInstant()
        coVerify { srsAuthorizationApi.authenticate(headers, any()) }
    }

    @Test
    fun `invalid expiry time`() = runTest {
        coEvery { srsAuthorizationApi.authenticate(any(), any()) } returns Response.success(
            """
             {
               "expirationDate": ""
             }
            """.trimIndent().toResponseBody()
        )
        shouldThrow<SrsSubmissionException> {
            instance().authorize(request)
        }.cause.shouldBeInstanceOf<DateTimeException>()
    }

    @Test
    fun `no network errors`() = runTest {
        coEvery { srsAuthorizationApi.authenticate(any(), any()) } throws
            CwaUnknownHostException(cause = Exception("CwaUnknownHostException"))
        shouldThrow<SrsSubmissionException> { instance().authorize(request) }.errorCode shouldBe
            ErrorCode.SRS_OTP_NO_NETWORK

        coEvery { srsAuthorizationApi.authenticate(any(), any()) } throws
            NetworkReadTimeoutException(message = "NetworkReadTimeoutException")
        shouldThrow<SrsSubmissionException> { instance().authorize(request) }.errorCode shouldBe
            ErrorCode.SRS_OTP_NO_NETWORK

        coEvery { srsAuthorizationApi.authenticate(any(), any()) } throws
            NetworkConnectTimeoutException(message = "NetworkConnectTimeoutException")
        shouldThrow<SrsSubmissionException> { instance().authorize(request) }.errorCode shouldBe
            ErrorCode.SRS_OTP_NO_NETWORK
    }

    @Test
    fun `authorize - server error codes`() = runTest {
        listOf(
            "ANDROID_ID_INVALID" to ErrorCode.ANDROID_ID_INVALID,
            "APK_CERTIFICATE_MISMATCH" to ErrorCode.APK_CERTIFICATE_MISMATCH,
            "APK_PACKAGE_NAME_MISMATCH" to ErrorCode.APK_PACKAGE_NAME_MISMATCH,
            "ATTESTATION_EXPIRED" to ErrorCode.ATTESTATION_EXPIRED,
            "BASIC_INTEGRITY_REQUIRED" to ErrorCode.BASIC_INTEGRITY_REQUIRED,
            "CTS_PROFILE_MATCH_REQUIRED" to ErrorCode.CTS_PROFILE_MATCH_REQUIRED,
            "DEVICE_QUOTA_EXCEEDED" to ErrorCode.DEVICE_QUOTA_EXCEEDED,
            "EVALUATION_TYPE_BASIC_REQUIRED" to ErrorCode.EVALUATION_TYPE_BASIC_REQUIRED,
            "EVALUATION_TYPE_HARDWARE_BACKED_REQUIRED" to ErrorCode.EVALUATION_TYPE_HARDWARE_BACKED_REQUIRED,
            "JWS_SIGNATURE_VERIFICATION_FAILED" to ErrorCode.JWS_SIGNATURE_VERIFICATION_FAILED,
            "NONCE_MISMATCH" to ErrorCode.NONCE_MISMATCH,
            "SALT_REDEEMED" to ErrorCode.SALT_REDEEMED,
            // iOS error that does not map to Android Error :D
            "API_TOKEN_ALREADY_ISSUED" to ErrorCode.SRS_OTP_SERVER_ERROR,
        ).forEach { (serverErrorCode, errorCode) ->
            coEvery { srsAuthorizationApi.authenticate(any(), any()) } returns Response.success(
                """
                  {
                     "errorCode": "$serverErrorCode"
                  }
                """.trimIndent().toResponseBody()
            )

            shouldThrow<SrsSubmissionException> {
                instance().authorize(request)
            }.errorCode shouldBe errorCode
        }
    }

    @Test
    fun `authorize - response error codes`() = runTest {
        listOf(
            400 to ErrorCode.SRS_OTP_400,
            401 to ErrorCode.SRS_OTP_401,
            403 to ErrorCode.SRS_OTP_403,
            404 to ErrorCode.SRS_OTP_CLIENT_ERROR,
            505 to ErrorCode.SRS_OTP_SERVER_ERROR,
        ).forEach { (responseErrorCode, errorCode) ->
            coEvery { srsAuthorizationApi.authenticate(any(), any()) } returns
                Response.error(responseErrorCode, "".toResponseBody())
            shouldThrow<SrsSubmissionException> {
                instance().authorize(request)
            }.errorCode shouldBe errorCode
        }
    }

    private fun instance() = SrsAuthorizationServer(
        srsAuthorizationApi = { srsAuthorizationApi },
        dispatcherProvider = TestDispatcherProvider(),
        mapper = SerializationModule.jacksonBaseMapper,
        srsDevSettings = srsDevSettings,
        appConfigProvider = appConfigProvider,
        paddingTool = paddingTool,
    )
}
