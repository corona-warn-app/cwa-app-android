package de.rki.coronawarnapp.covidcertificate.revocation.error

class RevocationException(
    val errorCode: ErrorCode,
    cause: Throwable? = null
) : Exception(errorCode.message, cause) {
    
    enum class ErrorCode(val message: String) {
        // Add error codes
    }
}
