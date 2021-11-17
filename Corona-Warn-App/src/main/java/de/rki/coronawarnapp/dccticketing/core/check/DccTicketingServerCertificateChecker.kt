package de.rki.coronawarnapp.dccticketing.core.check

import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import okio.ByteString.Companion.toByteString
import timber.log.Timber
import java.security.cert.Certificate
import javax.inject.Inject

@Reusable
class DccTicketingServerCertificateChecker @Inject constructor() {

    /**
     * Checks the server certificate against a set of [DccJWK]
     *
     * Note that the absence of an error code indicates a successful check
     */
    @Throws(DccTicketingServerCertificateCheckException::class)
    fun checkCertificate(certificateChain: List<Certificate>, jwkSet: Set<DccJWK>) {
        // 1. Extract leafCertificate
        val leafCertificate = certificateChain.first()

        // 2. Determine requiredKid
        val requiredKid = leafCertificate.createKid().also { Timber.d("requiredKid=%s", it) }

        // 3. Find requiredJwkSet

        // 4. Find requiredCertificates

        // 5. Find requiredFingerprints

        // 6. Compare fingerprints

        // TODO: Implement all steps
    }

    // Takes the first 8 bytes of the SHA-256 fingerprint of the certificate and encodes them with base64
    private fun Certificate.createKid(): String = encoded.toByteString()
        .sha256()
        .substring(0, BYTE_COUNT)
        .base64()
}

private const val BYTE_COUNT: Int = 8
