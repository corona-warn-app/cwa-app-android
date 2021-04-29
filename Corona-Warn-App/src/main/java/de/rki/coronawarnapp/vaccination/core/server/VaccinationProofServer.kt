package de.rki.coronawarnapp.vaccination.core.server

import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate

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
