package de.rki.coronawarnapp.vaccination.core.server.proof

import dagger.Lazy
import dagger.Reusable
import okio.ByteString
import javax.inject.Inject

@Reusable
class VaccinationProofServer @Inject constructor(
    private val api: Lazy<VaccinationProofApiV2>
) {

    suspend fun getProofCertificate(
        vaccinationCertificate: ByteString
    ): ProofCertificateResponse {
        val obtainProofCertificateBase45: String = api.get().obtainProofCertificateBase45(vaccinationCertificate)
        throw NotImplementedError()
    }
}
