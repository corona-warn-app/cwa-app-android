package de.rki.coronawarnapp.dccticketing.core.qrcode

import android.content.Context
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.covidcertificate.common.exception.ERROR_MESSAGE_GENERIC
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.ui.CachedString
import de.rki.coronawarnapp.util.ui.LazyString

class DccTicketingException(
    val errorCode: ErrorCode,
    cause: Throwable? = null,
) : HasHumanReadableError, InvalidQRCodeException(errorCode.message, cause) {

    enum class ErrorCode(
        val message: String
    ) {
        INIT_DATA_PARSE_ERR(""),
        INIT_DATA_PROTOCOL_INVALID(""),
        INIT_DATA_SUBJECT_EMPTY(""),
        INIT_DATA_SP_EMPTY(""),
        VD_ID_CLIENT_ERR(""),
        VD_ID_NO_NETWORK(""),
        VD_ID_NO_ATS(""),
        VD_ID_NO_ATS_SIGN_KEY(""),
        VD_ID_NO_ATS_SVC_KEY(""),
        VD_ID_NO_VS(""),
        VD_ID_NO_VS_SVC_KEY(""),
        VD_ID_SERVER_ERR(""),
        VD_ID_PARSE_ERR(""),
    }

    val errorMessage: LazyString
        get() {
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
