package de.rki.coronawarnapp.srs.core.server

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.PresenceTracingConfigContainer
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import de.rki.coronawarnapp.exception.http.NetworkReadTimeoutException
import de.rki.coronawarnapp.presencetracing.checkins.CheckInsReport
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass.SubmissionPayload.SubmissionType
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException
import de.rki.coronawarnapp.srs.core.model.SrsOtp
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionPayload
import de.rki.coronawarnapp.util.PaddingTool
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response

import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import kotlin.random.Random

internal class SrsSubmissionServerTest : BaseTest() {

    @MockK lateinit var srsSubmissionApi: SrsSubmissionApi
    private val paddingTool: PaddingTool = PaddingTool(Random.Default)
    @MockK lateinit var appConfigProvider: AppConfigProvider

    private val payload = SrsSubmissionPayload(
        srsOtp = SrsOtp(),
        checkInsReport = CheckInsReport(
            unencryptedCheckIns = emptyList(),
            encryptedCheckIns = emptyList()
        ),
        visitedCountries = listOf("DE"),
        submissionType = SubmissionType.SUBMISSION_TYPE_SRS_SELF_TEST,
        exposureKeys = listOf()
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { srsSubmissionApi.submitPayload(any(), any()) } returns Response.success("".toResponseBody())
        coEvery { appConfigProvider.getAppConfig() } returns mockk<ConfigData>().apply {
            every { presenceTracing } returns PresenceTracingConfigContainer()
        }
    }

    @Test
    fun `submit pass`() = runTest {
        shouldNotThrowAny {
            instance().submit(payload)
        }
    }

    @Test
    fun `no network errors`() = runTest {
        coEvery { srsSubmissionApi.submitPayload(any(), any()) } throws
            CwaUnknownHostException(cause = Exception("CwaUnknownHostException"))
        shouldThrow<SrsSubmissionException> { instance().submit(payload) }.errorCode shouldBe
            SrsSubmissionException.ErrorCode.SRS_SUB_NO_NETWORK

        coEvery { srsSubmissionApi.submitPayload(any(), any()) } throws
            NetworkReadTimeoutException(message = "NetworkReadTimeoutException")
        shouldThrow<SrsSubmissionException> { instance().submit(payload) }.errorCode shouldBe
            SrsSubmissionException.ErrorCode.SRS_SUB_NO_NETWORK

        coEvery { srsSubmissionApi.submitPayload(any(), any()) } throws
            NetworkConnectTimeoutException(message = "NetworkConnectTimeoutException")
        shouldThrow<SrsSubmissionException> { instance().submit(payload) }.errorCode shouldBe
            SrsSubmissionException.ErrorCode.SRS_SUB_NO_NETWORK
    }

    @Test
    fun `other error maps to server error`() = runTest {
        coEvery { srsSubmissionApi.submitPayload(any(), any()) } throws Exception("Surprise!")
        shouldThrow<SrsSubmissionException> { instance().submit(payload) }.errorCode shouldBe
            SrsSubmissionException.ErrorCode.SRS_SUB_SERVER_ERROR
    }

    @Test
    fun `submit - response error codes`() = runTest {
        listOf(
            400 to SrsSubmissionException.ErrorCode.SRS_SUB_400,
            403 to SrsSubmissionException.ErrorCode.SRS_SUB_403,
            429 to SrsSubmissionException.ErrorCode.SRS_SUB_429,
            404 to SrsSubmissionException.ErrorCode.SRS_SUB_CLIENT_ERROR,
            505 to SrsSubmissionException.ErrorCode.SRS_SUB_SERVER_ERROR,
        ).forEach { (responseErrorCode, errorCode) ->
            coEvery { srsSubmissionApi.submitPayload(any(), any()) } returns
                Response.error(responseErrorCode, "".toResponseBody())
            shouldThrow<SrsSubmissionException> {
                instance().submit(payload)
            }.errorCode shouldBe errorCode
        }
    }

    private fun instance() = SrsSubmissionServer(
        srsSubmissionApi = { srsSubmissionApi },
        paddingTool = paddingTool,
        appConfigProvider = appConfigProvider,
        dispatcherProvider = TestDispatcherProvider()
    )
}
