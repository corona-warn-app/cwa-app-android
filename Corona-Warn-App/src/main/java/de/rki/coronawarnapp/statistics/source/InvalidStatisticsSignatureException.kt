package de.rki.coronawarnapp.statistics.source

import de.rki.coronawarnapp.exception.reporting.ErrorCodes
import de.rki.coronawarnapp.util.security.InvalidSignatureException

class InvalidStatisticsSignatureException(message: String) : InvalidSignatureException(
    code = ErrorCodes.APPLICATION_CONFIGURATION_INVALID.code,
    message = message
)
