package de.rki.coronawarnapp.qrcode.scanner

import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.coronatest.qrcode.PcrQrCodeExtractor
import de.rki.coronawarnapp.coronatest.qrcode.RapidAntigenQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.CheckInQrCodeExtractor
import de.rki.coronawarnapp.util.collections.replaceAll
import timber.log.Timber
import javax.inject.Inject

class QrCodeValidator @Inject constructor(
    dccQrCodeExtractor: DccQrCodeExtractor,
    raExtractor: RapidAntigenQrCodeExtractor,
    pcrExtractor: PcrQrCodeExtractor,
    checkInQrCodeExtractor: CheckInQrCodeExtractor,
) {
    private val extractors =
        mutableSetOf<QrCodeExtractor<*>>(
            dccQrCodeExtractor,
            raExtractor,
            pcrExtractor,
            checkInQrCodeExtractor
        )

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
