package de.rki.coronawarnapp.util.security

import de.rki.coronawarnapp.exception.reporting.ReportedException

open class InvalidSignatureException(
    code: Int,
    message: String,
    cause: Throwable? = null
) : ReportedException(
    code = code,
    message = message,
    cause = cause
)
