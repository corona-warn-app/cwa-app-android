package de.rki.coronawarnapp.appconfig.download

import de.rki.coronawarnapp.exception.reporting.ErrorCodes
import de.rki.coronawarnapp.exception.reporting.ReportedException

class ApplicationConfigurationInvalidException(
    cause: Exception? = null
) : ReportedException(
    code = ErrorCodes.APPLICATION_CONFIGURATION_INVALID.code,
    message = "the application configuration is invalid",
    cause = cause
)
