package de.rki.coronawarnapp.covidcertificate.common.exception

import android.content.Context
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.ui.LazyString

class InvalidRecoveryCertificateException(errorCode: ErrorCode) : InvalidHealthCertificateException(errorCode) {
    override fun toHumanReadableError(context: Context): HumanReadableError {
        return HumanReadableError(
            description = errorMessage.get(context) + " ($errorCode)"
        )
    }

    override val errorMessage: LazyString
        get() = when (errorCode) {
            else -> super.errorMessage
        }
}
