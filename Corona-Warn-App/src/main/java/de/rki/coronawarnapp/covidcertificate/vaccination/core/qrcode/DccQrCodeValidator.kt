package de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.PREFIX_INVALID
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DccQrCodeValidator @Inject constructor(
    dccQrCodeExtractor: DccQrCodeExtractor
) {
    private val extractors = setOf(dccQrCodeExtractor)

    fun validate(rawString: String): DccQrCode {
        // If there is more than one "extractor" in the future, check censoring again.
        // CertificateQrCodeCensor.addQRCodeStringToCensor(rawString)
        return findExtractor(rawString)
            ?.extract(rawString, mode = DccV1Parser.Mode.CERT_SINGLE_STRICT)
            ?.also { Timber.i("Extracted data from QR code is %s", it) }
            ?: throw InvalidHealthCertificateException(PREFIX_INVALID)
    }

    private fun findExtractor(rawString: String): DccQrCodeExtractor? {
        return extractors.find { it.canHandle(rawString) }
    }
}
