package de.rki.coronawarnapp.coronatest.qrcode

open class InvalidQRCodeException(
    message: String = "An error occurred while parsing the qr code",
    cause: Throwable? = null,
) : Exception(message, cause)
