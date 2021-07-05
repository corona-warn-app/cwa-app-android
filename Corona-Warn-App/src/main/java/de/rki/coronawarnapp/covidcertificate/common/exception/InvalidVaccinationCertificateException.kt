package de.rki.coronawarnapp.covidcertificate.common.exception

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.ui.CachedString
import de.rki.coronawarnapp.util.ui.LazyString

class InvalidVaccinationCertificateException(
    errorCode: ErrorCode,
    cause: Throwable? = null,
) : InvalidHealthCertificateException(errorCode, cause) {
    override fun toHumanReadableError(context: Context): HumanReadableError {
        return HumanReadableError(
            description = errorMessage.get(context) + " ($PREFIX$errorCode)"
        )
    }

    override val errorMessage: LazyString
        get() = when (errorCode) {
            ErrorCode.NO_VACCINATION_ENTRY -> CachedString { context ->
                context.getString(ERROR_MESSAGE_VC_NOT_YET_SUPPORTED)
            }

            ErrorCode.MULTIPLE_VACCINATION_ENTRIES -> CachedString { context ->
                context.getString(ERROR_MESSAGE_VC_NOT_YET_SUPPORTED)
            }

            ErrorCode.NAME_MISMATCH,
            ErrorCode.DOB_MISMATCH -> CachedString { context ->
                context.getString(ERROR_MESSAGE_VC_DIFFERENT_PERSON)
            }
            else -> super.errorMessage
        }
}

private const val PREFIX = "VC_"

private const val ERROR_MESSAGE_VC_NOT_YET_SUPPORTED = R.string.error_vc_not_yet_supported
private const val ERROR_MESSAGE_VC_DIFFERENT_PERSON = R.string.error_vc_different_person
