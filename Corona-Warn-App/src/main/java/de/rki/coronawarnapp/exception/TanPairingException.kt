package de.rki.coronawarnapp.exception

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError

/**
 * Specific Exception type to identify an error case happening when TAN is retrieved.
 * @see <a href="https://jira-ibs.wbs.net.sap/browse/EXPOSUREAPP-4515">EXPOSUREAPP-4515</a>
 */
class TanPairingException(
    override val code: Int,
    override val message: String?,
    override val cause: Throwable?
) : CwaClientError(code, message, cause), HasHumanReadableError {
    override fun toHumanReadableError(context: Context): HumanReadableError {
        return HumanReadableError(
            title = context.getString(R.string.submission_error_dialog_web_paring_invalid_title),
            description = context.getString(R.string.submission_error_dialog_web_paring_invalid_body)
        )
    }
}
