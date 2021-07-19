package de.rki.coronawarnapp.covidcertificate.signature.core.exception

class DscSignatureValidationException(
    val code: String? = null,
    override val cause: Throwable? = null
) : Exception(cause) {

    companion object {
        const val HC_COSE_NO_ALG = "HC_COSE_NO_ALG"
        const val HC_COSE_UNKNOWN_ALG = "HC_COSE_UNKNOWN_ALG"
    }
}
