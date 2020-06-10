package de.rki.coronawarnapp.exception

import de.rki.coronawarnapp.exception.reporting.ErrorCodes
import de.rki.coronawarnapp.exception.reporting.ReportedException

class CwaSecurityException(cause: Throwable) : ReportedException(
    ErrorCodes.CWA_SECURITY_PROBLEM.code,
    "something went wrong during a critical part of the application ensuring security, please refer" +
            "to the details for more information",
    cause
)
