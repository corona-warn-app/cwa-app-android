package de.rki.coronawarnapp.dccticketing.core.allowlist.internal

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.ui.toResolvingString

class DccTicketingAllowListException(
    val errorCode: ErrorCode,
    cause: Throwable? = null,
) : HasHumanReadableError, Exception(errorCode.message, cause) {

    enum class ErrorCode(
        val message: String
    ) {
        FILE_MISSING("Allow list archive is missing files."),
        SIGNATURE_INVALID("Allow list archive has an invalid signature."),
        NO_NETWORK("No or poor network when downloading allow list."),
        SERVER_ERROR("Update of Allow list failed with server error."),
        CLIENT_ERROR("Update of Allow list failed with client error."),

        ALLOWLIST_NO_MATCH("AllowList no match."),
    }

    override fun toHumanReadableError(context: Context): HumanReadableError {
        return HumanReadableError(
            title = TITLE_ID.toResolvingString().get(context),
            description = MESSAGE_ID.toResolvingString().get(context)
        )
    }

    companion object {
        private const val TITLE_ID = R.string.dcc_ticketing_not_in_allow_list_dialog_title
        private const val MESSAGE_ID = R.string.dcc_ticketing_not_in_allow_list_dialog_message
    }
}
