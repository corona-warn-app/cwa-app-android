package de.rki.coronawarnapp.ui.submission

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R

class SubmissionBlockingDialog(
    val context: Context
) {

    private val dialog by lazy {
        MaterialAlertDialogBuilder(context).apply {
            setCancelable(false)
            setView(R.layout.submission_blocking_dialog_view)
        }.create()
    }

    fun setState(show: Boolean) {
        if (show && !dialog.isShowing) {
            dialog.show()
        } else if (!show && dialog.isShowing) {
            dialog.dismiss()
        }
    }
}
