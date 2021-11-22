package de.rki.coronawarnapp.dccticketing.core.common

import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.ATR_AUD_INVALID
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.ATR_PARSE_ERR
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.JWT_VER_ALG_NOT_SUPPORTED
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.JWT_VER_NO_JWKS
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.JWT_VER_NO_JWK_FOR_KID
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.JWT_VER_NO_KID
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.JWT_VER_SIG_INVALID
import io.jsonwebtoken.Jwts
import java.security.PublicKey
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.interfaces.RSAPublicKey
import kotlin.reflect.full.findParameterByName

class DccJWKVerification() {

    private val certificateFactory by lazy {
        CertificateFactory.getInstance("X.509")
    }

    fun verify(jwt: String, jwkSet: Set<DccJWK>) {
        if (jwkSet.isEmpty()) throw DccTicketingException(JWT_VER_NO_JWKS)

        val header = getJwtHeader(jwt)
        if (header.alg !in listOf(ALG.ES256, ALG.PS256, ALG.RS256)) throw DccTicketingException(
            JWT_VER_ALG_NOT_SUPPORTED
        )
        if (header.kid.isNullOrEmpty()) throw DccTicketingException(JWT_VER_NO_KID)

        if (jwkSet.none { it.kid == header.kid }) throw DccTicketingException(JWT_VER_NO_JWK_FOR_KID)

        jwkSet.filter { it.kid == header.kid }.forEach {
            verify(jwt, it.getPublicKey(certificateFactory))
        }

        throw DccTicketingException(JWT_VER_SIG_INVALID)
    }

    fun verify(jwtString: String, publicKey: PublicKey) {
        Jwts.parserBuilder()
            .setSigningKey(publicKey)
            .build()
            .parse(jwtString)
    }

    fun getJwtHeader(jwtString: String): JWTHeader {
        val simpleJwt = jwtString.substring(0, jwtString.lastIndexOf('.') + 1)
        val jwt = Jwts.parserBuilder()
            .build()
            .parseClaimsJwt(simpleJwt)

        val alg = ALG::typ.find(jwt.header["alg"]) ?: ALG.UNKNOWN
        val kid = jwt.header["kid"] as String?

        return JWTHeader(alg, kid)
    }

    enum class ALG(val typ: String) {
        ES256("ES256"),
        RS256("RS256"),
        PS256("PS256"),
        UNKNOWN("")
    }

    data class JWTHeader(val alg: ALG, val kid: String?)

    inline fun <reified T : Enum<T>, V> ((T) -> V).find(value: V): T? {
        return enumValues<T>().firstOrNull { this(it) == value }
    }
}
