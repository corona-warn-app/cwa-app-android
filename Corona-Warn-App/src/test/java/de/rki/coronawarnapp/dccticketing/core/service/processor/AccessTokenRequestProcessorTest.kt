package de.rki.coronawarnapp.dccticketing.core.service.processor

import de.rki.coronawarnapp.bugreporting.censors.dccticketing.DccTicketingJwtCensor
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateCheckException
import de.rki.coronawarnapp.dccticketing.core.common.DccJWKVerification
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingJwtException
import de.rki.coronawarnapp.dccticketing.core.common.JwtTokenParser
import de.rki.coronawarnapp.dccticketing.core.server.AccessTokenResponse
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServer
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingAccessToken
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingService
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.InternalServerErrorException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class AccessTokenRequestProcessorTest : BaseTest() {

    @MockK lateinit var dccTicketingServer: DccTicketingServer
    @MockK lateinit var jwtTokenParser: JwtTokenParser
    @MockK lateinit var jwtVerification: DccJWKVerification
    @MockK lateinit var accessToken: DccTicketingAccessToken
    @MockK lateinit var jwtCensor: DccTicketingJwtCensor

    private val dccTicketingService = DccTicketingService("", "", "", "")
    private val validationService = DccTicketingService("", "", "", "")
    private val accessTokenServiceJwkSet = emptySet<DccJWK>()
    private val accessTokenSignJwkSet = emptySet<DccJWK>()
    private val publicKeyBase64 = "ABC"
    private val authorization = ""
    private val accessTokenResponse = AccessTokenResponse("", "")

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { dccTicketingServer.getAccessToken(any(), any(), any(), any()) } returns accessTokenResponse
        every { jwtVerification.verify(any(), any<Set<DccJWK>>()) } just Runs
        every { jwtTokenParser.getAccessToken(any()) } returns accessToken
        every { accessToken.t } returns 1
        every { accessToken.aud } returns "."
        coEvery { jwtCensor.addVc(any()) } just Runs
        coEvery { jwtCensor.addJwt(any()) } just Runs
    }

    @Test
    fun `checkServerCertificate throws ATR_CERT_PIN_MISMATCH`() = runTest {
        coEvery { dccTicketingServer.getAccessToken(any(), any(), any(), any()) } throws
            DccTicketingServerCertificateCheckException(
                DccTicketingServerCertificateCheckException.ErrorCode.CERT_PIN_MISMATCH
            )

        shouldThrow<DccTicketingException> {
            callRequestAccessToken()
        }.errorCode shouldBe DccTicketingException.ErrorCode.ATR_CERT_PIN_MISMATCH
    }

    @Test
    fun `checkServerCertificate throws ATR_CERT_PIN_NO_JWK_FOR_KID`() = runTest {
        coEvery { dccTicketingServer.getAccessToken(any(), any(), any(), any()) } throws
            DccTicketingServerCertificateCheckException(
                DccTicketingServerCertificateCheckException.ErrorCode.CERT_PIN_NO_JWK_FOR_KID
            )

        shouldThrow<DccTicketingException> {
            callRequestAccessToken()
        }.errorCode shouldBe DccTicketingException.ErrorCode.ATR_CERT_PIN_NO_JWK_FOR_KID
    }

    @Test
    fun `checkServerCertificate throws ATR_CLIENT_ERR`() = runTest {
        coEvery { dccTicketingServer.getAccessToken(any(), any(), any(), any()) } throws BadRequestException("")

        shouldThrow<DccTicketingException> {
            callRequestAccessToken()
        }.errorCode shouldBe DccTicketingException.ErrorCode.ATR_CLIENT_ERR
    }

    @Test
    fun `checkServerCertificate throws ATR_SERVER_ERR`() = runTest {
        coEvery {
            dccTicketingServer.getAccessToken(
                any(),
                any(),
                any(),
                any()
            )
        } throws InternalServerErrorException("")

        shouldThrow<DccTicketingException> {
            callRequestAccessToken()
        }.errorCode shouldBe DccTicketingException.ErrorCode.ATR_SERVER_ERR
    }

    @Test
    fun `checkServerCertificate throws ATR_NO_NETWORK`() = runTest {
        coEvery {
            dccTicketingServer.getAccessToken(
                any(),
                any(),
                any(),
                any()
            )
        } throws NetworkConnectTimeoutException()

        shouldThrow<DccTicketingException> {
            callRequestAccessToken()
        }.errorCode shouldBe DccTicketingException.ErrorCode.ATR_NO_NETWORK
    }

    @Test
    fun `verifyJWT throws JWT_VER_EMPTY_JWKS`() = runTest {
        every { jwtVerification.verify(any(), any<Set<DccJWK>>()) } throws
            DccTicketingJwtException(DccTicketingJwtException.ErrorCode.JWT_VER_EMPTY_JWKS)
        shouldThrow<DccTicketingException> {
            callRequestAccessToken()
        }.errorCode shouldBe DccTicketingException.ErrorCode.ATR_JWT_VER_EMPTY_JWKS
    }

    @Test
    fun `verifyJWT throws ATR_JWT_VER_ALG_NOT_SUPPORTED`() = runTest {
        every { jwtVerification.verify(any(), any<Set<DccJWK>>()) } throws
            DccTicketingJwtException(DccTicketingJwtException.ErrorCode.JWT_VER_ALG_NOT_SUPPORTED)
        shouldThrow<DccTicketingException> {
            callRequestAccessToken()
        }.errorCode shouldBe DccTicketingException.ErrorCode.ATR_JWT_VER_ALG_NOT_SUPPORTED
    }

    @Test
    fun `verifyJWT throws ATR_JWT_VER_NO_JWK_FOR_KID`() = runTest {
        every { jwtVerification.verify(any(), any<Set<DccJWK>>()) } throws
            DccTicketingJwtException(DccTicketingJwtException.ErrorCode.JWT_VER_NO_JWK_FOR_KID)
        shouldThrow<DccTicketingException> {
            callRequestAccessToken()
        }.errorCode shouldBe DccTicketingException.ErrorCode.ATR_JWT_VER_NO_JWK_FOR_KID
    }

    @Test
    fun `verifyJWT throws ATR_JWT_VER_NO_KID`() = runTest {
        every { jwtVerification.verify(any(), any<Set<DccJWK>>()) } throws
            DccTicketingJwtException(DccTicketingJwtException.ErrorCode.JWT_VER_NO_KID)
        shouldThrow<DccTicketingException> {
            callRequestAccessToken()
        }.errorCode shouldBe DccTicketingException.ErrorCode.ATR_JWT_VER_NO_KID
    }

    @Test
    fun `verifyJWT throws ATR_PARSE_ERR`() = runTest {
        every { jwtTokenParser.getAccessToken(any()) } throws Exception()

        shouldThrow<DccTicketingException> {
            callRequestAccessToken()
        }.errorCode shouldBe DccTicketingException.ErrorCode.ATR_PARSE_ERR
    }

    @Test
    suspend fun `requestAccessToken pass`() = runTest {
        shouldNotThrowAny {
            callRequestAccessToken()
        }
    }

    private suspend fun callRequestAccessToken() = getInstance().requestAccessToken(
        accessTokenService = dccTicketingService,
        accessTokenServiceJwkSet = accessTokenServiceJwkSet,
        accessTokenSignJwkSet = accessTokenSignJwkSet,
        validationService = validationService,
        publicKeyBase64 = publicKeyBase64,
        authorization = authorization
    )

    private fun getInstance() = AccessTokenRequestProcessor(
        dccTicketingServer = dccTicketingServer,
        jwtTokenParser = jwtTokenParser,
        jwtVerification = jwtVerification,
        jwtCensor = jwtCensor,
    )
}
