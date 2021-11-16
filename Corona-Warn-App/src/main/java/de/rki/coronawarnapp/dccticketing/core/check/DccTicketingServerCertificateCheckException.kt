package de.rki.coronawarnapp.dccticketing.core.check

class DccTicketingServerCertificateCheckException(
    val errorCode: ErrorCode,
    override val cause: Throwable? = null
) : Exception(errorCode.message, cause) {

    enum class ErrorCode(
        val message: String
    ) {
        CERT_PIN_NO_JWK_FOR_KID(""),
        CERT_PIN_MISMATCH("")
    }
}
