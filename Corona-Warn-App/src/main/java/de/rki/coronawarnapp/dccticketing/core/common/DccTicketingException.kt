package de.rki.coronawarnapp.dccticketing.core.common

import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ui.CachedString
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.toResolvingString

class DccTicketingException(
    val errorCode: ErrorCode,
    override val cause: Throwable? = null
) : Exception(errorCode.message, cause) {

    enum class TextKey {
        SERVICE_PROVIDER_ERROR_NO_NAME,
        SERVICE_PROVIDER_ERROR,
        MIN_VER_ERROR,
        TRY_AGAIN
    }

    @Suppress("MaxLineLength")
    enum class ErrorCode(
        val message: String,
        val textKey: TextKey = TextKey.SERVICE_PROVIDER_ERROR
    ) {

        /**
         *  Encryption error codes.
         *  Failure while encrypting DCC; user may retry
         */
        AES_CBC_INVALID_IV(message = ENC_ERROR_MSG),
        AES_CBC_INVALID_KEY(message = ENC_ERROR_MSG),
        AES_CBC_NOT_SUPPORTED(message = ENC_ERROR_MSG),
        AES_GCM_INVALID_IV(message = ENC_ERROR_MSG),
        AES_GCM_INVALID_KEY(message = ENC_ERROR_MSG),
        AES_GCM_NOT_SUPPORTED(message = ENC_ERROR_MSG),

        EC_SIGN_INVALID_KEY(message = ENC_ERROR_MSG),
        EC_SIGN_NOT_SUPPORTED(message = ENC_ERROR_MSG),

        RSA_ENC_INVALID_KEY(message = ENC_ERROR_MSG),
        RSA_ENC_NOT_SUPPORTED(message = ENC_ERROR_MSG),

        /**
         *  Request Access Token error codes.
         *  Failure when obtaining Access Token; user may retry
         */
        ATR_AUD_INVALID(message = ATR_ERROR_MSG),
        ATR_PARSE_ERR(message = ATR_ERROR_MSG),
        ATR_CERT_PIN_MISMATCH(message = ATR_ERROR_MSG),
        ATR_CERT_PIN_NO_JWK_FOR_KID(message = ATR_ERROR_MSG),
        ATR_CLIENT_ERR(message = ATR_ERROR_MSG),
        ATR_JWT_VER_ALG_NOT_SUPPORTED(message = ATR_ERROR_MSG),
        ATR_JWT_VER_EMPTY_JWKS(message = ATR_ERROR_MSG),
        ATR_JWT_VER_NO_JWK_FOR_KID(message = ATR_ERROR_MSG),
        ATR_JWT_VER_NO_KID(message = ATR_ERROR_MSG),
        ATR_JWT_VER_SIG_INVALID(message = ATR_ERROR_MSG),
        ATR_TYPE_INVALID(message = ATR_ERROR_MSG),
        ATR_NO_NETWORK(message = ATR_ERROR_MSG, textKey = TextKey.TRY_AGAIN),
        ATR_SERVER_ERR(message = ATR_ERROR_MSG, textKey = TextKey.TRY_AGAIN),

        /**
         *  Request Result Token error codes.
         *  Failure when obtaining Result Token; user may retry
         */
        RTR_CERT_PIN_MISMATCH(message = RTR_ERROR_MSG),
        RTR_CERT_PIN_HOST_MISMATCH(message = RTR_ERROR_MSG),
        RTR_JWT_VER_ALG_NOT_SUPPORTED(message = RTR_ERROR_MSG),
        RTR_JWT_VER_EMPTY_JWKS(message = RTR_ERROR_MSG),
        RTR_JWT_VER_NO_JWK_FOR_KID(message = RTR_ERROR_MSG),
        RTR_JWT_VER_NO_KID(message = RTR_ERROR_MSG),
        RTR_JWT_VER_SIG_INVALID(message = RTR_ERROR_MSG),
        RTR_CLIENT_ERR(message = RTR_ERROR_MSG),
        RTR_NO_NETWORK(message = RTR_ERROR_MSG, textKey = TextKey.TRY_AGAIN),
        RTR_SERVER_ERR(message = RTR_ERROR_MSG, textKey = TextKey.TRY_AGAIN),

        /**
         * Validation Decorator error codes.
         * Failure when obtaining Service Identity Document of Validation Decorator; user may retry
         */
        VD_ID_CLIENT_ERR(message = VD_ID_ERROR_MSG),
        VD_ID_NO_ATS_SIGN_KEY(message = VD_ID_ERROR_MSG),
        VD_ID_NO_ATS_SVC_KEY(message = VD_ID_ERROR_MSG),
        VD_ID_NO_ATS(message = VD_ID_ERROR_MSG),
        VD_ID_NO_VS_SVC_KEY(message = VD_ID_ERROR_MSG),
        VD_ID_NO_VS(message = VD_ID_ERROR_MSG),
        VD_ID_PARSE_ERR(message = VD_ID_ERROR_MSG),
        VD_ID_EMPTY_X5C(message = VD_ID_ERROR_MSG),
        VD_ID_NO_NETWORK(message = VD_ID_ERROR_MSG, textKey = TextKey.TRY_AGAIN),
        VD_ID_SERVER_ERR(message = VD_ID_ERROR_MSG, textKey = TextKey.TRY_AGAIN),

        /**
         * Validation Service error codes
         * Failure when obtaining Service Identity Document of Validation Service; user may retry
         */
        VS_ID_CERT_PIN_MISMATCH(message = VS_ID_ERROR_MSG),
        VS_ID_CERT_PIN_HOST_MISMATCH(message = VS_ID_ERROR_MSG),
        VS_ID_CLIENT_ERR(message = VS_ID_ERROR_MSG),
        VS_ID_NO_ENC_KEY(message = VS_ID_ERROR_MSG),
        VS_ID_NO_SIGN_KEY(message = VS_ID_ERROR_MSG),
        VS_ID_PARSE_ERR(message = VS_ID_ERROR_MSG),
        VS_ID_EMPTY_X5C(message = VS_ID_ERROR_MSG),
        VS_ID_NO_NETWORK(message = VS_ID_ERROR_MSG, textKey = TextKey.TRY_AGAIN),
        VS_ID_SERVER_ERR(message = VS_ID_ERROR_MSG, textKey = TextKey.TRY_AGAIN),

        SP_ALLOWLIST_NO_MATCH(message = SP_ERROR_MSG, textKey = TextKey.SERVICE_PROVIDER_ERROR_NO_NAME),

        MIN_VERSION_REQUIRED(message = MIN_VER_ERROR_MSG, textKey = TextKey.MIN_VER_ERROR)
    }

    private fun ErrorCode.errorMessageRes(serviceProvider: String): LazyString = when (textKey) {
        TextKey.SERVICE_PROVIDER_ERROR_NO_NAME ->
            R.string.dcc_ticketing_error_service_provider_error_no_name.toResolvingString()
        TextKey.SERVICE_PROVIDER_ERROR ->
            R.string.dcc_ticketing_error_service_provider_error.toResolvingString(
                serviceProvider
            )
        TextKey.TRY_AGAIN ->
            R.string.dcc_ticketing_error_try_again.toResolvingString()
        TextKey.MIN_VER_ERROR -> R.string.dcc_ticketing_error_min_version.toResolvingString()
    }

    fun errorMessage(serviceProvider: String): LazyString = CachedString { context ->
        "${errorCode.errorMessageRes(serviceProvider).get(context)} ($errorCode)"
    }
}

private const val ATR_ERROR_MSG = "Failure when obtaining Access Token"
private const val VS_ID_ERROR_MSG = "Failure when obtaining Service Identity Document of Validation Service"
private const val VD_ID_ERROR_MSG = "Failure when obtaining Service Identity Document of Validation Decorator"
private const val RTR_ERROR_MSG = "Failure when obtaining Result Token"
private const val ENC_ERROR_MSG = "Failure while encrypting DCC"
private const val SP_ERROR_MSG = "Failure when scaning QR code"
private const val MIN_VER_ERROR_MSG = "minimum version of CWA not supported for this feature anymore"

typealias DccTicketingErrorCode = DccTicketingException.ErrorCode
