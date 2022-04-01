package de.rki.coronawarnapp.covidcertificate.revocation.error

class RevocationException(
    val errorCode: ErrorCode,
    cause: Throwable? = null
) : Exception(errorCode.message, cause) {

    // TODO(Add error codes)
    enum class ErrorCode(val message: String) {
    }
}
