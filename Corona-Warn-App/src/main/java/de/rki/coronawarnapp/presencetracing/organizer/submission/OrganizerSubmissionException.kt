package de.rki.coronawarnapp.presencetracing.organizer.submission

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.ui.CachedString
import de.rki.coronawarnapp.util.ui.LazyString

class OrganizerSubmissionException(
    val errorCode: ErrorCode,
    override val cause: Throwable? = null
) : Exception(errorCode.message, cause), HasHumanReadableError {

    enum class ErrorCode(
        val message: String
    ) {
        REGTOKEN_OB_CLIENT_ERROR("Obtaining a registration token with the TAN failed, most likely, TAN is incorrect."),
        REGTOKEN_OB_SERVER_ERROR("Obtaining a registration token failed due to a (temporary) server error. The user may try to submit again."),
        REGTOKEN_OB_NO_NETWORK("Obtaining a registration token failed due to missing or poor network. The user may try to submit again."),

        /** If the submission fails with an HTTP status code `40x` */
        SUBMISSION_OB_CLIENT_ERROR("Submitting check-ins on behalf failed due to a client error. There is nothing the user can do to recover. Might be with calling the hotline."),

        /** If the submission fails with an HTTP status code `50x` */
        SUBMISSION_OB_SERVER_ERROR("Submitting check-ins on behalf failed due to a (temporary) server error. The user may try to submit again."),

        /** If the submission fails due to missing or poor network connection */
        SUBMISSION_OB_NO_NETWORK("Submitting check-ins on behalf failed due to missing or poor network. The user may try to submit again."),

        TAN_OB_CLIENT_ERROR("Requesting an upload TAN for the registration token failed due to a client error. There is nothing the user can do to recover. Might be with calling the hotline."),
        TAN_OB_SERVER_ERROR("Submitting check-ins on behalf failed due to a (temporary) server error. The user may try to submit again."),
        TAN_OB_NO_NETWORK("Submitting check-ins on behalf failed due to missing or poor network. The user may try to submit again.")
    }

    val errorMessage: LazyString
        get() = CachedString { context ->
            when (errorCode) {
                ErrorCode.REGTOKEN_OB_CLIENT_ERROR -> R.string.submission_ob_tan_error
                ErrorCode.REGTOKEN_OB_SERVER_ERROR, ErrorCode.SUBMISSION_OB_SERVER_ERROR, ErrorCode.TAN_OB_SERVER_ERROR -> R.string.submission_ob_try_again
                ErrorCode.REGTOKEN_OB_NO_NETWORK, ErrorCode.SUBMISSION_OB_NO_NETWORK, ErrorCode.TAN_OB_NO_NETWORK -> R.string.submission_ob_no_network
                ErrorCode.SUBMISSION_OB_CLIENT_ERROR, ErrorCode.TAN_OB_CLIENT_ERROR -> R.string.submission_ob_failed
            }.let { context.getString(it) }
        }

    override fun toHumanReadableError(context: Context): HumanReadableError = HumanReadableError(
        description = errorMessage.get(context) + " (${errorCode})"
    )
}
