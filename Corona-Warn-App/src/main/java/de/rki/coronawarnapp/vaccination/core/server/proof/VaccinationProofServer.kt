package de.rki.coronawarnapp.vaccination.core.server.proof

import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.vaccination.core.server.ProofCertificateServerData

import dagger.Reusable
import okio.ByteString
import javax.inject.Inject


/**
 * Talks with IBM servers?
 */
@Reusable
class VaccinationProofServer @Inject constructor() {

    suspend fun getProofCertificate(
        vaccinationCertificate: ByteString
    ): ProofCertificateResponse {
        throw NotImplementedError()
    }
}
