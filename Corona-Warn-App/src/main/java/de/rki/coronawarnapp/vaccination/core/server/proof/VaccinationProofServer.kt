package de.rki.coronawarnapp.vaccination.core.server.proof

import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.vaccination.core.common.RawCOSEObject
import javax.inject.Inject

@Reusable
class VaccinationProofServer @Inject constructor(
    private val api: Lazy<VaccinationProofApiV2>
) {

    suspend fun getProofCertificate(vaccinationCertificate: RawCOSEObject) =
        api.get().obtainProofCertificate(vaccinationCertificate).let {
            object : ProofCertificateResponse {
                override val proofCertificateData = ProofCertificateCOSEParser().parse(it)
                override val proofCertificateCOSE = it
            }
        }
}
