package de.rki.coronawarnapp.dccticketing.core.qrcode

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.ui.CachedString
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.toResolvingString

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
        get() = R.string.dcc_ticketing_error_service_provider_error_no_name.toResolvingString()

    override fun toHumanReadableError(context: Context): HumanReadableError {
        return HumanReadableError(
            description = errorMessage.get(context) + " ($errorCode)"
        )
    }
}
