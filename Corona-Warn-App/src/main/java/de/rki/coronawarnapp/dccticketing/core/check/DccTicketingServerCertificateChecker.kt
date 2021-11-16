package de.rki.coronawarnapp.dccticketing.core.check

import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
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

        // 2. Determine requiredKid

        // 3. Find requiredJwkSet

        // 4. Find requiredCertificates

        // 5. Find requiredFingerprints

        // 6. Compare fingerprints

        //TODO: Implement all steps
    }
}
