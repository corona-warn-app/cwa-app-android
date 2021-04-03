package de.rki.coronawarnapp.eventregistration.checkins.qrcode

sealed class QRCodeException constructor(message: String? = null) : Exception(message)

class InvalidQrCodeUriException constructor(message: String? = null) : QRCodeException(message)
class InvalidQrCodeDataException constructor(message: String? = null) : QRCodeException(message)
class InvalidQrCodePayloadException constructor(message: String? = null) : QRCodeException(message)
