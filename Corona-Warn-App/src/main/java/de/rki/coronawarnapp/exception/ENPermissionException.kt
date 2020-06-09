package de.rki.coronawarnapp.exception

import de.rki.coronawarnapp.exception.reporting.ErrorCodes
import de.rki.coronawarnapp.exception.reporting.ReportedException

class ENPermissionException :
    ReportedException(
        ErrorCodes.EN_PERMISSION_PROBLEM.code,
        "user did not grant the exposure notification permission"
    )
