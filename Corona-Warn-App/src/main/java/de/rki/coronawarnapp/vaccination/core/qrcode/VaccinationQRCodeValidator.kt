package de.rki.coronawarnapp.vaccination.core.qrcode

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.qrcode.QrCodeExtractor
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_PREFIX_INVALID
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidVaccinationCertificateException
import timber.log.Timber
import javax.inject.Inject

@Reusable
class VaccinationQRCodeValidator @Inject constructor(
    vaccinationQRCodeExtractor: VaccinationQRCodeExtractor
) {
    private val extractors = setOf(vaccinationQRCodeExtractor)

    fun validate(rawString: String): VaccinationCertificateQRCode {
        // If there is more than one "extractor" in the future, check censoring again.
        // CertificateQrCodeCensor.addQRCodeStringToCensor(rawString)
        return findExtractor(rawString)
            ?.extract(rawString)
            ?.also { Timber.i("Extracted data from QR code is %s", it) }
            ?: throw InvalidVaccinationCertificateException(VC_PREFIX_INVALID)
    }

    private fun findExtractor(rawString: String): QrCodeExtractor<VaccinationCertificateQRCode>? {
        return extractors.find { it.canHandle(rawString) }
    }
}
