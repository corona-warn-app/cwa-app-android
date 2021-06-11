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
        var errorCodeString = errorCode.toString()
        errorCodeString = if (errorCodeString.startsWith(PREFIX_VC)) errorCodeString else PREFIX_VC + errorCodeString
        return HumanReadableError(
            description = errorMessage.get(context) + " ($errorCodeString)"
        )
    }

    val showFaqButton: Boolean
        get() = errorCode in codesVcInvalid

    private val codesVcInvalid = listOf(
        ErrorCode.HC_BASE45_DECODING_FAILED,
        ErrorCode.HC_CBOR_DECODING_FAILED,
        ErrorCode.HC_COSE_MESSAGE_INVALID,
        ErrorCode.HC_ZLIB_DECOMPRESSION_FAILED,
        ErrorCode.HC_COSE_TAG_INVALID,
        ErrorCode.VC_PREFIX_INVALID,
        ErrorCode.HC_CWT_NO_DGC,
        ErrorCode.HC_CWT_NO_EXP,
        ErrorCode.HC_CWT_NO_HCERT,
        ErrorCode.HC_CWT_NO_ISS,
        ErrorCode.JSON_SCHEMA_INVALID
    )

    override val errorMessage: LazyString
        get() = when (errorCode) {
            in codesVcInvalid -> CachedString { context ->
                context.getString(ERROR_MESSAGE_VC_INVALID)
            }

            ErrorCode.VC_NO_VACCINATION_ENTRY -> CachedString { context ->
                context.getString(ERROR_MESSAGE_VC_NOT_YET_SUPPORTED)
            }

            ErrorCode.VC_MULTIPLE_VACCINATION_ENTRIES -> CachedString { context ->
                context.getString(ERROR_MESSAGE_VC_NOT_YET_SUPPORTED)
            }

            ErrorCode.VC_STORING_FAILED -> CachedString { context ->
                context.getString(ERROR_MESSAGE_VC_SCAN_AGAIN)
            }

            ErrorCode.VC_NAME_MISMATCH,
            ErrorCode.VC_DOB_MISMATCH -> CachedString { context ->
                context.getString(ERROR_MESSAGE_VC_DIFFERENT_PERSON)
            }

            ErrorCode.VC_ALREADY_REGISTERED -> CachedString { context ->
                context.getString(ERROR_MESSAGE_VC_ALREADY_REGISTERED)
            }
            else -> super.errorMessage
        }
}

private const val PREFIX_VC = "VC_"

private const val ERROR_MESSAGE_VC_INVALID = R.string.error_vc_invalid
private const val ERROR_MESSAGE_VC_NOT_YET_SUPPORTED = R.string.error_vc_not_yet_supported
private const val ERROR_MESSAGE_VC_SCAN_AGAIN = R.string.error_vc_scan_again
private const val ERROR_MESSAGE_VC_DIFFERENT_PERSON = R.string.error_vc_different_person
private const val ERROR_MESSAGE_VC_ALREADY_REGISTERED = R.string.error_vc_already_registered
