package de.rki.coronawarnapp.eventregistration.checkins.qrcode

sealed class QRCodeException constructor(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)

class InvalidQrCodeUriException constructor(message: String? = null) : QRCodeException(message)

class InvalidQrCodePayloadException constructor(message: String? = null) : QRCodeException(message)

class InvalidQrCodeDataException constructor(
    message: String? = null, cause: Throwable? = null
) : QRCodeException(message, cause)
