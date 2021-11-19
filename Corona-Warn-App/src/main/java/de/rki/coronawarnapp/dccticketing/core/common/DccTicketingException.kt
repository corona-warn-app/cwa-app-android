package de.rki.coronawarnapp.dccticketing.core.common

import android.content.Context
import androidx.annotation.StringRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.ui.CachedString
import de.rki.coronawarnapp.util.ui.LazyString

class DccTicketingException(
    val errorCode: ErrorCode,
    override val cause: Throwable? = null
) : Exception(errorCode.message, cause), HasHumanReadableError {

    @Suppress("MaxLineLength")
    enum class ErrorCode(val message: String) {
        /*
         TODO provide right text
         AES_CBC_INVALID_IV("failure while encrypting DCC; user may retry"),
         AES_CBC_INVALID_KEY("failure while encrypting DCC; user may retry"),
         AES_CBC_NOT_SUPPORTED("failure while encrypting DCC; user may retry"),
         AES_GCM_INVALID_IV("failure while encrypting DCC; user may retry"),
         AES_GCM_INVALID_KEY("failure while encrypting DCC; user may retry"),
         AES_GCM_NOT_SUPPORTED("failure while encrypting DCC; user may retry"),
*/
         ATR_AUD_INVALID("failure when obtaining Access Token; user may retry"),
         ATR_PARSE_ERR("failure when obtaining Access Token; user may retry"),
/*
        ATR_CERT_PIN_MISMATCH("failure when obtaining Access Token; user may retry"),
        ATR_CERT_PIN_NO_JWK_FOR_KID("failure when obtaining Access Token; user may retry"),
        ATR_CLIENT_ERR("failure when obtaining Access Token; user may retry"),
        ATR_JWT_VER_ALG_NOT_SUPPORTED("failure when obtaining Access Token; user may retry"),
        ATR_JWT_VER_EMPTY_JWKS("failure when obtaining Access Token; user may retry"),
        ATR_JWT_VER_NO_JWK_FOR_KID("failure when obtaining Access Token; user may retry"),
        ATR_JWT_VER_NO_KID("failure when obtaining Access Token; user may retry"),
        ATR_JWT_VER_SIG_INVALID("failure when obtaining Access Token; user may retry"),
        ATR_NO_NETWORK("failure when obtaining Access Token; user may retry"),
        ATR_SERVER_ERR("failure when obtaining Access Token; user may retry"),
        ATR_TYPE_INVALID("failure when obtaining Access Token; user may retry"),
         EC_SIGN_INVALID_KEY("failure while encrypting DCC; user may retry"),
         EC_SIGN_NOT_SUPPORTED("failure while encrypting DCC; user may retry"),
         RSA_ENC_INVALID_KEY("failure while encrypting DCC; user may retry"),
         RSA_ENC_NOT_SUPPORTED("failure while encrypting DCC; user may retry"),
 */
        /**
         *  equest Result Token error codes.
         *  Failure when obtaining Result Token; user may retry
         */
        RTR_CERT_PIN_MISMATCH("RTR_CERT_PIN_MISMATCH"),
        RTR_CERT_PIN_NO_JWK_FOR_KID("RTR_CERT_PIN_NO_JWK_FOR_KID"),
        RTR_CLIENT_ERR("RTR_CLIENT_ERR"),
        RTR_JWT_VER_ALG_NOT_SUPPORTED("RTR_JWT_VER_ALG_NOT_SUPPORTED"),
        RTR_JWT_VER_EMPTY_JWKS("RTR_JWT_VER_EMPTY_JWKS"),
        RTR_JWT_VER_NO_JWK_FOR_KID("RTR_JWT_VER_NO_JWK_FOR_KID"),
        RTR_JWT_VER_NO_KID("RTR_JWT_VER_NO_KID"),
        RTR_JWT_VER_SIG_INVALID("RTR_JWT_VER_SIG_INVALID"),
        RTR_NO_NETWORK("RTR_NO_NETWORK"),
        RTR_SERVER_ERR("RTR_SERVER_ERR"),

        /**
         * Validation Decorator error codes.
         * Failure when obtaining Service Identity Document of Validation Decorator; user may retry
         */
        VD_ID_CLIENT_ERR("VD_ID_CLIENT_ERR"),
        VD_ID_NO_ATS_SIGN_KEY("VD_ID_NO_ATS_SIGN_KEY"),
        VD_ID_NO_ATS_SVC_KEY("VD_ID_NO_ATS_SVC_KEY"),
        VD_ID_NO_ATS("VD_ID_NO_ATS"),
        VD_ID_NO_NETWORK("VD_ID_NO_NETWORK"),
        VD_ID_NO_VS_SVC_KEY("VD_ID_NO_VS_SVC_KEY"),
        VD_ID_NO_VS("VD_ID_NO_VS"),
        VD_ID_PARSE_ERR("VD_ID_PARSE_ERR"),
        VD_ID_SERVER_ERR("VD_ID_SERVER_ERR"),
        VD_ID_EMPTY_X5C("VD_ID_EMPTY_X5C"),

        /**
         * Validation Service error codes
         * Failure when obtaining Service Identity Document of Validation Service; user may retry
         */
        VS_ID_CERT_PIN_MISMATCH("VS_ID_CERT_PIN_MISMATCH"),
        VS_ID_CERT_PIN_NO_JWK_FOR_KID("VS_ID_CERT_PIN_NO_JWK_FOR_KID"),
        VS_ID_CLIENT_ERR("VS_ID_CLIENT_ERR"),
        VS_ID_NO_ENC_KEY("VS_ID_NO_ENC_KEY"),
        VS_ID_NO_NETWORK("VS_ID_NO_NETWORK"),
        VS_ID_NO_SIGN_KEY("VS_ID_NO_SIGN_KEY"),
        VS_ID_PARSE_ERR("VS_ID_PARSE_ERR"),
        VS_ID_SERVER_ERR("VS_ID_SERVER_ERR"),
        VS_ID_EMPTY_X5C("VS_ID_EMPTY_X5C")
    }

    // to-do: Add all error codes
    val errorMessage: LazyString
        get() = CachedString { context ->
            when (errorCode) {
                ErrorCode.VS_ID_SERVER_ERR -> R.string.dcc_ticketing_error_try_again
                else -> ERROR_MESSAGE_GENERIC
            }.let { context.getString(it) }
        }

    override fun toHumanReadableError(context: Context): HumanReadableError = HumanReadableError(
        description = errorMessage.get(context) + " ($errorCode)"
    )
}

@StringRes
private const val ERROR_MESSAGE_GENERIC = R.string.errors_generic_text_unknown_error_cause

typealias DccTicketingErrorCode = DccTicketingException.ErrorCode
