package de.rki.coronawarnapp.srs.ui.dialogs

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.dialog.displayDialog

fun Fragment.showCloseDialog(cancelFunction: () -> Unit) = displayDialog {
    title(R.string.srs_cancel_dialog_title)
    message(R.string.srs_cancel_dialog_message)
    positiveButton(R.string.srs_cancel_dialog_continue)
    negativeButton(R.string.srs_cancel_dialog_cancel) { cancelFunction() }
}

fun Fragment.showSubmissionWarningDialog(positiveFunction: () -> Unit) = displayDialog {
    title(R.string.srs_submission_warning_dialog_title)
    message(R.string.srs_submission_warning_dialog_message)
    positiveButton(R.string.srs_submission_warning_dialog_positive_button) { positiveFunction() }
    negativeButton(R.string.srs_submission_warning_dialog_negative_button)
}
