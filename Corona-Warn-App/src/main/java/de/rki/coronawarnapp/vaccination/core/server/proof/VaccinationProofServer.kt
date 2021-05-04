package de.rki.coronawarnapp.vaccination.core.server.proof

import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.vaccination.core.server.ProofCertificateServerData

/**
 * Talks with IBM servers?
 */
class VaccinationProofServer {

    suspend fun getProofCertificate(
        vaccinationCertificate: Set<VaccinationCertificate>
    ): ProofCertificateServerData {
        throw NotImplementedError()
    }
}
