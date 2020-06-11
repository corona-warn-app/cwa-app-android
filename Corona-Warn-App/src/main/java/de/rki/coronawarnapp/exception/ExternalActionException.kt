package de.rki.coronawarnapp.exception

import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.exception.reporting.ErrorCodes
import de.rki.coronawarnapp.exception.reporting.ReportedException

class ExternalActionException(cause: Throwable) : ReportedException(
    ErrorCodes.EXTERNAL_NAVIGATION.code,
    "Error during external navigation, likely due to bad target / action not available",
    cause,
    R.string.errors_external_action
)
