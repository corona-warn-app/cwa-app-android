package de.rki.coronawarnapp.covidcertificate.common.exception

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.ui.CachedString
import de.rki.coronawarnapp.util.ui.LazyString

@Suppress("MaxLineLength")
open class InvalidHealthCertificateException(
    val errorCode: ErrorCode,
    cause: Throwable? = null,
) : HasHumanReadableError, InvalidQRCodeException(errorCode.message, cause) {
    enum class ErrorCode(
        val message: String
    ) {
        HC_BASE45_DECODING_FAILED("Base45 decoding failed."),
        HC_BASE45_ENCODING_FAILED("Base45 encoding failed."),
        HC_ZLIB_DECOMPRESSION_FAILED("Zlib decompression failed."),
        HC_ZLIB_COMPRESSION_FAILED("Zlib compression failed."),
        HC_COSE_TAG_INVALID("COSE tag invalid."),
        HC_COSE_MESSAGE_INVALID("COSE message invalid."),
        HC_CBOR_DECODING_FAILED("CBOR decoding failed."),
        NO_VACCINATION_ENTRY("Vaccination certificate missing."),
        MULTIPLE_VACCINATION_ENTRIES("Multiple vaccination certificates."),
        MULTIPLE_TEST_ENTRIES("Multiple test certificates."),
        MULTIPLE_RECOVERY_ENTRIES("Multiple recovery certificates."),
        NO_TEST_ENTRY("Test certificate missing."),
        NO_RECOVERY_ENTRY("Recovery certificate missing."),
        HC_PREFIX_INVALID("Prefix invalid."),
        STORING_FAILED("Storing failed."),
        HC_JSON_SCHEMA_INVALID("Json schema invalid."),
        NAME_MISMATCH("Name does not match."),
        ALREADY_REGISTERED("Certificate already registered."),
        DOB_MISMATCH("Date of birth does not match."),
        HC_CWT_NO_DGC("Dgc missing."),
        HC_CWT_NO_EXP("Expiration date missing."),
        HC_CWT_NO_HCERT("Health certificate missing."),
        HC_CWT_NO_ISS("Issuer missing."),

        AES_DECRYPTION_FAILED("AES decryption failed"),
        RSA_DECRYPTION_FAILED("RSA decryption failed."),
        RSA_KP_GENERATION_FAILED("RSA key pair generation failed."),

        HC_COSE_NO_SIGN1("Signature is not a byte sequence."),
        HC_COSE_PH_INVALID("Parsing of the protected header fails."),
        HC_COSE_NO_ALG("No algorithm key."),
        HC_COSE_UNKNOWN_ALG("Unknown algorithm."),
        HC_DSC_NO_MATCH("No DSC match."),
        HC_DSC_OID_MISMATCH_TC("Test certificate OID mismatch."),
        HC_DSC_OID_MISMATCH_VC("Vaccination certificate OID mismatch."),
        HC_DSC_OID_MISMATCH_RC("Recovery certificate OID mismatch."),
        HC_DSC_NOT_YET_VALID("DSC is not valid yet."),
        HC_DSC_EXPIRED("DSC expired."),
    }

    val isCertificateInvalid: Boolean get() = errorCode in codesCertificateInvalid
    private val codesCertificateInvalid = listOf(
        ErrorCode.HC_BASE45_DECODING_FAILED,
        ErrorCode.HC_CBOR_DECODING_FAILED,
        ErrorCode.HC_COSE_MESSAGE_INVALID,
        ErrorCode.HC_ZLIB_DECOMPRESSION_FAILED,
        ErrorCode.HC_COSE_TAG_INVALID,
        ErrorCode.HC_PREFIX_INVALID,
        ErrorCode.HC_CWT_NO_DGC,
        ErrorCode.HC_CWT_NO_EXP,
        ErrorCode.HC_CWT_NO_HCERT,
        ErrorCode.HC_CWT_NO_ISS,
        ErrorCode.HC_JSON_SCHEMA_INVALID
    )

    val isSignatureInvalid: Boolean get() = errorCode in signatureErrorCodes
    private val signatureErrorCodes = listOf(
        ErrorCode.HC_COSE_NO_SIGN1,
        ErrorCode.HC_COSE_PH_INVALID,
        ErrorCode.HC_COSE_NO_ALG,
        ErrorCode.HC_COSE_UNKNOWN_ALG,
        ErrorCode.HC_DSC_NO_MATCH,
        ErrorCode.HC_DSC_OID_MISMATCH_TC,
        ErrorCode.HC_DSC_OID_MISMATCH_VC,
        ErrorCode.HC_DSC_OID_MISMATCH_RC,
        ErrorCode.HC_DSC_NOT_YET_VALID,
        ErrorCode.HC_DSC_EXPIRED,
    )

    val isAlreadyRegistered: Boolean get() = errorCode == ErrorCode.ALREADY_REGISTERED

    open val errorMessage: LazyString
        get() = when (errorCode) {
            ErrorCode.STORING_FAILED -> CachedString { context ->
                context.getString(ERROR_MESSAGE_SCAN_AGAIN)
            }
            ErrorCode.ALREADY_REGISTERED -> CachedString { context ->
                context.getString(ERROR_MESSAGE_ALREADY_REGISTERED)
            }
            in codesCertificateInvalid -> CachedString { context ->
                context.getString(ERROR_MESSAGE_CERTIFICATE_INVALID)
            }
            in signatureErrorCodes -> CachedString { context ->
                context.getString(ERROR_MESSAGE_SIGNATURE_INVALID)
            }
            else -> CachedString { context ->
                context.getString(ERROR_MESSAGE_GENERIC)
            }
        }

    override fun toHumanReadableError(context: Context): HumanReadableError {
        return HumanReadableError(
            description = errorMessage.get(context) + " ($errorCode)"
        )
    }
}

private const val ERROR_MESSAGE_SIGNATURE_INVALID = R.string.dcc_signature_validation_dialog_message
private const val ERROR_MESSAGE_CERTIFICATE_INVALID = R.string.error_dcc_invalid
private const val ERROR_MESSAGE_SCAN_AGAIN = R.string.error_dcc_scan_again
private const val ERROR_MESSAGE_ALREADY_REGISTERED = R.string.error_dcc_already_registered
