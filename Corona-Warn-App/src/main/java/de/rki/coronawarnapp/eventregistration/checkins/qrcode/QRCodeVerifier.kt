package de.rki.coronawarnapp.eventregistration.checkins.qrcode

interface QRCodeVerifier {

    suspend fun verify(uri: String): EventQRCode
}
