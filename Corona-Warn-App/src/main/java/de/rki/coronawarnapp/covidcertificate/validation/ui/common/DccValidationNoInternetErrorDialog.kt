package de.rki.coronawarnapp.covidcertificate.validation.ui.common

import android.app.Dialog
import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R

class DccValidationNoInternetErrorDialog(private val context: Context) {

    fun show(): Dialog = MaterialAlertDialogBuilder(context)
        .setTitle(R.string.validation_start_no_internet_dialog_title)
        .setMessage(R.string.validation_start_no_internet_dialog_msg)
        .setPositiveButton(R.string.validation_start_no_internet_dialog_positive_button) { _, _ -> }
        .show()
}
