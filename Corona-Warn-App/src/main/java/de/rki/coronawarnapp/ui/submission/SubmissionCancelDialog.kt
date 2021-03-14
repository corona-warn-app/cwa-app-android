package de.rki.coronawarnapp.ui.submission

import android.content.Context
import androidx.appcompat.app.AlertDialog
import de.rki.coronawarnapp.R

class SubmissionCancelDialog(
    val context: Context
) {
    fun show(onUserDidCancel: () -> Unit) {
        AlertDialog.Builder(context).apply {
            setTitle(R.string.submission_error_dialog_confirm_cancellation_title)
            setMessage(R.string.submission_error_dialog_confirm_cancellation_body)
            setPositiveButton(R.string.submission_error_dialog_confirm_cancellation_button_positive) { _, _ ->
                onUserDidCancel()
            }
            setNegativeButton(R.string.submission_error_dialog_confirm_cancellation_button_negative) { _, _ ->
                // NOOP
            }
        }.show()
    }
}
