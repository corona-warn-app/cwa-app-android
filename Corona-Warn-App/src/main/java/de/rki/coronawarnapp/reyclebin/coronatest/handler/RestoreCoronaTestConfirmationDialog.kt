package de.rki.coronawarnapp.reyclebin.coronatest.handler

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R

object RestoreCoronaTestConfirmationDialog {

    fun showDialog(context: Context, onConfirmation: () -> Unit) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.recycle_bin_restore_corona_test_dialog_title)
            .setCancelable(false)
            .setMessage(R.string.recycle_bin_restore_corona_test_dialog_message)
            .setPositiveButton(android.R.string.ok) { _, _ -> onConfirmation() }
            .show()
    }
}
