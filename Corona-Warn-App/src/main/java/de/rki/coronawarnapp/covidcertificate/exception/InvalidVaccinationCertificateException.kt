package de.rki.coronawarnapp.covidcertificate.exception

import android.content.Context
import de.rki.coronawarnapp.util.HumanReadableError

class InvalidVaccinationCertificateException(errorCode: ErrorCode) : InvalidHealthCertificateException(errorCode) {
    override fun toHumanReadableError(context: Context): HumanReadableError {
        var errorCodeString = errorCode.toString()
        errorCodeString = if (errorCodeString.startsWith(PREFIX_VC)) errorCodeString else PREFIX_VC + errorCodeString
        return HumanReadableError(
            description = errorMessage.get(context) + "\n\n$errorCodeString"
        )
    }
}

private const val PREFIX_VC = "VC_"
