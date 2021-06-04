package de.rki.coronawarnapp.covidcertificate.exception

import android.content.Context
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.ui.CachedString
import de.rki.coronawarnapp.util.ui.LazyString

class InvalidTestCertificateException(errorCode: ErrorCode) : InvalidHealthCertificateException(errorCode) {
    override fun toHumanReadableError(context: Context): HumanReadableError {
        var errorCodeString = errorCode.toString()
        errorCodeString = if (errorCodeString.startsWith(PREFIX_TC)) errorCodeString else PREFIX_TC + errorCodeString
        return HumanReadableError(
            description = errorMessage.get(context) + "\n\n$errorCodeString"
        )
    }

    override val errorMessage: LazyString
        get() = when (errorCode) {

            ErrorCode.DCC_COMP_NO_NETWORK,
            ErrorCode.PKR_NO_NETWORK -> CachedString { context ->
                context.getString(ERROR_MESSAGE_NO_NETWORK)
            }

            ErrorCode.DCC_COMP_202 -> CachedString { context ->
                context.getString(ERROR_MESSAGE_TRY_AGAIN_DCC_NOT_AVAILABLE_YET)
            }

            ErrorCode.DCC_COMP_410 -> CachedString { context ->
                context.getString(ERROR_MESSAGE_DCC_EXPIRED)
            }

            // TODO
/*            ErrorCode.HC_BASE45_DECODING_FAILED,
            ErrorCode.HC_CBOR_DECODING_FAILED,
            ErrorCode.HC_COSE_MESSAGE_INVALID,
            ErrorCode.HC_ZLIB_DECOMPRESSION_FAILED,
            ErrorCode.HC_COSE_TAG_INVALID,
            ErrorCode.HC_CWT_NO_DGC,
            ErrorCode.HC_CWT_NO_EXP,
            ErrorCode.HC_CWT_NO_HCERT,
            ErrorCode.HC_CWT_NO_ISS,
            ErrorCode.JSON_SCHEMA_INVALID,*/

            ErrorCode.AES_DECRYPTION_FAILED,
            ErrorCode.RSA_DECRYPTION_FAILED,
            ErrorCode.DCC_COSE_MESSAGE_INVALID,
            ErrorCode.DCC_COSE_TAG_INVALID,
            ErrorCode.DCC_COMP_404,
            ErrorCode.DCC_COMP_412,
            ErrorCode.PKR_403,
            ErrorCode.PKR_404 -> CachedString { context ->
                context.getString(ERROR_MESSAGE_E2E_ERROR_CALL_HOTLINE)
            }

            ErrorCode.DCC_COMP_400,
            ErrorCode.PKR_400 -> CachedString { context ->
                context.getString(ERROR_MESSAGE_CLIENT_ERROR_CALL_HOTLINE)
            }

            ErrorCode.PKR_FAILED,
            ErrorCode.RSA_KP_GENERATION_FAILED,
            ErrorCode.PKR_500,
            ErrorCode.DCC_COMP_500_INTERNAL -> CachedString { context ->
                context.getString(ERROR_MESSAGE_TRY_AGAIN)
            }

            ErrorCode.HC_BASE45_ENCODING_FAILED,
            ErrorCode.HC_ZLIB_COMPRESSION_FAILED,
            ErrorCode.NO_TEST_ENTRY,
            ErrorCode.DCC_COMP_500_LAB_INVALID_RESPONSE,
            ErrorCode.DCC_COMP_500_SIGNING_CLIENT_ERROR,
            ErrorCode.DCC_COMP_500_SIGNING_SERVER_ERROR,
            ErrorCode.PKR_409 -> CachedString { context ->
                context.getString(ERROR_MESSAGE_GENERIC)
            }
            else -> super.errorMessage
        }
}

private const val PREFIX_TC = "TC_"

// TODO change to correct error message once provided
private const val ERROR_MESSAGE_TRY_AGAIN = ERROR_MESSAGE_GENERIC
private const val ERROR_MESSAGE_DCC_NOT_SUPPORTED_BY_LAB = ERROR_MESSAGE_GENERIC
private const val ERROR_MESSAGE_NO_NETWORK = ERROR_MESSAGE_GENERIC
private const val ERROR_MESSAGE_E2E_ERROR_CALL_HOTLINE = ERROR_MESSAGE_GENERIC
private const val ERROR_MESSAGE_TRY_AGAIN_DCC_NOT_AVAILABLE_YET = ERROR_MESSAGE_GENERIC
private const val ERROR_MESSAGE_CLIENT_ERROR_CALL_HOTLINE = ERROR_MESSAGE_GENERIC
private const val ERROR_MESSAGE_DCC_EXPIRED = ERROR_MESSAGE_GENERIC
