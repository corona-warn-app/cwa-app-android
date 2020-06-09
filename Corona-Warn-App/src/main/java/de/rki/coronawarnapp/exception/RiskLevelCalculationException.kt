package de.rki.coronawarnapp.exception

import de.rki.coronawarnapp.exception.reporting.ErrorCodes
import de.rki.coronawarnapp.exception.reporting.ReportedException

class RiskLevelCalculationException(cause: Throwable) :
    ReportedException(
        ErrorCodes.RISK_LEVEL_CALCULATION_PROBLEM.code,
        "an exception occurred during risk level calculation", cause
    )
