package de.rki.coronawarnapp.ui.submission

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.dialog.createDialog

fun Fragment.submissionCancelDialog(cancelFunction: () -> Unit) = createDialog {
    title(R.string.submission_error_dialog_confirm_cancellation_title)
    message(R.string.submission_error_dialog_confirm_cancellation_body)
    positiveButton(R.string.submission_error_dialog_confirm_cancellation_button_positive) { cancelFunction() }
    negativeButton(R.string.submission_error_dialog_confirm_cancellation_button_negative)
}
