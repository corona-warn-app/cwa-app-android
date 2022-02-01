package de.rki.coronawarnapp.ccl.configuration.server

import de.rki.coronawarnapp.exception.reporting.ErrorCodes
import de.rki.coronawarnapp.util.security.InvalidSignatureException

class CCLConfigurationInvalidSignatureException(val msg: String) : InvalidSignatureException(
    code = ErrorCodes.CWA_WEB_REQUEST_PROBLEM.code,
    message = msg
)
