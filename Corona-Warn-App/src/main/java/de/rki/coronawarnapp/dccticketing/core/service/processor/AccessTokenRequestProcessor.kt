package de.rki.coronawarnapp.dccticketing.core.service.processor

import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingErrorCode
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.common.JwtTokenConverter
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
    private val convertor: JwtTokenConverter,
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
            )
        )

        // TODO: cert pinning using [accessTokenServiceJwkSet] in another PR

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
    ) = try {
        val authorizationHeader = "Bearer $authorization"
        dccTicketingServer.getAccessToken(url, authorizationHeader, body)
    } catch (e: Exception) {
        Timber.e(e, "Getting access token failed")
        throw when (e) {
            is CwaUnknownHostException,
            is NetworkReadTimeoutException,
            is NetworkConnectTimeoutException -> DccTicketingErrorCode.VS_ID_NO_NETWORK
            is CwaClientError -> DccTicketingErrorCode.VS_ID_CLIENT_ERR
            // Blame the server for everything else
            else -> DccTicketingErrorCode.VS_ID_SERVER_ERR
        }.let { DccTicketingException(it, e) }
    }

    private fun verifyJWT(jwt: String, jwkSet: Set<DccJWK>) {
        // TODO: implementation in another PR
    }

    private fun getAccessTokenPayload(jwt: String): DccTicketingAccessToken = try {
        jwtTokenParser.parse(jwt).let {
            convertor.jsonToJwtToken(it?.body)
        }
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
