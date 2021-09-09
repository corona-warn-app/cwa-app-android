package de.rki.coronawarnapp.qrcode.scanner

import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.coronatest.qrcode.PcrQrCodeExtractor
import de.rki.coronawarnapp.coronatest.qrcode.QrCodeExtractor
import de.rki.coronawarnapp.coronatest.qrcode.RapidAntigenQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.util.collections.replaceAll
import timber.log.Timber
import javax.inject.Inject

class QrCodeExtractor @Inject constructor(
    dccQrCodeExtractor: DccQrCodeExtractor,
    raExtractor: RapidAntigenQrCodeExtractor,
    pcrExtractor: PcrQrCodeExtractor,
) {
    private val extractors = mutableSetOf(dccQrCodeExtractor, raExtractor, pcrExtractor)

    fun setExtractors(newExtractors: Set<QrCodeExtractor<*>>) {
        extractors.replaceAll(newExtractors)
    }

    /**
     * @throws [UnsupportedQrCodeException], [InvalidHealthCertificateException], [InvalidQRCodeException]
     */
    fun validate(rawString: String): QrCode {
        return findExtractor(rawString)
            ?.extract(rawString)
            ?.also { Timber.i("Extracted data from QR code is %s", it) }
            ?: throw UnsupportedQrCodeException()
    }

    private fun findExtractor(rawString: String): QrCodeExtractor<*>? {
        return extractors.find { it.canHandle(rawString) }
    }
}
