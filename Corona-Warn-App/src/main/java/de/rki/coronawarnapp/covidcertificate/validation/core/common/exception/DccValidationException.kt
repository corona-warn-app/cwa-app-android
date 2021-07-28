package de.rki.coronawarnapp.covidcertificate.validation.core.common.exception

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.exception.CovidCertificateException
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.ui.CachedString
import de.rki.coronawarnapp.util.ui.LazyString

open class DccValidationException(
    val errorCode: ErrorCode,
    cause: Throwable? = null,
) : CovidCertificateException(errorCode.message, cause), HasHumanReadableError {

    enum class ErrorCode(
        val message: String
    ) {
        ACCEPTANCE_RULE_CLIENT_ERROR("Update of acceptance rules failed with client error."),
        ACCEPTANCE_RULE_JSON_ARCHIVE_FILE_MISSING("Acceptance rules archive is missing files."),
        ACCEPTANCE_RULE_JSON_ARCHIVE_SIGNATURE_INVALID("Acceptance rules archive has an invalid signature."),
        ACCEPTANCE_RULE_JSON_EXTRACTION_FAILED("Acceptance rules could not be extracted from archive."),
        ACCEPTANCE_RULE_SERVER_ERROR("Update of acceptance rules failed with server error."),
        ACCEPTANCE_RULE_JSON_DECODING_FAILED("Decoding acceptance rules failed."),
        INVALIDATION_RULE_CLIENT_ERROR("Update of invalidation rules failed with client error."),
        INVALIDATION_RULE_JSON_ARCHIVE_FILE_MISSING("Invalidation rules archive is missing files."),
        INVALIDATION_RULE_JSON_ARCHIVE_SIGNATURE_INVALID("Invalidation rules archive has an invalid signature."),
        INVALIDATION_RULE_JSON_EXTRACTION_FAILED("Invalidation rules could not be extracted from archive."),
        INVALIDATION_RULE_SERVER_ERROR("Update of invalidation rules failed with server error."),
        INVALIDATION_RULE_JSON_DECODING_FAILED("Decoding invalidation rules failed."),
        ONBOARDED_COUNTRIES_CLIENT_ERROR("Update of onboarded countries failed with client error."),
        ONBOARDED_COUNTRIES_JSON_ARCHIVE_FILE_MISSING("Onboarded countries archive is missing files."),
        ONBOARDED_COUNTRIES_JSON_ARCHIVE_SIGNATURE_INVALID("Onboarded countries archive has invalid signature."),
        ONBOARDED_COUNTRIES_JSON_EXTRACTION_FAILED("Onboarded countries could not be extracted from archive."),
        ONBOARDED_COUNTRIES_SERVER_ERROR("Update of onboarded countries failed with server error."),
        ONBOARDED_COUNTRIES_JSON_DECODING_FAILED("Decoding onboarded dcc countries failed."),
        NO_NETWORK("No or poor network when downloading value sets, acceptance rules, or invalidation rules."),
        VALUE_SET_SERVER_ERROR("Update of value sets failed with server error."),
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
