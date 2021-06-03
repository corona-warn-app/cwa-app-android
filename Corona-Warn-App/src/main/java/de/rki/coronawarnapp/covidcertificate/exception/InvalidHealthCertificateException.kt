package de.rki.coronawarnapp.covidcertificate.exception

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.ui.CachedString
import de.rki.coronawarnapp.util.ui.LazyString

@Suppress("MaxLineLength")
open class InvalidHealthCertificateException(
    val errorCode: ErrorCode
) : HasHumanReadableError, InvalidQRCodeException(errorCode.message) {
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
        VC_NO_VACCINATION_ENTRY("Vaccination certificate missing."),
        NO_TEST_ENTRY("Test certificate missing."),
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
        AES_DECRYPTION_FAILED("AES decryption failed"),
        DCC_COMP_202("DCC Test Certificate Components failed with error 202: DCC pending."),
        DCC_COMP_400("DCC Test Certificate Components failed with error 400: Bad request (e.g. wrong format of registration token)"),
        DCC_COMP_404("DCC Test Certificate Components failed with error 404: Registration token does not exist."),
        DCC_COMP_410("DCC Test Certificate Components failed with error 410: DCC already cleaned up."),
        DCC_COMP_412("DCC Test Certificate Components failed with error 412: Test result not yet received"),
        DCC_COMP_500_INTERNAL("DCC Test Certificate Components failed with error 500: Internal server error."),
        DCC_COMP_500_LAB_INVALID_RESPONSE("DCC Test Certificate Components failed with error 500."),
        DCC_COMP_500_SIGNING_CLIENT_ERROR("DCC Test Certificate Components failed with error 500."),
        DCC_COMP_500_SIGNING_SERVER_ERROR("DCC Test Certificate Components failed with error 500."),
        DCC_COMP_NO_NETWORK("DCC Test Certificate Components failed due to no network connection."),
        PKR_400("Public Key Registration failed with error 400: Bad request (e.g. wrong format of registration token or public key)."),
        PKR_403("Public Key Registration failed with error 403: Registration token is not allowed to issue a DCC."),
        PKR_404("Public Key Registration failed with error 404: Registration token does not exist."),
        PKR_409("Public Key Registration failed with error 409: Registration token is already assigned to a public key."),
        PKR_500("Public Key Registration failed with error 500: Internal server error."),
        PKR_FAILED("Private key request failed."),
        PKR_NO_NETWORK("Private key request failed due to no network connection."),
        RSA_DECRYPTION_FAILED("RSA decryption failed."),
        RSA_KP_GENERATION_FAILED("RSA key pair generation failed."),
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
                context.getString(ERROR_MESSAGE_VC_ALREADY_REGISTERED)
            }
            ErrorCode.DCC_COMP_400,
            ErrorCode.DCC_COMP_404,
            ErrorCode.DCC_COMP_500_INTERNAL,
            ErrorCode.DCC_COMP_500_LAB_INVALID_RESPONSE,
            ErrorCode.DCC_COMP_500_SIGNING_CLIENT_ERROR,
            ErrorCode.DCC_COMP_500_SIGNING_SERVER_ERROR,
            ErrorCode.PKR_400,
            ErrorCode.PKR_500,
            ErrorCode.PKR_FAILED,
            ErrorCode.PKR_NO_NETWORK,
            ErrorCode.RSA_DECRYPTION_FAILED,
            ErrorCode.RSA_KP_GENERATION_FAILED -> CachedString { context ->
                context.getString(ERROR_MESSAGE_HC_TRY_AGAIN)
            }
            ErrorCode.DCC_COMP_NO_NETWORK -> CachedString { context ->
                context.getString(ERROR_MESSAGE_HC_NO_NETWORK)
            }
            else -> CachedString { context ->
                context.getString(ERROR_MESSAGE_GENERIC)
            }
        }

    override fun toHumanReadableError(context: Context): HumanReadableError {
        return HumanReadableError(
            description = errorMessage.get(context)
        )
    }
}

private const val ERROR_MESSAGE_VC_INVALID = R.string.error_vc_invalid
private const val ERROR_MESSAGE_VC_NOT_YET_SUPPORTED = R.string.error_vc_not_yet_supported
private const val ERROR_MESSAGE_VC_SCAN_AGAIN = R.string.error_vc_scan_again
private const val ERROR_MESSAGE_VC_DIFFERENT_PERSON = R.string.error_vc_different_person
private const val ERROR_MESSAGE_VC_ALREADY_REGISTERED = R.string.error_vc_already_registered

// TODO change to correct error message once provided
private const val ERROR_MESSAGE_HC_TRY_AGAIN = R.string.error_hc_try_again
private const val ERROR_MESSAGE_HC_DCC_NOT_SUPPORTED_BY_LAB = R.string.error_hc_dcc_not_supported_by_lab
private const val ERROR_MESSAGE_HC_NO_NETWORK = R.string.error_hc_no_network
private const val ERROR_MESSAGE_GENERIC = R.string.errors_generic_text_unknown_error_cause
