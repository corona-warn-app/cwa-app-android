package de.rki.coronawarnapp.dccticketing.core.check

import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateCheckException.ErrorCode
import de.rki.coronawarnapp.dccticketing.core.common.DccJWKConverter
import okio.ByteString
import okio.ByteString.Companion.toByteString
import timber.log.Timber
import java.security.cert.Certificate
import javax.inject.Inject

@Reusable
class DccTicketingServerCertificateChecker @Inject constructor(
    private val dccJWKConverter: DccJWKConverter
) {

    /**
     * Checks the server certificate against a set of [DccJWK]
     *
     * @throws [DccTicketingServerCertificateCheckException] if the check fails
     *
     * Note that the absence of an error code indicates a successful check
     */
    @Throws(DccTicketingServerCertificateCheckException::class)
    fun checkCertificate(certificateChain: List<Certificate>, jwkSet: Set<DccJWK>) = try {
        Timber.d("checkCertificate(certificateChain=%s, jwkSet=%s)", certificateChain, jwkSet)

        // 1. Extract leafCertificate
        val leafCertificate = certificateChain.first()

        // 2. Determine requiredKid
        val requiredKid = leafCertificate.createKid()
            .also { Timber.d("requiredKid=%s", it) }

        // 3. Find requiredJwkSet
        val requiredJwkSet = jwkSet.findRequiredJwkSet(requiredKid = requiredKid)

        // 4. Find requiredCertificates
        val requiredCertificates = requiredJwkSet.map { dccJWKConverter.createX509Certificate(jwk = it) }

        // 5. Find requiredFingerprints
        val requiredFingerprints = requiredCertificates.map { it.createSha256Fingerprint() }

        // 6. Compare fingerprints
        when (requiredFingerprints.contains(leafCertificate.createSha256Fingerprint())) {
            true -> Timber.d("Certificate check was successful")
            false -> throw DccTicketingServerCertificateCheckException(ErrorCode.CERT_PIN_MISMATCH)
        }
    } catch (e: Exception) {
        throw when (e) {
            is DccTicketingServerCertificateCheckException -> e
            else ->
                DccTicketingServerCertificateCheckException(errorCode = ErrorCode.CERT_PIN_UNSPECIFIED_ERR, cause = e)
        }
    }

    private fun Certificate.createSha256Fingerprint(): ByteString = encoded.toByteString()
        .sha256()

    // Takes the first 8 bytes of the SHA-256 fingerprint of the certificate and encodes them with base64
    private fun Certificate.createKid(): String = createSha256Fingerprint()
        .substring(0, BYTE_COUNT)
        .base64()

    private fun Set<DccJWK>.findRequiredJwkSet(requiredKid: String): Set<DccJWK> {
        Timber.d("findRequiredJwkSet(requiredKid=%s)", requiredKid)
        val requiredJwkSet = filter { it.kid == requiredKid }.toSet()

        if (requiredJwkSet.isEmpty()) {
            Timber.d("Didn't find jwk for required kid, aborting")
            throw DccTicketingServerCertificateCheckException(ErrorCode.CERT_PIN_NO_JWK_FOR_KID)
        }

        return requiredJwkSet
    }
}

private const val BYTE_COUNT: Int = 8
