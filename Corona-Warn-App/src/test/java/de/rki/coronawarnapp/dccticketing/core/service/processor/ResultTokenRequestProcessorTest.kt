package de.rki.coronawarnapp.dccticketing.core.service.processor

import com.google.gson.Gson
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateCheckException
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateChecker
import de.rki.coronawarnapp.dccticketing.core.common.DccJWKVerification
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingJwtException
import de.rki.coronawarnapp.dccticketing.core.common.JwtTokenConverter
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
import kotlinx.coroutines.test.runBlockingTest
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
    private val converter = JwtTokenConverter(Gson())

    private val jsonResultToken = """
        {
           "sub": "1044236f-48df-43cb-8bdf-bed142e507ab",
           "iss": "https://dgca-validation-service-eu-acc.cfapps.eu10.hana.ondemand.com",
           "iat": 1635864502,
           "exp": 1635950902,
           "category": [
              "Standard"
           ],
           "confirmation": "eyJraWQiOiJSQU0yU3R3N0VrRT0iLCJhbGciOiJFUzI1NiJ9.eyJqdGkiOiJlMWU2YjU4MS1lN2NmLTQyZTAtYjM1ZS1jZmFhMTRkZTcxN2UiLCJzdWIiOiIxMDQ0MjM2Zi00OGRmLTQzY2ItOGJkZi1iZWQxNDJlNTA3YWIiLCJpc3MiOiJodHRwczovL2RnY2EtdmFsaWRhdGlvbi1zZXJ2aWNlLWV1LWFjYy5jZmFwcHMuZXUxMC5oYW5hLm9uZGVtYW5kLmNvbSIsImlhdCI6MTYzNTg2NDUwMiwiZXhwIjoxNjM1OTUwOTAyLCJyZXN1bHQiOiJOT0siLCJjYXRlZ29yeSI6WyJTdGFuZGFyZCJdfQ.OLnS59EWkpkZoEMfbyOs18dUauch9eaXxGK8Zrn-jo-S1kcgAxP8z8rdzLzNjCNTfi4CbVUnF6FV0lHuMnYBOw",
           "results": [
              {
                 "identifier": "KID",
                 "result": "NOK",
                 "type": "TechnicalVerification",
                 "details": "\"unknown dcc signing kid\""
              }
           ],
           "result": "NOK"
        }
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
        encryptionScheme = "encryptionScheme"
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { dccTicketingServerCertificateChecker.checkCertificate(any(), any()) } just Runs
        coEvery { dccTicketingServer.getResultToken(any(), any(), any()) } returns Response.success(jsonResultToken)
        every { jwtVerification.verify(any(), any<Set<DccJWK>>()) } just Runs
    }

    @Test
    fun requestResultToken() = runBlockingTest {
        instance().requestResultToken(input) shouldBe ResultTokenOutput(
            resultToken = jsonResultToken,
            resultTokenPayload = converter.jsonToResultToken(jsonResultToken)
        )

        coVerifySequence {
            dccTicketingServer.getResultToken(any(), any(), any())
            dccTicketingServerCertificateChecker.checkCertificate(any(), any())
            jwtVerification.verify(any(), any<Set<DccJWK>>())
        }
    }

    @Test
    fun `checkServerCertificate throws RTR_CERT_PIN_MISMATCH `() = runBlockingTest {
        every { dccTicketingServerCertificateChecker.checkCertificate(any(), any()) } throws
            DccTicketingServerCertificateCheckException(DccTicketingServerCertificateCheckException.ErrorCode.CERT_PIN_MISMATCH)

        val jwtSet = setOf<DccJWK>()
        val response = Response.success(jsonResultToken)
        shouldThrow<DccTicketingException> {
            instance().checkServerCertificate(response, jwtSet)
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_CERT_PIN_MISMATCH
    }

    @Test
    fun `checkServerCertificate throws RTR_CERT_PIN_NO_JWK_FOR_KID `() = runBlockingTest {
        every { dccTicketingServerCertificateChecker.checkCertificate(any(), any()) } throws
            DccTicketingServerCertificateCheckException(DccTicketingServerCertificateCheckException.ErrorCode.CERT_PIN_NO_JWK_FOR_KID)

        val jwtSet = setOf<DccJWK>()
        val response = Response.success(jsonResultToken)
        shouldThrow<DccTicketingException> {
            instance().checkServerCertificate(response, jwtSet)
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_CERT_PIN_NO_JWK_FOR_KID
    }

    @Test
    fun `checkServerCertificate Pass`() = runBlockingTest {
        val jwtSet = setOf<DccJWK>()
        val response = Response.success(jsonResultToken)
        shouldNotThrowAny {
            instance().checkServerCertificate(response, jwtSet)
        }
    }

    @Test
    fun `verifyJWT throws RTR_JWT_VER_EMPTY_JWKS`() = runBlockingTest {
        every { jwtVerification.verify(any(), any<Set<DccJWK>>()) } throws
            DccTicketingJwtException(DccTicketingJwtException.ErrorCode.JWT_VER_EMPTY_JWKS)
        shouldThrow<DccTicketingException> {
            instance().verifyJWT("jwt", emptySet())
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_JWT_VER_EMPTY_JWKS
    }

    @Test
    fun `verifyJWT throws RTR_JWT_VER_ALG_NOT_SUPPORTED`() = runBlockingTest {
        every { jwtVerification.verify(any(), any<Set<DccJWK>>()) } throws
            DccTicketingJwtException(DccTicketingJwtException.ErrorCode.JWT_VER_ALG_NOT_SUPPORTED)
        shouldThrow<DccTicketingException> {
            instance().verifyJWT("jwt", emptySet())
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_JWT_VER_ALG_NOT_SUPPORTED
    }

    @Test
    fun `verifyJWT throws JWT_VER_NO_JWK_FOR_KID`() = runBlockingTest {
        every { jwtVerification.verify(any(), any<Set<DccJWK>>()) } throws
            DccTicketingJwtException(DccTicketingJwtException.ErrorCode.JWT_VER_NO_JWK_FOR_KID)
        shouldThrow<DccTicketingException> {
            instance().verifyJWT("jwt", emptySet())
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_JWT_VER_NO_JWK_FOR_KID
    }

    @Test
    fun `verifyJWT throws JWT_VER_NO_KID`() = runBlockingTest {
        every { jwtVerification.verify(any(), any<Set<DccJWK>>()) } throws
            DccTicketingJwtException(DccTicketingJwtException.ErrorCode.JWT_VER_NO_KID)
        shouldThrow<DccTicketingException> {
            instance().verifyJWT("jwt", emptySet())
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_JWT_VER_NO_KID
    }

    @Test
    fun `verifyJWT throws JWT_VER_SIG_INVALID`() = runBlockingTest {
        every { jwtVerification.verify(any(), any<Set<DccJWK>>()) } throws
            DccTicketingJwtException(DccTicketingJwtException.ErrorCode.JWT_VER_SIG_INVALID)
        shouldThrow<DccTicketingException> {
            instance().verifyJWT("jwt", emptySet())
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_JWT_VER_SIG_INVALID
    }

    @Test
    fun `verifyJWT Pass`() = runBlockingTest {
        shouldNotThrowAny {
            instance().verifyJWT("jwt", emptySet())
        }
    }

    @Test
    fun `resultTokenResponse verify request and response`() = runBlockingTest {
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

            Response.success(jsonResultToken)
        }
        instance().resultTokenResponse(input).body() shouldBe jsonResultToken
    }

    @Test
    fun `resultTokenResponse verify null response`() = runBlockingTest {
        coEvery { dccTicketingServer.getResultToken(any(), any(), any()) } answers {
            Response.success(null)
        }
        shouldThrow<DccTicketingException> {
            instance().requestResultToken(input)
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_SERVER_ERR
    }

    @Test
    fun `resultTokenResponse throws RTR_NO_NETWORK when CwaUnknownHostException`() = runBlockingTest {
        coEvery { dccTicketingServer.getResultToken(any(), any(), any()) } throws
            CwaUnknownHostException(cause = null)
        shouldThrow<DccTicketingException> {
            instance().resultTokenResponse(input)
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_NO_NETWORK
    }

    @Test
    fun `resultTokenResponse throws RTR_NO_NETWORK when NetworkReadTimeoutException`() = runBlockingTest {
        coEvery { dccTicketingServer.getResultToken(any(), any(), any()) } throws
            NetworkReadTimeoutException(message = null)

        shouldThrow<DccTicketingException> {
            instance().resultTokenResponse(input)
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_NO_NETWORK
    }

    @Test
    fun `resultTokenResponse throws RTR_NO_NETWORK when NetworkConnectTimeoutException`() = runBlockingTest {
        coEvery { dccTicketingServer.getResultToken(any(), any(), any()) } throws NetworkConnectTimeoutException()
        shouldThrow<DccTicketingException> {
            instance().resultTokenResponse(input)
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_NO_NETWORK
    }

    @Test
    fun `resultTokenResponse throws RTR_CLIENT_ERR when CwaClientError`() = runBlockingTest {
        coEvery { dccTicketingServer.getResultToken(any(), any(), any()) } throws
            CwaClientError(404, "message")
        shouldThrow<DccTicketingException> {
            instance().resultTokenResponse(input)
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_CLIENT_ERR
    }

    @Test
    fun `resultTokenResponse throws RTR_NO_NETWORK when any error`() = runBlockingTest {
        coEvery { dccTicketingServer.getResultToken(any(), any(), any()) } throws
            Exception()
        shouldThrow<DccTicketingException> {
            instance().resultTokenResponse(input)
        }.errorCode shouldBe DccTicketingException.ErrorCode.RTR_SERVER_ERR
    }

    private fun instance() = ResultTokenRequestProcessor(
        dccTicketingServer = dccTicketingServer,
        dccTicketingServerCertificateChecker = dccTicketingServerCertificateChecker,
        convertor = converter,
        jwtVerification = jwtVerification
    )
}
