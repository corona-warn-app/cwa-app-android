package de.rki.coronawarnapp.dccticketing.core.common

import com.nimbusds.jose.JWSAlgorithm.ES256
import com.nimbusds.jose.JWSAlgorithm.PS256
import com.nimbusds.jose.JWSAlgorithm.RS256
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton
import com.nimbusds.jose.util.X509CertUtils
import com.nimbusds.jwt.SignedJWT
import de.rki.coronawarnapp.SecurityProvider
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

/**
 * Security provider is added by [SecurityProvider] at app start
 */
class DccJWKVerification @Inject constructor(securityProvider: SecurityProvider) {

    init {
        securityProvider.setup()
    }

    fun verify(jwt: String, jwkSet: Set<DccJWK>) {
        // 1. Check for empty jwkSet
        if (jwkSet.isEmpty()) throw DccTicketingException(JWT_VER_NO_JWKS)

        // 2. Check alg of JWT
        val signedJWT = try {
            SignedJWT.parse(jwt)
        } catch (e: Exception) {
            Timber.e("Can't parse JWT token $jwt", e)
            throw DccTicketingException(JWT_VER_ALG_NOT_SUPPORTED)
        }

        if (signedJWT.header.algorithm !in listOf(ES256, PS256, RS256))
            throw DccTicketingException(JWT_VER_ALG_NOT_SUPPORTED)

        // 3. Extract kid from JWT
        if (signedJWT.header.keyID.isNullOrEmpty()) throw DccTicketingException(JWT_VER_NO_KID)

        // 4. Check for empty jwkSet
        if (jwkSet.none { it.kid == signedJWT.header.keyID }) throw DccTicketingException(JWT_VER_NO_JWK_FOR_KID)

        // 5. Filter jwkSet by kid
        jwkSet.filter { it.kid == signedJWT.header.keyID }.forEach {
            try {
                // 6. Verify signature
                val publicKey = X509CertUtils.parse(it.x5c.first().decodeBase64()?.toByteArray()).publicKey
                verify(signedJWT, publicKey)
                return
            } catch (e: Exception) {
                Timber.w("JWT with matching kid ${it.kid} was not verified", e)
            }
        }

        throw DccTicketingException(JWT_VER_SIG_INVALID)
    }

    fun verify(signedJWT: SignedJWT, publicKey: PublicKey) {

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
