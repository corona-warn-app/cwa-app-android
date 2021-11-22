package de.rki.coronawarnapp.dccticketing.core.common

import com.nimbusds.jose.JWSAlgorithm.ES256
import com.nimbusds.jose.JWSAlgorithm.PS256
import com.nimbusds.jose.JWSAlgorithm.RS256
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton
import com.nimbusds.jwt.SignedJWT
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.JWT_VER_ALG_NOT_SUPPORTED
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.JWT_VER_NO_JWKS
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.JWT_VER_NO_JWK_FOR_KID
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.JWT_VER_NO_KID
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.JWT_VER_SIG_INVALID
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import java.security.PublicKey
import java.security.cert.CertificateFactory
import java.security.interfaces.RSAPublicKey

class DccJWKVerification() {

    private val certificateFactory by lazy {
        CertificateFactory.getInstance("X.509")
    }

    fun verify(jwt: String, jwkSet: Set<DccJWK>) {
        if (jwkSet.isEmpty()) throw DccTicketingException(JWT_VER_NO_JWKS)

        val signedJWT = SignedJWT.parse(jwt)
        val kid = signedJWT.header.customParams["kid"] as String?

        if (signedJWT.header.algorithm !in listOf(ES256, PS256, RS256)) throw DccTicketingException(
            JWT_VER_ALG_NOT_SUPPORTED
        )
        if (kid.isNullOrEmpty()) throw DccTicketingException(JWT_VER_NO_KID)

        if (jwkSet.none { it.kid == kid }) throw DccTicketingException(JWT_VER_NO_JWK_FOR_KID)

        jwkSet.filter { it.kid == kid }.forEach {
            verify(signedJWT, it.getPublicKey(certificateFactory))
        }

        throw DccTicketingException(JWT_VER_SIG_INVALID)
    }

    fun verify(signedJWT: SignedJWT, publicKey: PublicKey) {

        // TODO: use factory? [DefaultJWSVerifierFactory]
        val verifier = when (signedJWT.header.algorithm) {
            ES256 -> ECDSAVerifier(publicKey as BCECPublicKey).apply {
                jcaContext.provider = BouncyCastleProviderSingleton.getInstance()
            }
            PS256, RS256 -> RSASSAVerifier(publicKey as RSAPublicKey).apply {
                jcaContext.provider = BouncyCastleProviderSingleton.getInstance()
            }
            else -> throw DccTicketingException(JWT_VER_ALG_NOT_SUPPORTED)
        }

        if (!signedJWT.verify(verifier)) throw DccTicketingException(JWT_VER_SIG_INVALID)
    }

}
