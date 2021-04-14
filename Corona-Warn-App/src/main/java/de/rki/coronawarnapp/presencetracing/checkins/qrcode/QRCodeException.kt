package de.rki.coronawarnapp.presencetracing.checkins.qrcode

sealed class QRCodeException constructor(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)

class InvalidQrCodeUriException constructor(
    message: String? = null,
    cause: Throwable? = null
) : QRCodeException(message, cause)

class InvalidQrCodePayloadException constructor(
    message: String? = null,
    cause: Throwable? = null
) : QRCodeException(message, cause)

class InvalidQrCodeDataException constructor(
    message: String? = null,
    cause: Throwable? = null
) : QRCodeException(message, cause)
