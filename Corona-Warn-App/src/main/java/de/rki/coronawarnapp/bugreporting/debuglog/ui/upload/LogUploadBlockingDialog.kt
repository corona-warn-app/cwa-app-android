package de.rki.coronawarnapp.bugreporting.debuglog.ui.upload

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R

class LogUploadBlockingDialog(val context: Context) {

    private val dialog by lazy {
        MaterialAlertDialogBuilder(context).apply {
            setCancelable(false)
            setView(R.layout.bugreporting_debuglog_upload_dialog)
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
