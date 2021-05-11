package de.rki.coronawarnapp.vaccination.core.qrcode

import de.rki.coronawarnapp.vaccination.core.certificate.HealthCertificateCOSEDecoder
import de.rki.coronawarnapp.vaccination.core.certificate.HealthCertificateHeaderParser
import de.rki.coronawarnapp.vaccination.core.certificate.RawCOSEObject
import de.rki.coronawarnapp.vaccination.core.certificate.VaccinationDGCV1Parser
import timber.log.Timber
import javax.inject.Inject

class VaccinationCertificateCOSEParser @Inject constructor(
    private val coseDecoder: HealthCertificateCOSEDecoder,
    private val headerParser: HealthCertificateHeaderParser,
    private val bodyParser: VaccinationDGCV1Parser,
) {

    fun parse(rawCOSEObject: RawCOSEObject): VaccinationCertificateData {
        Timber.v("Parsing COSE for vaccination certificate.")
        val cbor = coseDecoder.decode(rawCOSEObject)

        return VaccinationCertificateData(
            header = headerParser.decode(cbor),
            certificate = bodyParser.parse(cbor)
        ).also {
            Timber.v("Parsed vaccination certificate for %s", it.certificate.nameData.familyNameStandardized)
        }
    }

    companion object {
        val STORAGE_INSTANCE = VaccinationCertificateCOSEParser(
            coseDecoder = HealthCertificateCOSEDecoder(),
            headerParser = HealthCertificateHeaderParser(),
            bodyParser = VaccinationDGCV1Parser(),
        )
    }
}
