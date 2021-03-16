package de.rki.coronawarnapp.eventregistration.checkins.qrcode

interface QRCodeVerifier {

    suspend fun verify(rawTraceLocation: ByteArray): QRCodeVerifyResult
}
