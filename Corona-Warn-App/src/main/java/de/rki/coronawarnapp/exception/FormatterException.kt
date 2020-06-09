package de.rki.coronawarnapp.exception

import de.rki.coronawarnapp.exception.reporting.ErrorCodes
import de.rki.coronawarnapp.exception.reporting.ReportedException

class FormatterException(cause: Throwable?) :
    ReportedException(
        ErrorCodes.FORMATTER_PROBLEM.code,
        "exception occurred during formatting",
        cause
    ) {
    constructor() : this(null)
}
