package de.rki.coronawarnapp.covidcertificate.common.exception

open class CovidCertificateException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)
