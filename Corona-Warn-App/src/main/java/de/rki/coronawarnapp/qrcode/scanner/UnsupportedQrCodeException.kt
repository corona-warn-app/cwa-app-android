package de.rki.coronawarnapp.qrcode.scanner

class UnsupportedQrCodeException(val errorCode: ErrorCode = ErrorCode.UNSUPPORTED_QR_CODE) : Exception() {
    enum class ErrorCode {
        UNSUPPORTED_QR_CODE
    }
}
