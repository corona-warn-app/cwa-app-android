package de.rki.coronawarnapp.vaccination.core.server.proof

import dagger.Lazy
import dagger.Reusable
import okio.ByteString
import javax.inject.Inject

@Reusable
class VaccinationProofServer @Inject constructor(
    private val api: Lazy<VaccinationProofApiV2>
) {

    suspend fun getProofCertificate(vaccinationCertificate: ByteString) =
        api.get().obtainProofCertificate(vaccinationCertificate).let {
            object : ProofCertificateResponse {
                override val proofCertificateData = ProofCertificateCOSEParser().parse(it)
                override val proofCertificateCOSE = it
            }
        }
}
