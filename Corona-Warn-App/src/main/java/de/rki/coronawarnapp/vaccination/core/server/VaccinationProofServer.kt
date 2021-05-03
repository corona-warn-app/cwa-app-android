package de.rki.coronawarnapp.vaccination.core.server

import dagger.Reusable
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import javax.inject.Inject

/**
 * Talks with IBM servers?
 */
@Reusable
class VaccinationProofServer @Inject constructor() {

    suspend fun getProofCertificate(
        vaccinationCertificate: Set<VaccinationCertificate>
    ): ProofCertificateServerData {
        throw NotImplementedError()
    }
}
