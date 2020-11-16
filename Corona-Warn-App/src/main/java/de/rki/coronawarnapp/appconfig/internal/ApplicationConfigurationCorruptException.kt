package de.rki.coronawarnapp.appconfig.internal

import de.rki.coronawarnapp.exception.reporting.ErrorCodes
import de.rki.coronawarnapp.exception.reporting.ReportedException

class ApplicationConfigurationCorruptException : ReportedException(
    ErrorCodes.APPLICATION_CONFIGURATION_CORRUPT.code, "the application configuration is corrupt"
)
