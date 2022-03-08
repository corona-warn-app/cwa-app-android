package de.rki.coronawarnapp.dccreissuance.core.error

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceErrorResponse
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.toResolvingString

class DccReissuanceException(
    val errorCode: ErrorCode,
    cause: Throwable? = null,
    val serverErrorResponse: DccReissuanceErrorResponse? = null
) : Exception(combine(errorCode.message, serverErrorResponse), cause), HasHumanReadableError {

    enum class TextKey {
        CONTACT_SUPPORT,
        NO_NETWORK,
        TRY_AGAIN,
        REISSUANCE_NOT_SUPPORTED
    }

    enum class ErrorCode(val message: String, val textKey: TextKey) {
        DCC_RI_PIN_MISMATCH(
            message = "Public key SHA-256 hash of the leaf certificate and reissueServicePublicKeyDigest did not match",
            textKey = TextKey.CONTACT_SUPPORT
        ),
        DCC_RI_PARSE_ERR(
            message = "Response cannot be parsed to a list of DccReissuanceResponse",
            textKey = TextKey.CONTACT_SUPPORT
        ),
        DCC_RI_NO_NETWORK(
            message = "DCC Reissuance request failed because of a missing or poor network connection",
            textKey = TextKey.NO_NETWORK
        ),
        DCC_RI_400(
            message = statusCodeMsg(code = 400),
            textKey = TextKey.TRY_AGAIN
        ),
        DCC_RI_401(
            message = statusCodeMsg(code = 401),
            textKey = TextKey.REISSUANCE_NOT_SUPPORTED
        ),
        DCC_RI_403(
            message = statusCodeMsg(code = 403),
            textKey = TextKey.REISSUANCE_NOT_SUPPORTED
        ),
        DCC_RI_406(
            message = statusCodeMsg(code = 406),
            textKey = TextKey.TRY_AGAIN
        ),
        DCC_RI_429(
            message = statusCodeMsg(code = 429),
            textKey = TextKey.TRY_AGAIN
        ),
        DCC_RI_500(
            message = statusCodeMsg(code = 500),
            textKey = TextKey.TRY_AGAIN
        ),
        DCC_RI_CLIENT_ERR(
            "DCC Reissuance request failed because of a client error",
            textKey = TextKey.TRY_AGAIN
        ),
        DCC_RI_SERVER_ERR(
            message = "DCC Reissuance request failed because of a server error",
            textKey = TextKey.TRY_AGAIN
        ),
        DCC_RI_NO_RELATION(
            message = "DCC Reissuance failed because the relation was not found",
            textKey = TextKey.CONTACT_SUPPORT
        )
    }

    val errorMessage: LazyString
        get() = when (errorCode.textKey) {
            TextKey.CONTACT_SUPPORT -> R.string.dcc_reissuance_error_handling_text_key_contact_support
            TextKey.NO_NETWORK -> R.string.dcc_reissuance_error_handling_text_key_no_network
            TextKey.TRY_AGAIN -> R.string.dcc_reissuance_error_handling_text_key_try_again
            TextKey.REISSUANCE_NOT_SUPPORTED -> R.string.dcc_reissuance_error_handling_text_key_reissuance_not_supported
        }.toResolvingString()

    override fun toHumanReadableError(context: Context): HumanReadableError {
        var errorCodeMsg = errorCode.name

        if (serverErrorResponse != null) {
            errorCodeMsg += " ${serverErrorResponse.error}"
        }

        return HumanReadableError(
            description = "${errorMessage.get(context)} ($errorCodeMsg)"
        )
    }
}

private fun statusCodeMsg(code: Int) = "DCC Reissuance request failed with status code $code"

private fun combine(message: String, serverErrorResponse: DccReissuanceErrorResponse?): String = when {
    serverErrorResponse != null -> "$message - $serverErrorResponse"
    else -> message
}
