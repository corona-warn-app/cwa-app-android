package de.rki.coronawarnapp.presencetracing.warning.download

import de.rki.coronawarnapp.exception.reporting.ErrorCodes
import de.rki.coronawarnapp.util.security.InvalidSignatureException

class TraceTimeWarningPackageValidationException(message: String) : InvalidSignatureException(
    code = ErrorCodes.APPLICATION_CONFIGURATION_INVALID.code,
    message = message
)
