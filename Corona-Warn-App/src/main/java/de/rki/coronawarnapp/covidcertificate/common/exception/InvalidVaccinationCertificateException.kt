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

    override val showFaqButton: Boolean
        get() = errorCode in codesVcInvalid
    override val faqButtonText: Int = R.string.error_button_vc_faq
    override val faqLink: Int = R.string.error_button_vc_faq_link

    private val codesVcInvalid = listOf(
        ErrorCode.HC_BASE45_DECODING_FAILED,
        ErrorCode.HC_CBOR_DECODING_FAILED,
        ErrorCode.HC_COSE_MESSAGE_INVALID,
        ErrorCode.HC_ZLIB_DECOMPRESSION_FAILED,
        ErrorCode.HC_COSE_TAG_INVALID,
        ErrorCode.PREFIX_INVALID,
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
            ErrorCode.NO_VACCINATION_ENTRY,
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

private const val ERROR_MESSAGE_VC_INVALID = R.string.error_vc_invalid
private const val ERROR_MESSAGE_VC_NOT_YET_SUPPORTED = R.string.error_vc_not_yet_supported
private const val ERROR_MESSAGE_VC_DIFFERENT_PERSON = R.string.error_vc_different_person
