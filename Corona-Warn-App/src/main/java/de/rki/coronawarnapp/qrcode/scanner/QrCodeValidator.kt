package de.rki.coronawarnapp.qrcode.scanner

import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.coronatest.qrcode.PcrQrCodeExtractor
import de.rki.coronawarnapp.coronatest.qrcode.RapidAntigenQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.CheckInQrCodeExtractor
import timber.log.Timber
import javax.inject.Inject

class QrCodeValidator @Inject constructor(
    dccQrCodeExtractor: DccQrCodeExtractor,
    raExtractor: RapidAntigenQrCodeExtractor,
    pcrExtractor: PcrQrCodeExtractor,
    checkInQrCodeExtractor: CheckInQrCodeExtractor,
) {
    private val extractors = mutableSetOf<QrCodeExtractor<*>>(
        dccQrCodeExtractor,
        raExtractor,
        pcrExtractor,
        checkInQrCodeExtractor
    )

    /**
     * @throws [UnsupportedQrCodeException], [InvalidHealthCertificateException], [InvalidQRCodeException]
     */
    suspend fun validate(rawString: String): QrCode = findExtractor(rawString)
        ?.extract(rawString)
        ?.also { Timber.i("Extracted data from QR code is %s", it) }
        ?: throw UnsupportedQrCodeException()

    private suspend fun findExtractor(rawString: String): QrCodeExtractor<*>? {
        return extractors.find { it.canHandle(rawString) }
    }
}
