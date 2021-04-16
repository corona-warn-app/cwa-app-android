package de.rki.coronawarnapp.coronatest.qrcode

open class InvalidQRCodeException(
    message: String = "An error occurred when parsing the qr code"
) : Exception(message)
