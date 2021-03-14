package de.rki.coronawarnapp.ui.submission

import android.content.Context
import androidx.appcompat.app.AlertDialog
import de.rki.coronawarnapp.R

class SubmissionBlockingDialog(
    val context: Context
) {

    private val dialog by lazy {
        AlertDialog.Builder(context).apply {
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
