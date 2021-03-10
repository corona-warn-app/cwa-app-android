package de.rki.coronawarnapp.eventregistration.checkins.qrcode

class InvalidQRCodeSignatureException constructor(
    message: String? = null,
    cause: Throwable? = null
) : QRCodeException(message, cause)
