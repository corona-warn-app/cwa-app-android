package de.rki.coronawarnapp.covidcertificate.common.exception

import android.content.Context
import de.rki.coronawarnapp.util.HumanReadableError

class InvalidRecoveryCertificateException(errorCode: ErrorCode) : InvalidHealthCertificateException(errorCode) {
    override fun toHumanReadableError(context: Context): HumanReadableError {
        return HumanReadableError(
            description = errorMessage.get(context) + " ($PREFIX$errorCode)"
        )
    }
}

private const val PREFIX = "RC_"
