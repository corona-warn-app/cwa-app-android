package de.rki.coronawarnapp.dccticketing.core.common

import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.ATR_AUD_INVALID
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.ATR_PARSE_ERR
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.JWT_VER_ALG_NOT_SUPPORTED
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.JWT_VER_NO_JWKS
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.JWT_VER_NO_JWK_FOR_KID
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.JWT_VER_NO_KID
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.JWT_VER_SIG_INVALID
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingAccessToken
import de.rki.coronawarnapp.dccticketing.core.transaction.toX509certificate

class JwtTokenValidator {

    fun signatureCheck(jwt: String, accessTokenSignJwkSet: Set<DccJWK>) {

        if (accessTokenSignJwkSet.isEmpty()) throw DccTicketingException(JWT_VER_NO_JWKS)

        val alg = "" // TODO: get alg from jwt header
        val kid = "" // TODO: get kid from jwt header
        if (alg != "ES256" && alg != "RS256" && alg != "PS256") throw DccTicketingException(JWT_VER_ALG_NOT_SUPPORTED)
        if (kid.isNullOrEmpty()) throw DccTicketingException(JWT_VER_NO_KID)

        if (accessTokenSignJwkSet.none { it.kid == kid }) throw DccTicketingException(JWT_VER_NO_JWK_FOR_KID)

        accessTokenSignJwkSet.filter { it.kid == kid }.forEach {
            // TODO: check signature
            val publicKey = it.toX509certificate().publicKey
        }

        throw DccTicketingException(JWT_VER_SIG_INVALID)
    }

    fun validateAccessToken(accessToken: DccTicketingAccessToken) {
        if (accessToken.t != 1L && accessToken.t != 2L) throw DccTicketingException(ATR_PARSE_ERR)
        if (accessToken.aud.isBlank()) throw DccTicketingException(ATR_AUD_INVALID)
    }
}
