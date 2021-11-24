package de.rki.coronawarnapp.dccticketing.core.service.processor

import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateCheckException
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateCheckException.ErrorCode.CERT_PIN_NO_JWK_FOR_KID
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateCheckException.ErrorCode.CERT_PIN_MISMATCH
import de.rki.coronawarnapp.dccticketing.core.common.DccJWKVerification
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingErrorCode
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingJwtException
import de.rki.coronawarnapp.dccticketing.core.common.JwtTokenParser
import de.rki.coronawarnapp.dccticketing.core.common.validate
import de.rki.coronawarnapp.dccticketing.core.server.AccessTokenRequest
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServer
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingAccessToken
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingService
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import de.rki.coronawarnapp.exception.http.NetworkReadTimeoutException
import timber.log.Timber
import javax.inject.Inject

class AccessTokenRequestProcessor @Inject constructor(
    private val dccTicketingServer: DccTicketingServer,
    private val jwtTokenParser: JwtTokenParser,
    private val jwtVerification: DccJWKVerification
) {

    @Suppress("LongParameterList")
    suspend fun requestAccessToken(
        accessTokenService: DccTicketingService,
        accessTokenServiceJwkSet: Set<DccJWK>,
        accessTokenSignJwkSet: Set<DccJWK>,
        validationService: DccTicketingService,
        publicKeyBase64: String,
        authorization: String
    ): Output {

        // Call Access Token Service
        val response = getAccessToken(
            url = accessTokenService.serviceEndpoint,
            authorization = authorization,
            body = AccessTokenRequest(
                validationService.id,
                publicKeyBase64
            ),
            jwkSet = accessTokenServiceJwkSet
        )

        // Verifying the Signature of a JWT with a Set of JWKs
        verifyJWT(response.jwt, accessTokenSignJwkSet)

        // Determine accessTokenPayload
        val accessTokenPayload = getAccessTokenPayload(response.jwt)

        return Output(response.jwt, accessTokenPayload, response.iv)
    }

    private suspend fun getAccessToken(
        url: String,
        authorization: String,
        body: AccessTokenRequest,
        jwkSet: Set<DccJWK>
    ) = try {
        val authorizationHeader = "Bearer $authorization"
        dccTicketingServer.getAccessToken(url, authorizationHeader, body, jwkSet)
    } catch (e: DccTicketingServerCertificateCheckException) {
        throw when (e.errorCode) {
            CERT_PIN_NO_JWK_FOR_KID -> DccTicketingException.ErrorCode.ATR_CERT_PIN_NO_JWK_FOR_KID
            CERT_PIN_MISMATCH -> DccTicketingException.ErrorCode.ATR_CERT_PIN_MISMATCH
        }.let { DccTicketingException(it) }
    } catch (e: Exception) {
        Timber.e(e, "Getting access token failed")
        throw when (e) {
            is CwaUnknownHostException,
            is NetworkReadTimeoutException,
            is NetworkConnectTimeoutException -> DccTicketingErrorCode.ATR_NO_NETWORK
            is CwaClientError -> DccTicketingErrorCode.ATR_CLIENT_ERR
            // Blame the server for everything else
            else -> DccTicketingErrorCode.ATR_SERVER_ERR
        }.let { DccTicketingException(it, e) }
    }

    private fun verifyJWT(jwt: String, jwkSet: Set<DccJWK>) = try {
        jwtVerification.verify(jwt, jwkSet)
    } catch (e: DccTicketingJwtException) {
        Timber.e(e, "verifyJWT for result token failed")
        throw when (e.errorCode) {
            DccTicketingJwtException.ErrorCode.JWT_VER_NO_JWKS ->
                DccTicketingException.ErrorCode.ATR_JWT_VER_NO_JWKS
            DccTicketingJwtException.ErrorCode.JWT_VER_ALG_NOT_SUPPORTED ->
                DccTicketingException.ErrorCode.ATR_JWT_VER_ALG_NOT_SUPPORTED
            DccTicketingJwtException.ErrorCode.JWT_VER_NO_KID ->
                DccTicketingException.ErrorCode.ATR_JWT_VER_NO_KID
            DccTicketingJwtException.ErrorCode.JWT_VER_NO_JWK_FOR_KID ->
                DccTicketingException.ErrorCode.ATR_JWT_VER_NO_JWK_FOR_KID
            DccTicketingJwtException.ErrorCode.JWT_VER_SIG_INVALID ->
                DccTicketingException.ErrorCode.ATR_JWT_VER_SIG_INVALID
        }.let { DccTicketingException(it) }
    }

    private fun getAccessTokenPayload(jwt: String): DccTicketingAccessToken = try {
        jwtTokenParser.getAccessToken(jwt)
    } catch (e: Exception) {
        throw DccTicketingException(DccTicketingException.ErrorCode.ATR_PARSE_ERR, e)
    }?.apply {
        validate()
    } ?: throw DccTicketingException(DccTicketingException.ErrorCode.ATR_PARSE_ERR)

    data class Output(
        val accessToken: String,
        val accessTokenPayload: DccTicketingAccessToken,
        val nonceBase64: String
    )
}
