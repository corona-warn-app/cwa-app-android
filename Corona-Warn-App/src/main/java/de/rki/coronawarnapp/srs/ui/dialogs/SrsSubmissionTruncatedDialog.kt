package de.rki.coronawarnapp.srs.ui.dialogs

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.dialog.displayDialog

fun Fragment.showTruncatedSubmissionDialog(numberOfDays: String?, positiveFunction: () -> Unit) = displayDialog {
    title(R.string.srs_submission_truncated_warning_dialog_title)
    message(getString(R.string.srs_submission_truncated_warning_dialog_message, numberOfDays))
    positiveButton(R.string.srs_submission_truncated_warning_dialog_button) { positiveFunction() }
}
