package de.rki.coronawarnapp.dccticketing.core.common

import com.nimbusds.jose.JWSAlgorithm.ES256
import com.nimbusds.jose.JWSAlgorithm.PS256
import com.nimbusds.jose.JWSAlgorithm.RS256
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton
import com.nimbusds.jose.util.X509CertUtils
import com.nimbusds.jwt.SignedJWT
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.JWT_VER_ALG_NOT_SUPPORTED
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.JWT_VER_NO_JWKS
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.JWT_VER_NO_JWK_FOR_KID
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.JWT_VER_NO_KID
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.JWT_VER_SIG_INVALID
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import okio.ByteString.Companion.decodeBase64
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import timber.log.Timber
import java.security.PublicKey
import java.security.interfaces.RSAPublicKey
import javax.inject.Inject

class DccJWKVerification @Inject constructor() {

    init {
        X509CertUtils.setProvider(BouncyCastleProviderSingleton.getInstance())
    }

    fun verify(jwt: String, jwkSet: Set<DccJWK>) {
        if (jwkSet.isEmpty()) throw DccTicketingException(JWT_VER_NO_JWKS)

        val signedJWT = try {
            SignedJWT.parse(jwt)
        } catch (e: Exception) {
            Timber.e("Can't parse JWT token $jwt", e)
            throw DccTicketingException(JWT_VER_ALG_NOT_SUPPORTED)
        }

        if (signedJWT.header.algorithm !in listOf(ES256, PS256, RS256))
            throw DccTicketingException(JWT_VER_ALG_NOT_SUPPORTED)

        if (signedJWT.header.keyID.isNullOrEmpty()) throw DccTicketingException(JWT_VER_NO_KID)

        if (jwkSet.none { it.kid == signedJWT.header.keyID }) throw DccTicketingException(JWT_VER_NO_JWK_FOR_KID)

        jwkSet.filter { it.kid == signedJWT.header.keyID }.forEach {
            try {
                val publicKey = X509CertUtils.parse(it.x5c.first().decodeBase64()?.toByteArray()).publicKey
                verify(signedJWT, publicKey)
                return
            } catch (e:Exception) {
                Timber.w("JWT with matching kid ${it.kid} was not verified", e)
            }
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
