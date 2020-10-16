package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.exception.reporting.ErrorCodes
import de.rki.coronawarnapp.exception.reporting.ReportedException

class ApplicationConfigurationInvalidException : ReportedException(
    ErrorCodes.APPLICATION_CONFIGURATION_INVALID.code, "the application configuration is invalid"
)
