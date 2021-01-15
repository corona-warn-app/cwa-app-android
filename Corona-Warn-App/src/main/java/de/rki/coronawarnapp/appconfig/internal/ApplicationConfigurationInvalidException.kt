package de.rki.coronawarnapp.appconfig.internal

import de.rki.coronawarnapp.exception.reporting.ErrorCodes
import de.rki.coronawarnapp.util.security.InvalidSignatureException

class ApplicationConfigurationInvalidException(
    cause: Exception? = null,
    message: String
) : InvalidSignatureException(
    code = ErrorCodes.APPLICATION_CONFIGURATION_INVALID.code,
    message = message,
    cause = cause
)
