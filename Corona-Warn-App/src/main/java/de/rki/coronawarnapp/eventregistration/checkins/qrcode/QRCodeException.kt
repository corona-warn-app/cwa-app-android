package de.rki.coronawarnapp.eventregistration.checkins.qrcode

open class QRCodeException constructor(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)
