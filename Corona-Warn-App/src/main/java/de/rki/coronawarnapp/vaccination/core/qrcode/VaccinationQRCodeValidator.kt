package de.rki.coronawarnapp.vaccination.core.qrcode

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.qrcode.QrCodeExtractor
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.VC_PREFIX_INVALID
import timber.log.Timber
import javax.inject.Inject

@Reusable
class VaccinationQRCodeValidator @Inject constructor(
    vaccinationQRCodeExtractor: VaccinationQRCodeExtractor
) {
    private val extractors = setOf(vaccinationQRCodeExtractor)

    fun validate(rawString: String): VaccinationCertificateQRCode {
        return findExtractor(rawString)
            ?.extract(rawString)
            ?.also { Timber.i("Extracted data from QR code is $it") }
            ?: throw InvalidHealthCertificateException(VC_PREFIX_INVALID)
    }

    private fun findExtractor(rawString: String): QrCodeExtractor<VaccinationCertificateQRCode>? {
        return extractors.find { it.canHandle(rawString) }
    }
}
