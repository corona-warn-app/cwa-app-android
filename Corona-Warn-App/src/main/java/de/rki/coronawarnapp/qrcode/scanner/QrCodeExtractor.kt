package de.rki.coronawarnapp.qrcode.scanner

interface QrCodeExtractor<T : QrCode> {
    fun canHandle(rawString: String): Boolean
    fun extract(rawString: String): T
}
