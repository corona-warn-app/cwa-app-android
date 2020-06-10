package de.rki.coronawarnapp.exception

import de.rki.coronawarnapp.exception.reporting.ErrorCodes
import de.rki.coronawarnapp.exception.reporting.ReportedIOException

class CwaWebSecurityException(cause: Throwable) : ReportedIOException(
    ErrorCodes.CWA_WEB_SECURITY_PROBLEM.code,
    "an error occurred while trying to establish a secure connection to the server",
    cause
)
