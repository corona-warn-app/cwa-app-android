package de.rki.coronawarnapp.presencetracing.checkins.qrcode

import de.rki.coronawarnapp.qrcode.scanner.QrCode
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.QRCodePayload

data class CheckInQrCode(
    val qrCodePayload: QRCodePayload
) : QrCode
