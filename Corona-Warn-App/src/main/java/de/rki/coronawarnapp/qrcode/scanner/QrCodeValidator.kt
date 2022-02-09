package de.rki.coronawarnapp.qrcode.scanner

import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.coronatest.qrcode.PcrQrCodeExtractor
import de.rki.coronawarnapp.coronatest.qrcode.rapid.RapidAntigenQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.CheckInQrCodeExtractor
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeExtractor
import timber.log.Timber
import javax.inject.Inject

class QrCodeValidator @Inject constructor(
    dccQrCodeExtractor: DccQrCodeExtractor,
    raExtractor: RapidAntigenQrCodeExtractor,
    pcrExtractor: PcrQrCodeExtractor,
    checkInQrCodeExtractor: CheckInQrCodeExtractor,
    dccTicketingQrCodeExtractor: DccTicketingQrCodeExtractor,
) {
    private val extractors = mutableSetOf<QrCodeExtractor<*>>(
        dccQrCodeExtractor,
        raExtractor,
        pcrExtractor,
        checkInQrCodeExtractor,
        dccTicketingQrCodeExtractor,
    )

    /**
     * @throws [UnsupportedQrCodeException], [InvalidHealthCertificateException], [InvalidQRCodeException]
     */
    suspend fun validate(rawString: String): QrCode = findExtractor(rawString)
        ?.extract(rawString)
        // QR code could contains anything, don't write decoded data into logs to prevent privacy issues
        ?.also { Timber.i("Data from ${it::class.simpleName} QR code has been extracted") }
        ?: throw UnsupportedQrCodeException()

    private suspend fun findExtractor(rawString: String): QrCodeExtractor<*>? {
        return extractors.find { it.canHandle(rawString) }
    }
}
