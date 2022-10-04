package de.rki.coronawarnapp.ui.submission

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.dialog.displayDialog

fun Fragment.submissionCancelDialog(cancelFunction: () -> Unit) = displayDialog {
    setTitle(R.string.submission_error_dialog_confirm_cancellation_title)
    setMessage(R.string.submission_error_dialog_confirm_cancellation_body)
    setPositiveButton(R.string.submission_error_dialog_confirm_cancellation_button_positive) { _, _ ->
        cancelFunction()
    }
    setNegativeButton(R.string.submission_error_dialog_confirm_cancellation_button_negative) { _, _ -> }
}
