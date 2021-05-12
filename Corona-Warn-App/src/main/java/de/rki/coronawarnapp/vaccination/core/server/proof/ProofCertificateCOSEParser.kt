package de.rki.coronawarnapp.vaccination.core.server.proof

import dagger.Reusable
import de.rki.coronawarnapp.vaccination.core.certificate.HealthCertificateCOSEDecoder
import de.rki.coronawarnapp.vaccination.core.certificate.HealthCertificateHeaderParser
import de.rki.coronawarnapp.vaccination.core.certificate.RawCOSEObject
import de.rki.coronawarnapp.vaccination.core.certificate.VaccinationDGCV1Parser
import timber.log.Timber
import javax.inject.Inject

@Reusable
class ProofCertificateCOSEParser @Inject constructor(
    private val coseDecoder: HealthCertificateCOSEDecoder,
    private val headerParser: HealthCertificateHeaderParser,
    private val bodyParser: VaccinationDGCV1Parser,
) {

    fun parse(rawCOSEObject: RawCOSEObject): ProofCertificateData {
        Timber.v("Parsing COSE for proof certificate.")
        val cbor = coseDecoder.decode(rawCOSEObject)

        return ProofCertificateData(
            header = headerParser.parse(cbor),
            certificate = bodyParser.parse(cbor)
        ).also {
            Timber.v("Parsed proof certificate for %s", it.certificate.nameData.familyNameStandardized)
        }
    }
}
