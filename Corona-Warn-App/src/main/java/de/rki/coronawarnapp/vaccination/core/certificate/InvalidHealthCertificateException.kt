package de.rki.coronawarnapp.vaccination.core.certificate

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.ui.CachedString
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.HC_BASE45_DECODING_FAILED
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.HC_CBOR_DECODING_FAILED
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.HC_COSE_MESSAGE_INVALID
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.HC_COSE_TAG_INVALID
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.HC_ZLIB_DECOMPRESSION_FAILED
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_ALREADY_REGISTERED
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_DOB_MISMATCH
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_HC_CWT_NO_DGC
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_HC_CWT_NO_EXP
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_HC_CWT_NO_HCERT
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_HC_CWT_NO_ISS
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_JSON_SCHEMA_INVALID
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_MULTIPLE_VACCINATION_ENTRIES
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_NAME_MISMATCH
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_NO_VACCINATION_ENTRY
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_PREFIX_INVALID
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_STORING_FAILED

class InvalidHealthCertificateException(
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
        VC_MULTIPLE_VACCINATION_ENTRIES("Multiple vaccination certificates."),
        VC_PREFIX_INVALID("Prefix invalid."),
        VC_STORING_FAILED("Storing failed."),
        VC_JSON_SCHEMA_INVALID("Json schema invalid."),
        VC_NAME_MISMATCH("Name does not match."),
        VC_ALREADY_REGISTERED("Certificate already registered."),
        VC_DOB_MISMATCH("Date of birth does not match."),
        VC_HC_CWT_NO_DGC("Dgc missing."),
        VC_HC_CWT_NO_EXP("Expiration date missing."),
        VC_HC_CWT_NO_HCERT("Health certificate missing."),
        VC_HC_CWT_NO_ISS("Issuer missing."),
    }

    val showFaqButton: Boolean
        get() = errorCode in codesVcInvalid

    private val codesVcInvalid = listOf(
        HC_BASE45_DECODING_FAILED,
        HC_CBOR_DECODING_FAILED,
        HC_COSE_MESSAGE_INVALID,
        HC_ZLIB_DECOMPRESSION_FAILED,
        HC_COSE_TAG_INVALID,
        VC_PREFIX_INVALID,
        VC_HC_CWT_NO_DGC,
        VC_HC_CWT_NO_EXP,
        VC_HC_CWT_NO_HCERT,
        VC_HC_CWT_NO_ISS,
        VC_JSON_SCHEMA_INVALID
    )

    val errorMessage: LazyString
        get() = when (errorCode) {
            in codesVcInvalid -> CachedString { context ->
                context.getString(ERROR_MESSAGE_VC_INVALID)
            }
            VC_NO_VACCINATION_ENTRY -> CachedString { context ->
                context.getString(ERROR_MESSAGE_VC_NOT_YET_SUPPORTED)
            }
            VC_MULTIPLE_VACCINATION_ENTRIES -> CachedString { context ->
                context.getString(ERROR_MESSAGE_VC_NOT_YET_SUPPORTED)
            }
            VC_STORING_FAILED -> CachedString { context ->
                context.getString(ERROR_MESSAGE_VC_SCAN_AGAIN)
            }
            VC_NAME_MISMATCH, VC_DOB_MISMATCH -> CachedString { context ->
                context.getString(ERROR_MESSAGE_VC_DIFFERENT_PERSON)
            }
            VC_ALREADY_REGISTERED -> CachedString { context ->
                context.getString(ERROR_MESSAGE_ALREADY_REGISTERED)
            }
            // should never get here
            else -> CachedString { context ->
                context.getString(ERROR_MESSAGE_VC_INVALID)
            }
        }

    override fun toHumanReadableError(context: Context): HumanReadableError {
        var errorCodeString = errorCode.toString()
        errorCodeString = if (errorCodeString.startsWith(PREFIX)) errorCodeString else PREFIX + errorCodeString
        return HumanReadableError(
            description = errorMessage.get(context) + "\n\n$errorCodeString"
        )
    }
}

private const val PREFIX = "VC_"
private const val ERROR_MESSAGE_VC_INVALID = R.string.error_vc_invalid
private const val ERROR_MESSAGE_VC_NOT_YET_SUPPORTED = R.string.error_vc_not_yet_supported
private const val ERROR_MESSAGE_VC_SCAN_AGAIN = R.string.error_vc_scan_again
private const val ERROR_MESSAGE_VC_DIFFERENT_PERSON = R.string.error_vc_different_person
private const val ERROR_MESSAGE_ALREADY_REGISTERED = R.string.error_vc_already_registered
