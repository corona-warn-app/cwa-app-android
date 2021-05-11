package de.rki.coronawarnapp.vaccination.core.server.proof

import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.vaccination.core.common.RawCOSEObject
import de.rki.coronawarnapp.vaccination.core.qrcode.HealthCertificateCOSEDecoder
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateCOSEParser
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateV1Parser
import okio.ByteString
import timber.log.Timber
import javax.inject.Inject

@Reusable
class VaccinationProofServer @Inject constructor(
    private val api: Lazy<VaccinationProofApiV2>
) {

    suspend fun getProofCertificate(vaccinationCertificate: RawCOSEObject): ProofCertificateResponse =
        api.get().obtainProofCertificate(
            vaccinationCertificate.toRequestBody()
        ).let {
            val data = it.byteString()
            val data2 = VaccinationCertificateCOSEParser(
                HealthCertificateCOSEDecoder(),
                VaccinationCertificateV1Parser()
            ).parse(data.toByteArray())
            Timber.v("%s", data2)
            return object : ProofCertificateResponse {
                override val proofCertificateData: ProofCertificateData
                    get() = TODO("Not yet implemented")
                override val proofCertificateCOSE: ByteString
                    get() = TODO("Not yet implemented")
            }
        }
}
