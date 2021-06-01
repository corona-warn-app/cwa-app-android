package de.rki.coronawarnapp.vaccination.core.certificate

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.ui.CachedString
import de.rki.coronawarnapp.util.ui.LazyString

class InvalidVaccinationCertificateException(errorCode: ErrorCode) : InvalidHealthCertificateException(errorCode) {
    override fun toHumanReadableError(context: Context): HumanReadableError {
        var errorCodeString = errorCode.toString()
        errorCodeString = if (errorCodeString.startsWith(PREFIX_VC)) errorCodeString else PREFIX_VC + errorCodeString
        return HumanReadableError(
            description = errorMessage.get(context) + "\n\n$errorCodeString"
        )
    }
}

class InvalidTestCertificateException(errorCode: ErrorCode) : InvalidHealthCertificateException(errorCode)

open class InvalidHealthCertificateException(
    val errorCode: ErrorCode
) : HasHumanReadableError, InvalidQRCodeException(errorCode.message) {
    enum class ErrorCode(
        val message: String
    ) {
        HC_BASE45_DECODING_FAILED("Base45 decoding failed."),
        HC_ZLIB_DECOMPRESSION_FAILED("Zlib decompression failed."),
        HC_COSE_TAG_INVALID("COSE tag invalid."),
        HC_COSE_MESSAGE_INVALID("COSE message invalid."),
        HC_CBOR_DECODING_FAILED("CBOR decoding failed."),
        VC_NO_VACCINATION_ENTRY("Vaccination certificate missing."),
        VC_PREFIX_INVALID("Prefix invalid."),
        VC_STORING_FAILED("Storing failed."),
        JSON_SCHEMA_INVALID("Json schema invalid."),
        VC_NAME_MISMATCH("Name does not match."),
        VC_ALREADY_REGISTERED("Certificate already registered."),
        VC_DOB_MISMATCH("Date of birth does not match."),
        HC_CWT_NO_DGC("Dgc missing."),
        HC_CWT_NO_EXP("Expiration date missing."),
        HC_CWT_NO_HCERT("Health certificate missing."),
        HC_CWT_NO_ISS("Issuer missing."),
        RSA_DECRYPTION_FAILED("RSA decryption failed"),
    }

    val errorMessage: LazyString
        get() = when (errorCode) {
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
            ErrorCode.JSON_SCHEMA_INVALID,
            -> CachedString { context ->
                context.getString(ERROR_MESSAGE_VC_INVALID)
            }
            ErrorCode.VC_NO_VACCINATION_ENTRY -> CachedString { context ->
                context.getString(ERROR_MESSAGE_VC_NOT_YET_SUPPORTED)
            }
            ErrorCode.VC_STORING_FAILED -> CachedString { context ->
                context.getString(ERROR_MESSAGE_VC_SCAN_AGAIN)
            }
            ErrorCode.VC_NAME_MISMATCH, ErrorCode.VC_DOB_MISMATCH -> CachedString { context ->
                context.getString(ERROR_MESSAGE_VC_DIFFERENT_PERSON)
            }
            ErrorCode.VC_ALREADY_REGISTERED -> CachedString { context ->
                context.getString(ERROR_MESSAGE_ALREADY_REGISTERED)
            }
            // todo
            else -> CachedString { context ->
                context.getString(ERROR_MESSAGE_VC_INVALID)
            }
        }

    override fun toHumanReadableError(context: Context): HumanReadableError {
        return HumanReadableError(
            description = errorMessage.get(context)
        )
    }
}

private const val PREFIX_VC = "VC_"
private const val ERROR_MESSAGE_VC_INVALID = R.string.error_vc_invalid
private const val ERROR_MESSAGE_VC_NOT_YET_SUPPORTED = R.string.error_vc_not_yet_supported
private const val ERROR_MESSAGE_VC_SCAN_AGAIN = R.string.error_vc_scan_again
private const val ERROR_MESSAGE_VC_DIFFERENT_PERSON = R.string.error_vc_different_person
private const val ERROR_MESSAGE_ALREADY_REGISTERED = R.string.error_vc_already_registered
