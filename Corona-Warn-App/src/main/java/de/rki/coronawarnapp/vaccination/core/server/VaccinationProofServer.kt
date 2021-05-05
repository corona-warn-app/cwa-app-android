package de.rki.coronawarnapp.vaccination.core.server

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
