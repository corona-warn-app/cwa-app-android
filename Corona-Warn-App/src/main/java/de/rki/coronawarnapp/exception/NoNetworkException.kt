package de.rki.coronawarnapp.exception

import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.exception.reporting.ErrorCodes
import de.rki.coronawarnapp.exception.reporting.ReportedException

class NoNetworkException(cause: Throwable) : ReportedException(
    ErrorCodes.NO_NETWORK_CONNECTIVITY.code,
    "The application is not connected to a network",
    cause,
    R.string.errors_no_network
)
