package de.rki.coronawarnapp.dccticketing.core.qrcode

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.ui.CachedString
import de.rki.coronawarnapp.util.ui.LazyString

class DccTicketingInvalidQrCodeException(
    val errorCode: ErrorCode,
    cause: Throwable? = null,
) : HasHumanReadableError, InvalidQRCodeException(errorCode.message, cause) {

    enum class ErrorCode(
        val message: String
    ) {
        INIT_DATA_PARSE_ERR("Json parsing failed."),
        INIT_DATA_PROTOCOL_INVALID("Invalid protocol."),
        INIT_DATA_SUBJECT_EMPTY("Data subject is missing."),
        INIT_DATA_SP_EMPTY("Service provider is missing."),
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
