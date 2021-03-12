package de.rki.coronawarnapp.eventregistration.checkins.qrcode

class InvalidQRCodeDataException constructor(
    message: String? = null,
    cause: Throwable? = null
) : QRCodeException(message, cause)
