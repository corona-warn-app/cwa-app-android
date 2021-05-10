package de.rki.coronawarnapp.vaccination.core.qrcode

import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException

class InvalidHealthCertificateException(
    val errorCode: ErrorCode
) : InvalidQRCodeException(errorCode.message) {
    enum class ErrorCode(
        val message: String
    ) {
        HC_BASE45_DECODING_FAILED("Base45 decoding failed."),
        HC_ZLIB_DECOMPRESSION_FAILED("Zlib decompression failed."),
        HC_COSE_TAG_INVALID("COSE tag invalid."),
        HC_COSE_MESSAGE_INVALID("COSE message invalid."),
        HC_CBOR_DECODING_FAILED("CBOR decoding failed."),
        VC_NO_VACCINATION_ENTRY("Vaccination certificate missing.")
    }
}
