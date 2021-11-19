package de.rki.coronawarnapp.dccticketing.core.service.processor

import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServer
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingAccessToken
import javax.inject.Inject

class ResultTokenRequestProcessor @Inject constructor(
    private val dccTicketingServer: DccTicketingServer,
    private val jwtTokenParser: JwtTokenParser,
    private val convertor: JwtTokenConverter,
) {

    suspend fun requestResultToken(resultTokenInput: ResultTokenInput): ResultTokenOutput {
        TODO()
    }
}

data class ResultTokenInput(
    val serviceEndpoint: String,
    val validationServiceJwkSet: Set<DccJWK>,
    val validationServiceSignKeyJwkSet: Set<DccJWK>,
    val jwt: String,
    val encryptionKeyKid: String,
    val encryptedDCCBase64: String,
    val encryptionKeyBase64: String,
    val signatureBase64: String,
    val signatureAlgorithm: String,
    val encryptionScheme: String,
)

data class ResultTokenOutput(
    val resultToken: String,
    val resultTokenPayload: DccTicketingAccessToken
)
