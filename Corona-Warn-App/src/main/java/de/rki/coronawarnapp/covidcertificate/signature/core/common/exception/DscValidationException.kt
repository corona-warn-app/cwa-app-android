package de.rki.coronawarnapp.covidcertificate.signature.core.common.exception

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.exception.CovidCertificateException
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.ui.CachedString
import de.rki.coronawarnapp.util.ui.LazyString

open class DscValidationException(
    val errorCode: ErrorCode,
    cause: Throwable? = null,
) : CovidCertificateException(errorCode.message, cause), HasHumanReadableError {

    enum class ErrorCode(
        val message: String
    ) {
        FILE_MISSING("Acceptance rules archive is missing files."),
        SIGNATURE_INVALID("Acceptance rules archive has an invalid signature."),
        EXTRACTION_FAILED("Acceptance rules could not be extracted from archive."),
        NO_NETWORK("No or poor network when downloading value sets, acceptance rules, or invalidation rules."),
        SERVER_ERROR("Update of DSCs failed with server error."),
    }

    open val errorMessage: LazyString
        get() = when (errorCode) {
            ErrorCode.NO_NETWORK -> CachedString { context ->
                context.getString(R.string.dcc_validation_error_no_network)
            }
            else -> CachedString { context ->
                context.getString(R.string.dcc_validation_error_try_again)
            }
        }

    override fun toHumanReadableError(context: Context): HumanReadableError = HumanReadableError(
        description = errorMessage.get(context) + " ($errorCode)"
    )
}
