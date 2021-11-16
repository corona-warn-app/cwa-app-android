package de.rki.coronawarnapp.dccticketing.core.decorator

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.ui.CachedString
import de.rki.coronawarnapp.util.ui.LazyString

class DccTicketingServiceException(
    val errorCode: ErrorCode,
    cause: Throwable? = null,
) : HasHumanReadableError, InvalidQRCodeException(errorCode.message, cause) {

    enum class ErrorCode(
        val message: String
    ) {
        VD_ID_CLIENT_ERR("Client error."),
        VD_ID_NO_NETWORK("No network."),
        VD_ID_NO_ATS("No ATS."),
        VD_ID_NO_ATS_SIGN_KEY("No ATS sign key."),
        VD_ID_NO_ATS_SVC_KEY("No ATS ?."),
        VD_ID_NO_VS("?."),
        VD_ID_NO_VS_SVC_KEY("?."),
        VD_ID_SERVER_ERR("Server error."),
        VD_ID_PARSE_ERR("Json parsing failed."),
    }

    val errorMessage: LazyString
        get() {
            // TODO error messages
            return CachedString { context ->
                context.getString(ERROR_MESSAGE_GENERIC)
            }
        }

    override fun toHumanReadableError(context: Context): HumanReadableError {
        return HumanReadableError(
            description = errorMessage.get(context) + " ($errorCode)"
        )
    }
}

private const val ERROR_MESSAGE_GENERIC = R.string.errors_generic_text_unknown_error_cause
