package de.rki.coronawarnapp.vaccination.core.server.proof

import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.vaccination.core.certificate.RawCOSEObject
import timber.log.Timber
import javax.inject.Inject

@Reusable
class VaccinationProofServer @Inject constructor(
    private val apiProvider: Lazy<VaccinationProofApiV2>,
    private val proofCertificateCOSEParser: ProofCertificateCOSEParser
) {

    private val api: VaccinationProofApiV2
        get() = apiProvider.get()

    suspend fun getProofCertificate(vaccinationCertificate: RawCOSEObject): ProofCertificateResponse {
        val response = api.obtainProofCertificate(vaccinationCertificate)
        Timber.tag(TAG).v("Received RawCose response (size=%d)", response.data.size)

        val proofCertificateData = proofCertificateCOSEParser.parse(response)

        return ProofCertificateResponse(
            proofData = proofCertificateData,
            rawCose = response,
        )
    }

    companion object {
        private const val TAG = "VaccinationProofServer"
    }
}
