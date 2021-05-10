package de.rki.coronawarnapp.vaccination.core.server.proof

import dagger.Reusable
import de.rki.coronawarnapp.vaccination.core.RawCOSEObject
import javax.inject.Inject

/**
 * Talks with IBM servers?
 */
@Reusable
class VaccinationProofServer @Inject constructor() {

    suspend fun getProofCertificate(
        vaccinationCertificate: RawCOSEObject
    ): ProofCertificateResponse {
        throw NotImplementedError()
    }
}
