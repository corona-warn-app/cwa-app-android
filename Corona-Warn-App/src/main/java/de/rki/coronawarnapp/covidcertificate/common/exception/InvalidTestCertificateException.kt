package de.rki.coronawarnapp.covidcertificate.common.exception

import android.content.Context
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.ui.CachedString
import de.rki.coronawarnapp.util.ui.LazyString

class InvalidTestCertificateException(errorCode: ErrorCode) : InvalidHealthCertificateException(errorCode) {
    override fun toHumanReadableError(context: Context): HumanReadableError {
        return HumanReadableError(
            description = errorMessage.get(context) + " ($errorCode)"
        )
    }

    override val errorMessage: LazyString
        get() = when (errorCode) {
            ErrorCode.AES_DECRYPTION_FAILED,
            ErrorCode.RSA_DECRYPTION_FAILED,
            ErrorCode.HC_COSE_MESSAGE_INVALID,
            ErrorCode.HC_COSE_TAG_INVALID -> CachedString { context ->
                context.getString(ERROR_MESSAGE_E2E_ERROR_CALL_HOTLINE)
            }

            ErrorCode.RSA_KP_GENERATION_FAILED -> CachedString { context ->
                context.getString(ERROR_MESSAGE_TRY_AGAIN)
            }

            ErrorCode.HC_BASE45_DECODING_FAILED,
            ErrorCode.HC_BASE45_ENCODING_FAILED,
            ErrorCode.HC_ZLIB_DECOMPRESSION_FAILED,
            ErrorCode.HC_ZLIB_COMPRESSION_FAILED -> CachedString { context ->
                context.getString(ERROR_MESSAGE_CLIENT_ERROR_CALL_HOTLINE)
            }

            else -> super.errorMessage
        }
}
