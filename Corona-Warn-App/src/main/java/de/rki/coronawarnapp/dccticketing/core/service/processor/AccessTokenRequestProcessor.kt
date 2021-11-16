package de.rki.coronawarnapp.dccticketing.core.service.processor

import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingErrorCode
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.common.JwtTokenConverter
import de.rki.coronawarnapp.dccticketing.core.common.JwtTokenParser
import de.rki.coronawarnapp.dccticketing.core.common.JwtTokenValidator
import de.rki.coronawarnapp.dccticketing.core.server.AccessTokenRequest
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServer
import de.rki.coronawarnapp.dccticketing.core.server.getAccessToken
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingAccessToken
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingService
import javax.inject.Inject

class AccessTokenRequestProcessor @Inject constructor(
    private val dccTicketingServer: DccTicketingServer,
    private val jwtTokenParser: JwtTokenParser,
    private val convertor: JwtTokenConverter,
    private val validator: JwtTokenValidator,
) {

    suspend fun requestAccessToken(
        validationService: DccTicketingService,
        publicKeyBase64: String,
        authorization: String
    ): Output {
        val requestBody = AccessTokenRequest(
            validationService.id,
            publicKeyBase64
        )

        // 1. Call Access Token Service:
        val response = dccTicketingServer.getAccessToken(
            url = validationService.serviceEndpoint,
            authorization = authorization,
            body = requestBody,
            clientErrorCode = DccTicketingErrorCode.VS_ID_CLIENT_ERR,
            serverErrorCode = DccTicketingErrorCode.VS_ID_SERVER_ERR,
            noNetworkErrorCode = DccTicketingErrorCode.VS_ID_NO_NETWORK
        )

        // TODO: Verify signature

        // Determine accessTokenPayload

        val accessTokenPayload = try {
            jwtTokenParser.parse(response.accessToken).let { convertor.jsonToJwtToken(it.body) }
        } catch (e: Exception) {
            throw DccTicketingException(DccTicketingException.ErrorCode.ATR_PARSE_ERR, e)
        } ?: throw DccTicketingException(DccTicketingException.ErrorCode.ATR_PARSE_ERR)

        // Validate accessTokenPayload
        validator.validateAccessToken(accessTokenPayload)

        return Output(response.accessToken, accessTokenPayload, response.iv)
    }

    data class Output(
        val accessToken: String,
        val accessTokenPayload: DccTicketingAccessToken,
        val nonceBase64: String
    )
}
