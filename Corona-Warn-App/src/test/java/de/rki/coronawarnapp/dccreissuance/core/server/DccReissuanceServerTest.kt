package de.rki.coronawarnapp.dccreissuance.core.server

import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException
import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceErrorResponse
import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceRequestBody
import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceResponse
import de.rki.coronawarnapp.dccreissuance.core.server.validation.DccReissuanceServerCertificateValidator
import de.rki.coronawarnapp.exception.http.NetworkReadTimeoutException
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class DccReissuanceServerTest : BaseTest() {

    @MockK lateinit var dccReissuanceApi: DccReissuanceApi
    @RelaxedMockK lateinit var dccReissuanceServerCertificateValidator: DccReissuanceServerCertificateValidator

    private val gson = SerializationModule().baseGson()

    private val testAction = "renew"
    private val testCerts = listOf("HC1:1235....", "HC1:ABCD...", "HC1:6789...")
    private val testResponse = DccReissuanceResponse(
        dccReissuances = listOf(
            DccReissuanceResponse.DccReissuance(
                certificate = "HC1:EFGH...",
                relations = listOf(
                    DccReissuanceResponse.Relation(
                        index = 0,
                        action = "replace"
                    )
                )
            )
        )
    )

    private val testDccReissuanceListJson = """
        [
          {
            "certificate": "HC1:EFGH...",
            "relations": [
              {
                "index": 0,
                "action": "replace"
              }
            ]
          }
        ]
    """.trimIndent()

    private val body: ResponseBody
        get() = testDccReissuanceListJson.toResponseBody()

    private val instance: DccReissuanceServer
        get() = DccReissuanceServer(
            dccReissuanceApiLazy = { dccReissuanceApi },
            dispatcherProvider = TestDispatcherProvider(),
            dccReissuanceServerCertificateValidator = dccReissuanceServerCertificateValidator,
            gson = gson
        )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { dccReissuanceApi.requestReissuance(any()) } returns Response.success(body)
    }

    @Test
    fun `Happy path`() = runBlockingTest {
        instance.requestDccReissuance() shouldBe testResponse

        coVerify {
            dccReissuanceServerCertificateValidator.checkCertificateChain(any())
            dccReissuanceApi.requestReissuance(
                dccReissuanceRequestBody = DccReissuanceRequestBody(
                    action = testAction,
                    certificates = testCerts
                )
            )
        }
    }

    @Test
    fun `forwards server certification validator errors`() = runBlockingTest {
        val errorCode = DccReissuanceException.ErrorCode.DCC_RI_PIN_MISMATCH
        val testError = Exception("Test error")

        coEvery { dccReissuanceServerCertificateValidator.checkCertificateChain(any()) } throws DccReissuanceException(
            errorCode = errorCode,
            cause = testError
        )

        shouldThrow<DccReissuanceException> {
            instance.requestDccReissuance()
        }.also {
            it.errorCode shouldBe errorCode
            it.cause shouldBe testError
        }
    }

    @Test
    fun `throws on failed response with corresponding error code`() = runBlockingTest {
        checkStatusToErrorCodeMapping(statusCode = 400, errorCode = DccReissuanceException.ErrorCode.DCC_RI_400)
        checkStatusToErrorCodeMapping(statusCode = 401, errorCode = DccReissuanceException.ErrorCode.DCC_RI_401)
        checkStatusToErrorCodeMapping(statusCode = 403, errorCode = DccReissuanceException.ErrorCode.DCC_RI_403)
        checkStatusToErrorCodeMapping(statusCode = 406, errorCode = DccReissuanceException.ErrorCode.DCC_RI_406)
        checkStatusToErrorCodeMapping(statusCode = 429, errorCode = DccReissuanceException.ErrorCode.DCC_RI_429)
        checkStatusToErrorCodeMapping(statusCode = 500, errorCode = DccReissuanceException.ErrorCode.DCC_RI_500)
        checkStatusToErrorCodeMapping(statusCode = 430, errorCode = DccReissuanceException.ErrorCode.DCC_RI_CLIENT_ERR)
        checkStatusToErrorCodeMapping(statusCode = 501, errorCode = DccReissuanceException.ErrorCode.DCC_RI_SERVER_ERR)
    }

    @Test
    fun `throws DCC_RI_NO_NETWORK on no network`() = runBlockingTest {
        checkNetworkErrorMapping(error = UnknownHostException("Test error"))
        checkNetworkErrorMapping(error = SocketTimeoutException("Test error"))
        checkNetworkErrorMapping(error = NetworkReadTimeoutException("Test error"))
    }

    @Test
    fun `blames the server if something else fails`() = runBlockingTest {
        coEvery { dccReissuanceApi.requestReissuance(any()) } throws Exception("Test error")

        shouldThrow<DccReissuanceException> {
            instance.requestDccReissuance()
        }.errorCode shouldBe DccReissuanceException.ErrorCode.DCC_RI_SERVER_ERR
    }

    @Test
    fun `throws DCC_RI_PARSE_ERR if response cannot be parsed`() = runBlockingTest {
        val body = "faulty response".toResponseBody()
        coEvery { dccReissuanceApi.requestReissuance(any()) } returns Response.success(body)

        shouldThrow<DccReissuanceException> {
            instance.requestDccReissuance()
        }.errorCode shouldBe DccReissuanceException.ErrorCode.DCC_RI_PARSE_ERR

        coEvery { dccReissuanceApi.requestReissuance(any()) } returns Response.success(null)

        shouldThrow<DccReissuanceException> {
            instance.requestDccReissuance()
        }.errorCode shouldBe DccReissuanceException.ErrorCode.DCC_RI_PARSE_ERR
    }

    @Test
    fun `adds server error response`() = runBlockingTest {
        val dccReissuanceErrorResponse = DccReissuanceErrorResponse(
            error = "RI400-1200",
            message = "certificates not acceptable for action"
        )
        val errorResponseJson = gson.toJson(dccReissuanceErrorResponse)
        val errorResponse = Response.error<ResponseBody>(400, errorResponseJson.toResponseBody())
        coEvery { dccReissuanceApi.requestReissuance(any()) } returns errorResponse

        shouldThrow<DccReissuanceException> {
            instance.requestDccReissuance()
        }.serverErrorResponse shouldBe dccReissuanceErrorResponse
    }

    private suspend fun checkStatusToErrorCodeMapping(statusCode: Int, errorCode: DccReissuanceException.ErrorCode) {
        coEvery { dccReissuanceApi.requestReissuance(any()) } returns Response.error(statusCode, body)

        shouldThrow<DccReissuanceException> {
            instance.requestDccReissuance()
        }.errorCode shouldBe errorCode
    }

    private suspend fun checkNetworkErrorMapping(error: Exception) {
        coEvery { dccReissuanceApi.requestReissuance(any()) } throws error
        shouldThrow<DccReissuanceException> {
            instance.requestDccReissuance()
        }.errorCode shouldBe DccReissuanceException.ErrorCode.DCC_RI_NO_NETWORK
    }

    private suspend fun DccReissuanceServer.requestDccReissuance() = requestDccReissuance(
        action = testAction,
        certificates = testCerts
    )
}
