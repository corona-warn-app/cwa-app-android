package de.rki.coronawarnapp.eventregistration.checkins.qrcode

interface QRCodeVerifier {

    suspend fun verify(encodedEvent: String): Boolean
}
