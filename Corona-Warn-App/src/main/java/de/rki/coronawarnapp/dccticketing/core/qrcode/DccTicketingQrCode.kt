package de.rki.coronawarnapp.dccticketing.core.qrcode

import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.qrcode.scanner.QrCode

data class DccTicketingQrCode(
    val qrCode: QrCodeString,
    val data: DccTicketingData
) : QrCode
