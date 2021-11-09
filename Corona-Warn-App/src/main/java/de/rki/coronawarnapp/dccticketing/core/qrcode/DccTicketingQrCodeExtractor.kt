package de.rki.coronawarnapp.dccticketing.core.qrcode

import de.rki.coronawarnapp.qrcode.scanner.QrCodeExtractor
import javax.inject.Inject

class DccTicketingQrCodeExtractor @Inject constructor() : QrCodeExtractor<DccTicketingQrCode> {
    override suspend fun canHandle(rawString: String): Boolean {
        // TODO
        return false
    }

    override suspend fun extract(rawString: String): DccTicketingQrCode {
        // TODO
        return object : DccTicketingQrCode {}
    }
}
