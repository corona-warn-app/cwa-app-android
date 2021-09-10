package de.rki.coronawarnapp.qrcode.scanner

interface QrCodeExtractor<T : QrCode> {
    suspend fun canHandle(rawString: String): Boolean
    suspend fun extract(rawString: String): T
}
