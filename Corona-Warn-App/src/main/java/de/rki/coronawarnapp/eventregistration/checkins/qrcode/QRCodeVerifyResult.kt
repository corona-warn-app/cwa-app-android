package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.server.protocols.internal.evreg.EventOuterClass

sealed class QRCodeVerifyResult {
    data class Success(val event: EventOuterClass.Event) : QRCodeVerifyResult()
    data class StartTimeWarning(val event: EventOuterClass.Event) : QRCodeVerifyResult()
    data class EndTimeWarning(val event: EventOuterClass.Event) : QRCodeVerifyResult()
}
