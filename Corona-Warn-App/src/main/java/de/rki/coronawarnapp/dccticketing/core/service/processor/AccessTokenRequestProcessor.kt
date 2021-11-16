package de.rki.coronawarnapp.dccticketing.core.service.processor

import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingErrorCode
import de.rki.coronawarnapp.dccticketing.core.common.JwtTokenConverter
import de.rki.coronawarnapp.dccticketing.core.common.JwtTokenParser
import de.rki.coronawarnapp.dccticketing.core.server.AccessTokenRequest
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServer
import de.rki.coronawarnapp.dccticketing.core.server.getAccessToken
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingAccessToken
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingService
import javax.inject.Inject

class AccessTokenRequestProcessor @Inject constructor(
    private val dccTicketingServer: DccTicketingServer,
    private val jwtTokenParser: JwtTokenParser,
    private val convertor: JwtTokenConverter
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


        // TODO: Determine accessTokenPayload

        jwtTokenParser.parse(response.accessToken).let { convertor.jsonToJwtToken(it.body) }

        // TODO: Validate accessTokenPayload

        TODO()
    }

    data class Output(
        val accessToken: String,
        val accessTokenPayload: DccTicketingAccessToken,
        val nonceBase64: String
    )
}
