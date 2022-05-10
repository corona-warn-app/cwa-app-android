package de.rki.coronawarnapp.dccticketing.core.service.processor

import com.google.gson.Gson
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateCheckException
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateChecker
import de.rki.coronawarnapp.dccticketing.core.common.DccJWKVerification
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingJwtException
import de.rki.coronawarnapp.dccticketing.core.common.JwtTokenConverter
import de.rki.coronawarnapp.dccticketing.core.common.JwtTokenParser
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServer
import de.rki.coronawarnapp.dccticketing.core.server.ResultTokenRequest
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import de.rki.coronawarnapp.exception.http.NetworkReadTimeoutException
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response
import testhelpers.BaseTest
import java.lang.Exception

@Suppress("MaxLineLength")
internal class ResultTokenRequestProcessorTest : BaseTest() {

    @MockK lateinit var dccTicketingServer: DccTicketingServer
    @MockK lateinit var dccTicketingServerCertificateChecker: DccTicketingServerCertificateChecker
    @MockK lateinit var jwtVerification: DccJWKVerification
    private lateinit var response: Response<ResponseBody>
    private val parser = JwtTokenParser(JwtTokenConverter(Gson()))

    private val jsonResultToken = """
        eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InBHV3F6QjlCeldZPSJ9.eyJzdWIiOiIwNGM4OGE3My04YzZmLTQ5YzktYmE0Mi00NWY0ZmFmODA5YjUiLCJpc3MiOiIiLCJleHAiOjE2MzgzMDk1MjQsImNhdGVnb3J5IjpbIlN0YW5kYXJkIl0sImNvbmZpcm1hdGlvbiI6ImV5SmhiR2NpT2lKRlV6STFOaUlzSW5SNWNDSTZJa3BYVkNJc0ltdHBaQ0k2SW5CSFYzRjZRamxDZWxkWlBTSjkuZXlKcFlYUWlPakUyTXpneU1qTTNNRFY5LmVCM0gzZ3hsaXA0RFUwTGVzQVRZVEdNM2hpX3JIX2ZVb3k3UWNLT2daYWRfT01SX2NpWU9NblRfVW5TeHFzSThaaTBEdERnRmZWU0Z2LXFpYVVTS1pBIiwicmVzdWx0cyI6W10sInJlc3VsdCI6Ik9LIiwiaWF0IjoxNjM4MjIzNzA1fQ.UcxMoxWkQMTt6Dzz5WbttqOumu3C_d_hdSuu6_ic-dDF6Rys62Y-pC9BFe2D_Oo6s3FSjWwCWFqYBbQQ-w5vKA
    """.trimIndent()

    private val input = ResultTokenInput(
        serviceEndpoint = "serviceEndpoint",
        validationServiceJwkSet = setOf(),
        validationServiceSignKeyJwkSet = setOf(),
        jwt = "jwt",
        encryptionKeyKid = "encryptionKeyKid",
        encryptedDCCBase64 = "encryptedDCCBase64",
        encryptionKeyBase64 = "encryptionKeyBase64",
        signatureBase64 = "signatureBase64",
        signatureAlgorithm = "signatureAlgorithm",
        encryptionScheme = "encryptionScheme",
        allowlist = emptySet()
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        val responseBody = mockk<ResponseBody>().apply { every { string() } returns jsonResultToken }
        response = Response.success(responseBody)

        every { dccTicketingServerCertificateChecker.checkCertificateAgainstAllowlist(any(), any()) } just Runs
        coEvery { dccTicketingServer.getResultToken(any(), any(), any()) } returns response
        every { jwtVerification.verify(any(), any<Set<DccJWK>>()) } just Runs
    }

    @Test
    fun requestResultToken() = runTest {
        instance().requestResultToken(input) shouldBe ResultTokenOutput(
            resultToken = jsonResultToken,
            resultTokenPayload = parser.getResultToken(jsonResultToken)
        )

        coVerifySequence {
            dccTicketingServer.getResultToken(any(), any(), any())
            dccTicketingServerCertificateChecker.checkCertificateAgainstAllowlist(any(), any())
            jwtVerification.verify(any(), any<Set<DccJWK>>())
        }
    }

    @Test
    fun `checkServerCertificate throws RTR_CERT_PIN_MISMATCH `() = runTest {
        every { dccTicketingServerCertificateChecker.checkCertificateAgainstAllowlist(any(), any()) } throws
            DccTicketingServerCertificateCheckException(DccTicketingServerCertificateCheckException.ErrorCode.CERT_PIN_MISMATCH)

        shouldThrow<DccTicketingException> {
            instance().checkServerCertificate(response, input.allowlist)
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_CERT_PIN_MISMATCH
    }

    @Test
    fun `checkServerCertificate throws RTR_CERT_PIN_HOST_MISMATCH `() = runTest {
        every { dccTicketingServerCertificateChecker.checkCertificateAgainstAllowlist(any(), any()) } throws
            DccTicketingServerCertificateCheckException(DccTicketingServerCertificateCheckException.ErrorCode.CERT_PIN_NO_JWK_FOR_KID)

        shouldThrow<DccTicketingException> {
            instance().checkServerCertificate(response, input.allowlist)
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_CERT_PIN_HOST_MISMATCH

        every { dccTicketingServerCertificateChecker.checkCertificateAgainstAllowlist(any(), any()) } throws
            DccTicketingServerCertificateCheckException(DccTicketingServerCertificateCheckException.ErrorCode.CERT_PIN_HOST_MISMATCH)

        shouldThrow<DccTicketingException> {
            instance().checkServerCertificate(response, input.allowlist)
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_CERT_PIN_HOST_MISMATCH
    }

    @Test
    fun `checkServerCertificate Pass`() = runTest {
        shouldNotThrowAny {
            instance().checkServerCertificate(response, input.allowlist)
        }
    }

    @Test
    fun `verifyJWT throws RTR_JWT_VER_EMPTY_JWKS`() = runTest {
        every { jwtVerification.verify(any(), any<Set<DccJWK>>()) } throws
            DccTicketingJwtException(DccTicketingJwtException.ErrorCode.JWT_VER_EMPTY_JWKS)
        shouldThrow<DccTicketingException> {
            instance().verifyJWT("jwt", emptySet())
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_JWT_VER_EMPTY_JWKS
    }

    @Test
    fun `verifyJWT throws RTR_JWT_VER_ALG_NOT_SUPPORTED`() = runTest {
        every { jwtVerification.verify(any(), any<Set<DccJWK>>()) } throws
            DccTicketingJwtException(DccTicketingJwtException.ErrorCode.JWT_VER_ALG_NOT_SUPPORTED)
        shouldThrow<DccTicketingException> {
            instance().verifyJWT("jwt", emptySet())
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_JWT_VER_ALG_NOT_SUPPORTED
    }

    @Test
    fun `verifyJWT throws JWT_VER_NO_JWK_FOR_KID`() = runTest {
        every { jwtVerification.verify(any(), any<Set<DccJWK>>()) } throws
            DccTicketingJwtException(DccTicketingJwtException.ErrorCode.JWT_VER_NO_JWK_FOR_KID)
        shouldThrow<DccTicketingException> {
            instance().verifyJWT("jwt", emptySet())
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_JWT_VER_NO_JWK_FOR_KID
    }

    @Test
    fun `verifyJWT throws JWT_VER_NO_KID`() = runTest {
        every { jwtVerification.verify(any(), any<Set<DccJWK>>()) } throws
            DccTicketingJwtException(DccTicketingJwtException.ErrorCode.JWT_VER_NO_KID)
        shouldThrow<DccTicketingException> {
            instance().verifyJWT("jwt", emptySet())
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_JWT_VER_NO_KID
    }

    @Test
    fun `verifyJWT throws JWT_VER_SIG_INVALID`() = runTest {
        every { jwtVerification.verify(any(), any<Set<DccJWK>>()) } throws
            DccTicketingJwtException(DccTicketingJwtException.ErrorCode.JWT_VER_SIG_INVALID)
        shouldThrow<DccTicketingException> {
            instance().verifyJWT("jwt", emptySet())
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_JWT_VER_SIG_INVALID
    }

    @Test
    fun `verifyJWT Pass`() = runTest {
        shouldNotThrowAny {
            instance().verifyJWT("jwt", emptySet())
        }
    }

    @Test
    fun `resultTokenResponse verify request and response`() = runTest {
        coEvery { dccTicketingServer.getResultToken(any(), any(), any()) } answers {
            arg<String>(0) shouldBe input.serviceEndpoint
            arg<String>(1) shouldBe "Bearer ${input.jwt}"
            arg<ResultTokenRequest>(2) shouldBe ResultTokenRequest(
                kid = input.encryptionKeyKid,
                dcc = input.encryptedDCCBase64,
                sig = input.signatureBase64,
                encKey = input.encryptionKeyBase64,
                encScheme = input.encryptionScheme,
                sigAlg = input.signatureAlgorithm
            )

            response
        }
        instance().resultTokenResponse(input) shouldBe response
    }

    @Test
    fun `resultTokenResponse verify null response`() = runTest {

        coEvery { dccTicketingServer.getResultToken(any(), any(), any()) } answers {
            Response.success(null)
        }
        shouldThrow<DccTicketingException> {
            instance().requestResultToken(input)
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_SERVER_ERR
    }

    @Test
    fun `resultTokenResponse throws RTR_NO_NETWORK when CwaUnknownHostException`() = runTest {
        coEvery { dccTicketingServer.getResultToken(any(), any(), any()) } throws
            CwaUnknownHostException(cause = null)
        shouldThrow<DccTicketingException> {
            instance().resultTokenResponse(input)
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_NO_NETWORK
    }

    @Test
    fun `resultTokenResponse throws RTR_NO_NETWORK when NetworkReadTimeoutException`() = runTest {
        coEvery { dccTicketingServer.getResultToken(any(), any(), any()) } throws
            NetworkReadTimeoutException(message = null)

        shouldThrow<DccTicketingException> {
            instance().resultTokenResponse(input)
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_NO_NETWORK
    }

    @Test
    fun `resultTokenResponse throws RTR_NO_NETWORK when NetworkConnectTimeoutException`() = runTest {
        coEvery { dccTicketingServer.getResultToken(any(), any(), any()) } throws NetworkConnectTimeoutException()
        shouldThrow<DccTicketingException> {
            instance().resultTokenResponse(input)
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_NO_NETWORK
    }

    @Test
    fun `resultTokenResponse throws RTR_CLIENT_ERR when CwaClientError`() = runTest {
        coEvery { dccTicketingServer.getResultToken(any(), any(), any()) } throws
            CwaClientError(404, "message")
        shouldThrow<DccTicketingException> {
            instance().resultTokenResponse(input)
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_CLIENT_ERR
    }

    @Test
    fun `resultTokenResponse throws RTR_NO_NETWORK when any error`() = runTest {
        coEvery { dccTicketingServer.getResultToken(any(), any(), any()) } throws
            Exception()
        shouldThrow<DccTicketingException> {
            instance().resultTokenResponse(input)
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_SERVER_ERR
    }

    private fun instance() = ResultTokenRequestProcessor(
        dccTicketingServer = dccTicketingServer,
        dccTicketingServerCertificateChecker = dccTicketingServerCertificateChecker,
        jwtTokenParser = parser,
        jwtVerification = jwtVerification
    )
}
