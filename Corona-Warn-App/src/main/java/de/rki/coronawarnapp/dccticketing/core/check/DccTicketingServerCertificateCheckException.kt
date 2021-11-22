package de.rki.coronawarnapp.dccticketing.core.check

class DccTicketingServerCertificateCheckException(
    val errorCode: ErrorCode,
    override val cause: Throwable? = null
) : Exception(errorCode.message, cause) {

    enum class ErrorCode(
        val message: String
    ) {
        CERT_PIN_NO_JWK_FOR_KID("No matching jwk for required kid"),
        CERT_PIN_MISMATCH("The SHA-256 fingerprint of leafCertificate is not included in requiredFingerprints")
    }
}
